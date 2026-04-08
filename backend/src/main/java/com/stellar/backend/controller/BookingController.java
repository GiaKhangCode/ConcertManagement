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
import com.stellar.backend.repository.ViCaNhanRepository;
import com.stellar.backend.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.stellar.backend.entity.ViCaNhan;

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

    @Autowired
    private ViCaNhanRepository viCaNhanRepository;

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

        // Tính tiền dựa theo giá DB
        BigDecimal tongTien = hangVe.getGiaNiemYet().multiply(BigDecimal.valueOf(request.getSoLuong()));

        // --- BẮT ĐẦU THANH TOÁN QUA VÍ ---
        ViCaNhan vi = viCaNhanRepository.findByTaiKhoan_MaTaiKhoan(taiKhoan.getMaTaiKhoan())
            .orElseThrow(() -> new RuntimeException("Người dùng chưa có ví cá nhân!"));

        if (vi.getSoDu().compareTo(tongTien) < 0) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Số dư ví không đủ để thanh toán (" + tongTien + " VNĐ). Vui lòng nạp thêm!");
            return ResponseEntity.badRequest().body(error);
        }

        // Trừ tiền
        vi.setSoDu(vi.getSoDu().subtract(tongTien));
        viCaNhanRepository.save(vi);
        // --- KẾT THÚC THANH TOÁN ---

        // Khởi tạo một Đơn Mua
        DonMua donMua = new DonMua();
        donMua.setTaiKhoan(taiKhoan);
        donMua.setSuKien(suKien);
        donMua.setTongTien(tongTien);
        donMua.setTrangThaiThanhToan("Đã thanh toán"); 
        donMua.setPhuongThucThanhToan("Ví cá nhân (Ve'ryGood Pay)");
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
