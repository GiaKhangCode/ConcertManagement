package com.stellar.backend.entity;

import java.io.Serializable;
import java.util.Objects;

public class ApDungId implements Serializable {
    private Long donMua;
    private Long maGiamGia;

    public ApDungId() {}

    public ApDungId(Long donMua, Long maGiamGia) {
        this.donMua = donMua;
        this.maGiamGia = maGiamGia;
    }

    // Getters and Setters
    public Long getDonMua() { return donMua; }
    public void setDonMua(Long donMua) { this.donMua = donMua; }

    public Long getMaGiamGia() { return maGiamGia; }
    public void setMaGiamGia(Long maGiamGia) { this.maGiamGia = maGiamGia; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApDungId apDungId = (ApDungId) o;
        return Objects.equals(donMua, apDungId.donMua) && Objects.equals(maGiamGia, apDungId.maGiamGia);
    }

    @Override
    public int hashCode() {
        return Objects.hash(donMua, maGiamGia);
    }
}
