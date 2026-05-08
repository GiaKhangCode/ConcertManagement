package com.stellar.backend.service;

import com.stellar.backend.entity.LichSuBienDongVi;
import com.stellar.backend.entity.ViCaNhan;
import com.stellar.backend.repository.LichSuBienDongViRepository;
import com.stellar.backend.repository.ViCaNhanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class WalletService {
    @Autowired
    private ViCaNhanRepository viCaNhanRepository;

    @Autowired
    private LichSuBienDongViRepository lichSuBienDongViRepository;

    public ViCaNhan getWalletByUserId(Long userId) {
        return viCaNhanRepository.findByTaiKhoan_MaTaiKhoan(userId).orElse(null);
    }

    @Transactional
    public void deposit(Long userId, BigDecimal amount, String content) {
        ViCaNhan vi = getWalletByUserId(userId);
        if (vi == null) throw new RuntimeException("Ví không tồn tại");

        vi.setSoDu(vi.getSoDu().add(amount));
        viCaNhanRepository.save(vi);

        LichSuBienDongVi gd = new LichSuBienDongVi();
        gd.setViCaNhan(vi);
        gd.setLoaiBienDong("Tăng");
        gd.setSoTien(amount);
        gd.setNoiDung(content != null ? content : "Nạp tiền vào ví");
        lichSuBienDongViRepository.save(gd);
    }

    @Transactional
    public void pay(Long userId, BigDecimal amount, String content) {
        ViCaNhan vi = getWalletByUserId(userId);
        if (vi == null) throw new RuntimeException("Ví không tồn tại");
        if (vi.getSoDu().compareTo(amount) < 0) throw new RuntimeException("Số dư không đủ");

        vi.setSoDu(vi.getSoDu().subtract(amount));
        viCaNhanRepository.save(vi);

        LichSuBienDongVi gd = new LichSuBienDongVi();
        gd.setViCaNhan(vi);
        gd.setLoaiBienDong("Giảm");
        gd.setSoTien(amount);
        gd.setNoiDung(content != null ? content : "Thanh toán dịch vụ");
        lichSuBienDongViRepository.save(gd);
    }

    @Transactional
    public void receive(Long userId, BigDecimal amount, String content) {
        ViCaNhan vi = getWalletByUserId(userId);
        if (vi == null) throw new RuntimeException("Ví không tồn tại");

        vi.setSoDu(vi.getSoDu().add(amount));
        viCaNhanRepository.save(vi);

        LichSuBienDongVi gd = new LichSuBienDongVi();
        gd.setViCaNhan(vi);
        gd.setLoaiBienDong("Tăng");
        gd.setSoTien(amount);
        gd.setNoiDung(content != null ? content : "Nhận tiền từ hệ thống");
        lichSuBienDongViRepository.save(gd);
    }

    public List<LichSuBienDongVi> getHistory(Long userId) {
        ViCaNhan vi = getWalletByUserId(userId);
        if (vi == null) return List.of();
        return lichSuBienDongViRepository.findByViCaNhan_MaViOrderByThoiGianDesc(vi.getMaVi());
    }
}
