package com.stellar.backend.repository;

import com.stellar.backend.entity.TrangThaiGheTheoSuat;
import com.stellar.backend.entity.TrangThaiGheTheoSuatId;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TrangThaiGheTheoSuatRepository extends JpaRepository<TrangThaiGheTheoSuat, TrangThaiGheTheoSuatId> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "0")})
    Optional<TrangThaiGheTheoSuat> findByMaGheAndMaLichDien(Long maGhe, Long maLichDien);
    List<TrangThaiGheTheoSuat> findByMaLichDien(Long maLichDien);
    List<TrangThaiGheTheoSuat> findByTrangThaiAndThoiGianHetHanBefore(String trangThai, LocalDateTime time);
    List<TrangThaiGheTheoSuat> findByMaLichDienAndTaiKhoanMaTaiKhoanAndTrangThai(Long maLichDien, Long maTaiKhoan, String trangThai);
    
    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true, flushAutomatically = true)
    @org.springframework.data.jpa.repository.Query("DELETE FROM TrangThaiGheTheoSuat t WHERE t.maLichDien = :maLichDien")
    void deleteByMaLichDien(@org.springframework.data.repository.query.Param("maLichDien") Long maLichDien);
}
