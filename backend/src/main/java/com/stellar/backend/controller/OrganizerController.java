package com.stellar.backend.controller;

import com.stellar.backend.dto.RevenueResponseDto;
import com.stellar.backend.entity.DonMua;
import com.stellar.backend.entity.SuKien;
import com.stellar.backend.repository.DonMuaRepository;
import com.stellar.backend.repository.SuKienRepository;
import com.stellar.backend.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/organizer")
public class OrganizerController {

    @Autowired
    private SuKienRepository suKienRepository;

    @Autowired
    private DonMuaRepository donMuaRepository;

    /**
     * Lấy báo cáo doanh thu tổng hợp cho nhà tổ chức hiện tại.
     * Chỉ tính doanh thu của các sự kiện mà người đang đăng nhập đã tạo.
     */
    @GetMapping("/revenue")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<?> getRevenue() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getId();

        // Lấy tất cả sự kiện do user này tạo
        List<SuKien> suKienList = suKienRepository.findByNguoiTao_MaTaiKhoan(userId);

        BigDecimal tongDoanhThu = BigDecimal.ZERO;
        int tongSoVeBan = 0;
        List<RevenueResponseDto.EventRevenueDetail> chiTiet = new ArrayList<>();

        for (SuKien sk : suKienList) {
            // Lấy tất cả Đơn Mua của sự kiện này
            List<DonMua> donMuas = donMuaRepository.findBySuKien_MaSuKien(sk.getMaSuKien());

            BigDecimal doanhThuSK = BigDecimal.ZERO;
            int soVeSK = 0;

            for (DonMua dm : donMuas) {
                doanhThuSK = doanhThuSK.add(dm.getTongTien());
                // Tính số vé = tổng tiền / giá hạng vé (gần đúng), hoặc dùng count vé
                soVeSK++;
            }

            tongDoanhThu = tongDoanhThu.add(doanhThuSK);
            tongSoVeBan += soVeSK;

            RevenueResponseDto.EventRevenueDetail detail = new RevenueResponseDto.EventRevenueDetail();
            detail.setMaSuKien(sk.getMaSuKien());
            detail.setTenSuKien(sk.getTenSuKien());
            detail.setTrangThai(sk.getTrangThai());
            detail.setDoanhThu(doanhThuSK);
            detail.setSoVeBan(soVeSK);
            detail.setAnhBiaUrl(sk.getAnhBiaUrl());
            chiTiet.add(detail);
        }

        RevenueResponseDto response = new RevenueResponseDto();
        response.setTongDoanhThu(tongDoanhThu);
        response.setTongSoVeBan(tongSoVeBan);
        response.setTongSoSuKien(suKienList.size());
        response.setChiTietSuKien(chiTiet);

        return ResponseEntity.ok(response);
    }
}
