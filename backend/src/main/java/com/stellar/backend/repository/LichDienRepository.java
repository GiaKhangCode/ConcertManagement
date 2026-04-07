package com.stellar.backend.repository;
import com.stellar.backend.entity.LichDien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LichDienRepository extends JpaRepository<LichDien, Long> {}
