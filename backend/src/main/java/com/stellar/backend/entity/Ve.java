package com.stellar.backend.entity;

import jakarta.persistence.*;
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

    @Column(name = "TrangThaiVe", nullable = false)
    private String trangThaiVe = "Hiệu lực";

    @Column(name = "ThoiGianDaBan", insertable = false, updatable = false)
    private LocalDateTime thoiGianDaBan;

    @Column(name = "DaBanLai")
    private Integer daBanLai = 0;

    public Long getMaVe() { return maVe; }
    public void setMaVe(Long maVe) { this.maVe = maVe; }
    public DonMua getDonMua() { return donMua; }
    public void setDonMua(DonMua donMua) { this.donMua = donMua; }
    public HangVe getHangVe() { return hangVe; }
    public void setHangVe(HangVe hangVe) { this.hangVe = hangVe; }
    public String getTrangThaiVe() { return trangThaiVe; }
    public void setTrangThaiVe(String trangThaiVe) { this.trangThaiVe = trangThaiVe; }
    public LocalDateTime getThoiGianDaBan() { return thoiGianDaBan; }
    public void setThoiGianDaBan(LocalDateTime thoiGianDaBan) { this.thoiGianDaBan = thoiGianDaBan; }
    public Integer getDaBanLai() { return daBanLai; }
    public void setDaBanLai(Integer daBanLai) { this.daBanLai = daBanLai; }
}
