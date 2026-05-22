package com.stellar.backend.repository;

import com.stellar.backend.entity.HangVe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HangVeRepository extends JpaRepository<HangVe, Long> {
    List<HangVe> findBySuKien_MaSuKien(Long maSuKien);
    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true, flushAutomatically = true)
    @org.springframework.data.jpa.repository.Query("DELETE FROM HangVe h WHERE h.suKien.maSuKien = :maSuKien")
    void deleteBySuKien_MaSuKien(@org.springframework.data.repository.query.Param("maSuKien") Long maSuKien);

    @org.springframework.data.jpa.repository.Query(value = "SELECT FN_LaySoVeConLai(:maHangVe, :maKhuVuc) FROM DUAL", nativeQuery = true)
    Long callFnLaySoVeConLai(@org.springframework.data.repository.query.Param("maHangVe") Long maHangVe, @org.springframework.data.repository.query.Param("maKhuVuc") Long maKhuVuc);
}
