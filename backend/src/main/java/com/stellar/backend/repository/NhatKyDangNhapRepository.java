package com.stellar.backend.repository;

import com.stellar.backend.entity.NhatKyDangNhap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NhatKyDangNhapRepository extends JpaRepository<NhatKyDangNhap, Long> {
}
