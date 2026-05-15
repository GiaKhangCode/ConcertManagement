package com.stellar.backend.repository;

import com.stellar.backend.entity.NhatKySoatVe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NhatKySoatVeRepository extends JpaRepository<NhatKySoatVe, Long> {
    @org.springframework.data.jpa.repository.Query("SELECT nk FROM NhatKySoatVe nk " +
            "JOIN FETCH nk.ve v " +
            "LEFT JOIN FETCH v.lichDien ld " +
            "LEFT JOIN FETCH ld.suKien sk " +
            "LEFT JOIN FETCH v.hangVe hv " +
            "LEFT JOIN FETCH hv.suKien hvs " +
            "LEFT JOIN FETCH v.donMua dm " +
            "LEFT JOIN FETCH dm.taiKhoan tk " +
            "LEFT JOIN FETCH tk.nguoiDung nd " +
            "LEFT JOIN FETCH v.gheNgoi gn " +
            "WHERE nk.taiKhoan.maTaiKhoan = :maTaiKhoan AND nk.trangThaiSoatVe = :trangThaiSoatVe " +
            "ORDER BY nk.thoiGianQuetMa DESC")
    List<NhatKySoatVe> findByTaiKhoan_MaTaiKhoanAndTrangThaiSoatVeOrderByThoiGianQuetMaDesc(
            @org.springframework.data.repository.query.Param("maTaiKhoan") Long maTaiKhoan, 
            @org.springframework.data.repository.query.Param("trangThaiSoatVe") Integer trangThaiSoatVe);
}
