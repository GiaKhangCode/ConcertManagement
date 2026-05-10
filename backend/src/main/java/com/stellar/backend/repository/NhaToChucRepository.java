package com.stellar.backend.repository;

import com.stellar.backend.entity.NhaToChuc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NhaToChucRepository extends JpaRepository<NhaToChuc, Long> {
    Optional<NhaToChuc> findByTaiKhoan_MaTaiKhoan(Long maTaiKhoan);
}
