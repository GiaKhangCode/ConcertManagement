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
    private String anhThumbnailUrl;
    private String phanLoai;
    private String moTa; // Mô tả sự kiện
    private String lyDoTuChoi; // Lý do từ chối (nếu có)

    private List<LichDienDto> lichDienList;
    private List<HangVeDto> hangVeList;
    private RefundPolicyDto refundPolicy;
    private List<SponsorDto> sponsors;
    private List<NgheSiDto> ngheSiList;

    public List<NgheSiDto> getNgheSiList() { return ngheSiList; }
    public void setNgheSiList(List<NgheSiDto> ngheSiList) { this.ngheSiList = ngheSiList; }

    public List<SponsorDto> getSponsors() { return sponsors; }
    public void setSponsors(List<SponsorDto> sponsors) { this.sponsors = sponsors; }

    public RefundPolicyDto getRefundPolicy() { return refundPolicy; }
    public void setRefundPolicy(RefundPolicyDto refundPolicy) { this.refundPolicy = refundPolicy; }

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
    public String getAnhThumbnailUrl() { return anhThumbnailUrl; }
    public void setAnhThumbnailUrl(String anhThumbnailUrl) { this.anhThumbnailUrl = anhThumbnailUrl; }
    public String getPhanLoai() { return phanLoai; }
    public void setPhanLoai(String phanLoai) { this.phanLoai = phanLoai; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public String getLyDoTuChoi() { return lyDoTuChoi; }
    public void setLyDoTuChoi(String lyDoTuChoi) { this.lyDoTuChoi = lyDoTuChoi; }
    public List<LichDienDto> getLichDienList() { return lichDienList; }
    public void setLichDienList(List<LichDienDto> lichDienList) { this.lichDienList = lichDienList; }
    public List<HangVeDto> getHangVeList() { return hangVeList; }
    public void setHangVeList(List<HangVeDto> hangVeList) { this.hangVeList = hangVeList; }

    public static class LichDienDto {
        private Long maLichDien;
        private String tenLichDien;
        private LocalDateTime thoiGianBatDau;
        private LocalDateTime thoiGianKetThuc;

        public Long getMaLichDien() { return maLichDien; }
        public void setMaLichDien(Long maLichDien) { this.maLichDien = maLichDien; }
        public String getTenLichDien() { return tenLichDien; }
        public void setTenLichDien(String tenLichDien) { this.tenLichDien = tenLichDien; }
        public LocalDateTime getThoiGianBatDau() { return thoiGianBatDau; }
        public void setThoiGianBatDau(LocalDateTime thoiGianBatDau) { this.thoiGianBatDau = thoiGianBatDau; }
        public LocalDateTime getThoiGianKetThuc() { return thoiGianKetThuc; }
        public void setThoiGianKetThuc(LocalDateTime thoiGianKetThuc) { this.thoiGianKetThuc = thoiGianKetThuc; }
    }

    public static class HangVeDto {
        private Long maHangVe;
        private String tenHangVe;
        private BigDecimal giaNiemYet;
        private Integer tongSoLuong;
        private List<KhuVucDto> khuVucList;

        public Long getMaHangVe() { return maHangVe; }
        public void setMaHangVe(Long maHangVe) { this.maHangVe = maHangVe; }
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
        private Long maKhuVuc;
        private String tenKhuVuc;
        private Integer sucChuaKv;
        /** Danh sách ký hiệu hàng ghế, VD: ["A","B","C"] — nếu null thì không tạo ghế */
        private java.util.List<String> rows;
        /** Số ghế mỗi hàng — nếu null thì không tạo ghế */
        private Integer seatsPerRow;

        public Long getMaKhuVuc() { return maKhuVuc; }
        public void setMaKhuVuc(Long maKhuVuc) { this.maKhuVuc = maKhuVuc; }
        public String getTenKhuVuc() { return tenKhuVuc; }
        public void setTenKhuVuc(String tenKhuVuc) { this.tenKhuVuc = tenKhuVuc; }
        public Integer getSucChuaKv() { return sucChuaKv; }
        public void setSucChuaKv(Integer sucChuaKv) { this.sucChuaKv = sucChuaKv; }
        public java.util.List<String> getRows() { return rows; }
        public void setRows(java.util.List<String> rows) { this.rows = rows; }
        public Integer getSeatsPerRow() { return seatsPerRow; }
        public void setSeatsPerRow(Integer seatsPerRow) { this.seatsPerRow = seatsPerRow; }

        private List<RowConfigDto> rowConfigs;
        public List<RowConfigDto> getRowConfigs() { return rowConfigs; }
        public void setRowConfigs(List<RowConfigDto> rowConfigs) { this.rowConfigs = rowConfigs; }
    }

    public static class RowConfigDto {
        private String rowLabel;
        private Integer seatCount;

        public String getRowLabel() { return rowLabel; }
        public void setRowLabel(String rowLabel) { this.rowLabel = rowLabel; }
        public Integer getSeatCount() { return seatCount; }
        public void setSeatCount(Integer seatCount) { this.seatCount = seatCount; }
    }

    public static class RefundPolicyDto {
        private String name;
        private List<RuleDto> rules;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public List<RuleDto> getRules() { return rules; }
        public void setRules(List<RuleDto> rules) { this.rules = rules; }

        public static class RuleDto {
            private Integer hoursBefore;
            private BigDecimal percentage;
            
            public Integer getHoursBefore() { return hoursBefore; }
            public void setHoursBefore(Integer hoursBefore) { this.hoursBefore = hoursBefore; }
            public BigDecimal getPercentage() { return percentage; }
            public void setPercentage(BigDecimal percentage) { this.percentage = percentage; }
        }
    }

    public static class SponsorDto {
        private String name;
        private String rank; // Hạng tài trợ (Kim Cương, Vàng, Bạc, Đồng)

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getRank() { return rank; }
        public void setRank(String rank) { this.rank = rank; }
    }

    public static class NgheSiDto {
        private String tenNgheSi;
        
        public String getTenNgheSi() { return tenNgheSi; }
        public void setTenNgheSi(String tenNgheSi) { this.tenNgheSi = tenNgheSi; }
    }
}
