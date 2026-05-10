package com.stellar.backend.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "CHIEN_DICH_KHUYEN_MAI")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ChienDichKhuyenMai {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaChienDich")
    private Long maChienDich;

    @Column(name = "TenChienDich", nullable = false)
    private String tenChienDich;

    @Column(name = "ThoiDiemBD", nullable = false)
    private LocalDateTime thoiDiemBD;

    @Column(name = "ThoiDiemKT", nullable = false)
    private LocalDateTime thoiDiemKT;

    @Column(name = "TrangThai", nullable = false)
    private String trangThai = "Chưa diễn ra";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaSuKien")
    private SuKien suKien;

    @OneToMany(mappedBy = "chienDich", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MaGiamGia> danhSachMaGiamGia;

    // Getters and Setters
    public Long getMaChienDich() { return maChienDich; }
    public void setMaChienDich(Long maChienDich) { this.maChienDich = maChienDich; }

    public String getTenChienDich() { return tenChienDich; }
    public void setTenChienDich(String tenChienDich) { this.tenChienDich = tenChienDich; }

    public LocalDateTime getThoiDiemBD() { return thoiDiemBD; }
    public void setThoiDiemBD(LocalDateTime thoiDiemBD) { this.thoiDiemBD = thoiDiemBD; }

    public LocalDateTime getThoiDiemKT() { return thoiDiemKT; }
    public void setThoiDiemKT(LocalDateTime thoiDiemKT) { this.thoiDiemKT = thoiDiemKT; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public SuKien getSuKien() { return suKien; }
    public void setSuKien(SuKien suKien) { this.suKien = suKien; }

    public List<MaGiamGia> getDanhSachMaGiamGia() { return danhSachMaGiamGia; }
    public void setDanhSachMaGiamGia(List<MaGiamGia> danhSachMaGiamGia) { this.danhSachMaGiamGia = danhSachMaGiamGia; }
}
