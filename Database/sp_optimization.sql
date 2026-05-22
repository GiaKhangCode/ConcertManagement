-- File: sp_optimization.sql
-- Áp dụng 4 đề xuất tối ưu hóa (Stored Procedures)

-- ==============================================================================
-- 1. SP Tạo Vé Hàng Loạt (Dành cho BookingService)
-- Tối ưu hóa việc INSERT nhiều vé và UPDATE trạng thái ghế tránh N+1 Query
-- ==============================================================================
CREATE OR REPLACE PROCEDURE SP_TAO_VE_HANG_LOAT (
    p_MaDonMua IN NUMBER,
    p_MaTaiKhoan IN NUMBER,
    p_MaSuKien IN NUMBER,
    p_MaLichDien IN NUMBER,
    p_MaHangVe IN NUMBER,
    p_MaKhuVuc IN NUMBER,
    p_SoLuong IN NUMBER,
    p_IsSkipSeatCheck IN NUMBER,
    p_TrangThaiVe IN NVARCHAR2,
    p_TrangThaiGhe IN NVARCHAR2
)
IS
    v_MaLichDien_Dung NUMBER;
    v_MaKhuVuc_Dung NUMBER;
    v_Count NUMBER := 0;
BEGIN
    -- Lấy suất diễn hợp lệ
    IF p_MaLichDien IS NULL THEN
        SELECT MIN(MaLichDien) INTO v_MaLichDien_Dung FROM LICH_DIEN WHERE MaSuKien = p_MaSuKien;
    ELSE
        v_MaLichDien_Dung := p_MaLichDien;
    END IF;

    IF p_IsSkipSeatCheck = 1 THEN
        -- Vé tự do (không ghế cố định)
        IF p_MaKhuVuc IS NULL THEN
            SELECT MIN(MaKhuVuc) INTO v_MaKhuVuc_Dung FROM KHU_VUC WHERE MaHangVe = p_MaHangVe;
        ELSE
            v_MaKhuVuc_Dung := p_MaKhuVuc;
        END IF;

        FOR i IN 1..p_SoLuong LOOP
            INSERT INTO VE (MaDonMua, MaHangVe, DaBanLai, TrangThaiVe, MaKhuVuc, MaLichDien)
            VALUES (p_MaDonMua, p_MaHangVe, 0, NVL(p_TrangThaiVe, N'Hiệu lực'), v_MaKhuVuc_Dung, v_MaLichDien_Dung);
        END LOOP;
    ELSE
        -- Khách mua ghế đã giữ chỗ
        FOR rec IN (
            SELECT MaGhe, MaLichDien, (SELECT MaKhuVuc FROM GHE_NGOI WHERE MaGhe = tt.MaGhe) AS MaKV
            FROM TRANG_THAI_GHE_THEO_SUAT tt
            WHERE MaLichDien = v_MaLichDien_Dung 
              AND TaiKhoan = p_MaTaiKhoan 
              AND TrangThai = N'Đang giữ chỗ'
        ) LOOP
            IF v_Count < p_SoLuong THEN
                -- INSERT VE
                INSERT INTO VE (MaDonMua, MaHangVe, DaBanLai, TrangThaiVe, MaGhe, MaKhuVuc, MaLichDien)
                VALUES (p_MaDonMua, p_MaHangVe, 0, NVL(p_TrangThaiVe, N'Hiệu lực'), rec.MaGhe, rec.MaKV, rec.MaLichDien);

                -- UPDATE TRANG_THAI_GHE
                UPDATE TRANG_THAI_GHE_THEO_SUAT
                SET TrangThai = NVL(p_TrangThaiGhe, N'Đã đặt'), 
                    ThoiGianHetHan = CASE WHEN p_TrangThaiGhe = N'Đang giữ chỗ' THEN ThoiGianHetHan ELSE NULL END
                WHERE MaGhe = rec.MaGhe AND MaLichDien = rec.MaLichDien;
                
                v_Count := v_Count + 1;
            END IF;
        END LOOP;
    END IF;
END;
/

-- ==============================================================================
-- 2. SP Hoàn Vé Cho Đơn Mua (Dành cho TicketService)
-- Cập nhật đồng loạt trạng thái Vé -> Đã hoàn vé và Ghế -> Còn trống
-- ==============================================================================
CREATE OR REPLACE PROCEDURE SP_HOAN_VE_DON_MUA (
    p_MaDonMua IN NUMBER
)
IS
BEGIN
    -- Nhả ghế
    UPDATE TRANG_THAI_GHE_THEO_SUAT
    SET TrangThai = N'Còn trống', TaiKhoan = NULL, ThoiGianHetHan = NULL
    WHERE (MaGhe, MaLichDien) IN (
        SELECT MaGhe, MaLichDien FROM VE WHERE MaDonMua = p_MaDonMua AND MaGhe IS NOT NULL
    );

    -- Hủy vé
    UPDATE VE 
    SET TrangThaiVe = N'Đã hoàn vé' 
    WHERE MaDonMua = p_MaDonMua;
END;
/

-- ==============================================================================
-- 3. SP Dọn Dẹp Ghế Hết Hạn (Dành cho SeatService unlockExpiredSeats)
-- ==============================================================================
CREATE OR REPLACE PROCEDURE SP_UNLOCK_EXPIRED_SEATS
IS
BEGIN
    UPDATE TRANG_THAI_GHE_THEO_SUAT
    SET TrangThai = N'Còn trống', TaiKhoan = NULL, ThoiGianHetHan = NULL
    WHERE TrangThai = N'Đang giữ chỗ' 
      AND ThoiGianHetHan < CURRENT_TIMESTAMP;
END;
/

