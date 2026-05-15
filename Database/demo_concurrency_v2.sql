-- ==========================================
-- SCRIPT DEMO CONCURRENCY V2 - HỖ TRỢ TOGGLE FIX
-- Tác giả: Antigravity AI
-- ==========================================

-- 1. LOST UPDATE
CREATE OR REPLACE PROCEDURE PROC_DEMO_WALLET_PAY(
    p_ma_tai_khoan IN NUMBER,
    p_amount       IN NUMBER,
    p_is_fixed     IN NUMBER DEFAULT 0  -- 0: Lỗi, 1: Fix
)
AS
    v_sodu NUMBER;
    C_SLEEP CONSTANT NUMBER := 7;
BEGIN
    -- Nếu chọn FIX, thiết lập mức cô lập SERIALIZABLE ngay đầu Transaction
    IF p_is_fixed = 1 THEN
        EXECUTE IMMEDIATE 'SET TRANSACTION ISOLATION LEVEL SERIALIZABLE';
    END IF;

    -- Đọc số dư hiện tại
    SELECT SoDu INTO v_sodu FROM VI_CA_NHAN WHERE MaTaiKhoan = p_ma_tai_khoan;

    DBMS_SESSION.SLEEP(C_SLEEP);

    -- Ghi đè số dư mới
    UPDATE VI_CA_NHAN SET SoDu = v_sodu - p_amount WHERE MaTaiKhoan = p_ma_tai_khoan;
    
    -- Lưu ý: Nếu p_is_fixed = 1 và có TX khác đã update trong lúc sleep, 
    -- Oracle sẽ quăng lỗi ORA-08177 tại câu lệnh UPDATE này.
END;
/

CREATE OR REPLACE PROCEDURE PROC_DEMO_WALLET_RECEIVE(
    p_ma_tai_khoan IN NUMBER,
    p_amount       IN NUMBER,
    p_is_fixed     IN NUMBER DEFAULT 0
)
AS
    v_sodu NUMBER;
    C_SLEEP CONSTANT NUMBER := 7;
BEGIN
    IF p_is_fixed = 1 THEN
        EXECUTE IMMEDIATE 'SET TRANSACTION ISOLATION LEVEL SERIALIZABLE';
    END IF;

    SELECT SoDu INTO v_sodu FROM VI_CA_NHAN WHERE MaTaiKhoan = p_ma_tai_khoan;
    DBMS_SESSION.SLEEP(C_SLEEP);
    UPDATE VI_CA_NHAN SET SoDu = v_sodu + p_amount WHERE MaTaiKhoan = p_ma_tai_khoan;
END;
/

-- 2. NON-REPEATABLE READ
CREATE OR REPLACE FUNCTION FUNC_DEMO_NRR_TICKET_PRICE(
    p_ma_ve    IN NUMBER,
    p_is_fixed IN NUMBER DEFAULT 0
) RETURN NUMBER
AS
    v_price_1 NUMBER;
    v_price_2 NUMBER;
    C_SLEEP   CONSTANT NUMBER := 7;
BEGIN
    IF p_is_fixed = 1 THEN
        -- Lưu ý: SET TRANSACTION trong Function chỉ hoạt động nếu TX chưa bắt đầu các lệnh DML/SELECT trước đó.
        EXECUTE IMMEDIATE 'SET TRANSACTION ISOLATION LEVEL SERIALIZABLE';
    END IF;

    SELECT GiaBanLai INTO v_price_1 FROM VE WHERE MaVe = p_ma_ve;
    DBMS_SESSION.SLEEP(C_SLEEP);
    SELECT GiaBanLai INTO v_price_2 FROM VE WHERE MaVe = p_ma_ve;

    -- Nếu fix thành công, v_price_2 sẽ LUÔN BẰNG v_price_1 kể cả khi TX khác đã commit thay đổi.
    RETURN v_price_2;
END;
/

-- 3. DEADLOCK (Bổ sung procedure đang thiếu)
CREATE OR REPLACE PROCEDURE PROC_DEMO_DEADLOCK_SEATS(
    p_ma_ghe_1 IN NUMBER,
    p_ma_ghe_2 IN NUMBER,
    p_is_fixed IN NUMBER DEFAULT 0
)
AS
    C_SLEEP CONSTANT NUMBER := 5;
BEGIN
    IF p_is_fixed = 1 THEN
        EXECUTE IMMEDIATE 'SET TRANSACTION ISOLATION LEVEL SERIALIZABLE';
    END IF;

    -- Bước 1: Khóa ghế 1 (X-Lock)
    UPDATE TRANG_THAI_GHE_THEO_SUAT SET TrangThai = TrangThai WHERE MaGhe = p_ma_ghe_1;

    DBMS_SESSION.SLEEP(C_SLEEP);

    -- Bước 2: Khóa ghế 2
    UPDATE TRANG_THAI_GHE_THEO_SUAT SET TrangThai = TrangThai WHERE MaGhe = p_ma_ghe_2;
END;
/

-- 4. PHANTOM READ
CREATE OR REPLACE PROCEDURE PROC_GET_EVENT_TICKET_STATS(
    p_ma_su_kien  IN  NUMBER,
    p_is_fixed    IN  NUMBER DEFAULT 0,
    p_so_ve_1     OUT NUMBER,
    p_doanh_thu_1 OUT NUMBER,
    p_so_ve_2     OUT NUMBER,
    p_doanh_thu_2 OUT NUMBER
)
AS
    C_SLEEP CONSTANT NUMBER := 7;
BEGIN
    IF p_is_fixed = 1 THEN
        EXECUTE IMMEDIATE 'SET TRANSACTION ISOLATION LEVEL SERIALIZABLE';
    END IF;

    -- Lần đọc 1
    SELECT COUNT(v.MaVe), NVL(SUM(hv.GiaNiemYet), 0) INTO p_so_ve_1, p_doanh_thu_1
    FROM VE v JOIN HANG_VE hv ON v.MaHangVe = hv.MaHangVe JOIN DON_MUA dm ON v.MaDonMua = dm.MaDonMua
    WHERE hv.MaSuKien = p_ma_su_kien AND v.TrangThaiVe IN ('Hiệu lực', 'Đã Check-in');

    DBMS_SESSION.SLEEP(C_SLEEP);

    -- Lần đọc 2
    SELECT COUNT(v.MaVe), NVL(SUM(hv.GiaNiemYet), 0) INTO p_so_ve_2, p_doanh_thu_2
    FROM VE v JOIN HANG_VE hv ON v.MaHangVe = hv.MaHangVe JOIN DON_MUA dm ON v.MaDonMua = dm.MaDonMua
    WHERE hv.MaSuKien = p_ma_su_kien AND v.TrangThaiVe IN ('Hiệu lực', 'Đã Check-in');
END;
/
