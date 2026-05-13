package com.stellar.backend.repository;

import com.stellar.backend.entity.NhatKyThongBao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NhatKyThongBaoRepository extends JpaRepository<NhatKyThongBao, Long> {
}
