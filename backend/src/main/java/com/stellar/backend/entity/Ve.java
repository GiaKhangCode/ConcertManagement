package com.stellar.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "VE")
public class Ve {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaVe")
    private Long maVe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaDonMua")
    private DonMua donMua;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaHangVe")
    private HangVe hangVe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaGhe")
    private GheNgoi gheNgoi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaLichDien")
    private LichDien lichDien;

    @Column(name = "TrangThaiVe", nullable = false)
    private String trangThaiVe = "Hiệu lực";

    @Column(name = "ThoiGianDaBan", insertable = false, updatable = false)
    private LocalDateTime thoiGianDaBan;

    @Column(name = "DaBanLai", nullable = false)
    private Integer daBanLai = 0;

    @Column(name = "GiaBanLai")
    private BigDecimal giaBanLai;

    public Long getMaVe() { return maVe; }
    public void setMaVe(Long maVe) { this.maVe = maVe; }
    public BigDecimal getGiaBanLai() { return giaBanLai; }
    public void setGiaBanLai(BigDecimal giaBanLai) { this.giaBanLai = giaBanLai; }
    public DonMua getDonMua() { return donMua; }
    public void setDonMua(DonMua donMua) { this.donMua = donMua; }
    public HangVe getHangVe() { return hangVe; }
    public void setHangVe(HangVe hangVe) { this.hangVe = hangVe; }
    public GheNgoi getGheNgoi() { return gheNgoi; }
    public void setGheNgoi(GheNgoi gheNgoi) { this.gheNgoi = gheNgoi; }
    public LichDien getLichDien() { return lichDien; }
    public void setLichDien(LichDien lichDien) { this.lichDien = lichDien; }
    public String getTrangThaiVe() { return trangThaiVe; }
    public void setTrangThaiVe(String trangThaiVe) { this.trangThaiVe = trangThaiVe; }
    public LocalDateTime getThoiGianDaBan() { return thoiGianDaBan; }
    public void setThoiGianDaBan(LocalDateTime thoiGianDaBan) { this.thoiGianDaBan = thoiGianDaBan; }
    public Integer getDaBanLai() { return daBanLai; }
    public void setDaBanLai(Integer daBanLai) { this.daBanLai = daBanLai; }
}
