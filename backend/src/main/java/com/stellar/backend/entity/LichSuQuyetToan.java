package com.stellar.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "LICH_SU_QUYET_TOAN")
public class LichSuQuyetToan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaQT")
    private Long maQT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaNhaToChuc", nullable = false)
    private NhaToChuc nhaToChuc;

    @Column(name = "KyQT")
    private String kyQT;

    @Column(name = "NgayQuyetToan")
    private java.time.LocalDate ngayQuyetToan;

    @Column(name = "TongDoanhThu")
    private BigDecimal tongDoanhThu;

    @Column(name = "PhiNenTang")
    private BigDecimal phiNenTang;

    @Column(name = "SoTienChuyen")
    private BigDecimal soTienChuyen;

    @Column(name = "ChungTuThanhToan")
    private String chungTuThanhToan;

    @Column(name = "TrangThai")
    private String trangThai;

    @PrePersist
    protected void onCreate() {
        if (ngayQuyetToan == null) {
            ngayQuyetToan = java.time.LocalDate.now();
        }
        if (trangThai == null) {
            trangThai = "Đang xử lý";
        }
    }

    // Getters and Setters
    public Long getMaQT() { return maQT; }
    public void setMaQT(Long maQT) { this.maQT = maQT; }

    public NhaToChuc getNhaToChuc() { return nhaToChuc; }
    public void setNhaToChuc(NhaToChuc nhaToChuc) { this.nhaToChuc = nhaToChuc; }

    public String getKyQT() { return kyQT; }
    public void setKyQT(String kyQT) { this.kyQT = kyQT; }

    public java.time.LocalDate getNgayQuyetToan() { return ngayQuyetToan; }
    public void setNgayQuyetToan(java.time.LocalDate ngayQuyetToan) { this.ngayQuyetToan = ngayQuyetToan; }

    public BigDecimal getTongDoanhThu() { return tongDoanhThu; }
    public void setTongDoanhThu(BigDecimal tongDoanhThu) { this.tongDoanhThu = tongDoanhThu; }

    public BigDecimal getPhiNenTang() { return phiNenTang; }
    public void setPhiNenTang(BigDecimal phiNenTang) { this.phiNenTang = phiNenTang; }

    public BigDecimal getSoTienChuyen() { return soTienChuyen; }
    public void setSoTienChuyen(BigDecimal soTienChuyen) { this.soTienChuyen = soTienChuyen; }

    public String getChungTuThanhToan() { return chungTuThanhToan; }
    public void setChungTuThanhToan(String chungTuThanhToan) { this.chungTuThanhToan = chungTuThanhToan; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
