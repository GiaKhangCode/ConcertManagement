package com.stellar.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "TRANG_THAI_GHE_THEO_SUAT")
@IdClass(TrangThaiGheTheoSuatId.class)
public class TrangThaiGheTheoSuat {

    @Id
    @Column(name = "MaGhe")
    private Long maGhe;

    @Id
    @Column(name = "MaLichDien")
    private Long maLichDien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaGhe", insertable = false, updatable = false)
    private GheNgoi gheNgoi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaLichDien", insertable = false, updatable = false)
    private LichDien lichDien;

    @Column(name = "TrangThai", nullable = false)
    private String trangThai = "Còn trống"; // Còn trống, Đã đặt, Đang giữ chỗ

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaTaiKhoan")
    private TaiKhoan taiKhoan;

    @Column(name = "ThoiGianHetHan")
    private LocalDateTime thoiGianHetHan;

    public Long getMaGhe() { return maGhe; }
    public void setMaGhe(Long maGhe) { this.maGhe = maGhe; }

    public Long getMaLichDien() { return maLichDien; }
    public void setMaLichDien(Long maLichDien) { this.maLichDien = maLichDien; }

    public GheNgoi getGheNgoi() { return gheNgoi; }
    public void setGheNgoi(GheNgoi gheNgoi) { this.gheNgoi = gheNgoi; }

    public LichDien getLichDien() { return lichDien; }
    public void setLichDien(LichDien lichDien) { this.lichDien = lichDien; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public TaiKhoan getTaiKhoan() { return taiKhoan; }
    public void setTaiKhoan(TaiKhoan taiKhoan) { this.taiKhoan = taiKhoan; }

    public LocalDateTime getThoiGianHetHan() { return thoiGianHetHan; }
    public void setThoiGianHetHan(LocalDateTime thoiGianHetHan) { this.thoiGianHetHan = thoiGianHetHan; }
}
