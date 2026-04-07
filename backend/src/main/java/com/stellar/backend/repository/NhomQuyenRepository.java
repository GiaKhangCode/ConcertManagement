package com.stellar.backend.repository;
import com.stellar.backend.entity.NhomQuyen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NhomQuyenRepository extends JpaRepository<NhomQuyen, Long> {
    Optional<NhomQuyen> findByTenNhomQuyen(String tenNhomQuyen);
}
