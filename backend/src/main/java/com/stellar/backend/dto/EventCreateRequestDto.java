package com.stellar.backend.dto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class EventCreateRequestDto {
    private String tenSuKien;
    private Long maDiaDiem;
    private LocalDateTime thoiGianBD;
    private LocalDateTime thoiGianKT;
    private LocalDateTime thoiGianMoBanVe;
    private LocalDateTime thoiGianNgungBanVe;
    private String anhBiaUrl;
    private String phanLoai;
    private String moTa; // Mô tả sự kiện

    private List<LichDienDto> lichDienList;
    private List<HangVeDto> hangVeList;

    public String getTenSuKien() { return tenSuKien; }
    public void setTenSuKien(String tenSuKien) { this.tenSuKien = tenSuKien; }
    public Long getMaDiaDiem() { return maDiaDiem; }
    public void setMaDiaDiem(Long maDiaDiem) { this.maDiaDiem = maDiaDiem; }
    public LocalDateTime getThoiGianBD() { return thoiGianBD; }
    public void setThoiGianBD(LocalDateTime thoiGianBD) { this.thoiGianBD = thoiGianBD; }
    public LocalDateTime getThoiGianKT() { return thoiGianKT; }
    public void setThoiGianKT(LocalDateTime thoiGianKT) { this.thoiGianKT = thoiGianKT; }
    public LocalDateTime getThoiGianMoBanVe() { return thoiGianMoBanVe; }
    public void setThoiGianMoBanVe(LocalDateTime thoiGianMoBanVe) { this.thoiGianMoBanVe = thoiGianMoBanVe; }
    public LocalDateTime getThoiGianNgungBanVe() { return thoiGianNgungBanVe; }
    public void setThoiGianNgungBanVe(LocalDateTime thoiGianNgungBanVe) { this.thoiGianNgungBanVe = thoiGianNgungBanVe; }
    public String getAnhBiaUrl() { return anhBiaUrl; }
    public void setAnhBiaUrl(String anhBiaUrl) { this.anhBiaUrl = anhBiaUrl; }
    public String getPhanLoai() { return phanLoai; }
    public void setPhanLoai(String phanLoai) { this.phanLoai = phanLoai; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public List<LichDienDto> getLichDienList() { return lichDienList; }
    public void setLichDienList(List<LichDienDto> lichDienList) { this.lichDienList = lichDienList; }
    public List<HangVeDto> getHangVeList() { return hangVeList; }
    public void setHangVeList(List<HangVeDto> hangVeList) { this.hangVeList = hangVeList; }

    public static class LichDienDto {
        private String tenLichDien;
        private LocalDateTime thoiGianBatDau;
        private LocalDateTime thoiGianKetThuc;

        public String getTenLichDien() { return tenLichDien; }
        public void setTenLichDien(String tenLichDien) { this.tenLichDien = tenLichDien; }
        public LocalDateTime getThoiGianBatDau() { return thoiGianBatDau; }
        public void setThoiGianBatDau(LocalDateTime thoiGianBatDau) { this.thoiGianBatDau = thoiGianBatDau; }
        public LocalDateTime getThoiGianKetThuc() { return thoiGianKetThuc; }
        public void setThoiGianKetThuc(LocalDateTime thoiGianKetThuc) { this.thoiGianKetThuc = thoiGianKetThuc; }
    }

    public static class HangVeDto {
        private String tenHangVe;
        private BigDecimal giaNiemYet;
        private Integer tongSoLuong;
        private List<KhuVucDto> khuVucList;

        public String getTenHangVe() { return tenHangVe; }
        public void setTenHangVe(String tenHangVe) { this.tenHangVe = tenHangVe; }
        public BigDecimal getGiaNiemYet() { return giaNiemYet; }
        public void setGiaNiemYet(BigDecimal giaNiemYet) { this.giaNiemYet = giaNiemYet; }
        public Integer getTongSoLuong() { return tongSoLuong; }
        public void setTongSoLuong(Integer tongSoLuong) { this.tongSoLuong = tongSoLuong; }
        public List<KhuVucDto> getKhuVucList() { return khuVucList; }
        public void setKhuVucList(List<KhuVucDto> khuVucList) { this.khuVucList = khuVucList; }
    }

    public static class KhuVucDto {
        private String tenKhuVuc;
        private Integer sucChuaKv;

        public String getTenKhuVuc() { return tenKhuVuc; }
        public void setTenKhuVuc(String tenKhuVuc) { this.tenKhuVuc = tenKhuVuc; }
        public Integer getSucChuaKv() { return sucChuaKv; }
        public void setSucChuaKv(Integer sucChuaKv) { this.sucChuaKv = sucChuaKv; }
    }
}
