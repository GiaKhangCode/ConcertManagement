package com.stellar.backend.service;

import com.stellar.backend.entity.DonMua;
import com.stellar.backend.entity.SuKien;
import com.stellar.backend.repository.DonMuaRepository;
import com.stellar.backend.repository.SuKienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SettlementService {
    @Autowired
    private DonMuaRepository donMuaRepository;

    @Autowired
    private SuKienRepository suKienRepository;

    public Map<String, Object> getEventSettlement(Long eventId) {
        SuKien sk = suKienRepository.findById(eventId).orElseThrow(() -> new RuntimeException("Sự kiện không tồn tại"));
        List<DonMua> orders = donMuaRepository.findBySuKien_MaSuKien(eventId);

        BigDecimal grossRevenue = orders.stream()
                .filter(o -> "Đã thanh toán".equals(o.getTrangThaiThanhToan()))
                .map(DonMua::getTongTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Giả sử phí nền tảng là 5%
        BigDecimal platformFeeRate = new BigDecimal("0.05");
        BigDecimal platformFee = grossRevenue.multiply(platformFeeRate);

        // Giả sử phí cổng thanh toán là 2%
        BigDecimal gatewayFeeRate = new BigDecimal("0.02");
        BigDecimal gatewayFee = grossRevenue.multiply(gatewayFeeRate);

        BigDecimal netRevenue = grossRevenue.subtract(platformFee).subtract(gatewayFee);

        Map<String, Object> report = new HashMap<>();
        report.put("eventId", sk.getMaSuKien());
        report.put("eventName", sk.getTenSuKien());
        report.put("grossRevenue", grossRevenue);
        report.put("platformFee", platformFee);
        report.put("gatewayFee", gatewayFee);
        report.put("netRevenue", netRevenue);
        report.put("orderCount", orders.size());

        return report;
    }
}
