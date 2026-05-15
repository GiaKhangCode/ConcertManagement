package com.stellar.backend.repository;
import com.stellar.backend.entity.Ve;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VeRepository extends JpaRepository<Ve, Long> {
    List<Ve> findByDonMua_MaDonMua(Long maDonMua);
    List<Ve> findByHangVe_SuKien_MaSuKien(Long maSuKien);

    long countByHangVe_SuKien_MaSuKien(Long maSuKien);
    long countByHangVe_MaHangVe(Long maHangVe);
    long countByKhuVuc_MaKhuVuc(Long maKhuVuc);
    long countByKhuVuc_MaKhuVucAndTrangThaiVeNotIn(Long maKhuVuc, java.util.Collection<String> trangThaiVe);
    long countByHangVe_MaHangVeAndTrangThaiVeNotIn(Long maHangVe, java.util.Collection<String> trangThaiVe);
    long countByHangVe_SuKien_MaSuKienAndTrangThaiVeIn(Long maSuKien, java.util.List<String> statuses);

    // Lấy tất cả vé của một sự kiện mà chưa bị hủy (phục vụ hủy sự kiện)
    List<Ve> findByHangVe_SuKien_MaSuKienAndTrangThaiVeNot(Long maSuKien, String trangThaiVe);

    @org.springframework.data.jpa.repository.Query("SELECT v FROM Ve v WHERE v.daBanLai = 1 AND v.giaBanLai IS NOT NULL")
    List<Ve> findActiveResales();
    
    // Lấy lịch sử soát vé của RIÊNG nhân viên đang đăng nhập
    @org.springframework.data.jpa.repository.Query("SELECT nk.ve FROM NhatKySoatVe nk WHERE nk.ve.trangThaiVe = 'Đã check-in' " +
            "AND nk.taiKhoan.maTaiKhoan = :staffId " +
            "ORDER BY nk.thoiGianQuetMa DESC")
    List<Ve> findLichSuTheoNhanVien(@org.springframework.data.repository.query.Param("staffId") Long staffId);

    /**
     * Demo Non-Repeatable Read: Gọi Oracle function FUNC_DEMO_NRR_TICKET_PRICE.
     * Sleep 7 giây đã được FIX CỨNG bên trong function Oracle.
     * Chạy trực tiếp trong SQL Developer để quan sát, không gọi qua backend.
     */
    @org.springframework.data.jpa.repository.Query(
        value = "SELECT FUNC_DEMO_NRR_TICKET_PRICE(:p_ma_ve) FROM DUAL",
        nativeQuery = true)
    java.math.BigDecimal demoNrrTicketPrice(
        @org.springframework.data.repository.query.Param("p_ma_ve") Long maVe);

    /**
     * Lấy số vé bán theo ngày của một sự kiện (dùng cho biểu đồ).
     * Trả về Object[]: [0]=ngay (Date), [1]=soVe (Long), [2]=doanhThu (BigDecimal)
     */
    @org.springframework.data.jpa.repository.Query(
        value = "SELECT TRUNC(dm.ThoiDiemMua) AS ngay, " +
                "       COUNT(v.MaVe) AS soVe, " +
                "       NVL(SUM(hv.GiaNiemYet), 0) AS doanhThu " +
                "FROM VE v " +
                "JOIN HANG_VE hv ON v.MaHangVe = hv.MaHangVe " +
                "JOIN DON_MUA dm ON v.MaDonMua = dm.MaDonMua " +
                "WHERE hv.MaSuKien = :maSuKien " +
                "  AND v.TrangThaiVe IN ('Hiệu lực', 'Đã Check-in') " +
                "GROUP BY TRUNC(dm.ThoiDiemMua) " +
                "ORDER BY 1",
        nativeQuery = true)
    List<Object[]> countTicketsByDay(
        @org.springframework.data.repository.query.Param("maSuKien") Long maSuKien);
}
