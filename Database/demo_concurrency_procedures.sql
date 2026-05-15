
-- ==========================================
-- SCRIPT TẠO STORED PROCEDURES DEMO LỖI CONCURRENCY (ORACLE)
-- Tác giả: Antigravity AI
-- Hướng dẫn sử dụng: Chạy toàn bộ script này trong Oracle SQL Developer / SQL*Plus.
-- Lưu ý: Cần cấp quyền DBMS_SESSION cho User:
--   GRANT EXECUTE ON DBMS_SESSION TO <YOUR_USER>;
-- ==========================================
-- Thời gian SLEEP được FIX CỨNG trong mỗi procedure:
--   - Lost Update / Non-Repeatable Read:  SLEEP 7 giây
--   - Phantom Read:                       SLEEP 7 giây
--   - Deadlock:                           SLEEP 5 giây
-- ==========================================

-- ============================================================
-- 1. LOST UPDATE
--    Kịch bản: 2 giao dịch cùng đọc số dư ví, cùng trừ tiền
--    mà không dùng SELECT FOR UPDATE → số dư cuối bị sai.
--
--    Cách demo:
--      Tab A: EXEC PROC_DEMO_WALLET_PAY(maTK, soTienA);
--      Tab B: EXEC PROC_DEMO_WALLET_PAY(maTK, soTienB);  -- chạy ngay sau tab A
--    Kết quả: Số dư cuối chỉ trừ 1 lần thay vì 2 lần.
-- ============================================================
CREATE OR REPLACE PROCEDURE PROC_DEMO_WALLET_PAY(
    p_ma_tai_khoan IN NUMBER,
    p_amount       IN NUMBER
)
AS
    v_sodu NUMBER;
    C_SLEEP CONSTANT NUMBER := 7;   -- FIX CỨNG 7 giây
BEGIN
--     SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;
    -- Đọc số dư hiện tại (Snapshot "dirty" ở READ COMMITTED)
    SELECT SoDu INTO v_sodu
    FROM VI_CA_NHAN
    WHERE MaTaiKhoan = p_ma_tai_khoan;

    -- Giả lập độ trễ xử lý (để TX khác kịp đọc cùng giá trị)
    DBMS_SESSION.SLEEP(C_SLEEP);

    -- Ghi đè số dư mới → Lost Update nếu có TX khác đã COMMIT
    UPDATE VI_CA_NHAN
    SET SoDu = v_sodu - p_amount
    WHERE MaTaiKhoan = p_ma_tai_khoan;
    -- Không COMMIT ở đây — Spring @Transactional quản lý commit
END;
/

CREATE OR REPLACE PROCEDURE PROC_DEMO_WALLET_RECEIVE(
    p_ma_tai_khoan IN NUMBER,
    p_amount       IN NUMBER
)
AS
    v_sodu NUMBER;
    C_SLEEP CONSTANT NUMBER := 7;   -- FIX CỨNG 7 giây
BEGIN
    SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;
    -- Đọc số dư hiện tại
    SELECT SoDu INTO v_sodu
    FROM VI_CA_NHAN
    WHERE MaTaiKhoan = p_ma_tai_khoan;

    -- Giả lập độ trễ
    DBMS_SESSION.SLEEP(C_SLEEP);

    -- Ghi đè số dư mới
    UPDATE VI_CA_NHAN
    SET SoDu = v_sodu + p_amount
    WHERE MaTaiKhoan = p_ma_tai_khoan;
    -- Không COMMIT ở đây — Spring @Transactional quản lý commit
END;
/


-- ============================================================
-- 2. NON-REPEATABLE READ
--    Kịch bản: T1 đọc giá bán lại vé (lần 1), bị trễ.
--    Trong thời gian đó T2 cập nhật lại giá vé.
--    T1 đọc lần 2 → giá đã thay đổi → khác lần đọc 1.
--
--    Cách demo:
--      Tab A: SELECT FUNC_DEMO_NRR_TICKET_PRICE(<maVe>) FROM DUAL;
--      Tab B: Trong lúc Tab A đang sleep, UPDATE VE SET GiaBanLai=<giaMoi>
--             WHERE MaVe=<maVe>; COMMIT;
--    Kết quả: Hàm trả về giá lần 2 (đã bị thay đổi bởi Tab B).
-- ============================================================
CREATE OR REPLACE FUNCTION FUNC_DEMO_NRR_TICKET_PRICE(
    p_ma_ve IN NUMBER
) RETURN NUMBER
AS
    v_price_1 NUMBER;
    v_price_2 NUMBER;
    C_SLEEP   CONSTANT NUMBER := 7; -- FIX CỨNG 7 giây
