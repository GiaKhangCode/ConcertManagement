package com.stellar.backend.repository;

import com.stellar.backend.entity.LichSuBienDongVi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LichSuBienDongViRepository extends JpaRepository<LichSuBienDongVi, Long> {
    List<LichSuBienDongVi> findByViCaNhan_MaViOrderByThoiGianDesc(Long maVi);
}
