package com.stellar.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "NHAT_KY_SOAT_VE")
public class NhatKySoatVe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaSoatVe")
    private Long maSoatVe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaTaiKhoan")
    private TaiKhoan taiKhoan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaVe")
    private Ve ve;

    @Column(name = "TrangThaiSoatVe", nullable = false)
    private Integer trangThaiSoatVe; // 1: Thành công, 0: Thất bại

    @Column(name = "ThoiGianQuetMa", nullable = false)
    private LocalDateTime thoiGianQuetMa = LocalDateTime.now();

    @Column(name = "ThietBiQuet", nullable = false)
    private String thietBiQuet;

    public Long getMaSoatVe() { return maSoatVe; }
    public void setMaSoatVe(Long maSoatVe) { this.maSoatVe = maSoatVe; }
    public TaiKhoan getTaiKhoan() { return taiKhoan; }
    public void setTaiKhoan(TaiKhoan taiKhoan) { this.taiKhoan = taiKhoan; }
    public Ve getVe() { return ve; }
    public void setVe(Ve ve) { this.ve = ve; }
    public Integer getTrangThaiSoatVe() { return trangThaiSoatVe; }
    public void setTrangThaiSoatVe(Integer trangThaiSoatVe) { this.trangThaiSoatVe = trangThaiSoatVe; }
    public LocalDateTime getThoiGianQuetMa() { return thoiGianQuetMa; }
    public void setThoiGianQuetMa(LocalDateTime thoiGianQuetMa) { this.thoiGianQuetMa = thoiGianQuetMa; }
    public String getThietBiQuet() { return thietBiQuet; }
    public void setThietBiQuet(String thietBiQuet) { this.thietBiQuet = thietBiQuet; }
}
