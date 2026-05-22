package com.stellar.backend.service;

import com.stellar.backend.entity.*;
import com.stellar.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PromotionService {

    @Autowired
    private MaGiamGiaRepository maGiamGiaRepository;

    @Autowired
    private ChienDichKhuyenMaiRepository chienDichRepository;

    @Autowired
    private ApDungRepository apDungRepository;

    @org.springframework.scheduling.annotation.Scheduled(cron = "0 * * * * *")
    @Transactional
    public void updatePromotionStatuses() {
        LocalDateTime now = LocalDateTime.now();

        // 1. Chuyển "Sắp diễn ra" thành "Đang diễn ra"
        java.util.List<ChienDichKhuyenMai> upcoming = chienDichRepository.findByTrangThai("Chưa diễn ra");
        for (ChienDichKhuyenMai cd : upcoming) {
            if (cd.getThoiDiemBD() != null && !now.isBefore(cd.getThoiDiemBD())) {
                cd.setTrangThai("Đang diễn ra");
                chienDichRepository.save(cd);
            }
        }

        // 2. Chuyển "Đang diễn ra" thành "Đã kết thúc"
        java.util.List<ChienDichKhuyenMai> ongoing = chienDichRepository.findByTrangThai("Đang diễn ra");
        for (ChienDichKhuyenMai cd : ongoing) {
            if (cd.getThoiDiemKT() != null && !now.isBefore(cd.getThoiDiemKT())) {
                cd.setTrangThai("Đã kết thúc");
                chienDichRepository.save(cd);
            }
        }
    }

    public MaGiamGia validateCode(String code, Long maSuKien) {
        Integer resultCode = maGiamGiaRepository.callFnCheckMaGiamGiaHopLe(code, maSuKien);

        if (resultCode == null) {
            throw new RuntimeException("Lỗi hệ thống khi xác minh mã giảm giá!");
        }

        switch (resultCode) {
            case 1:
                return maGiamGiaRepository.findByMaGiamGia(code)
                    .orElseThrow(() -> new RuntimeException("Mã giảm giá không tồn tại!"));
            case -1:
                throw new RuntimeException("Chiến dịch khuyến mãi chưa bắt đầu!");
            case -2:
                throw new RuntimeException("Chiến dịch khuyến mãi đã kết thúc!");
            case -3:
                throw new RuntimeException("Mã giảm giá đã hết lượt sử dụng!");
            case -4:
                throw new RuntimeException("Mã giảm giá không áp dụng cho sự kiện này!");
            case 0:
                throw new RuntimeException("Chiến dịch khuyến mãi hiện đang tạm dừng hoặc bị hủy!");
            default:
                throw new RuntimeException("Mã giảm giá không hợp lệ (Mã lỗi: " + resultCode + ")");
        }
    }

    public BigDecimal calculateDiscountAmount(MaGiamGia mgg, BigDecimal totalAmount) {
        BigDecimal discount = BigDecimal.ZERO;

        if ("Theo phần trăm".equals(mgg.getLoaiGiam())) {
            // Giảm theo %: (Phần trăm / 100) * Tổng tiền
            discount = totalAmount.multiply(mgg.getSoLuongGiam()).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
            
            // Không vượt quá mức giảm tối đa
            if (mgg.getGiamToiDa() != null && mgg.getGiamToiDa().compareTo(BigDecimal.ZERO) > 0) {
                if (discount.compareTo(mgg.getGiamToiDa()) > 0) {
                    discount = mgg.getGiamToiDa();
                }
            }
        } else if ("Số tiền cố định".equals(mgg.getLoaiGiam())) {
            discount = mgg.getSoLuongGiam();
        }

        // Số tiền giảm không được lớn hơn tổng tiền
        if (discount.compareTo(totalAmount) > 0) {
            discount = totalAmount;
        }

        return discount;
    }

    @Transactional
    public void applyPromotion(DonMua donMua, String code, BigDecimal discountAmount) {
        MaGiamGia mgg = maGiamGiaRepository.findByMaGiamGia(code)
                .orElseThrow(() -> new RuntimeException("Mã giảm giá không tồn tại!"));

        // Tạo bản ghi ApDung
        ApDung apDung = new ApDung();
        apDung.setDonMua(donMua);
        apDung.setMaGiamGia(mgg);
        apDung.setSoTienGiamThucTe(discountAmount);
        apDungRepository.save(apDung);

        // Cập nhật lượt dùng
        mgg.setSoLuotDaSuDung(mgg.getSoLuotDaSuDung() + 1);
        maGiamGiaRepository.save(mgg);
    }
}
