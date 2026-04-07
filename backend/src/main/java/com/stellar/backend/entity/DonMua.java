package com.stellar.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "DON_MUA")
public class DonMua {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaDonMua")
    private Long maDonMua;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaTaiKhoan")
    private TaiKhoan taiKhoan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaSuKien")
    private SuKien suKien;

    @Column(name = "ThoiDiemMua", insertable = false, updatable = false)
    private LocalDateTime thoiDiemMua;

    @Column(name = "TongTien", nullable = false)
    private BigDecimal tongTien;

    @Column(name = "TrangThaiThanhToan", nullable = false)
    private String trangThaiThanhToan = "Đã thanh toán"; // Simulated Payment

    @Column(name = "PhuongThucThanhToan")
    private String phuongThucThanhToan = "Stellar Pay";

    public Long getMaDonMua() { return maDonMua; }
    public void setMaDonMua(Long maDonMua) { this.maDonMua = maDonMua; }
    public TaiKhoan getTaiKhoan() { return taiKhoan; }
    public void setTaiKhoan(TaiKhoan taiKhoan) { this.taiKhoan = taiKhoan; }
    public SuKien getSuKien() { return suKien; }
    public void setSuKien(SuKien suKien) { this.suKien = suKien; }
    public LocalDateTime getThoiDiemMua() { return thoiDiemMua; }
    public void setThoiDiemMua(LocalDateTime thoiDiemMua) { this.thoiDiemMua = thoiDiemMua; }
    public BigDecimal getTongTien() { return tongTien; }
    public void setTongTien(BigDecimal tongTien) { this.tongTien = tongTien; }
    public String getTrangThaiThanhToan() { return trangThaiThanhToan; }
    public void setTrangThaiThanhToan(String trangThaiThanhToan) { this.trangThaiThanhToan = trangThaiThanhToan; }
    public String getPhuongThucThanhToan() { return phuongThucThanhToan; }
    public void setPhuongThucThanhToan(String phuongThucThanhToan) { this.phuongThucThanhToan = phuongThucThanhToan; }
}
