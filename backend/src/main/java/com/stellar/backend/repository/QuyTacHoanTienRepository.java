package com.stellar.backend.repository;

import com.stellar.backend.entity.QuyTacHoanTien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuyTacHoanTienRepository extends JpaRepository<QuyTacHoanTien, Long> {
    List<QuyTacHoanTien> findByMauChinhSachHoanTien_MaChinhSachHT(Long maChinhSachHT);
    void deleteByMauChinhSachHoanTien_MaChinhSachHT(Long maChinhSachHT);
}
