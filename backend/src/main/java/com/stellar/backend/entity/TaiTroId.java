package com.stellar.backend.entity;

import java.io.Serializable;
import java.util.Objects;

public class TaiTroId implements Serializable {
    private Long suKien;
    private Long nhaTaiTro;

    public TaiTroId() {}

    public TaiTroId(Long suKien, Long nhaTaiTro) {
        this.suKien = suKien;
        this.nhaTaiTro = nhaTaiTro;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaiTroId taiTroId = (TaiTroId) o;
        return Objects.equals(suKien, taiTroId.suKien) && Objects.equals(nhaTaiTro, taiTroId.nhaTaiTro);
    }

    @Override
    public int hashCode() {
        return Objects.hash(suKien, nhaTaiTro);
    }

    // Getters and Setters
    public Long getSuKien() { return suKien; }
    public void setSuKien(Long suKien) { this.suKien = suKien; }
    public Long getNhaTaiTro() { return nhaTaiTro; }
    public void setNhaTaiTro(Long nhaTaiTro) { this.nhaTaiTro = nhaTaiTro; }
}
