package com.stellar.backend.controller;

import com.stellar.backend.dto.RevenueResponseDto;
import com.stellar.backend.entity.SuKien;
import com.stellar.backend.entity.Ve;
import com.stellar.backend.entity.DonMua;
import com.stellar.backend.repository.DonMuaRepository;
import com.stellar.backend.repository.SuKienRepository;
import com.stellar.backend.repository.VeRepository;
import com.stellar.backend.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/organizer")
public class OrganizerController {

    @Autowired
    private SuKienRepository suKienRepository;

    @Autowired
    private VeRepository veRepository;

    @Autowired
    private DonMuaRepository donMuaRepository;

    // Chỉ tính vé có trạng thái "Hiệu lực" hoặc "Đã check-in" là vé đã bán hợp lệ
    private static final List<String> TRANG_THAI_VE_HOP_LE = List.of("Hiệu lực", "Đã check-in");

    /**
     * Lấy báo cáo doanh thu tổng hợp cho nhà tổ chức hiện tại.
     * Chỉ tính doanh thu của các sự kiện mà người đang đăng nhập đã tạo.
     * Vé bị hủy ("Đã hủy") KHÔNG được tính vào số vé bán và doanh thu.
     */
    @GetMapping("/revenue")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<?> getRevenue() {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder
                    .getContext().getAuthentication().getPrincipal();
            Long userId = userDetails.getId();

            // Lấy tất cả sự kiện do user này tạo
            List<SuKien> suKienList = suKienRepository.findByNguoiTao_MaTaiKhoan(userId);

            BigDecimal tongDoanhThu = BigDecimal.ZERO;
            long tongSoVeBan = 0;
            List<RevenueResponseDto.EventRevenueDetail> chiTiet = new ArrayList<>();

            for (SuKien sk : suKienList) {
                // Đếm vé hợp lệ (Hiệu lực + Đã check-in), BỎ QUA vé Đã hủy
                long soVeHopLe = veRepository.countByHangVe_SuKien_MaSuKienAndTrangThaiVeIn(
                        sk.getMaSuKien(), TRANG_THAI_VE_HOP_LE);

                // Tính doanh thu thực tế từ các đơn hàng thành công của sự kiện này
                BigDecimal doanhThuSK = donMuaRepository.sumGrossRevenueBySuKien_MaSuKien(sk.getMaSuKien());
                if (doanhThuSK == null) doanhThuSK = BigDecimal.ZERO;

                tongDoanhThu = tongDoanhThu.add(doanhThuSK);
                tongSoVeBan += soVeHopLe;

                RevenueResponseDto.EventRevenueDetail detail = new RevenueResponseDto.EventRevenueDetail();
                detail.setMaSuKien(sk.getMaSuKien());
                detail.setTenSuKien(sk.getTenSuKien());
                detail.setTrangThai(sk.getTrangThai());
                detail.setDoanhThu(doanhThuSK);
                detail.setSoVeBan((int) soVeHopLe);
                detail.setAnhBiaUrl(sk.getAnhBiaUrl());
                chiTiet.add(detail);
            }

            // Tính toán tăng trưởng doanh thu theo ngày hoặc tháng
            List<DonMua> donMuaList = donMuaRepository.findBySuKien_NguoiTao_MaTaiKhoan(userId);
            List<DonMua> donMuaHopLe = donMuaList.stream()
                    .filter(d -> d.getThoiDiemMua() != null && "Đã thanh toán".equals(d.getTrangThaiThanhToan()))
                    .collect(Collectors.toList());

            List<RevenueResponseDto.RevenueGrowthDetail> tangTruong = new ArrayList<>();

            if (!donMuaHopLe.isEmpty()) {
                // Sắp xếp các đơn hàng theo thời gian
                donMuaHopLe.sort(java.util.Comparator.comparing(DonMua::getThoiDiemMua));

                // Lấy tất cả các tháng duy nhất (format: yyyy-MM) để đếm
                java.util.Set<String> uniqueMonths = donMuaHopLe.stream()
                        .map(d -> d.getThoiDiemMua().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM")))
                        .collect(Collectors.toSet());

                boolean groupByDay = uniqueMonths.size() <= 2;

                if (groupByDay) {
                    // Gom nhóm theo ngày
                    java.util.Map<java.time.LocalDate, BigDecimal> mapByDay = new java.util.TreeMap<>();
                    for (DonMua dm : donMuaHopLe) {
                        java.time.LocalDate dateKey = dm.getThoiDiemMua().toLocalDate();
                        mapByDay.put(dateKey, mapByDay.getOrDefault(dateKey, BigDecimal.ZERO).add(dm.getTongTien()));
                    }
                    
                    java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    for (java.util.Map.Entry<java.time.LocalDate, BigDecimal> entry : mapByDay.entrySet()) {
                        tangTruong.add(new RevenueResponseDto.RevenueGrowthDetail(
                                entry.getKey().format(dtf),
                                entry.getValue()
                        ));
                    }
                } else {
                    // Gom nhóm theo tháng
                    java.util.Map<java.time.YearMonth, BigDecimal> mapByMonth = new java.util.TreeMap<>();
                    for (DonMua dm : donMuaHopLe) {
                        java.time.YearMonth monthKey = java.time.YearMonth.from(dm.getThoiDiemMua());
                        mapByMonth.put(monthKey, mapByMonth.getOrDefault(monthKey, BigDecimal.ZERO).add(dm.getTongTien()));
                    }

                    java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("MM/yyyy");
                    for (java.util.Map.Entry<java.time.YearMonth, BigDecimal> entry : mapByMonth.entrySet()) {
                        tangTruong.add(new RevenueResponseDto.RevenueGrowthDetail(
                                entry.getKey().format(dtf),
                                entry.getValue()
                        ));
                    }
                }
            }

            RevenueResponseDto response = new RevenueResponseDto();
            response.setTongDoanhThu(tongDoanhThu);
            response.setTongSoVeBan((int) tongSoVeBan);
            response.setTongSoSuKien(suKienList.size());
            response.setChiTietSuKien(chiTiet);
            response.setTangTruongDoanhThu(tangTruong);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                java.util.Map.of("message", "Lỗi tải dữ liệu doanh thu: " + e.getMessage())
            );
        }
    }

}
