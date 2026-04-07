package com.stellar.backend.entity;
import jakarta.persistence.*;

@Entity
@Table(name = "DIA_DIEM")
public class DiaDiem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaDiaDiem") private Long maDiaDiem;
    
    @Column(name = "TenDiaDiem", nullable = false) private String tenDiaDiem;
    @Column(name = "SucChua", nullable = false) private Integer sucChua;
    @Column(name = "TinhThanh", nullable = false) private String tinhThanh;
    @Column(name = "PhuongXa", nullable = false) private String phuongXa;
    @Column(name = "SoNhaTenDuong", nullable = false) private String soNhaTenDuong;

    public Long getMaDiaDiem() { return maDiaDiem; }
    public void setMaDiaDiem(Long maDiaDiem) { this.maDiaDiem = maDiaDiem; }
    public String getTenDiaDiem() { return tenDiaDiem; }
    public void setTenDiaDiem(String tenDiaDiem) { this.tenDiaDiem = tenDiaDiem; }
    public Integer getSucChua() { return sucChua; }
    public void setSucChua(Integer sucChua) { this.sucChua = sucChua; }
    public String getTinhThanh() { return tinhThanh; }
    public void setTinhThanh(String tinhThanh) { this.tinhThanh = tinhThanh; }
    public String getPhuongXa() { return phuongXa; }
    public void setPhuongXa(String phuongXa) { this.phuongXa = phuongXa; }
    public String getSoNhaTenDuong() { return soNhaTenDuong; }
    public void setSoNhaTenDuong(String soNhaTenDuong) { this.soNhaTenDuong = soNhaTenDuong; }
}
