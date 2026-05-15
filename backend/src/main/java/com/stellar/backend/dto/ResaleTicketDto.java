package com.stellar.backend.dto;

import java.math.BigDecimal;

public class ResaleTicketDto {
    private Long maVe;
    private String tenSuKien;
    private String thoiGian;
    private String hangGhe;
    private String viTri;
    private BigDecimal giaBanLai;
    private String nguoiBan;
    private String image;

    public ResaleTicketDto(Long maVe, String tenSuKien, String thoiGian, String hangGhe, String viTri, BigDecimal giaBanLai, String nguoiBan, String image) {
        this.maVe = maVe;
        this.tenSuKien = tenSuKien;
        this.thoiGian = thoiGian;
        this.hangGhe = hangGhe;
        this.viTri = viTri;
        this.giaBanLai = giaBanLai;
        this.nguoiBan = nguoiBan;
        this.image = image;
    }

    // Getters and Setters
    public Long getMaVe() { return maVe; }
    public void setMaVe(Long maVe) { this.maVe = maVe; }
    public String getTenSuKien() { return tenSuKien; }
    public void setTenSuKien(String tenSuKien) { this.tenSuKien = tenSuKien; }
    public String getThoiGian() { return thoiGian; }
    public void setThoiGian(String thoiGian) { this.thoiGian = thoiGian; }
    public String getHangGhe() { return hangGhe; }
    public void setHangGhe(String hangGhe) { this.hangGhe = hangGhe; }
    public String getViTri() { return viTri; }
    public void setViTri(String viTri) { this.viTri = viTri; }
    public BigDecimal getGiaBanLai() { return giaBanLai; }
    public void setGiaBanLai(BigDecimal giaBanLai) { this.giaBanLai = giaBanLai; }
    public String getNguoiBan() { return nguoiBan; }
    public void setNguoiBan(String nguoiBan) { this.nguoiBan = nguoiBan; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
}
