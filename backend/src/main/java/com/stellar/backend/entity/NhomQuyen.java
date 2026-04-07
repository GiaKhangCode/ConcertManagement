package com.stellar.backend.entity;
import jakarta.persistence.*;

@Entity
@Table(name = "NHOM_QUYEN")
public class NhomQuyen {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaNhomQuyen")
    private Long maNhomQuyen;

    @Column(name = "TenNhomQuyen", nullable = false)
    private String tenNhomQuyen;

    @Column(name = "MoTa")
    private String moTa;

    public Long getMaNhomQuyen() { return maNhomQuyen; }
    public void setMaNhomQuyen(Long maNhomQuyen) { this.maNhomQuyen = maNhomQuyen; }
    public String getTenNhomQuyen() { return tenNhomQuyen; }
    public void setTenNhomQuyen(String tenNhomQuyen) { this.tenNhomQuyen = tenNhomQuyen; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
}
