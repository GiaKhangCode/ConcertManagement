package com.stellar.backend.repository;

import com.stellar.backend.entity.ChienDichKhuyenMai;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChienDichKhuyenMaiRepository extends JpaRepository<ChienDichKhuyenMai, Long> {
    
    @Query("SELECT DISTINCT c FROM ChienDichKhuyenMai c " +
           "LEFT JOIN FETCH c.suKien " +
           "LEFT JOIN FETCH c.danhSachMaGiamGia " +
           "WHERE c.suKien.maSuKien = :maSuKien")
    List<ChienDichKhuyenMai> findBySuKienMaSuKien(@Param("maSuKien") Long maSuKien);

    @Query("SELECT DISTINCT c FROM ChienDichKhuyenMai c " +
           "LEFT JOIN FETCH c.suKien s " +
           "LEFT JOIN FETCH c.danhSachMaGiamGia " +
           "WHERE s.nguoiTao.maTaiKhoan = :maTaiKhoan")
    List<ChienDichKhuyenMai> findBySuKienNguoiTaoMaTaiKhoan(@Param("maTaiKhoan") Long maTaiKhoan);

    List<ChienDichKhuyenMai> findByTrangThai(String trangThai);
}
