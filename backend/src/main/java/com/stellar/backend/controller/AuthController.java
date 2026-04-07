package com.stellar.backend.controller;

import com.stellar.backend.dto.*;
import com.stellar.backend.entity.NguoiDung;
import com.stellar.backend.entity.TaiKhoan;
import com.stellar.backend.repository.NguoiDungRepository;
import com.stellar.backend.repository.TaiKhoanRepository;
import com.stellar.backend.entity.NhomQuyen;
import com.stellar.backend.repository.NhomQuyenRepository;
import com.stellar.backend.security.JwtUtils;
import com.stellar.backend.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    TaiKhoanRepository taiKhoanRepository;

    @Autowired
    NguoiDungRepository nguoiDungRepository;

    @Autowired
    NhomQuyenRepository nhomQuyenRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Update last login timestamp
        TaiKhoan tk = taiKhoanRepository.findById(userDetails.getId()).orElse(null);
        if (tk != null) {
            tk.setLanDangNhapCuoi(LocalDateTime.now());
            taiKhoanRepository.save(tk);
        }

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt, 
                                                 userDetails.getId(), 
                                                 userDetails.getUsername(), 
                                                 userDetails.getEmail(),
                                                 roles));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest signUpRequest) {
        if (taiKhoanRepository.existsByTenDangNhap(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Lỗi: Tên đăng nhập đã tồn tại!"));
        }

        if (nguoiDungRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Lỗi: Email đã được sử dụng!"));
        }

        if (nguoiDungRepository.existsBySoDienThoai(signUpRequest.getPhone())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Lỗi: Số điện thoại đã được sử dụng!"));
        }

        // 1. Tạo NguoiDung
        NguoiDung nguoiDung = new NguoiDung();
        nguoiDung.setHoTen(signUpRequest.getFullName());
        nguoiDung.setEmail(signUpRequest.getEmail());
        nguoiDung.setSoDienThoai(signUpRequest.getPhone());
        nguoiDung.setDaXacThuc(0);
        nguoiDung.setNgayTao(LocalDateTime.now());
        
        // 2. Tạo TaiKhoan -> NguoiDung là quan hệ liên đới (Cascade)
        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setTenDangNhap(signUpRequest.getUsername());
        taiKhoan.setMatKhauMaHoa(encoder.encode(signUpRequest.getPassword()));
        taiKhoan.setTrangThai("Hoạt động");
        taiKhoan.setDongYNhanMarketing(0);
        taiKhoan.setNguoiDung(nguoiDung);

        String reqRole = signUpRequest.getRole();
        String roleName = "ROLE_CUSTOMER"; // Mặc định là Khách hàng
        
        if (reqRole != null && reqRole.equalsIgnoreCase("ORGANIZER")) {
            roleName = "ROLE_ORGANIZER";
        }
        
        // Không cấp quyền ADMIN qua API register
        final String finalRoleName = roleName;

        NhomQuyen quyenDuocCap = nhomQuyenRepository.findByTenNhomQuyen(roleName)
                .orElseGet(() -> {
                    NhomQuyen rq = new NhomQuyen();
                    rq.setTenNhomQuyen(finalRoleName);
                    return nhomQuyenRepository.save(rq);
                });
        taiKhoan.getNhomQuyens().add(quyenDuocCap);

        taiKhoanRepository.save(taiKhoan);

        return ResponseEntity.ok(new MessageResponse("Đăng ký tài khoản thành công! Phân quyển được cập nhật tự động."));
    }
}
