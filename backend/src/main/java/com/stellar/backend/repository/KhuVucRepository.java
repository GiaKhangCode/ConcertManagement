package com.stellar.backend.repository;
import com.stellar.backend.entity.KhuVuc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KhuVucRepository extends JpaRepository<KhuVuc, Long> {
    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true, flushAutomatically = true)
    @org.springframework.data.jpa.repository.Query("DELETE FROM KhuVuc k WHERE k.hangVe.maHangVe = :maHangVe")
    void deleteByHangVe_MaHangVe(@org.springframework.data.repository.query.Param("maHangVe") Long maHangVe);

    java.util.List<KhuVuc> findByHangVe_MaHangVe(Long maHangVe);
    java.util.List<KhuVuc> findByHangVe_SuKien_MaSuKien(Long maSuKien);
}
