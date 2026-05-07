package com.stellar.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "GHE_NGOI")
public class GheNgoi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaGhe")
    private Long maGhe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaKhuVuc", nullable = false)
    private KhuVuc khuVuc;

    @Column(name = "ToaDo", nullable = false, length = 100)
    private String toaDo; // VD: A1, B10

    public Long getMaGhe() { return maGhe; }
    public void setMaGhe(Long maGhe) { this.maGhe = maGhe; }

    public KhuVuc getKhuVuc() { return khuVuc; }
    public void setKhuVuc(KhuVuc khuVuc) { this.khuVuc = khuVuc; }

    public String getToaDo() { return toaDo; }
    public void setToaDo(String toaDo) { this.toaDo = toaDo; }
}
