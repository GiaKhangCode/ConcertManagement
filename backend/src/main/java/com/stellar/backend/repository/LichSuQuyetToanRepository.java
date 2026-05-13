package com.stellar.backend.repository;

import com.stellar.backend.entity.LichSuQuyetToan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LichSuQuyetToanRepository extends JpaRepository<LichSuQuyetToan, Long> {
}
