package com.stellar.backend.entity;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "SU_KIEN")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SuKien {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaSuKien")
    private Long maSuKien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaDiaDiem")
    private DiaDiem diaDiem;

    @Column(name = "TenSuKien", nullable = false)
    private String tenSuKien;

    @Column(name = "ThoiGianBD", nullable = false)
    private LocalDateTime thoiGianBD;

    @Column(name = "ThoiGianKT", nullable = false)
    private LocalDateTime thoiGianKT;

    @Column(name = "ThoiGianMoBanVe", nullable = false)
    private LocalDateTime thoiGianMoBanVe;

    @Column(name = "ThoiGianNgungBanVe", nullable = false)
    private LocalDateTime thoiGianNgungBanVe;

    @Column(name = "TrangThai", nullable = false)
    private String trangThai;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaNguoiTao")
    private TaiKhoan nguoiTao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaChinhSachHT")
    private MauChinhSachHoanTien mauChinhSachHoanTien;


    @JsonIgnore
    @OneToMany(mappedBy = "suKien", fetch = FetchType.LAZY)
    private List<HangVe> danhSachHangVe;

    @JsonIgnore
    @OneToMany(mappedBy = "suKien", fetch = FetchType.LAZY)
    private List<LichDien> danhSachLichDien;

    @Column(name = "AnhBiaURL")
    private String anhBiaUrl; // Sẽ dùng làm ảnh Poster (Thẻ 3D)

    @Column(name = "AnhThumbnailURL")
    private String anhThumbnailUrl; // Sẽ dùng làm ảnh Card hiển thị

    @Column(name = "PhanLoai")
    private String phanLoai;

    @Column(name = "MoTa", columnDefinition = "CLOB")
    private String moTa;

    @Column(name = "LaSuKienNoiBat")
    private Integer laSuKienNoiBat = 0;

    @Column(name = "LyDoTuChoi", length = 1000)
    private String lyDoTuChoi;

    @JsonIgnore
    @OneToMany(mappedBy = "suKien", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaiTro> danhSachTaiTro;

    // Getters and Setters
    public List<TaiTro> getDanhSachTaiTro() { return danhSachTaiTro; }
    public void setDanhSachTaiTro(List<TaiTro> danhSachTaiTro) { this.danhSachTaiTro = danhSachTaiTro; }
    public Long getMaSuKien() { return maSuKien; }
    public void setMaSuKien(Long maSuKien) { this.maSuKien = maSuKien; }

    public DiaDiem getDiaDiem() { return diaDiem; }
    public void setDiaDiem(DiaDiem diaDiem) { this.diaDiem = diaDiem; }

    public String getTenSuKien() { return tenSuKien; }
    public void setTenSuKien(String tenSuKien) { this.tenSuKien = tenSuKien; }

    public LocalDateTime getThoiGianBD() { return thoiGianBD; }
    public void setThoiGianBD(LocalDateTime thoiGianBD) { this.thoiGianBD = thoiGianBD; }

    public LocalDateTime getThoiGianKT() { return thoiGianKT; }
    public void setThoiGianKT(LocalDateTime thoiGianKT) { this.thoiGianKT = thoiGianKT; }

    public LocalDateTime getThoiGianMoBanVe() { return thoiGianMoBanVe; }
    public void setThoiGianMoBanVe(LocalDateTime thoiGianMoBanVe) { this.thoiGianMoBanVe = thoiGianMoBanVe; }

    public LocalDateTime getThoiGianNgungBanVe() { return thoiGianNgungBanVe; }
    public void setThoiGianNgungBanVe(LocalDateTime thoiGianNgungBanVe) { this.thoiGianNgungBanVe = thoiGianNgungBanVe; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public TaiKhoan getNguoiTao() { return nguoiTao; }
    public void setNguoiTao(TaiKhoan nguoiTao) { this.nguoiTao = nguoiTao; }

    public MauChinhSachHoanTien getMauChinhSachHoanTien() { return mauChinhSachHoanTien; }
    public void setMauChinhSachHoanTien(MauChinhSachHoanTien mauChinhSachHoanTien) { this.mauChinhSachHoanTien = mauChinhSachHoanTien; }

    public List<HangVe> getDanhSachHangVe() { return danhSachHangVe; }
    public void setDanhSachHangVe(List<HangVe> danhSachHangVe) { this.danhSachHangVe = danhSachHangVe; }

    public String getAnhBiaUrl() { return anhBiaUrl; }
    public void setAnhBiaUrl(String anhBiaUrl) { this.anhBiaUrl = anhBiaUrl; }

    public String getAnhThumbnailUrl() { return anhThumbnailUrl; }
    public void setAnhThumbnailUrl(String anhThumbnailUrl) { this.anhThumbnailUrl = anhThumbnailUrl; }

    public String getPhanLoai() { return phanLoai; }
    public void setPhanLoai(String phanLoai) { this.phanLoai = phanLoai; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public Integer getLaSuKienNoiBat() { return laSuKienNoiBat; }
    public void setLaSuKienNoiBat(Integer laSuKienNoiBat) { this.laSuKienNoiBat = laSuKienNoiBat; }

    public String getLyDoTuChoi() { return lyDoTuChoi; }
    public void setLyDoTuChoi(String lyDoTuChoi) { this.lyDoTuChoi = lyDoTuChoi; }

    public List<LichDien> getDanhSachLichDien() { return danhSachLichDien; }
    public void setDanhSachLichDien(List<LichDien> danhSachLichDien) { this.danhSachLichDien = danhSachLichDien; }

}
