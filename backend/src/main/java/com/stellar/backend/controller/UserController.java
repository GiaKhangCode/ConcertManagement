package com.stellar.backend.controller;

import com.stellar.backend.dto.UserProfileResponseDto;
import com.stellar.backend.dto.UserTicketResponseDto;
import com.stellar.backend.entity.*;
import com.stellar.backend.repository.*;
import com.stellar.backend.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private TaiKhoanRepository taiKhoanRepository;

    @Autowired
    private ViCaNhanRepository viCaNhanRepository;

    @Autowired
    private DonMuaRepository donMuaRepository;
    
    @Autowired
    private VeRepository veRepository;

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        TaiKhoan tk = taiKhoanRepository.findById(userDetails.getId())
            .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại!"));
        
        NguoiDung nd = tk.getNguoiDung();
        
        UserProfileResponseDto response = new UserProfileResponseDto();
        response.setUsername(tk.getTenDangNhap());
        response.setFullName(nd != null ? nd.getHoTen() : "");
        response.setEmail(nd != null ? nd.getEmail() : "");
        response.setPhone(nd != null ? nd.getSoDienThoai() : "");
        response.setAccountStatus(nd != null ? nd.getDaXacThuc() : 0);

        Optional<ViCaNhan> vi = viCaNhanRepository.findByTaiKhoan_MaTaiKhoan(tk.getMaTaiKhoan());
        if(vi.isPresent()) {
            response.setWalletBalance(vi.get().getSoDu());
        } else {
            response.setWalletBalance(new java.math.BigDecimal("0"));
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/wallet/topup")
    public ResponseEntity<?> topUpWallet(@RequestBody java.util.Map<String, Object> request) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        java.math.BigDecimal amount = new java.math.BigDecimal(request.get("amount").toString());
        if (amount.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", "Số tiền nạp phải lớn hơn 0!"));
        }

        ViCaNhan vi = viCaNhanRepository.findByTaiKhoan_MaTaiKhoan(userDetails.getId())
            .orElseGet(() -> {
                ViCaNhan newVi = new ViCaNhan();
                newVi.setTaiKhoan(taiKhoanRepository.findById(userDetails.getId()).get());
                newVi.setSoDu(java.math.BigDecimal.ZERO);
                return newVi;
            });

        vi.setSoDu(vi.getSoDu().add(amount));
        viCaNhanRepository.save(vi);

        return ResponseEntity.ok(java.util.Map.of(
            "message", "Nạp tiền thành công!",
            "newBalance", vi.getSoDu()
        ));
    }

    @GetMapping("/tickets")
    public ResponseEntity<?> getUserTickets() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        List<DonMua> orders = donMuaRepository.findByTaiKhoan_MaTaiKhoan(userDetails.getId());
        
        List<UserTicketResponseDto> result = new ArrayList<>();
        for(DonMua don : orders) {
            UserTicketResponseDto dto = new UserTicketResponseDto();
            dto.setTransactionId(don.getMaDonMua());
            dto.setEventName(don.getSuKien().getTenSuKien());
            dto.setTotalPrice(don.getTongTien());
            dto.setBookingTime(don.getThoiDiemMua() != null ? don.getThoiDiemMua() : java.time.LocalDateTime.now());
            
            List<Ve> veList = veRepository.findByDonMua_MaDonMua(don.getMaDonMua());
            dto.setTicketCount(veList.size());
            if(!veList.isEmpty()) {
                dto.setTierName(veList.get(0).getHangVe().getTenHangVe());
            } else {
                dto.setTierName("N/A");
            }
            result.add(dto);
        }
        
        return ResponseEntity.ok(result);
    }
}
