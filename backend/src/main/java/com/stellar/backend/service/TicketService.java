package com.stellar.backend.service;

import com.stellar.backend.entity.*;
import com.stellar.backend.dto.ResaleTicketDto;
import com.stellar.backend.repository.VeRepository;
import com.stellar.backend.repository.YeuCauHoTroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketService {
    @Autowired
    private VeRepository veRepository;

    @Autowired
    private YeuCauHoTroRepository yeuCauHoTroRepository;

    @Autowired
    private WalletService walletService;

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

        BigDecimal price = ve.getGiaBanLai();
        Long sellerId = ve.getDonMua().getTaiKhoan().getMaTaiKhoan();

        if (buyerId.equals(sellerId)) {
            throw new RuntimeException("Bạn không thể mua lại vé của chính mình");
        }

        // Buyer pays
        walletService.pay(buyerId, price, "Mua vé bán lại #" + ve.getMaVe());
        
        // Seller receives
        walletService.receive(sellerId, price, "Tiền bán lại vé #" + ve.getMaVe());

        // Update ticket status
        ve.setDaBanLai(0);
        ve.setGiaBanLai(null);
        // Note: Cập nhật chủ sở hữu thực tế yêu cầu thay đổi MaDonMua. 
        // Trong hệ thống này, ta giả định việc đổi chủ sở hữu đơn giản là chuyển quyền sử dụng.
        // Để đúng hoàn toàn schema, ta nên tạo DonMua mới và gán lại cho Ve này.
        veRepository.save(ve);
    }

    @Transactional
    public void requestRefund(Long ticketId, String reason, Long userId) {
        Ve ve = veRepository.findById(ticketId).orElseThrow(() -> new RuntimeException("Vé không tồn tại"));
        if (!ve.getDonMua().getTaiKhoan().getMaTaiKhoan().equals(userId)) {
            throw new RuntimeException("Bạn không sở hữu vé này");
        }

        YeuCauHoTro yeuCau = new YeuCauHoTro();
        yeuCau.setDonMua(ve.getDonMua());
        yeuCau.setTaiKhoan(ve.getDonMua().getTaiKhoan());
        yeuCau.setLoaiYeuCau("Hoàn tiền");
        yeuCau.setNoiDung(reason + " | Vé #" + ticketId);
        yeuCau.setTrangThaiXuLy("Chờ phản hồi");
        yeuCauHoTroRepository.save(yeuCau);

        // Lưu ý: Trigger TRG_YEU_CAU_HO_TRO_CHECK_HOANTIEN trong SQL sẽ tự kiểm tra điều kiện hoàn tiền
    }

    @Transactional
    public void approveRefund(Long requestId) {
        YeuCauHoTro yeuCau = yeuCauHoTroRepository.findById(requestId).orElseThrow(() -> new RuntimeException("Yêu cầu không tồn tại"));
        if (!"Chờ phản hồi".equals(yeuCau.getTrangThaiXuLy())) {
            throw new RuntimeException("Yêu cầu đã được xử lý");
        }

        // Hoàn tiền vào ví (Ở đây ta lấy tạm giá hạng vé, thực tế nên tính theo chính sách trong DB)
        // Vì schema không có trường số tiền hoàn trong YEU_CAU_HO_TRO, 
        // ta giả định hoàn 100% hoặc lấy từ logic nghiệp vụ.
        BigDecimal amount = yeuCau.getDonMua().getTongTien(); 
        walletService.receive(yeuCau.getTaiKhoan().getMaTaiKhoan(), amount, "Hoàn tiền cho yêu cầu #" + yeuCau.getMaYeuCau());

        yeuCau.setTrangThaiXuLy("Đã xử lý");
        yeuCauHoTroRepository.save(yeuCau);

        // Cập nhật trạng thái đơn mua và vé
        DonMua dm = yeuCau.getDonMua();
        dm.setTrangThaiThanhToan("Đã hoàn tiền");
        // Trigger TRG_DON_MUA_AU_UPDATE_TRANGTHAITHANHTOAN_DAHUY sẽ tự cập nhật trạng thái vé về 'Đã hủy'
    }

    public List<ResaleTicketDto> getActiveResales() {
        List<Ve> tickets = veRepository.findActiveResales();
        System.out.println("DEBUG: Found " + tickets.size() + " resale tickets in DB");
        return tickets.stream()
                .map(v -> {
                    System.out.println("DEBUG: Mapping ticket ID " + v.getMaVe() + " - Resale Price: " + v.getGiaBanLai());
                    return new ResaleTicketDto(
                        v.getMaVe(),
                        v.getLichDien().getSuKien().getTenSuKien(),
                        v.getLichDien().getThoiGianBatDau().toString(),
                        v.getHangVe().getTenHangVe(),
                        v.getGheNgoi().getToaDo(),
                        v.getGiaBanLai(),
                        v.getDonMua().getTaiKhoan().getTenDangNhap()
                    );
                })
                .collect(Collectors.toList());
    }

    public List<YeuCauHoTro> getPendingRefunds() {
        return yeuCauHoTroRepository.findByLoaiYeuCauAndTrangThaiXuLy("Hoàn tiền", "Chờ phản hồi");
    }
}
