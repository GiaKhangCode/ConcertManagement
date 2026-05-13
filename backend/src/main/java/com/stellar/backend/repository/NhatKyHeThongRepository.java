package com.stellar.backend.repository;

import com.stellar.backend.entity.NhatKyHeThong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NhatKyHeThongRepository extends JpaRepository<NhatKyHeThong, Long> {
}
