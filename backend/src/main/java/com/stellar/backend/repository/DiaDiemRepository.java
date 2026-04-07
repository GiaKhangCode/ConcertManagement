package com.stellar.backend.repository;
import com.stellar.backend.entity.DiaDiem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiaDiemRepository extends JpaRepository<DiaDiem, Long> {}
