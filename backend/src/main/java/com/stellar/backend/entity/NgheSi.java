package com.stellar.backend.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "NGHE_SI")
public class NgheSi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaNgheSi")
    private Long maNgheSi;

    @Column(name = "TenNgheSi", nullable = false)
    private String tenNgheSi;

    @Column(name = "Email", unique = true)
    private String email;

    @Column(name = "AnhDaiDienURL")
    private String anhDaiDienURL;

    @Column(name = "SoDienThoai", unique = true)
    private String soDienThoai;

    @OneToMany(mappedBy = "ngheSi")
    private List<ThamGia> thamGiaList;

    public Long getMaNgheSi() { return maNgheSi; }
    public void setMaNgheSi(Long maNgheSi) { this.maNgheSi = maNgheSi; }

    public String getTenNgheSi() { return tenNgheSi; }
    public void setTenNgheSi(String tenNgheSi) { this.tenNgheSi = tenNgheSi; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAnhDaiDienURL() { return anhDaiDienURL; }
    public void setAnhDaiDienURL(String anhDaiDienURL) { this.anhDaiDienURL = anhDaiDienURL; }

    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }

    public List<ThamGia> getThamGiaList() { return thamGiaList; }
    public void setThamGiaList(List<ThamGia> thamGiaList) { this.thamGiaList = thamGiaList; }
}
