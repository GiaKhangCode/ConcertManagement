package com.stellar.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "TAI_KHOAN")
public class TaiKhoan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaTaiKhoan")
    private Long maTaiKhoan;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "MaNguoiDung")
    private NguoiDung nguoiDung;

    @Column(name = "TenDangNhap", nullable = false, unique = true)
    private String tenDangNhap;

    @Column(name = "MatKhauMaHoa", nullable = false)
    private String matKhauMaHoa;

    @Column(name = "TrangThai", nullable = false)
    private String trangThai = "Hoạt động";

    @Column(name = "LanDangNhapCuoi")
    private LocalDateTime lanDangNhapCuoi;

    @Column(name = "DongYNhanMarketing")
    private Integer dongYNhanMarketing = 0;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "PHAN_QUYEN_NHOM",
        joinColumns = @JoinColumn(name = "MaTaiKhoan"),
        inverseJoinColumns = @JoinColumn(name = "MaNhomQuyen")
    )
    private Set<NhomQuyen> nhomQuyens = new HashSet<>();

    // Getters and Setters
    public Long getMaTaiKhoan() { return maTaiKhoan; }
    public void setMaTaiKhoan(Long maTaiKhoan) { this.maTaiKhoan = maTaiKhoan; }
    public NguoiDung getNguoiDung() { return nguoiDung; }
    public void setNguoiDung(NguoiDung nguoiDung) { this.nguoiDung = nguoiDung; }
    public String getTenDangNhap() { return tenDangNhap; }
    public void setTenDangNhap(String tenDangNhap) { this.tenDangNhap = tenDangNhap; }
    public String getMatKhauMaHoa() { return matKhauMaHoa; }
    public void setMatKhauMaHoa(String matKhauMaHoa) { this.matKhauMaHoa = matKhauMaHoa; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public LocalDateTime getLanDangNhapCuoi() { return lanDangNhapCuoi; }
    public void setLanDangNhapCuoi(LocalDateTime lanDangNhapCuoi) { this.lanDangNhapCuoi = lanDangNhapCuoi; }
    public Integer getDongYNhanMarketing() { return dongYNhanMarketing; }
    public void setDongYNhanMarketing(Integer dongYNhanMarketing) { this.dongYNhanMarketing = dongYNhanMarketing; }
    public Set<NhomQuyen> getNhomQuyens() { return nhomQuyens; }
    public void setNhomQuyens(Set<NhomQuyen> nhomQuyens) { this.nhomQuyens = nhomQuyens; }
}
