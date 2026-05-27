package com.stellar.backend.repository;
import com.stellar.backend.entity.DonMua;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonMuaRepository extends JpaRepository<DonMua, Long> {
    List<DonMua> findByTaiKhoan_MaTaiKhoanOrderByThoiDiemMuaDesc(Long maTaiKhoan);

    // Lấy tất cả đơn mua thuộc về các sự kiện do một nhà tổ chức tạo ra
    List<DonMua> findBySuKien_NguoiTao_MaTaiKhoanOrderByThoiDiemMuaDesc(Long maTaiKhoan);

    // Lấy đơn mua theo mã sự kiện
    List<DonMua> findBySuKien_MaSuKienOrderByThoiDiemMuaDesc(Long maSuKien);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(d.tongTien) FROM DonMua d WHERE d.maDonMua IN " +
            "(SELECT DISTINCT d2.maDonMua FROM DonMua d2 WHERE d2.suKien.maSuKien = :maSuKien " +
            "AND d2.trangThaiThanhToan IN ('Đã thanh toán', 'Đã hủy', 'Đã hoàn tiền'))")
    java.math.BigDecimal sumGrossRevenueBySuKien_MaSuKien(Long maSuKien);
}
