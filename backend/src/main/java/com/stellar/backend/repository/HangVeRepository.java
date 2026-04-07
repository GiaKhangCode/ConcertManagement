package com.stellar.backend.repository;

import com.stellar.backend.entity.HangVe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HangVeRepository extends JpaRepository<HangVe, Long> {
    List<HangVe> findBySuKien_MaSuKien(Long maSuKien);
}
