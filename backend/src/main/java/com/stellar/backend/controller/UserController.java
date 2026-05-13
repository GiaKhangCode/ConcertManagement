package com.stellar.backend.controller;

import com.stellar.backend.dto.UserProfileResponseDto;
import com.stellar.backend.dto.UserOrderResponseDto;
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

    @Autowired
    private NhaToChucRepository nhaToChucRepository;

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

        // Lấy danh sách vai trò
        List<String> roles = userDetails.getAuthorities().stream()
            .map(item -> item.getAuthority())
            .collect(java.util.stream.Collectors.toList());
        response.setRoles(roles);

        // Lấy thông tin nhà tổ chức nếu có
        Optional<NhaToChuc> ntc = nhaToChucRepository.findByTaiKhoan_MaTaiKhoan(tk.getMaTaiKhoan());
        if(ntc.isPresent()) {
            response.setOrganizationName(ntc.get().getTenNhaToChuc());
            response.setTaxCode(ntc.get().getMaSoThue());
            response.setBankInfo(ntc.get().getThongTinNganHang());
            response.setSupportEmail(ntc.get().getEmailHoTro());
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/organizer-setup")
    public ResponseEntity<?> setupOrganizer(@RequestBody java.util.Map<String, String> request) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        TaiKhoan tk = taiKhoanRepository.findById(userDetails.getId())
            .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại!"));

        // Kiểm tra xem có quyền ROLE_ORGANIZER không
        boolean isOrganizer = userDetails.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ORGANIZER"));
        
        if(!isOrganizer) {
            return ResponseEntity.status(403).body(java.util.Map.of("message", "Bạn không có quyền thực hiện thao tác này!"));
        }

        NhaToChuc ntc = nhaToChucRepository.findByTaiKhoan_MaTaiKhoan(tk.getMaTaiKhoan())
            .orElse(new NhaToChuc());
        
        String orgName = request.get("organizationName");
        String taxCode = request.get("taxCode");
        String bankInfo = request.get("bankInfo");
        String supportEmail = request.get("supportEmail");

        if (orgName == null || orgName.trim().isEmpty() ||
            taxCode == null || taxCode.trim().isEmpty() ||
            bankInfo == null || bankInfo.trim().isEmpty() ||
            supportEmail == null || supportEmail.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", "Vui lòng cung cấp đầy đủ thông tin nhà tổ chức!"));
        }

        ntc.setTaiKhoan(tk);
        ntc.setNguoiDaiDien(tk.getNguoiDung());
        ntc.setTenNhaToChuc(orgName.trim());
        ntc.setMaSoThue(taxCode.trim());
        ntc.setThongTinNganHang(bankInfo.trim());
        ntc.setEmailHoTro(supportEmail.trim());

        nhaToChucRepository.save(ntc);

        return ResponseEntity.ok(java.util.Map.of("message", "Cập nhật thông tin nhà tổ chức thành công!"));
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
        
        List<UserOrderResponseDto> result = new ArrayList<>();
        for(DonMua don : orders) {
            List<Ve> veList = veRepository.findByDonMua_MaDonMua(don.getMaDonMua());
            if (veList.isEmpty()) continue;

            UserOrderResponseDto orderDto = new UserOrderResponseDto();
            orderDto.setTransactionId(don.getMaDonMua());
            orderDto.setEventName(don.getSuKien().getTenSuKien());
            orderDto.setEventId(don.getSuKien().getMaSuKien());
            orderDto.setTotalPrice(don.getTongTien());
            orderDto.setBookingTime(don.getThoiDiemMua() != null ? don.getThoiDiemMua() : java.time.LocalDateTime.now());

            List<UserTicketResponseDto> ticketDtos = new ArrayList<>();
            for(Ve ve : veList) {
                UserTicketResponseDto dto = new UserTicketResponseDto();
                dto.setTicketId(ve.getMaVe());
                dto.setTierName(ve.getHangVe() != null ? ve.getHangVe().getTenHangVe() : "");
                dto.setZoneName(ve.getKhuVuc() != null ? ve.getKhuVuc().getTenKhuVuc() : "Khu vực chung");
                dto.setSeatInfo(ve.getGheNgoi() != null ? ve.getGheNgoi().getToaDo() : "Đứng / Tự do");
                dto.setStatus(ve.getTrangThaiVe());
                ticketDtos.add(dto);
            }
            orderDto.setTickets(ticketDtos);
            if (!ticketDtos.isEmpty()) {
                result.add(orderDto);
            }
        }
        System.out.println("DEBUG: Trả về " + result.size() + " đơn mua.");
        return ResponseEntity.ok(result);
    }
}
