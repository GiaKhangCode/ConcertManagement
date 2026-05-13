package com.stellar.backend.repository;

import com.stellar.backend.entity.NgheSi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NgheSiRepository extends JpaRepository<NgheSi, Long> {
    Optional<NgheSi> findByTenNgheSi(String tenNgheSi);
}
