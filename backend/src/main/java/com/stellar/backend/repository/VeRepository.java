package com.stellar.backend.repository;
import com.stellar.backend.entity.Ve;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VeRepository extends JpaRepository<Ve, Long> {
    List<Ve> findByDonMua_MaDonMua(Long maDonMua);
}
