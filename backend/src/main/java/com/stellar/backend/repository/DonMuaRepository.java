package com.stellar.backend.repository;
import com.stellar.backend.entity.DonMua;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonMuaRepository extends JpaRepository<DonMua, Long> {
    List<DonMua> findByTaiKhoan_MaTaiKhoan(Long maTaiKhoan);

    // Lấy tất cả đơn mua thuộc về các sự kiện do một nhà tổ chức tạo ra
    List<DonMua> findBySuKien_NguoiTao_MaTaiKhoan(Long maTaiKhoan);

    // Lấy đơn mua theo mã sự kiện
    List<DonMua> findBySuKien_MaSuKien(Long maSuKien);
}
