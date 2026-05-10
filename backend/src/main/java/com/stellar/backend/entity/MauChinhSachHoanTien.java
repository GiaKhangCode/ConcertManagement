package com.stellar.backend.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@Entity
@Table(name = "MAU_CHINH_SACH_HOAN_TIEN")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class MauChinhSachHoanTien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaChinhSachHT")
    private Long maChinhSachHT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaTaiKhoan")
    private TaiKhoan taiKhoan;

    @Column(name = "TenChinhSach", nullable = false)
    private String tenChinhSach;

    @Column(name = "MoTa", columnDefinition = "CLOB")
    private String moTa;

    @OneToMany(mappedBy = "mauChinhSachHoanTien", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<QuyTacHoanTien> danhSachQuyTac;

    public Long getMaChinhSachHT() { return maChinhSachHT; }
    public void setMaChinhSachHT(Long maChinhSachHT) { this.maChinhSachHT = maChinhSachHT; }

    public TaiKhoan getTaiKhoan() { return taiKhoan; }
    public void setTaiKhoan(TaiKhoan taiKhoan) { this.taiKhoan = taiKhoan; }

    public String getTenChinhSach() { return tenChinhSach; }
    public void setTenChinhSach(String tenChinhSach) { this.tenChinhSach = tenChinhSach; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public List<QuyTacHoanTien> getDanhSachQuyTac() { return danhSachQuyTac; }
    public void setDanhSachQuyTac(List<QuyTacHoanTien> danhSachQuyTac) { this.danhSachQuyTac = danhSachQuyTac; }
}
