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
import com.stellar.backend.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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
    private VeRepository veRepository;

    @Autowired
    private SuKienRepository suKienRepository;

    @Autowired
    private HangVeRepository hangVeRepository;

    @Autowired
    private TaiKhoanRepository taiKhoanRepository;

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

        // Tính tiền dựa theo giá DB
        BigDecimal tongTien = hangVe.getGiaNiemYet().multiply(BigDecimal.valueOf(request.getSoLuong()));

        // Khởi tạo một Đơn Mua
        DonMua donMua = new DonMua();
        donMua.setTaiKhoan(taiKhoan);
        donMua.setSuKien(suKien);
        donMua.setTongTien(tongTien);
        // Giả sử tiền đã trừ tự động qua liên kết Ví (Stellar Pay)
        donMua.setTrangThaiThanhToan("Đã thanh toán"); 
        donMua.setPhuongThucThanhToan("Stellar Pay");
        donMua = donMuaRepository.save(donMua);

        // Khởi tạo hàng loạt các dòng dữ liệu VÉ thật phụ thuộc vào số lượng
        List<Ve> veList = new ArrayList<>();
        for (int i = 0; i < request.getSoLuong(); i++) {
            Ve ve = new Ve();
            ve.setDonMua(donMua);
            ve.setHangVe(hangVe);
            ve.setDaBanLai(0);
            ve.setTrangThaiVe("Hiệu lực");
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
