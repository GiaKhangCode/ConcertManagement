package com.stellar.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "YEU_CAU_HO_TRO")
public class YeuCauHoTro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaYeuCau")
    private Long maYeuCau;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaDonMua")
    private DonMua donMua;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaTaiKhoan")
    private TaiKhoan taiKhoan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaNhanVienXuLy")
    private TaiKhoan nhanVienXuLy;

    @Column(name = "LoaiYeuCau", nullable = false)
    private String loaiYeuCau; // Hoàn tiền, Lỗi thanh toán, ...

    @Column(name = "NoiDung")
    @Lob
    private String noiDung;

    @Column(name = "ThoiDiemYeuCau", insertable = false, updatable = false)
    private LocalDateTime thoiDiemYeuCau;

    @Column(name = "TrangThaiXuLy", nullable = false)
    private String trangThaiXuLy = "Chờ phản hồi";

    public Long getMaYeuCau() { return maYeuCau; }
    public void setMaYeuCau(Long maYeuCau) { this.maYeuCau = maYeuCau; }
    public DonMua getDonMua() { return donMua; }
    public void setDonMua(DonMua donMua) { this.donMua = donMua; }
    public TaiKhoan getTaiKhoan() { return taiKhoan; }
    public void setTaiKhoan(TaiKhoan taiKhoan) { this.taiKhoan = taiKhoan; }
    public TaiKhoan getNhanVienXuLy() { return nhanVienXuLy; }
    public void setNhanVienXuLy(TaiKhoan nhanVienXuLy) { this.nhanVienXuLy = nhanVienXuLy; }
    public String getLoaiYeuCau() { return loaiYeuCau; }
    public void setLoaiYeuCau(String loaiYeuCau) { this.loaiYeuCau = loaiYeuCau; }
    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }
    public LocalDateTime getThoiDiemYeuCau() { return thoiDiemYeuCau; }
    public void setThoiDiemYeuCau(LocalDateTime thoiDiemYeuCau) { this.thoiDiemYeuCau = thoiDiemYeuCau; }
    public String getTrangThaiXuLy() { return trangThaiXuLy; }
    public void setTrangThaiXuLy(String trangThaiXuLy) { this.trangThaiXuLy = trangThaiXuLy; }
}
