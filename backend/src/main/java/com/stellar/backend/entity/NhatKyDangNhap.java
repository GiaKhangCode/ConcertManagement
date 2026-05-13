package com.stellar.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "NHAT_KY_DANG_NHAP")
public class NhatKyDangNhap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaLog")
    private Long maLog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaTaiKhoan")
    private TaiKhoan taiKhoan;

    @Column(name = "TenDangNhapNhapVao")
    private String tenDangNhapNhapVao;

    @Column(name = "ThoiGian", insertable = false, updatable = false)
    private LocalDateTime thoiGian;

    @Column(name = "TrangThai")
    private String trangThai;

    @Column(name = "LyDoThatBai")
    private String lyDoThatBai;

    @Column(name = "DiaChiIP")
    private String diaChiIP;

    @Column(name = "UserAgent")
    private String userAgent;

    public Long getMaLog() { return maLog; }
    public void setMaLog(Long maLog) { this.maLog = maLog; }

    public TaiKhoan getTaiKhoan() { return taiKhoan; }
    public void setTaiKhoan(TaiKhoan taiKhoan) { this.taiKhoan = taiKhoan; }

    public String getTenDangNhapNhapVao() { return tenDangNhapNhapVao; }
    public void setTenDangNhapNhapVao(String tenDangNhapNhapVao) { this.tenDangNhapNhapVao = tenDangNhapNhapVao; }

    public LocalDateTime getThoiGian() { return thoiGian; }
    public void setThoiGian(LocalDateTime thoiGian) { this.thoiGian = thoiGian; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getLyDoThatBai() { return lyDoThatBai; }
    public void setLyDoThatBai(String lyDoThatBai) { this.lyDoThatBai = lyDoThatBai; }

    public String getDiaChiIP() { return diaChiIP; }
    public void setDiaChiIP(String diaChiIP) { this.diaChiIP = diaChiIP; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
}
