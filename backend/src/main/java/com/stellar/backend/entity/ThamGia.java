package com.stellar.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "THAM_GIA")
public class ThamGia {
    @EmbeddedId
    private ThamGiaId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("maNgheSi")
    @JoinColumn(name = "MaNgheSi")
    private NgheSi ngheSi;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("maSuKien")
    @JoinColumn(name = "MaSuKien")
    private SuKien suKien;

    @Column(name = "VaiTroChinh")
    private String vaiTroChinh;

    public ThamGiaId getId() { return id; }
    public void setId(ThamGiaId id) { this.id = id; }

    public NgheSi getNgheSi() { return ngheSi; }
    public void setNgheSi(NgheSi ngheSi) { this.ngheSi = ngheSi; }

    public SuKien getSuKien() { return suKien; }
    public void setSuKien(SuKien suKien) { this.suKien = suKien; }

    public String getVaiTroChinh() { return vaiTroChinh; }
    public void setVaiTroChinh(String vaiTroChinh) { this.vaiTroChinh = vaiTroChinh; }
}
