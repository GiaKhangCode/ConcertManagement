package com.stellar.backend.service;

import com.stellar.backend.entity.LichSuHoanTien;
import com.stellar.backend.entity.SuKien;
import com.stellar.backend.entity.Ve;
import com.stellar.backend.repository.DonMuaRepository;
import com.stellar.backend.repository.LichSuHoanTienRepository;
import com.stellar.backend.repository.SuKienRepository;
import com.stellar.backend.repository.VeRepository;
import com.stellar.backend.entity.DonMua;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Dịch vụ Quyết Toán — Tính toán chi tiết doanh thu thực tế của nhà tổ chức.
 *
 * Công thức:
 *   Doanh thu gộp   = Σ GiaNiemYet của TẤT CẢ vé đã bán (kể cả vé sau đó bị hủy)
 *   Tổng đã hoàn    = Σ SoTienHoan từ bảng LICH_SU_HOAN_TIEN
 *   Doanh thu thuần = Gộp - Đã hoàn
 *   Phí nền tảng    = Thuần × 5%
 *   Phí cổng TT     = Thuần × 2%
 *   Thực nhận (Net) = Thuần × 93%
 */
@Service
public class SettlementService {

    private static final BigDecimal PLATFORM_FEE_RATE = new BigDecimal("0.05");
    private static final BigDecimal GATEWAY_FEE_RATE  = new BigDecimal("0.02");

    @Autowired
    private SuKienRepository suKienRepository;

    @Autowired
    private VeRepository veRepository;

    @Autowired
    private DonMuaRepository donMuaRepository;

    @Autowired
    private LichSuHoanTienRepository lichSuHoanTienRepository;

    @Autowired
    private com.stellar.backend.repository.LichSuQuyetToanRepository lichSuQuyetToanRepository;

    @Autowired
    private WalletService walletService;

    @Autowired
    private com.stellar.backend.repository.NhaToChucRepository nhaToChucRepository;

    public Map<String, Object> getEventSettlement(Long eventId) {
        SuKien sk = suKienRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Sự kiện không tồn tại"));

        // ── 1. THỐNG KÊ VÉ ──────────────────────────────────────────────────
        List<Ve> allTickets = veRepository.findByHangVe_SuKien_MaSuKien(eventId);

        long totalTicketsSold  = allTickets.stream()
                .filter(v -> !"Đã hủy".equals(v.getTrangThaiVe())).count(); // vé còn hiệu lực
        long totalTicketsCancelled = allTickets.stream()
                .filter(v -> "Đã hủy".equals(v.getTrangThaiVe())).count();
        long totalTicketsAll   = allTickets.size(); // tổng vé đã phát hành

        // ── 2. DOANH THU GỘP (tổng số tiền thực tế khách đã trả cho các đơn hàng) ──
        BigDecimal grossRevenue = donMuaRepository.sumGrossRevenueBySuKien_MaSuKien(eventId);
        if (grossRevenue == null) grossRevenue = BigDecimal.ZERO;

        // ── 3. TỔNG TIỀN ĐÃ HOÀN (từ bảng LICH_SU_HOAN_TIEN) ───────────────
        BigDecimal totalRefunds = lichSuHoanTienRepository.sumSoTienHoanBySuKien(eventId);
        if (totalRefunds == null) totalRefunds = BigDecimal.ZERO;

        long refundsByUser      = lichSuHoanTienRepository.countBySuKien_MaSuKienAndLoaiHoan(eventId, "Yêu cầu người dùng");
        long refundsByCancel    = lichSuHoanTienRepository.countBySuKien_MaSuKienAndLoaiHoan(eventId, "Hủy sự kiện");

        // ── 4. TÍNH CÁC CHỈ SỐ TÀI CHÍNH ───────────────────────────────────
        BigDecimal netBeforeFee = grossRevenue.subtract(totalRefunds);
        if (netBeforeFee.compareTo(BigDecimal.ZERO) < 0) netBeforeFee = BigDecimal.ZERO;

        BigDecimal platformFee  = netBeforeFee.multiply(PLATFORM_FEE_RATE).setScale(0, RoundingMode.HALF_UP);
        BigDecimal gatewayFee   = netBeforeFee.multiply(GATEWAY_FEE_RATE).setScale(0, RoundingMode.HALF_UP);
        BigDecimal netRevenue   = netBeforeFee.subtract(platformFee).subtract(gatewayFee);

        // ── 5. CHI TIẾT LỊCH SỬ HOÀN TIỀN (10 gần nhất) ────────────────────
        List<LichSuHoanTien> recentRefunds = lichSuHoanTienRepository
                .findBySuKien_MaSuKienOrderByThoiDiemHoanDesc(eventId);

        List<Map<String, Object>> refundHistory = new ArrayList<>();
        for (LichSuHoanTien r : recentRefunds.stream().limit(10).toList()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("maVe", r.getVe().getMaVe());
            item.put("soTienHoan", r.getSoTienHoan());
            item.put("loaiHoan", r.getLoaiHoan());
            item.put("lyDoHoan", r.getLyDoHoan());
            item.put("thoiDiemHoan", r.getThoiDiemHoan() != null ? r.getThoiDiemHoan().toString() : null);
            String hoTen = (r.getTaiKhoan() != null && r.getTaiKhoan().getNguoiDung() != null)
                    ? r.getTaiKhoan().getNguoiDung().getHoTen()
                    : (r.getTaiKhoan() != null ? r.getTaiKhoan().getTenDangNhap() : "N/A");
            item.put("nguoiNhanHoan", hoTen);
            refundHistory.add(item);
        }

        // ── 6. BUILD RESPONSE ────────────────────────────────────────────────
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("eventId",              sk.getMaSuKien());
        report.put("eventName",            sk.getTenSuKien());
        report.put("trangThai",            sk.getTrangThai());

        // Thống kê vé
        report.put("totalTicketsAll",      totalTicketsAll);
        report.put("totalTicketsSold",     totalTicketsSold);
        report.put("totalTicketsCancelled",totalTicketsCancelled);

        // Dòng tiền
        report.put("grossRevenue",         grossRevenue);
        report.put("totalRefunds",         totalRefunds);
        report.put("refundsByUser",        refundsByUser);
        report.put("refundsByCancel",      refundsByCancel);
        report.put("netBeforeFee",         netBeforeFee);

        // Phí & thực nhận
        report.put("platformFeeRate",      "5%");
        report.put("platformFee",          platformFee);
        report.put("gatewayFeeRate",       "2%");
        report.put("gatewayFee",           gatewayFee);
        report.put("netRevenue",           netRevenue);

        // Lịch sử
        report.put("refundHistory",        refundHistory);
        report.put("totalRefundCount",     recentRefunds.size());

        return report;
    }

