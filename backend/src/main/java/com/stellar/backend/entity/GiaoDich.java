package com.stellar.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "GIAO_DICH")
public class GiaoDich {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaGiaoDich")
    private Long maGiaoDich;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaDonMua")
    private DonMua donMua;

    @Column(name = "MaGiaoDichCongTT")
    private String maGiaoDichCongTT;

    @Column(name = "SoTien", nullable = false)
    private BigDecimal soTien = BigDecimal.ZERO;

    @Column(name = "TrangThai", nullable = false)
    private String trangThai = "Chờ thanh toán";

    @Column(name = "MaLoi")
    private String maLoi;

    @Column(name = "DuLieuPhanHoi")
    @Lob
    private String duLieuPhanHoi;

    @Column(name = "ThoiGianTao", insertable = false, updatable = false)
    private LocalDateTime thoiGianTao;

    public Long getMaGiaoDich() { return maGiaoDich; }
    public void setMaGiaoDich(Long maGiaoDich) { this.maGiaoDich = maGiaoDich; }
    public DonMua getDonMua() { return donMua; }
    public void setDonMua(DonMua donMua) { this.donMua = donMua; }
    public String getMaGiaoDichCongTT() { return maGiaoDichCongTT; }
    public void setMaGiaoDichCongTT(String maGiaoDichCongTT) { this.maGiaoDichCongTT = maGiaoDichCongTT; }
    public BigDecimal getSoTien() { return soTien; }
    public void setSoTien(BigDecimal soTien) { this.soTien = soTien; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public String getMaLoi() { return maLoi; }
    public void setMaLoi(String maLoi) { this.maLoi = maLoi; }
    public String getDuLieuPhanHoi() { return duLieuPhanHoi; }
    public void setDuLieuPhanHoi(String duLieuPhanHoi) { this.duLieuPhanHoi = duLieuPhanHoi; }
    public LocalDateTime getThoiGianTao() { return thoiGianTao; }
    public void setThoiGianTao(LocalDateTime thoiGianTao) { this.thoiGianTao = thoiGianTao; }
}
