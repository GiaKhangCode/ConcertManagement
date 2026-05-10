package com.stellar.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "NHA_TO_CHUC")
public class NhaToChuc {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaNhaToChuc")
    private Long maNhaToChuc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaNguoiDaiDien")
    private NguoiDung nguoiDaiDien;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaTaiKhoan")
    private TaiKhoan taiKhoan;

    @Column(name = "TenNhaToChuc", nullable = false)
    private String tenNhaToChuc;

    @Column(name = "MaSoThue", nullable = false, unique = true)
    private String maSoThue;

    @Column(name = "ThongTinNganHang", nullable = false)
    private String thongTinNganHang;

    @Column(name = "EmailHoTro")
    private String emailHoTro;

    // Getters and Setters
    public Long getMaNhaToChuc() { return maNhaToChuc; }
    public void setMaNhaToChuc(Long maNhaToChuc) { this.maNhaToChuc = maNhaToChuc; }

    public NguoiDung getNguoiDaiDien() { return nguoiDaiDien; }
    public void setNguoiDaiDien(NguoiDung nguoiDaiDien) { this.nguoiDaiDien = nguoiDaiDien; }

    public TaiKhoan getTaiKhoan() { return taiKhoan; }
    public void setTaiKhoan(TaiKhoan taiKhoan) { this.taiKhoan = taiKhoan; }

    public String getTenNhaToChuc() { return tenNhaToChuc; }
    public void setTenNhaToChuc(String tenNhaToChuc) { this.tenNhaToChuc = tenNhaToChuc; }

    public String getMaSoThue() { return maSoThue; }
    public void setMaSoThue(String maSoThue) { this.maSoThue = maSoThue; }

    public String getThongTinNganHang() { return thongTinNganHang; }
    public void setThongTinNganHang(String thongTinNganHang) { this.thongTinNganHang = thongTinNganHang; }

    public String getEmailHoTro() { return emailHoTro; }
    public void setEmailHoTro(String emailHoTro) { this.emailHoTro = emailHoTro; }
}
