package com.stellar.backend.repository;

import com.stellar.backend.entity.LichDien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LichDienRepository extends JpaRepository<LichDien, Long> {
    List<LichDien> findBySuKien_MaSuKien(Long maSuKien);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true, flushAutomatically = true)
    @org.springframework.data.jpa.repository.Query("DELETE FROM LichDien l WHERE l.suKien.maSuKien = :maSuKien")
    void deleteBySuKien_MaSuKien(@org.springframework.data.repository.query.Param("maSuKien") Long maSuKien);
}
