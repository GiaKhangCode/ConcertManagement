package com.stellar.backend.repository;

import com.stellar.backend.entity.NhaTaiTro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface NhaTaiTroRepository extends JpaRepository<NhaTaiTro, Long> {
    Optional<NhaTaiTro> findByTenNhaTT(String tenNhaTT);
}
