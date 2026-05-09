package com.stellar.backend.controller;

import com.stellar.backend.dto.BookingRequestDto;
import com.stellar.backend.entity.DonMua;
import com.stellar.backend.entity.HangVe;
import com.stellar.backend.entity.SuKien;
import com.stellar.backend.entity.TaiKhoan;
import com.stellar.backend.entity.Ve;
import com.stellar.backend.repository.DonMuaRepository;
import com.stellar.backend.repository.HangVeRepository;
import com.stellar.backend.repository.SuKienRepository;
import com.stellar.backend.repository.TaiKhoanRepository;
import com.stellar.backend.repository.VeRepository;
import com.stellar.backend.repository.SoDoSuKienRepository;
import com.stellar.backend.repository.GheNgoiRepository;
import com.stellar.backend.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.stellar.backend.entity.ViCaNhan;
import com.stellar.backend.service.WalletService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/booking")
public class BookingController {

    @Autowired
    private DonMuaRepository donMuaRepository;

    @Autowired
    private WalletService walletService;

    @Autowired
    private VeRepository veRepository;

    @Autowired
    private SuKienRepository suKienRepository;

    @Autowired
    private HangVeRepository hangVeRepository;

    @Autowired
    private TaiKhoanRepository taiKhoanRepository;

    @Autowired
    private com.stellar.backend.repository.TrangThaiGheTheoSuatRepository trangThaiGheTheoSuatRepository;

    @Autowired
    private SoDoSuKienRepository soDoSuKienRepository;

    @Autowired
    private GheNgoiRepository gheNgoiRepository;

    @Transactional
    @PostMapping("/create")
    public ResponseEntity<?> createBooking(@RequestBody BookingRequestDto request) {
        // Lấy thông tin tài khoản đang login thông qua JWT Filter
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        TaiKhoan taiKhoan = taiKhoanRepository.findById(userDetails.getId())
            .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại!"));

        SuKien suKien = suKienRepository.findById(request.getMaSuKien())
            .orElseThrow(() -> new RuntimeException("Sự kiện không tồn tại!"));

        HangVe hangVe = hangVeRepository.findById(request.getMaHangVe())
            .orElseThrow(() -> new RuntimeException("Hạng vé không tồn tại!"));

        // Để kiểm tra chính xác, ta lấy danh sách các ghế User đang giữ trong SUẤT DIỄN này
        Long validMaLichDien = request.getMaLichDien();
        if (validMaLichDien == null && suKien.getDanhSachLichDien() != null && !suKien.getDanhSachLichDien().isEmpty()) {
            validMaLichDien = suKien.getDanhSachLichDien().get(0).getMaLichDien();
        } else if (validMaLichDien != null && suKien.getDanhSachLichDien() != null) {
            boolean isValid = suKien.getDanhSachLichDien().stream().anyMatch(ld -> ld.getMaLichDien().equals(request.getMaLichDien()));
            if (!isValid) {
                validMaLichDien = suKien.getDanhSachLichDien().isEmpty() ? request.getMaLichDien() : suKien.getDanhSachLichDien().get(0).getMaLichDien();
            }
        }

        // 2. Kiểm tra nếu dsGhe trống (vé đứng hoặc sự kiện không sơ đồ) -> cho phép đặt trực tiếp
        boolean skipSeatCheck = false;
        if (request.getDsGhe() == null || request.getDsGhe().isEmpty()) {
            skipSeatCheck = true;
        }

        List<com.stellar.backend.entity.TrangThaiGheTheoSuat> userLocks = new ArrayList<>();
        if (!skipSeatCheck) {
            userLocks = trangThaiGheTheoSuatRepository.findByMaLichDienAndTaiKhoanMaTaiKhoanAndTrangThai(validMaLichDien, taiKhoan.getMaTaiKhoan(), "Đang giữ chỗ");
            if (userLocks.size() < request.getSoLuong()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Bạn chưa khóa đủ số lượng ghế trên hệ thống."));
            }
        }

        // Tính tổng tiền
        BigDecimal tongTien = BigDecimal.ZERO;
        if (skipSeatCheck) {
            tongTien = hangVe.getGiaNiemYet().multiply(new BigDecimal(request.getSoLuong()));
        } else {
            for (com.stellar.backend.entity.TrangThaiGheTheoSuat lock : userLocks) {
                if (lock.getGheNgoi() != null && lock.getGheNgoi().getKhuVuc() != null && lock.getGheNgoi().getKhuVuc().getHangVe() != null) {
                    tongTien = tongTien.add(lock.getGheNgoi().getKhuVuc().getHangVe().getGiaNiemYet());
                } else {
                    tongTien = tongTien.add(hangVe.getGiaNiemYet());
                }
            }
        }

        // --- BẮT ĐẦU THANH TOÁN QUA VÍ ---
        walletService.pay(taiKhoan.getMaTaiKhoan(), tongTien, "Thanh toán đặt vé cho sự kiện: " + suKien.getTenSuKien());

        // Khởi tạo một Đơn Mua
        DonMua donMua = new DonMua();
        donMua.setTaiKhoan(taiKhoan);
        donMua.setSuKien(suKien);
        donMua.setTongTien(tongTien);
        donMua.setTrangThaiThanhToan("Đã thanh toán"); 
        donMua.setPhuongThucThanhToan("Ví cá nhân (Ve'ryGood Pay)");
        donMua = donMuaRepository.save(donMua);

        // Khởi tạo hàng loạt các dòng dữ liệu VÉ thật
        List<Ve> veList = new ArrayList<>();
        for (int i = 0; i < request.getSoLuong(); i++) {
            Ve ve = new Ve();
            ve.setDonMua(donMua);
            ve.setHangVe(hangVe);
            ve.setDaBanLai(0);
            ve.setTrangThaiVe("Hiệu lực");
            
            if (!skipSeatCheck && i < userLocks.size()) {
                com.stellar.backend.entity.TrangThaiGheTheoSuat lock = userLocks.get(i);
                ve.setGheNgoi(lock.getGheNgoi());
                ve.setLichDien(lock.getLichDien());
                
                lock.setTrangThai("Đã đặt");
                lock.setThoiGianHetHan(null);
                trangThaiGheTheoSuatRepository.save(lock);
            } else {
                // Trường hợp không có sơ đồ
                ve.setLichDien(suKien.getDanhSachLichDien().stream()
                    .filter(ld -> ld.getMaLichDien().equals(request.getMaLichDien()))
                    .findFirst().orElse(suKien.getDanhSachLichDien().isEmpty() ? null : suKien.getDanhSachLichDien().get(0)));
            }
            veList.add(ve);
        }
        veRepository.saveAll(veList);

        // Response xuất hóa đơn ảo
        Map<String, Object> response = new HashMap<>();
        response.put("invoiceId", donMua.getMaDonMua());
        response.put("message", "Giao dịch Đặt Vé Thành Công!");
        response.put("totalTickets", request.getSoLuong());

        return ResponseEntity.ok(response);
    }
}
