package com.stellar.backend.repository;

import com.stellar.backend.entity.LichSuHoanTien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface LichSuHoanTienRepository extends JpaRepository<LichSuHoanTien, Long> {

    /**
     * Lấy tất cả bản ghi hoàn tiền của một sự kiện, sắp xếp mới nhất trước
     */
    List<LichSuHoanTien> findBySuKien_MaSuKienOrderByThoiDiemHoanDesc(Long maSuKien);

    /**
     * Tổng tiền đã hoàn cho một sự kiện (dùng trong quyết toán)
     */
    @Query("SELECT COALESCE(SUM(h.soTienHoan), 0) FROM LichSuHoanTien h WHERE h.suKien.maSuKien = :maSuKien")
    BigDecimal sumSoTienHoanBySuKien(@Param("maSuKien") Long maSuKien);

    /**
     * Đếm số lần hoàn tiền theo loại (người dùng / hủy sự kiện)
     */
    long countBySuKien_MaSuKienAndLoaiHoan(Long maSuKien, String loaiHoan);
}
