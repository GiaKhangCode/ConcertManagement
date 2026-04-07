package com.stellar.backend.repository;
import com.stellar.backend.entity.SuKien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SuKienRepository extends JpaRepository<SuKien, Long> {
    // Lấy danh sách sự kiện theo trạng thái (VD: "Chờ phê duyệt", "Sắp diễn ra")
    List<SuKien> findByTrangThai(String trangThai);

    // Lấy danh sách sự kiện của một nhà tổ chức theo mã tài khoản
    List<SuKien> findByNguoiTao_MaTaiKhoan(Long maTaiKhoan);
}
