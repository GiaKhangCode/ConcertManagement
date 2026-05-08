package com.stellar.backend.repository;

import com.stellar.backend.entity.YeuCauHoTro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface YeuCauHoTroRepository extends JpaRepository<YeuCauHoTro, Long> {
    List<YeuCauHoTro> findByLoaiYeuCauAndTrangThaiXuLy(String loaiYeuCau, String trangThaiXuLy);
    List<YeuCauHoTro> findByTaiKhoan_MaTaiKhoan(Long maTaiKhoan);
}
