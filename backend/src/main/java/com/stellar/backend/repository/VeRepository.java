package com.stellar.backend.repository;
import com.stellar.backend.entity.Ve;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VeRepository extends JpaRepository<Ve, Long> {
    List<Ve> findByDonMua_MaDonMua(Long maDonMua);
    
    long countByHangVe_SuKien_MaSuKien(Long maSuKien);
    long countByHangVe_MaHangVe(Long maHangVe);
    
    @org.springframework.data.jpa.repository.Query("SELECT v FROM Ve v WHERE v.daBanLai = 1 AND v.giaBanLai IS NOT NULL")
    List<Ve> findActiveResales();
}
