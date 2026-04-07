package com.stellar.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "NGUOI_DUNG")
public class NguoiDung {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaNguoiDung")
    private Long maNguoiDung;

    @Column(name = "HoTen", nullable = false)
    private String hoTen;

    @Column(name = "Email", nullable = false, unique = true)
    private String email;

    @Column(name = "AnhDaiDienURL")
    private String anhDaiDienUrl;

    @Column(name = "SoDienThoai", nullable = false, unique = true)
    private String soDienThoai;

    @Column(name = "DaXacThuc", nullable = false)
    private Integer daXacThuc = 0;

    @Column(name = "NgayTao", insertable = false, updatable = false)
    private LocalDateTime ngayTao;

    // Getters and Setters
    public Long getMaNguoiDung() { return maNguoiDung; }
    public void setMaNguoiDung(Long maNguoiDung) { this.maNguoiDung = maNguoiDung; }
    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAnhDaiDienUrl() { return anhDaiDienUrl; }
    public void setAnhDaiDienUrl(String anhDaiDienUrl) { this.anhDaiDienUrl = anhDaiDienUrl; }
    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
    public Integer getDaXacThuc() { return daXacThuc; }
    public void setDaXacThuc(Integer daXacThuc) { this.daXacThuc = daXacThuc; }
    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }
}
