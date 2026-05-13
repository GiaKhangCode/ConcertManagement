package com.stellar.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ThamGiaId implements Serializable {
    @Column(name = "MaNgheSi")
    private Long maNgheSi;

    @Column(name = "MaSuKien")
    private Long maSuKien;

    public ThamGiaId() {}

    public ThamGiaId(Long maNgheSi, Long maSuKien) {
        this.maNgheSi = maNgheSi;
        this.maSuKien = maSuKien;
    }

    public Long getMaNgheSi() { return maNgheSi; }
    public void setMaNgheSi(Long maNgheSi) { this.maNgheSi = maNgheSi; }

    public Long getMaSuKien() { return maSuKien; }
    public void setMaSuKien(Long maSuKien) { this.maSuKien = maSuKien; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ThamGiaId that = (ThamGiaId) o;
        return Objects.equals(maNgheSi, that.maNgheSi) && Objects.equals(maSuKien, that.maSuKien);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maNgheSi, maSuKien);
    }
}
