package com.stellar.backend.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;

@Entity
@Table(name = "QUY_TAC_HOAN_TIEN")
public class QuyTacHoanTien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaQuyTac")
    private Long maQuyTac;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaChinhSachHT")
    private MauChinhSachHoanTien mauChinhSachHoanTien;

    @Column(name = "SoGioTruocSuKien", nullable = false)
    private Integer soGioTruocSuKien;

    @Column(name = "TyLeHoanTien", nullable = false)
    private BigDecimal tyLeHoanTien;

    public Long getMaQuyTac() { return maQuyTac; }
    public void setMaQuyTac(Long maQuyTac) { this.maQuyTac = maQuyTac; }

    public MauChinhSachHoanTien getMauChinhSachHoanTien() { return mauChinhSachHoanTien; }
    public void setMauChinhSachHoanTien(MauChinhSachHoanTien mauChinhSachHoanTien) { this.mauChinhSachHoanTien = mauChinhSachHoanTien; }

    public Integer getSoGioTruocSuKien() { return soGioTruocSuKien; }
    public void setSoGioTruocSuKien(Integer soGioTruocSuKien) { this.soGioTruocSuKien = soGioTruocSuKien; }

    public BigDecimal getTyLeHoanTien() { return tyLeHoanTien; }
    public void setTyLeHoanTien(BigDecimal tyLeHoanTien) { this.tyLeHoanTien = tyLeHoanTien; }
}
