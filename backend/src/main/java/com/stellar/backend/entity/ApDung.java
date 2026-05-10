package com.stellar.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "AP_DUNG")
@IdClass(ApDungId.class)
public class ApDung {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaDonMua")
    private DonMua donMua;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDGiamGia")
    private MaGiamGia maGiamGia;

    @Column(name = "ThoiDiemApDung", nullable = false)
    private LocalDateTime thoiDiemApDung = LocalDateTime.now();

    @Column(name = "SoTienGiamThucTe", nullable = false)
    private BigDecimal soTienGiamThucTe;

    // Getters and Setters
    public DonMua getDonMua() { return donMua; }
    public void setDonMua(DonMua donMua) { this.donMua = donMua; }

    public MaGiamGia getMaGiamGia() { return maGiamGia; }
    public void setMaGiamGia(MaGiamGia maGiamGia) { this.maGiamGia = maGiamGia; }

    public LocalDateTime getThoiDiemApDung() { return thoiDiemApDung; }
    public void setThoiDiemApDung(LocalDateTime thoiDiemApDung) { this.thoiDiemApDung = thoiDiemApDung; }

    public BigDecimal getSoTienGiamThucTe() { return soTienGiamThucTe; }
    public void setSoTienGiamThucTe(BigDecimal soTienGiamThucTe) { this.soTienGiamThucTe = soTienGiamThucTe; }
}
