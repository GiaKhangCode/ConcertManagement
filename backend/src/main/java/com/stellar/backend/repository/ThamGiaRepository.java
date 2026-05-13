package com.stellar.backend.repository;

import com.stellar.backend.entity.ThamGia;
import com.stellar.backend.entity.ThamGiaId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThamGiaRepository extends JpaRepository<ThamGia, ThamGiaId> {
    List<ThamGia> findBySuKien_MaSuKien(Long maSuKien);
    void deleteBySuKien_MaSuKien(Long maSuKien);
}
