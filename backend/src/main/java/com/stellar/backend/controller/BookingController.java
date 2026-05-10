package com.stellar.backend.controller;

import com.stellar.backend.dto.BookingRequestDto;
import com.stellar.backend.entity.DonMua;
import com.stellar.backend.entity.HangVe;
import com.stellar.backend.entity.SuKien;
import com.stellar.backend.entity.TaiKhoan;
import com.stellar.backend.entity.Ve;
import com.stellar.backend.entity.MaGiamGia;
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

    @Autowired
    private com.stellar.backend.service.BookingService bookingService;

    @PostMapping("/create")
    public ResponseEntity<?> createBooking(@RequestBody BookingRequestDto request) {
        try {
            // Lấy thông tin tài khoản đang login
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Long userId;
            if (principal instanceof UserDetailsImpl) {
                userId = ((UserDetailsImpl) principal).getId();
            } else {
                return ResponseEntity.status(401).body(Map.of("message", "Phiên đăng nhập không hợp lệ!"));
            }

            // Gọi Service để xử lý đặt vé (Transaction được quản lý tại Service)
            DonMua savedDonMua = bookingService.processBooking(userId, request);

            // Response xuất hóa đơn ảo
            Map<String, Object> result = new HashMap<>();
            result.put("invoiceId", savedDonMua.getMaDonMua());
            result.put("message", "Giao dịch Đặt Vé Thành Công!");
            result.put("totalTickets", request.getSoLuong());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Thanh toán thất bại: " + e.getMessage()));
        }
    }
}
