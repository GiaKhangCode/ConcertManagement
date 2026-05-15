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
}
