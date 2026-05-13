package com.stellar.backend.service;

import com.stellar.backend.dto.BookingRequestDto;
import com.stellar.backend.entity.*;
import com.stellar.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class BookingService {

    @Autowired
    private DonMuaRepository donMuaRepository;

    @Autowired
    private WalletService walletService;

    @Autowired
    private VeRepository veRepository;

    @Autowired
    private SuKienRepository suKienRepository;

    @Autowired
    private KhuVucRepository khuVucRepository;

    @Autowired
    private HangVeRepository hangVeRepository;

    @Autowired
    private TaiKhoanRepository taiKhoanRepository;

    @Autowired
    private TrangThaiGheTheoSuatRepository trangThaiGheTheoSuatRepository;

    @Autowired
    private PromotionService promotionService;

    @Transactional
    public DonMua processBooking(Long userId, BookingRequestDto request) throws Exception {
        // 1. Lấy thông tin cơ bản
        TaiKhoan taiKhoan = taiKhoanRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại!"));

        SuKien suKien = suKienRepository.findById(request.getMaSuKien())
                .orElseThrow(() -> new RuntimeException("Sự kiện không tồn tại!"));

        HangVe hangVe = hangVeRepository.findById(request.getMaHangVe())
                .orElseThrow(() -> new RuntimeException("Hạng vé không tồn tại!"));

        // 2. Xác định lịch diễn
        Long validMaLichDien = request.getMaLichDien();
        if (validMaLichDien == null && suKien.getDanhSachLichDien() != null && !suKien.getDanhSachLichDien().isEmpty()) {
            validMaLichDien = suKien.getDanhSachLichDien().get(0).getMaLichDien();
        }

        // 3. Kiểm tra ghế
        boolean skipSeatCheck = (request.getDsGhe() == null || request.getDsGhe().isEmpty());
        List<TrangThaiGheTheoSuat> userLocks = new ArrayList<>();
        KhuVuc finalKhuVuc = null;

        if (!skipSeatCheck) {
            userLocks = trangThaiGheTheoSuatRepository.findByMaLichDienAndTaiKhoanMaTaiKhoanAndTrangThai(validMaLichDien, userId, "Đang giữ chỗ");
            if (userLocks.size() < request.getSoLuong()) {
                throw new RuntimeException("Bạn chưa khóa đủ số lượng ghế trên hệ thống.");
            }
        } else {
            long capacity = hangVe.getTongSoLuong() != null ? hangVe.getTongSoLuong() : 0;
            
            if (request.getMaKhuVuc() != null) {
                finalKhuVuc = khuVucRepository.findById(request.getMaKhuVuc()).orElse(null);
            } else {
                List<KhuVuc> kvList = khuVucRepository.findByHangVe_MaHangVe(hangVe.getMaHangVe());
                if (kvList != null && !kvList.isEmpty()) {
                    finalKhuVuc = kvList.get(0);
                }
            }
            
            if (finalKhuVuc != null && finalKhuVuc.getSucChuaKv() != null) {
                capacity = finalKhuVuc.getSucChuaKv();
            }
            
            java.util.List<String> ignoredStatuses = java.util.Arrays.asList("Đã hủy", "Đã hoàn vé");
            long veDaBan = (finalKhuVuc != null) ? 
                veRepository.countByKhuVuc_MaKhuVucAndTrangThaiVeNotIn(finalKhuVuc.getMaKhuVuc(), ignoredStatuses) : 
                veRepository.countByHangVe_MaHangVeAndTrangThaiVeNotIn(hangVe.getMaHangVe(), ignoredStatuses);
            
            if (veDaBan + request.getSoLuong() > capacity) {
                throw new RuntimeException("Rất tiếc! Số lượng vé vượt quá giới hạn sức chứa còn lại của khu vực này.");
            }
        }

        // 4. Tính tiền
        BigDecimal tongTien = BigDecimal.ZERO;
        if (skipSeatCheck) {
            tongTien = hangVe.getGiaNiemYet().multiply(new BigDecimal(request.getSoLuong()));
        } else {
            for (TrangThaiGheTheoSuat lock : userLocks) {
                if (lock.getGheNgoi() != null && lock.getGheNgoi().getKhuVuc() != null && lock.getGheNgoi().getKhuVuc().getHangVe() != null) {
                    tongTien = tongTien.add(lock.getGheNgoi().getKhuVuc().getHangVe().getGiaNiemYet());
                } else {
                    tongTien = tongTien.add(hangVe.getGiaNiemYet());
                }
            }
        }

        // 5. Khuyến mãi
        BigDecimal soTienGiam = BigDecimal.ZERO;
        MaGiamGia appliedMgg = null;
        if (request.getDiscountCode() != null && !request.getDiscountCode().trim().isEmpty()) {
            appliedMgg = promotionService.validateCode(request.getDiscountCode(), request.getMaSuKien());
            soTienGiam = promotionService.calculateDiscountAmount(appliedMgg, tongTien);
            tongTien = tongTien.subtract(soTienGiam);
            if (tongTien.compareTo(BigDecimal.ZERO) < 0) tongTien = BigDecimal.ZERO;
        }

        // 6. Thanh toán qua ví
        walletService.pay(userId, tongTien, "Thanh toán đặt vé cho sự kiện: " + suKien.getTenSuKien());

        // 7. Lưu đơn mua (Ban đầu đặt 0 để Trigger Database tự tính)
        DonMua donMua = new DonMua();
        donMua.setTaiKhoan(taiKhoan);
        donMua.setSuKien(suKien);
        donMua.setTongTien(BigDecimal.ZERO);
        donMua.setTrangThaiThanhToan("Đã thanh toán");
        donMua.setPhuongThucThanhToan("Ví cá nhân (Ve'ryGood Pay)");
        DonMua savedDonMua = donMuaRepository.save(donMua);

        // 8. Lưu vé
        List<Ve> veList = new ArrayList<>();
        for (int i = 0; i < request.getSoLuong(); i++) {
            Ve ve = new Ve();
            ve.setDonMua(savedDonMua);
            ve.setHangVe(hangVe);
            ve.setDaBanLai(0);
            ve.setTrangThaiVe("Hiệu lực");

            if (!skipSeatCheck && i < userLocks.size()) {
                TrangThaiGheTheoSuat lock = userLocks.get(i);
                ve.setGheNgoi(lock.getGheNgoi());
                ve.setKhuVuc(lock.getGheNgoi() != null ? lock.getGheNgoi().getKhuVuc() : null);
                ve.setLichDien(lock.getLichDien());
                lock.setTrangThai("Đã đặt");
                lock.setThoiGianHetHan(null);
                trangThaiGheTheoSuatRepository.save(lock);
            } else {
                ve.setKhuVuc(finalKhuVuc);
                Long finalMaLichDien = validMaLichDien;
                ve.setLichDien(suKien.getDanhSachLichDien().stream()
                        .filter(ld -> ld.getMaLichDien().equals(finalMaLichDien))
                        .findFirst().orElse(suKien.getDanhSachLichDien().isEmpty() ? null : suKien.getDanhSachLichDien().get(0)));
            }
            veList.add(ve);
        }
        veRepository.saveAll(veList);

        // 9. Áp dụng khuyến mãi
        if (appliedMgg != null) {
            promotionService.applyPromotion(savedDonMua, request.getDiscountCode(), soTienGiam);
        }

        return savedDonMua;
    }
}
