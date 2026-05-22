package com.stellar.backend.repository;

import com.stellar.backend.entity.MaGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MaGiamGiaRepository extends JpaRepository<MaGiamGia, Long> {
    Optional<MaGiamGia> findByMaGiamGia(String maGiamGia);

    @org.springframework.data.jpa.repository.Query(value = "SELECT FN_Check_Ma_Giam_Gia_Hop_Le(:maGiamGia, :maSuKien) FROM DUAL", nativeQuery = true)
    Integer callFnCheckMaGiamGiaHopLe(@org.springframework.data.repository.query.Param("maGiamGia") String maGiamGia, @org.springframework.data.repository.query.Param("maSuKien") Long maSuKien);
}
