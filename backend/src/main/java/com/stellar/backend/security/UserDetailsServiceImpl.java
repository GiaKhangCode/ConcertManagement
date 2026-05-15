package com.stellar.backend.security;

import com.stellar.backend.entity.TaiKhoan;
import com.stellar.backend.repository.TaiKhoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    
    @Autowired
    TaiKhoanRepository taiKhoanRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        TaiKhoan user = taiKhoanRepository.findByTenDangNhap(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));
        
        // Cố tình gọi get để fetch dữ liệu từ bảng liên kết (nếu cấu hình là LAZY)
        user.getNhomQuyens().size(); 

        return UserDetailsImpl.build(user);
    }
}
