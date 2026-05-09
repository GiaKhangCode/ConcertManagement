package com.stellar.backend.repository;

import com.stellar.backend.entity.SoDoSuKien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SoDoSuKienRepository extends JpaRepository<SoDoSuKien, Long> {
    Optional<SoDoSuKien> findByMaSuKien(Long maSuKien);
}
