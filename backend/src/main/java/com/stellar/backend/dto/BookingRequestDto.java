package com.stellar.backend.dto;

public class BookingRequestDto {
    private Long maSuKien;
    private Long maLichDien;
    private Long maHangVe;
    private int soLuong;

    public Long getMaSuKien() { return maSuKien; }
    public void setMaSuKien(Long maSuKien) { this.maSuKien = maSuKien; }
    public Long getMaLichDien() { return maLichDien; }
    public void setMaLichDien(Long maLichDien) { this.maLichDien = maLichDien; }
    public Long getMaHangVe() { return maHangVe; }
    public void setMaHangVe(Long maHangVe) { this.maHangVe = maHangVe; }
    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }
    
    private Long maKhuVuc;
    public Long getMaKhuVuc() { return maKhuVuc; }
    public void setMaKhuVuc(Long maKhuVuc) { this.maKhuVuc = maKhuVuc; }
    
    private java.util.List<String> dsGhe;
    public java.util.List<String> getDsGhe() { return dsGhe; }
    public void setDsGhe(java.util.List<String> dsGhe) { this.dsGhe = dsGhe; }

    private String discountCode;
    public String getDiscountCode() { return discountCode; }
    public void setDiscountCode(String discountCode) { this.discountCode = discountCode; }
}
