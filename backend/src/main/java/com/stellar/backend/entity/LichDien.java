package com.stellar.backend.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "LICH_DIEN")
public class LichDien {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaLichDien") private Long maLichDien;
    
    @Column(name = "TenLichDien", nullable = false) private String tenLichDien;
    @Column(name = "TrangThaiLichDien", nullable = false) private String trangThaiLichDien = "Chưa diễn ra";
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaSuKien")
    private SuKien suKien;

    @Column(name = "ThoiGianBatDau", nullable = false) private LocalDateTime thoiGianBatDau;
    @Column(name = "ThoiGianKetThuc", nullable = false) private LocalDateTime thoiGianKetThuc;
    @Column(name = "TrangThaiBanVe", nullable = false) private String trangThaiBanVe = "Còn vé";

    public Long getMaLichDien() { return maLichDien; }
    public void setMaLichDien(Long maLichDien) { this.maLichDien = maLichDien; }
    public String getTenLichDien() { return tenLichDien; }
    public void setTenLichDien(String tenLichDien) { this.tenLichDien = tenLichDien; }
    public String getTrangThaiLichDien() { return trangThaiLichDien; }
    public void setTrangThaiLichDien(String trangThaiLichDien) { this.trangThaiLichDien = trangThaiLichDien; }
    public SuKien getSuKien() { return suKien; }
    public void setSuKien(SuKien suKien) { this.suKien = suKien; }
    public LocalDateTime getThoiGianBatDau() { return thoiGianBatDau; }
    public void setThoiGianBatDau(LocalDateTime thoiGianBatDau) { this.thoiGianBatDau = thoiGianBatDau; }
    public LocalDateTime getThoiGianKetThuc() { return thoiGianKetThuc; }
    public void setThoiGianKetThuc(LocalDateTime thoiGianKetThuc) { this.thoiGianKetThuc = thoiGianKetThuc; }
    public String getTrangThaiBanVe() { return trangThaiBanVe; }
    public void setTrangThaiBanVe(String trangThaiBanVe) { this.trangThaiBanVe = trangThaiBanVe; }
}
