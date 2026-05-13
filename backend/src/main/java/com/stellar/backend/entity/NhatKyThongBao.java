package com.stellar.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "NHAT_KY_THONG_BAO")
public class NhatKyThongBao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaThongBao")
    private Long maThongBao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaDonMua")
    private DonMua donMua;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaTaiKhoan")
    private TaiKhoan taiKhoan;

    @Column(name = "KenhGui", nullable = false)
    private String kenhGui;

    @Lob
    @Column(name = "NoiDung")
    private String noiDung;

    @Column(name = "TrangThai")
    private String trangThai;

    @Column(name = "ThoiDiemGui", nullable = false)
    private LocalDateTime thoiDiemGui;

    @Column(name = "ThoiDiemNhan")
    private LocalDateTime thoiDiemNhan;

    @PrePersist
    protected void onCreate() {
        if (thoiDiemGui == null) {
            thoiDiemGui = LocalDateTime.now();
        }
        if (trangThai == null) {
            trangThai = "Đã gửi";
        }
    }

    public Long getMaThongBao() { return maThongBao; }
    public void setMaThongBao(Long maThongBao) { this.maThongBao = maThongBao; }

    public DonMua getDonMua() { return donMua; }
    public void setDonMua(DonMua donMua) { this.donMua = donMua; }

    public TaiKhoan getTaiKhoan() { return taiKhoan; }
    public void setTaiKhoan(TaiKhoan taiKhoan) { this.taiKhoan = taiKhoan; }

    public String getKenhGui() { return kenhGui; }
    public void setKenhGui(String kenhGui) { this.kenhGui = kenhGui; }

    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public LocalDateTime getThoiDiemGui() { return thoiDiemGui; }
    public void setThoiDiemGui(LocalDateTime thoiDiemGui) { this.thoiDiemGui = thoiDiemGui; }

    public LocalDateTime getThoiDiemNhan() { return thoiDiemNhan; }
    public void setThoiDiemNhan(LocalDateTime thoiDiemNhan) { this.thoiDiemNhan = thoiDiemNhan; }
}
