package com.stellar.backend.service;

import com.stellar.backend.entity.*;
import com.stellar.backend.dto.*;
import com.stellar.backend.repository.VeRepository;
import com.stellar.backend.repository.YeuCauHoTroRepository;
import com.stellar.backend.repository.TaiKhoanRepository;
import com.stellar.backend.repository.DonMuaRepository;
import com.stellar.backend.repository.QuyTacHoanTienRepository;
import com.stellar.backend.repository.TrangThaiGheTheoSuatRepository;
import com.stellar.backend.repository.LichSuHoanTienRepository;
import com.stellar.backend.repository.NhatKySoatVeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketService {

    @Autowired
    private VeRepository veRepository;

    @Autowired
    private NhatKySoatVeRepository nhatKySoatVeRepository;

    @Autowired
    private YeuCauHoTroRepository yeuCauHoTroRepository;

    @Autowired
    private WalletService walletService;

    @Autowired
    private TaiKhoanRepository taiKhoanRepository;

    @Autowired
    private DonMuaRepository donMuaRepository;

    @Autowired
    private QuyTacHoanTienRepository quyTacHoanTienRepository;

    @Autowired
    private TrangThaiGheTheoSuatRepository trangThaiGheTheoSuatRepository;

    @Autowired
    private LichSuHoanTienRepository lichSuHoanTienRepository;

    @Transactional
    public void listForResale(Long ticketId, BigDecimal price, Long userId) {
        System.out.println("DEBUG: Listing ticket ID " + ticketId + " for resale at price " + price + " by user " + userId);
        Ve ve = veRepository.findById(ticketId).orElseThrow(() -> new RuntimeException("Vé không tồn tại"));
        if (!ve.getDonMua().getTaiKhoan().getMaTaiKhoan().equals(userId)) {
            throw new RuntimeException("Bạn không sở hữu vé này");
        }
        if (!"Hiệu lực".equals(ve.getTrangThaiVe())) {
            throw new RuntimeException("Vé không hợp lệ để bán lại (Trạng thái: " + ve.getTrangThaiVe() + ")");
        }

        ve.setDaBanLai(1);
        ve.setGiaBanLai(price);
        veRepository.save(ve);
        System.out.println("DEBUG: Ticket ID " + ticketId + " saved with DaBanLai=1 and price=" + price);
    }

    @Transactional
    public void buyResaleTicket(Long ticketId, Long buyerId) {
        Ve ve = veRepository.findById(ticketId).orElseThrow(() -> new RuntimeException("Vé không tồn tại"));
        if (ve.getDaBanLai() != 1 || ve.getGiaBanLai() == null) {
            throw new RuntimeException("Vé này không được niêm yết bán lại");
        }

        Long sellerId = ve.getDonMua().getTaiKhoan().getMaTaiKhoan();
        if (buyerId.equals(sellerId)) {
            throw new RuntimeException("Bạn không thể mua lại vé của chính mình");
        }

        // ─────────────────────────────────────────────────────────────
        // Demo NON-REPEATABLE READ — delay xảy ra BÊN TRONG Oracle function
        //
        // Gọi FUNC_DEMO_NRR_TICKET_PRICE(:ticketId) — function này:
        //   1. Đọc GiaBanLai lần 1 (ghi log DBMS_OUTPUT)
        //   2. DBMS_SESSION.SLEEP(7) — ngủ 7 giây tại DB
        //   3. Đọc GiaBanLai lần 2 — thấy thay đổi nếu TX khác đã UPDATE
        //   4. Trả về giá lần 2
        //
        // Cách demo:
        //   Tab A: bấm "Mua vé bán lại" → function bắt đầu đọc + sleep ở DB
        //   Tab B: trong lúc Tab A đang sleep, chỉnh lại GiaBanLai trên trang quản lý
        //   → Tab A nhận về giá mới (khác giá lần 1) → Non-Repeatable Read!
        // ─────────────────────────────────────────────────────────────
        BigDecimal priceRead1 = ve.getGiaBanLai(); // Giá đọc lần 1 (từ entity cache)

        // Gọi Oracle function — DBMS_SESSION.SLEEP(7) diễn ra ở DB level
        BigDecimal priceRead2 = veRepository.demoNrrTicketPrice(ticketId);

        if (priceRead2 == null) {
            throw new RuntimeException("Vé này đã bị hủy niêm yết trong lúc bạn xử lý!");
        }

        if (priceRead1.compareTo(priceRead2) != 0) {
            System.out.println("[NRR] *** NON-REPEATABLE READ! Giá thay đổi: "
                    + priceRead1 + " → " + priceRead2 + " ***");
        }
        // ─────────────────────────────────────────────────────────────

        BigDecimal price = priceRead2; // Dùng giá đọc lần 2 (sau sleep)

        // Buyer pays (gọi procedure PROC_DEMO_WALLET_PAY — thêm delay ở DB)
        walletService.pay(buyerId, price, "Mua vé bán lại #" + ve.getMaVe());
        walletService.receive(sellerId, price, "Tiền bán lại vé #" + ve.getMaVe());

        // Refresh vé từ DB (phòng trường hợp entity bị stale)
        Ve veFresh = veRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Vé không còn tồn tại"));
        // DB Trigger TRG_VE_BIUD_TINHTONGTIEN sẽ tự động xử lý giá và reset DaBanLai, GiaBanLai

        TaiKhoan buyerAccount = taiKhoanRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("Người mua không tồn tại"));

        DonMua newOrder = new DonMua();
        newOrder.setTaiKhoan(buyerAccount);
        newOrder.setSuKien(veFresh.getLichDien().getSuKien());
        newOrder.setTongTien(BigDecimal.ZERO); // DB Trigger sẽ tự động cộng giá bán lại vào
        newOrder.setTrangThaiThanhToan("Đã thanh toán");
        newOrder.setPhuongThucThanhToan("Stellar Pay (Mua lại)");
        donMuaRepository.save(newOrder);

        veFresh.setDonMua(newOrder);
        veRepository.save(veFresh);
    }

    private BigDecimal calculateRefundPercentage(SuKien sk, LocalDateTime thoiDiemYeuCau) {
        if (sk.getMauChinhSachHoanTien() == null) {
            throw new RuntimeException("Không hỗ trợ hoàn tiền");
        }
        LocalDateTime thoiGianBD = sk.getThoiGianBD();
        if (thoiDiemYeuCau.isAfter(thoiGianBD)) {
            throw new RuntimeException("Không hỗ trợ hoàn tiền do sự kiện đã bắt đầu");
        }

        long soGioTruocSuKien = Duration.between(thoiDiemYeuCau, thoiGianBD).toHours();
        List<QuyTacHoanTien> rules = quyTacHoanTienRepository
                .findByMauChinhSachHoanTien_MaChinhSachHT(sk.getMauChinhSachHoanTien().getMaChinhSachHT());

        System.out.println("[REFUND DEBUG] Sự kiện: " + sk.getTenSuKien()
                + " | Giờ còn lại đến SK: " + soGioTruocSuKien
                + " | Số quy tắc tìm được: " + rules.size());
        for (QuyTacHoanTien r : rules) {
            System.out.println("[REFUND DEBUG]   Rule ID=" + r.getMaQuyTac()
                    + " soGioTruocSuKien=" + r.getSoGioTruocSuKien()
                    + " tyLe=" + r.getTyLeHoanTien() + "%");
        }

        // Chọn rule có soGioTruocSuKien CAO NHẤT mà vẫn <= giờ thực tế còn lại.
        // Đây là rule "chặt nhất phù hợp" (most specific match).
        // Ví dụ: rules là [48h→50%, 90h→40%], giờ còn lại = 100h
        //   → 48 <= 100 ✓, 90 <= 100 ✓ → chọn 90h → 40%
        //   (Rule 90h áp dụng vì người dùng hủy sớm hơn mốc 90h)
        QuyTacHoanTien bestRule = null;
        for (QuyTacHoanTien rule : rules) {
            if (rule.getSoGioTruocSuKien() <= soGioTruocSuKien) {
                if (bestRule == null || rule.getSoGioTruocSuKien() > bestRule.getSoGioTruocSuKien()) {
                    bestRule = rule;
                }
            }
        }

        if (bestRule == null || bestRule.getTyLeHoanTien().compareTo(BigDecimal.ZERO) == 0) {
            throw new RuntimeException("Không hỗ trợ hoàn tiền (Quá hạn hoặc chính sách không cho phép)");
        }

        System.out.println("[REFUND DEBUG] Rule được chọn: soGioTruocSuKien="
                + bestRule.getSoGioTruocSuKien() + " → tyLe=" + bestRule.getTyLeHoanTien() + "%");
        return bestRule.getTyLeHoanTien();
    }

    private void cancelOrderTicketsAndFreeSeats(DonMua dm) {
        List<Ve> ticketsInOrder = veRepository.findByDonMua_MaDonMua(dm.getMaDonMua());
        for (Ve ticket : ticketsInOrder) {
            ticket.setTrangThaiVe("Đã hoàn vé");
            if (ticket.getGheNgoi() != null) {
                java.util.Optional<TrangThaiGheTheoSuat> optStatus = trangThaiGheTheoSuatRepository.findByMaGheAndMaLichDien(ticket.getGheNgoi().getMaGhe(), ticket.getLichDien().getMaLichDien());
                if (optStatus.isPresent()) {
                    TrangThaiGheTheoSuat status = optStatus.get();
                    status.setTrangThai("Còn trống");
                    status.setTaiKhoan(null);
                    status.setThoiGianHetHan(null);
                    trangThaiGheTheoSuatRepository.save(status);
                }
            }
        }
        veRepository.saveAll(ticketsInOrder);
    }

    @Transactional
    public void requestRefund(Long ticketId, String reason, Long userId) {
        Ve ve = veRepository.findById(ticketId).orElseThrow(() -> new RuntimeException("Vé không tồn tại"));
        if (!ve.getDonMua().getTaiKhoan().getMaTaiKhoan().equals(userId)) {
            throw new RuntimeException("Bạn không sở hữu vé này");
        }
        if ("Đã hủy".equals(ve.getTrangThaiVe()) || "Đã hoàn vé".equals(ve.getTrangThaiVe())) {
            throw new RuntimeException("Vé này đã được hoàn hoặc hủy trước đó");
        }

        SuKien sk = ve.getLichDien().getSuKien();
        LocalDateTime thoiDiemYeuCau = LocalDateTime.now();
        BigDecimal maxTyLe = calculateRefundPercentage(sk, thoiDiemYeuCau);

        YeuCauHoTro yeuCau = new YeuCauHoTro();
        yeuCau.setDonMua(ve.getDonMua());
        yeuCau.setTaiKhoan(ve.getDonMua().getTaiKhoan());
        yeuCau.setLoaiYeuCau("Hoàn tiền");
        yeuCau.setNoiDung(reason + " | Vé #" + ticketId);
        yeuCau.setTrangThaiXuLy("Đã xử lý");
        yeuCauHoTroRepository.save(yeuCau);

        BigDecimal ticketPrice = ve.getHangVe().getGiaNiemYet();
        BigDecimal hundred = new BigDecimal(100);
        BigDecimal amountToRefund = ticketPrice.multiply(maxTyLe)
                .divide(hundred, 0, RoundingMode.HALF_UP);

        System.out.println("[REFUND DEBUG] Giá vé: " + ticketPrice
                + " | Tỷ lệ: " + maxTyLe + "%"
                + " | Hoàn khách: " + amountToRefund);

        // 1. Hoàn phần % về cho khách hàng
        walletService.receive(
            ve.getDonMua().getTaiKhoan().getMaTaiKhoan(),
            amountToRefund,
            "Hoàn tiền vé #" + ticketId + " (" + maxTyLe.stripTrailingZeros().toPlainString() + "%)"
        );

        // 2. Ghi lịch sử hoàn tiền vào bảng LICH_SU_HOAN_TIEN
        LichSuHoanTien lichSuHoan = new LichSuHoanTien();
        lichSuHoan.setVe(ve);
        lichSuHoan.setSuKien(ve.getLichDien().getSuKien());
        lichSuHoan.setTaiKhoan(ve.getDonMua().getTaiKhoan());
        lichSuHoan.setSoTienHoan(amountToRefund);
        lichSuHoan.setLyDoHoan(reason);
        lichSuHoan.setLoaiHoan("Yêu cầu người dùng");
        lichSuHoanTienRepository.save(lichSuHoan);

        // 2. Phần còn lại (100% - refund%) cộng vào ví nhà tổ chức
        // CHÚ Ý: Đã bị xóa theo yêu cầu (không tự động cộng tiền cho nhà tổ chức)
        
        ve.setTrangThaiVe("Đã hoàn vé");
        veRepository.save(ve);

        if (ve.getGheNgoi() != null) {
            java.util.Optional<TrangThaiGheTheoSuat> optStatus = trangThaiGheTheoSuatRepository.findByMaGheAndMaLichDien(ve.getGheNgoi().getMaGhe(), ve.getLichDien().getMaLichDien());
            if (optStatus.isPresent()) {
                TrangThaiGheTheoSuat status = optStatus.get();
                status.setTrangThai("Còn trống");
                status.setTaiKhoan(null);
                status.setThoiGianHetHan(null);
                trangThaiGheTheoSuatRepository.save(status);
            }
        }
        
        List<Ve> allTickets = veRepository.findByDonMua_MaDonMua(ve.getDonMua().getMaDonMua());
        boolean allCanceled = allTickets.stream().allMatch(t -> "Đã hủy".equals(t.getTrangThaiVe()) || "Đã hoàn vé".equals(t.getTrangThaiVe()));
        if (allCanceled) {
            DonMua dm = ve.getDonMua();
            dm.setTrangThaiThanhToan("Đã hoàn tiền");
            donMuaRepository.save(dm);
        }
    }

    @Transactional
    public void approveRefund(Long requestId) {
        YeuCauHoTro yeuCau = yeuCauHoTroRepository.findById(requestId).orElseThrow(() -> new RuntimeException("Yêu cầu không tồn tại"));
        if (!"Chờ phản hồi".equals(yeuCau.getTrangThaiXuLy())) {
            throw new RuntimeException("Yêu cầu đã được xử lý");
        }

        SuKien sk = yeuCau.getDonMua().getSuKien();
        LocalDateTime thoiDiemYeuCau = yeuCau.getThoiDiemYeuCau() != null ? yeuCau.getThoiDiemYeuCau() : LocalDateTime.now();
        BigDecimal maxTyLe = calculateRefundPercentage(sk, thoiDiemYeuCau);

        List<Ve> ticketsInOrder = veRepository.findByDonMua_MaDonMua(yeuCau.getDonMua().getMaDonMua());
        BigDecimal totalValidPrice = BigDecimal.ZERO;
        for (Ve ticket : ticketsInOrder) {
            if (!"Đã hủy".equals(ticket.getTrangThaiVe()) && !"Đã hoàn vé".equals(ticket.getTrangThaiVe())) {
                totalValidPrice = totalValidPrice.add(ticket.getHangVe().getGiaNiemYet());
            }
        }

        if (totalValidPrice.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal amount = totalValidPrice.multiply(maxTyLe).divide(new BigDecimal(100));
            walletService.receive(yeuCau.getTaiKhoan().getMaTaiKhoan(), amount, "Hoàn tiền cho yêu cầu #" + yeuCau.getMaYeuCau() + " (" + maxTyLe + "%)");
        }

        yeuCau.setTrangThaiXuLy("Đã xử lý");
        yeuCauHoTroRepository.save(yeuCau);

        DonMua dm = yeuCau.getDonMua();
        dm.setTrangThaiThanhToan("Đã hoàn tiền");
        donMuaRepository.save(dm);
        
        cancelOrderTicketsAndFreeSeats(dm);
    }

    public List<ResaleTicketDto> getActiveResales() {
        List<Ve> tickets = veRepository.findActiveResales();
        System.out.println("DEBUG: Found " + tickets.size() + " resale tickets in DB");
        return tickets.stream()
                .map(v -> {
                    String thumbnailUrl = v.getLichDien().getSuKien().getAnhThumbnailUrl();
                    if (thumbnailUrl == null || thumbnailUrl.isEmpty()) {
                        thumbnailUrl = "https://via.placeholder.com/640x480.png?text=No+Thumbnail";
                    }
                    
                    return new ResaleTicketDto(
                        v.getMaVe(),
                        v.getLichDien().getSuKien().getTenSuKien(),
                        v.getLichDien().getThoiGianBatDau().toString(),
                        v.getHangVe().getTenHangVe(),
                        v.getGheNgoi() != null ? v.getGheNgoi().getToaDo() : "Tự do",
                        v.getGiaBanLai(),
                        v.getDonMua().getTaiKhoan().getTenDangNhap(),
                        thumbnailUrl
                    );
                })
                .collect(Collectors.toList());
    }

    public List<YeuCauHoTro> getPendingRefunds() {
        return yeuCauHoTroRepository.findByLoaiYeuCauAndTrangThaiXuLy("Hoàn tiền", "Chờ phản hồi");
    }

    @Transactional
    public CheckInResponse checkIn(CheckInRequest request, Long staffId) {
        try {
            Long ticketId = Long.parseLong(request.getQrCode());
            Ve ve = veRepository.findById(ticketId)
                    .orElseThrow(() -> new RuntimeException("Vé không tồn tại hoặc mã QR không hợp lệ"));

            // 1. Kiểm tra trạng thái vé
            if ("Đã Check-in".equals(ve.getTrangThaiVe())) {
                logCheckInHistory(staffId, ve, 0, request.getDeviceName());
                return new CheckInResponse(false, "Vé này đã được sử dụng lúc " + ve.getThoiGianCheckIn());
            }
            if (!"Hiệu lực".equals(ve.getTrangThaiVe())) {
                logCheckInHistory(staffId, ve, 0, request.getDeviceName());
                return new CheckInResponse(false, "Vé không hợp lệ (Trạng thái: " + ve.getTrangThaiVe() + ")");
            }

            // 2. Kiểm tra thời gian (Chỉ cho phép soát vé trước/trong khi sự kiện diễn ra)
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startTime = ve.getLichDien().getThoiGianBatDau();
            if (now.isBefore(startTime.minusHours(4))) { // Cho phép vào trước 4 tiếng
                return new CheckInResponse(false, "Chưa đến giờ soát vé cho sự kiện này");
            }

            // 3. Cập nhật trạng thái vé
            ve.setTrangThaiVe("Đã Check-in");
            ve.setThoiGianCheckIn(now);
            veRepository.save(ve);

            // 4. Ghi nhật ký thành công
            logCheckInHistory(staffId, ve, 1, request.getDeviceName());

            // 5. Trả về kết quả
            CheckInResponse response = new CheckInResponse(true, "Soát vé thành công!");
            response.setTicketId(ve.getMaVe().toString());
            response.setEventName(ve.getLichDien().getSuKien().getTenSuKien());
            response.setAttendeeName(ve.getDonMua().getTaiKhoan().getNguoiDung().getHoTen());
            response.setSeatInfo(ve.getGheNgoi() != null ? ve.getGheNgoi().getToaDo() : "Vé đứng/Tự do");
            response.setCheckInTime(now);
            return response;

        } catch (NumberFormatException e) {
            return new CheckInResponse(false, "Mã QR không đúng định dạng hệ thống");
        } catch (Exception e) {
            return new CheckInResponse(false, "Lỗi hệ thống: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<CheckInResponse> getStaffScanHistory(Long staffId) {
        // Truy vấn lịch sử quét vé của staffId (chỉ lấy các lượt quét thành công = 1)
        return nhatKySoatVeRepository.findByTaiKhoan_MaTaiKhoanAndTrangThaiSoatVeOrderByThoiGianQuetMaDesc(staffId, 1)
                .stream()
                .map(nk -> {
                    Ve t = nk.getVe();
                    if (t == null) return null;

                    String attendeeName = "Khách hàng";
                    if (t.getDonMua() != null && t.getDonMua().getTaiKhoan() != null && t.getDonMua().getTaiKhoan().getNguoiDung() != null) {
                        attendeeName = t.getDonMua().getTaiKhoan().getNguoiDung().getHoTen();
                    }
                    
                    String eventName = "Sự kiện";
                    if (t.getLichDien() != null && t.getLichDien().getSuKien() != null) {
                        eventName = t.getLichDien().getSuKien().getTenSuKien();
                    } else if (t.getHangVe() != null && t.getHangVe().getSuKien() != null) {
                        eventName = t.getHangVe().getSuKien().getTenSuKien();
                    }
                    
                    CheckInResponse res = new CheckInResponse(
                            true, 
                            "Checked", 
                            eventName, 
                            attendeeName,
                            String.valueOf(t.getMaVe())
                    );
                    res.setCheckInTime(nk.getThoiGianQuetMa());
                    res.setSeatInfo(t.getGheNgoi() != null ? t.getGheNgoi().getToaDo() : "Vé đứng/Tự do");
                    return res;
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    private void logCheckInHistory(Long staffId, Ve ve, Integer status, String deviceName) {
        System.out.println("DEBUG: Saving scan log for staffId: " + staffId + " and Ticket: " + ve.getMaVe());
        TaiKhoan staff = taiKhoanRepository.findById(staffId).orElse(null);
        if (staff == null) {
            System.out.println("DEBUG ERROR: Staff (TaiKhoan) not found with ID: " + staffId);
            return;
        }
        NhatKySoatVe log = new NhatKySoatVe();
        log.setTaiKhoan(staff);
        log.setVe(ve);
        log.setTrangThaiSoatVe(status);
        log.setThietBiQuet(deviceName != null ? deviceName : "Unknown Device");
        nhatKySoatVeRepository.save(log);
        System.out.println("DEBUG: Scan log saved successfully to NHAT_KY_SOAT_VE");
    }
}
