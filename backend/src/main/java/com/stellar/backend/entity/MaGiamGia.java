package com.stellar.backend.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

@Entity
@Table(name = "MA_GIAM_GIA")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class MaGiamGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaChienDich")
    private ChienDichKhuyenMai chienDich;

    @Column(name = "MaGiamGia", unique = true, nullable = false)
    private String maGiamGia;

    @Column(name = "LoaiGiam", nullable = false)
    private String loaiGiam = "Theo phần trăm";

    @Column(name = "SoLuongGiam", nullable = false)
    private BigDecimal soLuongGiam;

    @Column(name = "GiamToiDa", nullable = false)
    private BigDecimal giamToiDa;

    @Column(name = "LuotDungToiDa", nullable = false)
    private Integer luotDungToiDa;

    @Column(name = "SoLuotDaSuDung")
    private Integer soLuotDaSuDung = 0;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ChienDichKhuyenMai getChienDich() { return chienDich; }
    public void setChienDich(ChienDichKhuyenMai chienDich) { this.chienDich = chienDich; }

    public String getMaGiamGia() { return maGiamGia; }
    public void setMaGiamGia(String maGiamGia) { this.maGiamGia = maGiamGia; }

    public String getLoaiGiam() { return loaiGiam; }
    public void setLoaiGiam(String loaiGiam) { this.loaiGiam = loaiGiam; }

    public BigDecimal getSoLuongGiam() { return soLuongGiam; }
    public void setSoLuongGiam(BigDecimal soLuongGiam) { this.soLuongGiam = soLuongGiam; }

    public BigDecimal getGiamToiDa() { return giamToiDa; }
    public void setGiamToiDa(BigDecimal giamToiDa) { this.giamToiDa = giamToiDa; }

    public Integer getLuotDungToiDa() { return luotDungToiDa; }
    public void setLuotDungToiDa(Integer luotDungToiDa) { this.luotDungToiDa = luotDungToiDa; }

    public Integer getSoLuotDaSuDung() { return soLuotDaSuDung; }
    public void setSoLuotDaSuDung(Integer soLuotDaSuDung) { this.soLuotDaSuDung = soLuotDaSuDung; }
}
