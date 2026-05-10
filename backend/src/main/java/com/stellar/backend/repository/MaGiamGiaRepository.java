package com.stellar.backend.repository;

import com.stellar.backend.entity.MaGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MaGiamGiaRepository extends JpaRepository<MaGiamGia, Long> {
    Optional<MaGiamGia> findByMaGiamGia(String maGiamGia);
}
