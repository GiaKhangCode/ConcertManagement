package com.stellar.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "LICH_SU_BIEN_DONG_VI")
public class LichSuBienDongVi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaBienDong")
    private Long maBienDong;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaVi")
    private ViCaNhan viCaNhan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaGiaoDichLienQuan")
    private GiaoDich giaoDichLienQuan;

    @Column(name = "LoaiBienDong", nullable = false)
    private String loaiBienDong; // Tăng, Giảm

    @Column(name = "SoTien", nullable = false)
    private BigDecimal soTien;

    @Column(name = "NoiDung")
    private String noiDung;

    @Column(name = "ThoiGian", insertable = false, updatable = false)
    private LocalDateTime thoiGian;

    public Long getMaBienDong() { return maBienDong; }
    public void setMaBienDong(Long maBienDong) { this.maBienDong = maBienDong; }
    public ViCaNhan getViCaNhan() { return viCaNhan; }
    public void setViCaNhan(ViCaNhan viCaNhan) { this.viCaNhan = viCaNhan; }
    public GiaoDich getGiaoDichLienQuan() { return giaoDichLienQuan; }
    public void setGiaoDichLienQuan(GiaoDich giaoDichLienQuan) { this.giaoDichLienQuan = giaoDichLienQuan; }
    public String getLoaiBienDong() { return loaiBienDong; }
    public void setLoaiBienDong(String loaiBienDong) { this.loaiBienDong = loaiBienDong; }
    public BigDecimal getSoTien() { return soTien; }
    public void setSoTien(BigDecimal soTien) { this.soTien = soTien; }
    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }
    public LocalDateTime getThoiGian() { return thoiGian; }
    public void setThoiGian(LocalDateTime thoiGian) { this.thoiGian = thoiGian; }
}
