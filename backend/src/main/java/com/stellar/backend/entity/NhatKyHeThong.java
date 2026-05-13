package com.stellar.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "NHAT_KY_HE_THONG")
public class NhatKyHeThong {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaNhatKyHT")
    private Long maNhatKyHT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaTaiKhoan")
    private TaiKhoan taiKhoan;

    @Column(name = "HanhDong")
    private String hanhDong;

    @Column(name = "TenBang")
    private String tenBang;

    @Column(name = "MaDoiTuong")
    private Long maDoiTuong;

    @Lob
    @Column(name = "DuLieuCu")
    private String duLieuCu;

    @Lob
    @Column(name = "DuLieuMoi")
    private String duLieuMoi;

    @Column(name = "LyDo")
    private String lyDo;

    @Column(name = "ThoiDiemThucHien", insertable = false, updatable = false)
    private LocalDateTime thoiDiemThucHien;

    public Long getMaNhatKyHT() { return maNhatKyHT; }
    public void setMaNhatKyHT(Long maNhatKyHT) { this.maNhatKyHT = maNhatKyHT; }

    public TaiKhoan getTaiKhoan() { return taiKhoan; }
    public void setTaiKhoan(TaiKhoan taiKhoan) { this.taiKhoan = taiKhoan; }

    public String getHanhDong() { return hanhDong; }
    public void setHanhDong(String hanhDong) { this.hanhDong = hanhDong; }

    public String getTenBang() { return tenBang; }
    public void setTenBang(String tenBang) { this.tenBang = tenBang; }

    public Long getMaDoiTuong() { return maDoiTuong; }
    public void setMaDoiTuong(Long maDoiTuong) { this.maDoiTuong = maDoiTuong; }

    public String getDuLieuCu() { return duLieuCu; }
    public void setDuLieuCu(String duLieuCu) { this.duLieuCu = duLieuCu; }

    public String getDuLieuMoi() { return duLieuMoi; }
    public void setDuLieuMoi(String duLieuMoi) { this.duLieuMoi = duLieuMoi; }

    public String getLyDo() { return lyDo; }
    public void setLyDo(String lyDo) { this.lyDo = lyDo; }

    public LocalDateTime getThoiDiemThucHien() { return thoiDiemThucHien; }
    public void setThoiDiemThucHien(LocalDateTime thoiDiemThucHien) { this.thoiDiemThucHien = thoiDiemThucHien; }
}
