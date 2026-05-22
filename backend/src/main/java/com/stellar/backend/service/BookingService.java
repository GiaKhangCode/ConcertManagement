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
    private GiaoDichRepository giaoDichRepository;

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

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

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
            if (request.getMaKhuVuc() != null) {
                finalKhuVuc = khuVucRepository.findById(request.getMaKhuVuc()).orElse(null);
            } else {
                List<KhuVuc> kvList = khuVucRepository.findByHangVe_MaHangVe(hangVe.getMaHangVe());
                if (kvList != null && !kvList.isEmpty()) {
                    finalKhuVuc = kvList.get(0);
                }
            }
            
            Long khuVucIdForCount = (finalKhuVuc != null) ? finalKhuVuc.getMaKhuVuc() : null;
            Long soVeConLai = hangVeRepository.callFnLaySoVeConLai(hangVe.getMaHangVe(), khuVucIdForCount);
            
            if (soVeConLai == null || request.getSoLuong() > soVeConLai) {
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

        // 6. Cấu hình Thanh toán
        String paymentMethod = request.getPaymentMethod();
        boolean isWallet = false;
        
        if (paymentMethod == null || paymentMethod.isEmpty() || paymentMethod.equals("WALLET")) {
            paymentMethod = "Ví cá nhân"; // Trùng khớp với logic trong DB
            isWallet = true;
        } else {
            if (paymentMethod.equals("MOMO")) paymentMethod = "Ví MoMo";
            if (paymentMethod.equals("BANK")) paymentMethod = "Thẻ Ngân hàng";
        }

        // 7. Lưu đơn mua (LUÔN luôn khởi tạo ở trạng thái Chờ thanh toán)
        DonMua donMua = new DonMua();
        donMua.setTaiKhoan(taiKhoan);
        donMua.setSuKien(suKien);
        donMua.setTongTien(BigDecimal.ZERO); // Sẽ được tính lại bởi trigger DB
        donMua.setTrangThaiThanhToan("Chờ thanh toán");
        donMua.setPhuongThucThanhToan(paymentMethod);
        DonMua savedDonMua = donMuaRepository.save(donMua);

        // 8. Lưu vé qua SP_TAO_VE_HANG_LOAT (LUÔN tạo vé Chờ thanh toán)
        int isSkipCheck = skipSeatCheck ? 1 : 0;
        Long khuVucId = (finalKhuVuc != null) ? finalKhuVuc.getMaKhuVuc() : null;
        
        veRepository.callSpTaoVeHangLoat(
            savedDonMua.getMaDonMua(),
            userId,
            suKien.getMaSuKien(),
            validMaLichDien,
            hangVe.getMaHangVe(),
            khuVucId,
            request.getSoLuong(),
            isSkipCheck,
            "Chờ thanh toán",
            "Đang giữ chỗ"
        );

        // 9. Xử lý thanh toán qua Procedure tương ứng
        if (isWallet) {
            String walletPassword = request.getWalletPassword();
            if (walletPassword == null || walletPassword.isEmpty()) {
                walletPassword = "123456"; // Mặc định nếu Frontend chưa kịp gửi
            }

            jakarta.persistence.StoredProcedureQuery query = entityManager.createStoredProcedureQuery("SP_THANH_TOAN_VI");
            query.registerStoredProcedureParameter("p_MaDonMua", Long.class, jakarta.persistence.ParameterMode.IN);
            query.registerStoredProcedureParameter("p_MaTaiKhoan", Long.class, jakarta.persistence.ParameterMode.IN);
            query.registerStoredProcedureParameter("p_MaKhauThanhToan", String.class, jakarta.persistence.ParameterMode.IN);
            query.registerStoredProcedureParameter("p_KetQua", String.class, jakarta.persistence.ParameterMode.OUT);

            query.setParameter("p_MaDonMua", savedDonMua.getMaDonMua());
            query.setParameter("p_MaTaiKhoan", userId);
            query.setParameter("p_MaKhauThanhToan", walletPassword);

            query.execute();
            String result = (String) query.getOutputParameterValue("p_KetQua");
            
            if (result == null || result.startsWith("Lỗi") || result.startsWith("Không đủ") || result.contains("Thất Bại")) {
                throw new RuntimeException(result != null ? result : "Lỗi hệ thống khi thanh toán ví");
            }
            System.out.println("Wallet Payment Result: " + result);

        } else {
            // Giả lập Webhook cho cổng ngoài (MOMO/BANK)
            String transactionId = "MOCK_" + System.currentTimeMillis();
            jakarta.persistence.StoredProcedureQuery query = entityManager.createStoredProcedureQuery("SP_XAC_NHAN_TT_NGOAI");
            query.registerStoredProcedureParameter("p_MaDonMua", Long.class, jakarta.persistence.ParameterMode.IN);
            query.registerStoredProcedureParameter("p_MaGiaoDichCongTT", String.class, jakarta.persistence.ParameterMode.IN);
            query.registerStoredProcedureParameter("p_SoTienThucNhan", BigDecimal.class, jakarta.persistence.ParameterMode.IN);
            query.registerStoredProcedureParameter("p_PhuongThuc", String.class, jakarta.persistence.ParameterMode.IN);
            query.registerStoredProcedureParameter("p_TrangThaiGiaoDich", String.class, jakarta.persistence.ParameterMode.IN);
            query.registerStoredProcedureParameter("p_MaLoi", String.class, jakarta.persistence.ParameterMode.IN);
            query.registerStoredProcedureParameter("p_DuLieuPhanHoi", String.class, jakarta.persistence.ParameterMode.IN);
            query.registerStoredProcedureParameter("p_KetQua", String.class, jakarta.persistence.ParameterMode.OUT);

            query.setParameter("p_MaDonMua", savedDonMua.getMaDonMua());
            query.setParameter("p_MaGiaoDichCongTT", transactionId);
            query.setParameter("p_SoTienThucNhan", tongTien);
            query.setParameter("p_PhuongThuc", paymentMethod);
            query.setParameter("p_TrangThaiGiaoDich", "Thành công");
            query.setParameter("p_MaLoi", "");
            query.setParameter("p_DuLieuPhanHoi", "{\"status\":200, \"mock\":\"true\"}");

            query.execute();
            String result = (String) query.getOutputParameterValue("p_KetQua");
            System.out.println("Mock Webhook Result: " + result);
        }

        // 9. Áp dụng khuyến mãi
        if (appliedMgg != null) {
            promotionService.applyPromotion(savedDonMua, request.getDiscountCode(), soTienGiam);
        }

        return savedDonMua;
    }
}
