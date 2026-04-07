package com.stellar.backend.entity;
import jakarta.persistence.*;

@Entity
@Table(name = "KHU_VUC")
public class KhuVuc {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaKhuVuc") private Long maKhuVuc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaHangVe")
    private HangVe hangVe;

    @Column(name = "TenKhuVuc", nullable = false) private String tenKhuVuc;
    @Column(name = "SucChuaKV", nullable = false) private Integer sucChuaKv;

    public Long getMaKhuVuc() { return maKhuVuc; }
    public void setMaKhuVuc(Long maKhuVuc) { this.maKhuVuc = maKhuVuc; }
    public HangVe getHangVe() { return hangVe; }
    public void setHangVe(HangVe hangVe) { this.hangVe = hangVe; }
    public String getTenKhuVuc() { return tenKhuVuc; }
    public void setTenKhuVuc(String tenKhuVuc) { this.tenKhuVuc = tenKhuVuc; }
    public Integer getSucChuaKv() { return sucChuaKv; }
    public void setSucChuaKv(Integer sucChuaKv) { this.sucChuaKv = sucChuaKv; }
}
