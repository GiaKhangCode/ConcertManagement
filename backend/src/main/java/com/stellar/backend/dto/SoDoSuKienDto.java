package com.stellar.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class SoDoSuKienDto {
    private Long maSuKien;
    private String duLieuCanvas;
    private List<KhuVucSoDoDto> zones;
    private List<GheNgoiSoDoDto> dsGhe;

    @Data
    public static class GheNgoiSoDoDto {
        private Long maKhuVuc;
        private Long maGhe;
        private String toaDo;
    }

    @Data
    public static class KhuVucSoDoDto {
        private Long maKhuVuc;
        private Long maHangVe;
        private String tenHienThi;
        private String loaiHinhDang;
        private String mauSac;
        private String thuocTinhJson;
        private Integer kichThuocFont;
    }
}
