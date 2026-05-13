package com.stellar.backend.controller;

import com.stellar.backend.entity.LichSuBienDongVi;
import com.stellar.backend.entity.ViCaNhan;
import com.stellar.backend.security.UserDetailsImpl;
import com.stellar.backend.service.SettlementService;
import com.stellar.backend.service.TicketService;
import com.stellar.backend.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/finance")
public class FinancialController {
    @Autowired
    private WalletService walletService;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private SettlementService settlementService;

    @Autowired
    private com.stellar.backend.repository.LichSuQuyetToanRepository lichSuQuyetToanRepository;

    @Autowired
    private com.stellar.backend.repository.SuKienRepository suKienRepository;

    // --- WALLET ---
    @GetMapping("/wallet")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getWallet() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ViCaNhan vi = walletService.getWalletByUserId(userDetails.getId());
        return ResponseEntity.ok(vi);
    }

    @PostMapping("/wallet/deposit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deposit(@RequestBody Map<String, Object> body) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        walletService.deposit(userDetails.getId(), amount, "Nạp tiền vào ví");
        return ResponseEntity.ok(Map.of("message", "Nạp tiền thành công!"));
    }

    @GetMapping("/wallet/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getHistory() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<LichSuBienDongVi> history = walletService.getHistory(userDetails.getId());
        return ResponseEntity.ok(history);
    }

    // --- REFUND ---
    @GetMapping("/refunds/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPendingRefunds() {
        return ResponseEntity.ok(ticketService.getPendingRefunds());
    }

    @PostMapping("/tickets/{id}/refund")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> requestRefund(@PathVariable Long id, @RequestBody Map<String, String> body) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ticketService.requestRefund(id, body.get("reason"), userDetails.getId());
        return ResponseEntity.ok(Map.of("message", "Yêu cầu hoàn tiền đã được gửi!"));
    }

    @PostMapping("/refunds/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveRefund(@PathVariable Long id) {
        ticketService.approveRefund(id);
        return ResponseEntity.ok(Map.of("message", "Đã hoàn tiền thành công!"));
    }

    // --- RESALE ---
    @GetMapping("/resale")
    public ResponseEntity<?> getActiveResales() {
        return ResponseEntity.ok(ticketService.getActiveResales());
    }

    @PostMapping("/tickets/{id}/resale")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> listForResale(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        BigDecimal price = new BigDecimal(body.get("price").toString());
        ticketService.listForResale(id, price, userDetails.getId());
        return ResponseEntity.ok(Map.of("message", "Đã niêm yết bán lại vé!"));
    }

    @PostMapping("/resale/{id}/buy")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> buyResale(@PathVariable Long id) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ticketService.buyResaleTicket(id, userDetails.getId());
        return ResponseEntity.ok(Map.of("message", "Mua vé thành công!"));
    }

    // --- SETTLEMENT ---
    @GetMapping("/settlement/{eventId}")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<?> getSettlement(@PathVariable Long eventId) {
        return ResponseEntity.ok(settlementService.getEventSettlement(eventId));
    }

    @PostMapping("/settlement/{eventId}/execute")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    public ResponseEntity<?> executeSettlement(@PathVariable Long eventId) {
        try {
            Map<String, Object> result = settlementService.executeSettlement(eventId);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/settlement/history")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<?> getSettlementHistory() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        List<com.stellar.backend.entity.LichSuQuyetToan> history;
        if (isAdmin) {
            history = lichSuQuyetToanRepository.findAll();
        } else {
            history = lichSuQuyetToanRepository.findAll().stream()
                .filter(q -> q.getNhaToChuc() != null && q.getNhaToChuc().getTaiKhoan() != null && q.getNhaToChuc().getTaiKhoan().getMaTaiKhoan().equals(userDetails.getId()))
                .collect(java.util.stream.Collectors.toList());
        }

        List<Map<String, Object>> result = history.stream().map(h -> {
            Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("maQuyetToan", h.getMaQT());
            map.put("suKien", h.getKyQT());
            map.put("soTienQuyetToan", h.getSoTienChuyen());
            map.put("trangThai", h.getTrangThai());
            map.put("thoiDiemQuyetToan", h.getNgayQuyetToan() != null ? h.getNgayQuyetToan().toString() : "");
            map.put("noiDung", h.getChungTuThanhToan());
            return map;
        }).collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(result);
    }
}
