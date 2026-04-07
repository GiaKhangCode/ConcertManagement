package com.stellar.backend.dto;

import java.math.BigDecimal;
import java.util.List;

public class RevenueResponseDto {
    private BigDecimal tongDoanhThu;
    private int tongSoVeBan;
    private int tongSoSuKien;
    private List<EventRevenueDetail> chiTietSuKien;

    // Getter / Setter
    public BigDecimal getTongDoanhThu() { return tongDoanhThu; }
    public void setTongDoanhThu(BigDecimal tongDoanhThu) { this.tongDoanhThu = tongDoanhThu; }
    public int getTongSoVeBan() { return tongSoVeBan; }
    public void setTongSoVeBan(int tongSoVeBan) { this.tongSoVeBan = tongSoVeBan; }
    public int getTongSoSuKien() { return tongSoSuKien; }
    public void setTongSoSuKien(int tongSoSuKien) { this.tongSoSuKien = tongSoSuKien; }
    public List<EventRevenueDetail> getChiTietSuKien() { return chiTietSuKien; }
    public void setChiTietSuKien(List<EventRevenueDetail> chiTietSuKien) { this.chiTietSuKien = chiTietSuKien; }

    public static class EventRevenueDetail {
        private Long maSuKien;
        private String tenSuKien;
        private String trangThai;
        private BigDecimal doanhThu;
        private int soVeBan;
        private String anhBiaUrl;

        // Getter / Setter
        public Long getMaSuKien() { return maSuKien; }
        public void setMaSuKien(Long maSuKien) { this.maSuKien = maSuKien; }
        public String getTenSuKien() { return tenSuKien; }
        public void setTenSuKien(String tenSuKien) { this.tenSuKien = tenSuKien; }
        public String getTrangThai() { return trangThai; }
        public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
        public BigDecimal getDoanhThu() { return doanhThu; }
        public void setDoanhThu(BigDecimal doanhThu) { this.doanhThu = doanhThu; }
        public int getSoVeBan() { return soVeBan; }
        public void setSoVeBan(int soVeBan) { this.soVeBan = soVeBan; }
        public String getAnhBiaUrl() { return anhBiaUrl; }
        public void setAnhBiaUrl(String anhBiaUrl) { this.anhBiaUrl = anhBiaUrl; }
    }
}
