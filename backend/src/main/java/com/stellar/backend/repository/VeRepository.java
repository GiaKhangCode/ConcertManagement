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
    long countByTrangThaiVeIn(java.util.List<String> statuses);
    
    // Lấy tất cả vé của một sự kiện mà chưa bị hủy (phục vụ hủy sự kiện)
    List<Ve> findByHangVe_SuKien_MaSuKienAndTrangThaiVeNot(Long maSuKien, String trangThaiVe);

    @org.springframework.data.jpa.repository.Query("SELECT v FROM Ve v WHERE v.daBanLai = 1 AND v.giaBanLai IS NOT NULL")
    List<Ve> findActiveResales();

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true, flushAutomatically = true)
    @org.springframework.data.jpa.repository.Query(value = "CALL SP_HOAN_VE_DON_MUA(:maDonMua)", nativeQuery = true)
    void callSpHoanVeDonMua(@org.springframework.data.repository.query.Param("maDonMua") Long maDonMua);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true, flushAutomatically = true)
    @org.springframework.data.jpa.repository.Query(value = "CALL SP_TAO_VE_HANG_LOAT(:maDonMua, :maTaiKhoan, :maSuKien, :maLichDien, :maHangVe, :maKhuVuc, :soLuong, :isSkipSeatCheck, :trangThaiVe, :trangThaiGhe)", nativeQuery = true)
    void callSpTaoVeHangLoat(
        @org.springframework.data.repository.query.Param("maDonMua") Long maDonMua,
        @org.springframework.data.repository.query.Param("maTaiKhoan") Long maTaiKhoan,
        @org.springframework.data.repository.query.Param("maSuKien") Long maSuKien,
        @org.springframework.data.repository.query.Param("maLichDien") Long maLichDien,
        @org.springframework.data.repository.query.Param("maHangVe") Long maHangVe,
        @org.springframework.data.repository.query.Param("maKhuVuc") Long maKhuVuc,
        @org.springframework.data.repository.query.Param("soLuong") int soLuong,
        @org.springframework.data.repository.query.Param("isSkipSeatCheck") int isSkipSeatCheck,
        @org.springframework.data.repository.query.Param("trangThaiVe") String trangThaiVe,
        @org.springframework.data.repository.query.Param("trangThaiGhe") String trangThaiGhe
    );
}
