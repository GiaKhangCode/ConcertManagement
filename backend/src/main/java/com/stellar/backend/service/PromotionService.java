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
    private ApDungRepository apDungRepository;

    public MaGiamGia validateCode(String code, Long maSuKien) {
        Optional<MaGiamGia> mggOpt = maGiamGiaRepository.findByMaGiamGia(code);
        
        if (mggOpt.isEmpty()) {
            throw new RuntimeException("Mã giảm giá không tồn tại!");
        }

        MaGiamGia mgg = mggOpt.get();
        ChienDichKhuyenMai chienDich = mgg.getChienDich();

        // Kiểm tra sự kiện
        if (!chienDich.getSuKien().getMaSuKien().equals(maSuKien)) {
            throw new RuntimeException("Mã giảm giá không áp dụng cho sự kiện này!");
        }

        // Kiểm tra thời gian chiến dịch
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(chienDich.getThoiDiemBD())) {
            throw new RuntimeException("Chiến dịch khuyến mãi chưa bắt đầu!");
        }
        if (now.isAfter(chienDich.getThoiDiemKT())) {
            throw new RuntimeException("Chiến dịch khuyến mãi đã kết thúc!");
        }

        // Kiểm tra trạng thái chiến dịch
        if (!"Đang diễn ra".equals(chienDich.getTrangThai())) {
            throw new RuntimeException("Chiến dịch khuyến mãi hiện đang " + chienDich.getTrangThai() + "!");
        }

        // Kiểm tra lượt dùng
        if (mgg.getSoLuotDaSuDung() >= mgg.getLuotDungToiDa()) {
            throw new RuntimeException("Mã giảm giá đã hết lượt sử dụng!");
        }

        return mgg;
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