BEGIN
--     SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;
    -- Lần đọc 1
    SELECT GiaBanLai INTO v_price_1
    FROM VE
    WHERE MaVe = p_ma_ve;

    DBMS_SESSION.SLEEP(C_SLEEP);

    -- Lần đọc 2: Nếu có lỗi Non-Repeatable Read, v_price_2 ≠ v_price_1
    SELECT GiaBanLai INTO v_price_2
    FROM VE
    WHERE MaVe = p_ma_ve;

    -- Log để quan sát
    DBMS_OUTPUT.PUT_LINE('Gia lan 1: ' || v_price_1 || ' | Gia lan 2: ' || v_price_2);

    RETURN v_price_2;
END;
/


-- ============================================================
-- 4. PHANTOM READ - Lấy thống kê vé bán của sự kiện
--    Kịch bản:
--      T1 (Hệ thống tổng hợp báo cáo) đọc lần 1 → đếm được N vé + doanh thu R1.
--      T2 (Người dùng khác) mua thêm 1 vé → INSERT + COMMIT trong lúc T1 đang xử lý.
--      T1 đọc lần 2 → thấy N+1 vé + doanh thu R2 (phantom row).
--    → Số vé hiển thị trên UI (lấy từ lần 1) không khớp với doanh thu (lấy từ lần 2).
--
--    Cách chạy:
--      Tab A: Mở trang quản lý → click vào tên sự kiện → hệ thống gọi procedure này.
--      Tab B: Trong khi Tab A đang tải (~7 giây), vào trang đặt vé và mua thêm 1 vé → thanh toán.
--    Kết quả: Popup chi tiết hiển thị số vé ≠ số vé tương ứng với doanh thu.
-- ============================================================
CREATE OR REPLACE PROCEDURE PROC_GET_EVENT_TICKET_STATS(
    p_ma_su_kien IN  NUMBER,
    p_so_ve_1    OUT NUMBER,
    p_doanh_thu_1 OUT NUMBER,
    p_so_ve_2    OUT NUMBER,
    p_doanh_thu_2 OUT NUMBER
)
AS
    C_SLEEP CONSTANT NUMBER := 7; -- 7 giây đọc giữa 2 lần
BEGIN
    -- Lần đọc 1: Đếm vé đã bán hợp lệ + tổng doanh thu
    SELECT COUNT(v.MaVe),
           NVL(SUM(hv.GiaNiemYet), 0)
    INTO   p_so_ve_1, p_doanh_thu_1
    FROM   VE v
    JOIN   HANG_VE hv ON v.MaHangVe = hv.MaHangVe
    JOIN   DON_MUA dm ON v.MaDonMua = dm.MaDonMua
    WHERE  hv.MaSuKien = p_ma_su_kien
      AND  v.TrangThaiVe IN ('Hiệu lực', 'Đã Check-in');

    -- Giữ kết nối mở để xử lý nghiệp vụ khác (ví dụ: tính toán phí, tổng hợp báo cáo...)
    DBMS_SESSION.SLEEP(C_SLEEP);

    -- Lần đọc 2: Tổng hợp lại để đảm bảo tính nhất quán
    SELECT COUNT(v.MaVe),
           NVL(SUM(hv.GiaNiemYet), 0)
    INTO   p_so_ve_2, p_doanh_thu_2
    FROM   VE v
    JOIN   HANG_VE hv ON v.MaHangVe = hv.MaHangVe
    JOIN   DON_MUA dm ON v.MaDonMua = dm.MaDonMua
    WHERE  hv.MaSuKien = p_ma_su_kien
      AND  v.TrangThaiVe IN ('Hiệu lực', 'Đã Check-in');
END;
/


-- ============================================================
-- PHẦN BÁO CÁO: Chạy sau mỗi demo để xem kết quả
-- ============================================================

-- Kiểm tra số dư ví sau demo Lost Update
-- SELECT MaTaiKhoan, SoDu FROM VI_CA_NHAN WHERE MaTaiKhoan = <maTK>;

-- Kiểm tra ghế sau demo Deadlock
-- SELECT MaGhe, TrangThai FROM TRANG_THAI_GHE_THEO_SUAT
-- WHERE MaGhe IN (<maGheA>, <maGheB>);

-- Kiểm tra vé bán lại sau demo Phantom Read
-- SELECT MaVe, DaBanLai, GiaBanLai FROM VE
-- JOIN HANG_VE ON VE.MaHangVe = HANG_VE.MaHangVe
-- WHERE HANG_VE.MaSuKien = <maSuKien> AND DaBanLai = 1;