    public Map<String, Object> executeSettlement(Long eventId) {
        SuKien sk = suKienRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Sự kiện không tồn tại"));

        if (!"Đã kết thúc".equals(sk.getTrangThai())) {
            throw new RuntimeException("Chỉ có thể quyết toán khi sự kiện đã kết thúc");
        }

        // Kiểm tra xem đã quyết toán chưa
        if (lichSuQuyetToanRepository.findAll().stream()
                .anyMatch(q -> q.getKyQT() != null
                        && q.getKyQT().contains("Sự kiện: " + eventId + " -")
                        && "Đã xử lý".equals(q.getTrangThai()))) {
            throw new RuntimeException("Sự kiện này đã được quyết toán trước đó");
        }

        Map<String, Object> report = getEventSettlement(eventId);
        BigDecimal netRevenue = (BigDecimal) report.get("netRevenue");

        if (netRevenue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Doanh thu không đủ để quyết toán");
        }

        com.stellar.backend.entity.TaiKhoan tkNhan = sk.getNguoiTao();
        if (tkNhan == null) {
            throw new RuntimeException("Không tìm thấy tài khoản nhà tổ chức để nhận tiền");
        }

        com.stellar.backend.entity.NhaToChuc ntc = nhaToChucRepository
                .findByTaiKhoan_MaTaiKhoan(tkNhan.getMaTaiKhoan())
                .orElseThrow(() -> new RuntimeException("Tài khoản không phải nhà tổ chức hợp lệ"));

        // Thực hiện cộng tiền vào ví nhà tổ chức
        walletService.receive(tkNhan.getMaTaiKhoan(), netRevenue,
                "Quyết toán doanh thu sự kiện: " + sk.getTenSuKien());

        // Lưu lịch sử quyết toán
        com.stellar.backend.entity.LichSuQuyetToan ls = new com.stellar.backend.entity.LichSuQuyetToan();
        ls.setNhaToChuc(ntc);
        ls.setKyQT("Sự kiện: " + sk.getMaSuKien() + " - " + sk.getTenSuKien());
        ls.setTongDoanhThu((BigDecimal) report.get("grossRevenue"));
        ls.setPhiNenTang(((BigDecimal) report.get("platformFee")).add((BigDecimal) report.get("gatewayFee")));
        ls.setSoTienChuyen(netRevenue);
        ls.setTrangThai("Đã xử lý");
        ls.setChungTuThanhToan("Chuyển vào ví");
        lichSuQuyetToanRepository.save(ls);

        report.put("settlementStatus", "Đã xử lý");
        report.put("settlementAmount", netRevenue);
        return report;
    }
}
