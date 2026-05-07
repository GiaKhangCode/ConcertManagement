package com.stellar.backend.entity;

import java.io.Serializable;
import java.util.Objects;

public class TrangThaiGheTheoSuatId implements Serializable {
    private Long maGhe;
    private Long maLichDien;

    public TrangThaiGheTheoSuatId() {}

    public TrangThaiGheTheoSuatId(Long maGhe, Long maLichDien) {
        this.maGhe = maGhe;
        this.maLichDien = maLichDien;
    }

    public Long getMaGhe() { return maGhe; }
    public void setMaGhe(Long maGhe) { this.maGhe = maGhe; }

    public Long getMaLichDien() { return maLichDien; }
    public void setMaLichDien(Long maLichDien) { this.maLichDien = maLichDien; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrangThaiGheTheoSuatId that = (TrangThaiGheTheoSuatId) o;
        return Objects.equals(maGhe, that.maGhe) &&
               Objects.equals(maLichDien, that.maLichDien);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maGhe, maLichDien);
    }
}
