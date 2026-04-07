package com.stellar.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "VI_CA_NHAN")
public class ViCaNhan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaVi")
    private Long maVi;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaTaiKhoan")
    private TaiKhoan taiKhoan;

    @Column(name = "SoDu", nullable = false)
    private BigDecimal soDu = BigDecimal.ZERO;

    @Column(name = "MatKhauThanhToan", nullable = false)
    private String matKhauThanhToan = "123456";

    @Column(name = "TrangThai", nullable = false)
    private String trangThai = "Đang hoạt động";

    public Long getMaVi() { return maVi; }
    public void setMaVi(Long maVi) { this.maVi = maVi; }
    public TaiKhoan getTaiKhoan() { return taiKhoan; }
    public void setTaiKhoan(TaiKhoan taiKhoan) { this.taiKhoan = taiKhoan; }
    public BigDecimal getSoDu() { return soDu; }
    public void setSoDu(BigDecimal soDu) { this.soDu = soDu; }
    public String getMatKhauThanhToan() { return matKhauThanhToan; }
    public void setMatKhauThanhToan(String matKhauThanhToan) { this.matKhauThanhToan = matKhauThanhToan; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
