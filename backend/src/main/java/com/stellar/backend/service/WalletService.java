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

    /**
     * Thanh toán từ ví — delay xảy ra BÊN TRONG Oracle procedure.
     *
     * Flow:
     *   1. Kiểm tra số dư trước
     *   2. Gọi PROC_DEMO_WALLET_PAY → Oracle: đọc SoDu, DBMS_SESSION.SLEEP(7s), UPDATE SoDu
     *   3. Spring @Transactional commit sau khi method hoàn tất
     *
     * Demo Lost Update:
     *   Tab A gọi pay() → procedure đọc SoDu=X, ngủ 7s ở DB
     *   Tab B gọi pay() cùng lúc → đọc SoDu=X (TX A chưa commit)
     *   TX A thức dậy → UPDATE SoDu=X-a → commit
     *   TX B thức dậy → UPDATE SoDu=X-b (mất đi phần trừ của TX A) → Lost Update!
     */
    @Transactional
    public void pay(Long userId, BigDecimal amount, String content) {
        // Kiểm tra số dư trước khi gọi procedure
        ViCaNhan vi = getWalletByUserId(userId);
        if (vi == null) throw new RuntimeException("Ví không tồn tại");
        if (vi.getSoDu().compareTo(amount) < 0) throw new RuntimeException("Số dư không đủ");

        // Gọi Oracle procedure — delay DBMS_SESSION.SLEEP(7) xảy ra ở DB
        // Procedure đọc SoDu, sleep, UPDATE SoDu (không COMMIT — Spring quản lý)
        viCaNhanRepository.demoPay(userId, amount);

        // Log giao dịch (sau khi procedure đã UPDATE xong)
        // Refresh entity để lấy SoDu mới nhất từ DB
        viCaNhanRepository.flush();
        ViCaNhan viAfter = getWalletByUserId(userId);

        LichSuBienDongVi gd = new LichSuBienDongVi();
        gd.setViCaNhan(viAfter != null ? viAfter : vi);
        gd.setLoaiBienDong("Giảm");
        gd.setSoTien(amount);
        gd.setNoiDung(content != null ? content : "Thanh toán dịch vụ");
        lichSuBienDongViRepository.save(gd);
    }

    /**
     * Nhận tiền vào ví — delay xảy ra BÊN TRONG Oracle procedure.
     *
     * Gọi PROC_DEMO_WALLET_RECEIVE → Oracle: đọc SoDu, DBMS_SESSION.SLEEP(7s), UPDATE SoDu.
     * Spring @Transactional commit sau khi method hoàn tất.
     *
     * Demo Lost Update (hoàn vé):
     *   Tab A: ấn "Hoàn vé" → procedure đọc SoDu=X, ngủ 7s ở DB
     *   Tab B: cùng hành động → đọc SoDu=X (TX A chưa commit)
     *   TX A commit → SoDu=X+a
     *   TX B commit → SoDu=X+b (mất đi phần cộng của TX A) → Lost Update!
     */
    @Transactional
    public void receive(Long userId, BigDecimal amount, String content) {
        // Kiểm tra ví tồn tại
        ViCaNhan vi = getWalletByUserId(userId);
        if (vi == null) throw new RuntimeException("Ví không tồn tại");

        // Gọi Oracle procedure — delay DBMS_SESSION.SLEEP(7) xảy ra ở DB
        viCaNhanRepository.demoReceive(userId, amount);

        // Log giao dịch
        viCaNhanRepository.flush();
        ViCaNhan viAfter = getWalletByUserId(userId);

        LichSuBienDongVi gd = new LichSuBienDongVi();
        gd.setViCaNhan(viAfter != null ? viAfter : vi);
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
