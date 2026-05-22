package com.stellar.backend.dto;

import java.math.BigDecimal;
import java.util.List;

public class AdminAnalyticsResponseDto {
    private BigDecimal tongDoanhThuGop;
    private BigDecimal tongDoanhThuNenTang;
    private long tongSoVeHopLe;
    private long tongSoSuKien;

    private List<CategoryRevenueDetail> chiTietTheLoai;
    private List<RevenueGrowthDetail> tangTruongDoanhThu;
    private List<OrganizerRevenueDetail> topNhaToChuc;

    // Getters and Setters
    public BigDecimal getTongDoanhThuGop() { return tongDoanhThuGop; }
    public void setTongDoanhThuGop(BigDecimal tongDoanhThuGop) { this.tongDoanhThuGop = tongDoanhThuGop; }

    public BigDecimal getTongDoanhThuNenTang() { return tongDoanhThuNenTang; }
    public void setTongDoanhThuNenTang(BigDecimal tongDoanhThuNenTang) { this.tongDoanhThuNenTang = tongDoanhThuNenTang; }

    public long getTongSoVeHopLe() { return tongSoVeHopLe; }
    public void setTongSoVeHopLe(long tongSoVeHopLe) { this.tongSoVeHopLe = tongSoVeHopLe; }

    public long getTongSoSuKien() { return tongSoSuKien; }
    public void setTongSoSuKien(long tongSoSuKien) { this.tongSoSuKien = tongSoSuKien; }

    public List<CategoryRevenueDetail> getChiTietTheLoai() { return chiTietTheLoai; }
    public void setChiTietTheLoai(List<CategoryRevenueDetail> chiTietTheLoai) { this.chiTietTheLoai = chiTietTheLoai; }

    public List<RevenueGrowthDetail> getTangTruongDoanhThu() { return tangTruongDoanhThu; }
    public void setTangTruongDoanhThu(List<RevenueGrowthDetail> tangTruongDoanhThu) { this.tangTruongDoanhThu = tangTruongDoanhThu; }

    public List<OrganizerRevenueDetail> getTopNhaToChuc() { return topNhaToChuc; }
    public void setTopNhaToChuc(List<OrganizerRevenueDetail> topNhaToChuc) { this.topNhaToChuc = topNhaToChuc; }

    public static class CategoryRevenueDetail {
        private String theLoai;
        private BigDecimal doanhThuNenTang;
        private double tyLe;

        public CategoryRevenueDetail() {}

        public CategoryRevenueDetail(String theLoai, BigDecimal doanhThuNenTang, double tyLe) {
            this.theLoai = theLoai;
            this.doanhThuNenTang = doanhThuNenTang;
            this.tyLe = tyLe;
        }

        public String getTheLoai() { return theLoai; }
        public void setTheLoai(String theLoai) { this.theLoai = theLoai; }

        public BigDecimal getDoanhThuNenTang() { return doanhThuNenTang; }
        public void setDoanhThuNenTang(BigDecimal doanhThuNenTang) { this.doanhThuNenTang = doanhThuNenTang; }

        public double getTyLe() { return tyLe; }
        public void setTyLe(double tyLe) { this.tyLe = tyLe; }
    }

    public static class RevenueGrowthDetail {
        private String thoiGian; // "MM/yyyy"
        private BigDecimal doanhThuNenTang;

        public RevenueGrowthDetail() {}

        public RevenueGrowthDetail(String thoiGian, BigDecimal doanhThuNenTang) {
            this.thoiGian = thoiGian;
            this.doanhThuNenTang = doanhThuNenTang;
        }

        public String getThoiGian() { return thoiGian; }
        public void setThoiGian(String thoiGian) { this.thoiGian = thoiGian; }

        public BigDecimal getDoanhThuNenTang() { return doanhThuNenTang; }
        public void setDoanhThuNenTang(BigDecimal doanhThuNenTang) { this.doanhThuNenTang = doanhThuNenTang; }
    }

    public static class OrganizerRevenueDetail {
        private String tenNhaToChuc;
        private BigDecimal tongDoanhThuGop;
        private BigDecimal tongDoanhThuNenTang;

        public OrganizerRevenueDetail() {}

        public OrganizerRevenueDetail(String tenNhaToChuc, BigDecimal tongDoanhThuGop, BigDecimal tongDoanhThuNenTang) {
            this.tenNhaToChuc = tenNhaToChuc;
            this.tongDoanhThuGop = tongDoanhThuGop;
            this.tongDoanhThuNenTang = tongDoanhThuNenTang;
        }

        public String getTenNhaToChuc() { return tenNhaToChuc; }
        public void setTenNhaToChuc(String tenNhaToChuc) { this.tenNhaToChuc = tenNhaToChuc; }

        public BigDecimal getTongDoanhThuGop() { return tongDoanhThuGop; }
        public void setTongDoanhThuGop(BigDecimal tongDoanhThuGop) { this.tongDoanhThuGop = tongDoanhThuGop; }

        public BigDecimal getTongDoanhThuNenTang() { return tongDoanhThuNenTang; }
        public void setTongDoanhThuNenTang(BigDecimal tongDoanhThuNenTang) { this.tongDoanhThuNenTang = tongDoanhThuNenTang; }
    }
}
