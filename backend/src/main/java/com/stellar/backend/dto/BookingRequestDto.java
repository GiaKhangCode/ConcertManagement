package com.stellar.backend.dto;

public class BookingRequestDto {
    private Long maSuKien;
    private Long maHangVe;
    private int soLuong;

    public Long getMaSuKien() { return maSuKien; }
    public void setMaSuKien(Long maSuKien) { this.maSuKien = maSuKien; }
    public Long getMaHangVe() { return maHangVe; }
    public void setMaHangVe(Long maHangVe) { this.maHangVe = maHangVe; }
    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }
}
