package com.stellar.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "LICH_SU_HOAN_TIEN")
public class LichSuHoanTien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaHoanTien")
    private Long maHoanTien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaVe", nullable = false)
    private Ve ve;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaSuKien", nullable = false)
    private SuKien suKien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaTaiKhoan", nullable = false)
    private TaiKhoan taiKhoan;

    @Column(name = "SoTienHoan", nullable = false)
    private BigDecimal soTienHoan;

    @Column(name = "LyDoHoan", length = 500)
    private String lyDoHoan;

    /**
     * Loại hoàn tiền: "Yêu cầu người dùng" hoặc "Hủy sự kiện"
     */
    @Column(name = "LoaiHoan", length = 100)
    private String loaiHoan = "Yêu cầu người dùng";

    @Column(name = "ThoiDiemHoan", insertable = false, updatable = false)
    private LocalDateTime thoiDiemHoan;

    // Getters & Setters
    public Long getMaHoanTien() { return maHoanTien; }
    public void setMaHoanTien(Long maHoanTien) { this.maHoanTien = maHoanTien; }

    public Ve getVe() { return ve; }
    public void setVe(Ve ve) { this.ve = ve; }

    public SuKien getSuKien() { return suKien; }
    public void setSuKien(SuKien suKien) { this.suKien = suKien; }

    public TaiKhoan getTaiKhoan() { return taiKhoan; }
    public void setTaiKhoan(TaiKhoan taiKhoan) { this.taiKhoan = taiKhoan; }

    public BigDecimal getSoTienHoan() { return soTienHoan; }
    public void setSoTienHoan(BigDecimal soTienHoan) { this.soTienHoan = soTienHoan; }

    public String getLyDoHoan() { return lyDoHoan; }
    public void setLyDoHoan(String lyDoHoan) { this.lyDoHoan = lyDoHoan; }

    public String getLoaiHoan() { return loaiHoan; }
    public void setLoaiHoan(String loaiHoan) { this.loaiHoan = loaiHoan; }

    public LocalDateTime getThoiDiemHoan() { return thoiDiemHoan; }
    public void setThoiDiemHoan(LocalDateTime thoiDiemHoan) { this.thoiDiemHoan = thoiDiemHoan; }
}
