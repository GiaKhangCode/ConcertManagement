package com.stellar.backend.repository;

import com.stellar.backend.entity.NhatKySoatVe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NhatKySoatVeRepository extends JpaRepository<NhatKySoatVe, Long> {
    List<NhatKySoatVe> findByTaiKhoan_MaTaiKhoanOrderByThoiGianQuetMaDesc(Long maTaiKhoan);
}
