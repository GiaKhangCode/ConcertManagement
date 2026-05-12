package com.stellar.backend.repository;

import com.stellar.backend.entity.NhatKySoatVe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NhatKySoatVeRepository extends JpaRepository<NhatKySoatVe, Long> {
}
