package com.stellar.backend.repository;

import com.stellar.backend.entity.NguoiDung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NguoiDungRepository extends JpaRepository<NguoiDung, Long> {
    boolean existsByEmail(String email);
    boolean existsBySoDienThoai(String soDienThoai);
}
