package com.stellar.backend.controller;

import com.stellar.backend.dto.AdminAnalyticsResponseDto;
import com.stellar.backend.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin/analytics")
public class AdminAnalyticsController {

    @Autowired
    private SuKienRepository suKienRepository;

    @Autowired
    private VeRepository veRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private static final List<String> TRANG_THAI_VE_HOP_LE = List.of("Hiệu lực", "Đã check-in");
    private static final BigDecimal PLATFORM_FEE_RATE = new BigDecimal("0.05");

    @SuppressWarnings("unchecked")
    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getSummary() {
        try {
            // 1. Lấy tổng số sự kiện (Tối ưu dùng count thay vì findAll)
            long tongSoSuKien = suKienRepository.count();

            // 2. Tính doanh thu gộp (Gross Revenue) & Doanh thu nền tảng (Platform Revenue) từ View 1
            BigDecimal tongDoanhThuGop = BigDecimal.ZERO;
            BigDecimal tongDoanhThuNenTang = BigDecimal.ZERO;

            // Truy vấn dữ liệu từ View 1: Doanh thu theo thể loại
            List<Object[]> categoryRows = entityManager.createNativeQuery(
                    "SELECT THE_LOAI, GROSS_REVENUE, TONG_HOAN_TRA FROM V_ANALYTICS_CATEGORY"
            ).getResultList();

            Map<String, BigDecimal> categoryMap = new HashMap<>();

            for (Object[] row : categoryRows) {
                String theLoai = row[0] != null ? row[0].toString().trim() : "Khác";
                BigDecimal gross = row[1] != null ? new BigDecimal(row[1].toString()) : BigDecimal.ZERO;
                BigDecimal refund = row[2] != null ? new BigDecimal(row[2].toString()) : BigDecimal.ZERO;

                // Net = Gross - Refund
                BigDecimal net = gross.subtract(refund);
                if (net.compareTo(BigDecimal.ZERO) < 0) {
                    net = BigDecimal.ZERO;
                }
                
                // Platform fee = Net * 5%
                BigDecimal platformFee = net.multiply(PLATFORM_FEE_RATE).setScale(0, RoundingMode.HALF_UP);

                tongDoanhThuGop = tongDoanhThuGop.add(gross);
                tongDoanhThuNenTang = tongDoanhThuNenTang.add(platformFee);

                categoryMap.put(theLoai, categoryMap.getOrDefault(theLoai, BigDecimal.ZERO).add(platformFee));
            }

            // 3. Tính tổng số vé bán ra hợp lệ trên toàn hệ thống
            long tongSoVeHopLe = veRepository.countByTrangThaiVeIn(TRANG_THAI_VE_HOP_LE);

            // 4. Doanh thu theo thể loại (Category Revenue)
            List<AdminAnalyticsResponseDto.CategoryRevenueDetail> listTheLoai = new ArrayList<>();
            final BigDecimal checkTongNenTang = tongDoanhThuNenTang;
            categoryMap.forEach((theLoai, rev) -> {
                double tyLe = 0.0;
                if (checkTongNenTang.compareTo(BigDecimal.ZERO) > 0) {
                    tyLe = rev.multiply(new BigDecimal("100")).divide(checkTongNenTang, 2, RoundingMode.HALF_UP).doubleValue();
                }
                listTheLoai.add(new AdminAnalyticsResponseDto.CategoryRevenueDetail(theLoai, rev, tyLe));
            });

            // 5. Tốc độ tăng trưởng doanh thu nền tảng (Platform Revenue Growth) từ View 2
            List<Object[]> growthRows = entityManager.createNativeQuery(
                    "SELECT NGAY_PHAT_SINH, THANG_NAM, NGAY_THANG_NAM, GROSS_TIEN, SO_TIEN_HOAN FROM V_ANALYTICS_GROWTH"
            ).getResultList();

            // Đếm số tháng duy nhất phát sinh dữ liệu
            Set<String> uniqueMonths = new HashSet<>();
            for (Object[] row : growthRows) {
                if (row[1] != null) {
                    uniqueMonths.add(row[1].toString());
                }
            }

            List<AdminAnalyticsResponseDto.RevenueGrowthDetail> listGrowth = new ArrayList<>();

            if (uniqueMonths.size() <= 2) {
                // Nhóm theo Ngày (Sử dụng TreeMap để tự động sắp xếp theo thứ tự thời gian tăng dần)
                Map<java.time.LocalDate, BigDecimal[]> dayMap = new TreeMap<>();
                for (Object[] row : growthRows) {
                    if (row[0] == null) continue;
                    
                    java.time.LocalDate ld = null;
                    if (row[0] instanceof java.sql.Timestamp) {
                        ld = ((java.sql.Timestamp) row[0]).toLocalDateTime().toLocalDate();
                    } else if (row[0] instanceof java.sql.Date) {
                        ld = ((java.sql.Date) row[0]).toLocalDate();
                    } else if (row[0] instanceof java.util.Date) {
                        ld = new java.sql.Date(((java.util.Date) row[0]).getTime()).toLocalDate();
                    }
                    
                    if (ld == null) continue;
                    
                    BigDecimal gross = row[3] != null ? new BigDecimal(row[3].toString()) : BigDecimal.ZERO;
                    BigDecimal refund = row[4] != null ? new BigDecimal(row[4].toString()) : BigDecimal.ZERO;

                    BigDecimal[] values = dayMap.getOrDefault(ld, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
                    values[0] = values[0].add(gross);
                    values[1] = values[1].add(refund);
                    dayMap.put(ld, values);
                }

                DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                for (Map.Entry<java.time.LocalDate, BigDecimal[]> entry : dayMap.entrySet()) {
                    BigDecimal grossDay = entry.getValue()[0];
                    BigDecimal refundDay = entry.getValue()[1];
                    BigDecimal netDay = grossDay.subtract(refundDay);
                    if (netDay.compareTo(BigDecimal.ZERO) < 0) {
                        netDay = BigDecimal.ZERO;
                    }
                    BigDecimal platformDay = netDay.multiply(PLATFORM_FEE_RATE).setScale(0, RoundingMode.HALF_UP);
                    listGrowth.add(new AdminAnalyticsResponseDto.RevenueGrowthDetail(entry.getKey().format(dayFormatter), platformDay));
                }
            } else {
                // Nhóm theo Tháng (Sử dụng TreeMap để tự động sắp xếp theo thứ tự thời gian tăng dần)
                Map<YearMonth, BigDecimal[]> monthMap = new TreeMap<>();
                DateTimeFormatter monthParser = DateTimeFormatter.ofPattern("MM/yyyy");
                
                for (Object[] row : growthRows) {
                    if (row[1] == null) continue;
                    String thangNamStr = row[1].toString();
                    YearMonth ym = YearMonth.parse(thangNamStr, monthParser);
                    
                    BigDecimal gross = row[3] != null ? new BigDecimal(row[3].toString()) : BigDecimal.ZERO;
                    BigDecimal refund = row[4] != null ? new BigDecimal(row[4].toString()) : BigDecimal.ZERO;

                    BigDecimal[] values = monthMap.getOrDefault(ym, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
                    values[0] = values[0].add(gross);
                    values[1] = values[1].add(refund);
                    monthMap.put(ym, values);
                }

                for (Map.Entry<YearMonth, BigDecimal[]> entry : monthMap.entrySet()) {
                    BigDecimal grossMonth = entry.getValue()[0];
                    BigDecimal refundMonth = entry.getValue()[1];
                    BigDecimal netMonth = grossMonth.subtract(refundMonth);
                    if (netMonth.compareTo(BigDecimal.ZERO) < 0) {
                        netMonth = BigDecimal.ZERO;
                    }
                    BigDecimal platformMonth = netMonth.multiply(PLATFORM_FEE_RATE).setScale(0, RoundingMode.HALF_UP);
                    listGrowth.add(new AdminAnalyticsResponseDto.RevenueGrowthDetail(entry.getKey().format(monthParser), platformMonth));
                }
            }

            // 6. Xếp hạng Nhà tổ chức theo doanh thu từ View 3
            List<Object[]> organizerRows = entityManager.createNativeQuery(
                    "SELECT TEN_NHA_TO_CHUC, GROSS_REVENUE, TONG_HOAN_TRA FROM V_ANALYTICS_ORGANIZER"
            ).getResultList();

            Map<String, BigDecimal[]> organizerMap = new HashMap<>(); // key: tên NTC, value: [GrossRev, Refund]

            for (Object[] row : organizerRows) {
                String ntcName = row[0] != null ? row[0].toString().trim() : "Hệ thống";
                BigDecimal gross = row[1] != null ? new BigDecimal(row[1].toString()) : BigDecimal.ZERO;
                BigDecimal refund = row[2] != null ? new BigDecimal(row[2].toString()) : BigDecimal.ZERO;

                BigDecimal[] current = organizerMap.getOrDefault(ntcName, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
                current[0] = current[0].add(gross);
                current[1] = current[1].add(refund);
                organizerMap.put(ntcName, current);
            }

            List<AdminAnalyticsResponseDto.OrganizerRevenueDetail> listOrganizers = new ArrayList<>();
            organizerMap.forEach((name, revs) -> {
                BigDecimal gross = revs[0];
                BigDecimal refund = revs[1];
                BigDecimal net = gross.subtract(refund);
                if (net.compareTo(BigDecimal.ZERO) < 0) {
                    net = BigDecimal.ZERO;
                }
                BigDecimal platform = net.multiply(PLATFORM_FEE_RATE).setScale(0, RoundingMode.HALF_UP);
                listOrganizers.add(new AdminAnalyticsResponseDto.OrganizerRevenueDetail(name, gross, platform));
            });

            // Sắp xếp nhà tổ chức theo Doanh thu gộp giảm dần
            listOrganizers.sort((o1, o2) -> o2.getTongDoanhThuGop().compareTo(o1.getTongDoanhThuGop()));

            // Gán dữ liệu vào DTO
            AdminAnalyticsResponseDto response = new AdminAnalyticsResponseDto();
            response.setTongDoanhThuGop(tongDoanhThuGop);
            response.setTongDoanhThuNenTang(tongDoanhThuNenTang);
            response.setTongSoVeHopLe(tongSoVeHopLe);
            response.setTongSoSuKien(tongSoSuKien);
            response.setChiTietTheLoai(listTheLoai);
            response.setTangTruongDoanhThu(listGrowth);
            response.setTopNhaToChuc(listOrganizers);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("message", "Lỗi phân tích dữ liệu: " + e.getMessage()));
        }
    }
}
