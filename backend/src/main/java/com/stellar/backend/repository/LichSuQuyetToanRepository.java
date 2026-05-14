package com.stellar.backend.repository;

import com.stellar.backend.entity.LichSuQuyetToan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LichSuQuyetToanRepository extends JpaRepository<LichSuQuyetToan, Long> {
    // Phantom Read demo thực hiện qua Oracle procedure PROC_DEMO_PHANTOM_RESALE
    // trực tiếp trong SQL Developer, không thông qua backend API.
}
