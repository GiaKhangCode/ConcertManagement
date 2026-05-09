package com.stellar.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "KHU_VUC_SO_DO")
public class KhuVucSoDo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaKhuVucSoDo")
    private Long maKhuVucSoDo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaSoDo")
    private SoDoSuKien soDoSuKien;

    @Column(name = "MaKhuVuc")
    private Long maKhuVuc;

    @Column(name = "TenHienThi")
    private String tenHienThi;

    @Column(name = "LoaiHinhDang")
    private String loaiHinhDang;

    @Column(name = "MauSac")
    private String mauSac;

    @Lob
    @Column(name = "ThuocTinhJson")
    private String thuocTinhJson;

    @Column(name = "KichThuocFont")
    private Integer kichThuocFont;
}
