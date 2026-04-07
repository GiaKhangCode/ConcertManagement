package com.stellar.backend.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "HANG_VE")
public class HangVe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaHangVe")
    private Long maHangVe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaSuKien")
    private SuKien suKien;

    @Column(name = "TenHangVe", nullable = false)
    private String tenHangVe;

    @Column(name = "GiaNiemYet", nullable = false)
    private BigDecimal giaNiemYet;

    @Column(name = "TongSoLuong")
    private Integer tongSoLuong;

    // Getters and Setters
    public Long getMaHangVe() { return maHangVe; }
    public void setMaHangVe(Long maHangVe) { this.maHangVe = maHangVe; }

    public SuKien getSuKien() { return suKien; }
    public void setSuKien(SuKien suKien) { this.suKien = suKien; }

    public String getTenHangVe() { return tenHangVe; }
    public void setTenHangVe(String tenHangVe) { this.tenHangVe = tenHangVe; }

    public BigDecimal getGiaNiemYet() { return giaNiemYet; }
    public void setGiaNiemYet(BigDecimal giaNiemYet) { this.giaNiemYet = giaNiemYet; }

    public Integer getTongSoLuong() { return tongSoLuong; }
    public void setTongSoLuong(Integer tongSoLuong) { this.tongSoLuong = tongSoLuong; }
}
