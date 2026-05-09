package com.stellar.backend.repository;

import com.stellar.backend.entity.MauChinhSachHoanTien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MauChinhSachHoanTienRepository extends JpaRepository<MauChinhSachHoanTien, Long> {
    List<MauChinhSachHoanTien> findByTaiKhoan_MaTaiKhoan(Long maTaiKhoan);
}
