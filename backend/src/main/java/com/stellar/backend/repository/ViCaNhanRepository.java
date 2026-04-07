package com.stellar.backend.repository;

import com.stellar.backend.entity.ViCaNhan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ViCaNhanRepository extends JpaRepository<ViCaNhan, Long> {
    Optional<ViCaNhan> findByTaiKhoan_MaTaiKhoan(Long maTaiKhoan);
}
