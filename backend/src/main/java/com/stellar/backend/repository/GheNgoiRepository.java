package com.stellar.backend.repository;

import com.stellar.backend.entity.GheNgoi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GheNgoiRepository extends JpaRepository<GheNgoi, Long> {
    List<GheNgoi> findByKhuVucMaKhuVuc(Long maKhuVuc);
    boolean existsByKhuVucMaKhuVucAndToaDo(Long maKhuVuc, String toaDo);
    
    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true, flushAutomatically = true)
    @org.springframework.data.jpa.repository.Query("DELETE FROM GheNgoi g WHERE g.khuVuc.maKhuVuc = :maKhuVuc")
    void deleteByKhuVucMaKhuVuc(@org.springframework.data.repository.query.Param("maKhuVuc") Long maKhuVuc);
}
