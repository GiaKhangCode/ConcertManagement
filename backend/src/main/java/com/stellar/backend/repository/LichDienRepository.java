package com.stellar.backend.repository;

import com.stellar.backend.entity.LichDien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LichDienRepository extends JpaRepository<LichDien, Long> {
    List<LichDien> findBySuKien_MaSuKien(Long maSuKien);

    void deleteBySuKien_MaSuKien(Long maSuKien);
}
