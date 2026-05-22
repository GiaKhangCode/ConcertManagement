-- ====================================================================
-- TÊN FILE: create_analytics_views.sql
-- MỤC ĐÍCH: Tạo các View phục vụ truy vấn dữ liệu thô vẽ biểu đồ Admin
-- CƠ SỞ DỮ LIỆU: Oracle Database
-- LƯU Ý QUAN TRỌNG: 
-- 1. Nếu gặp lỗi "ORA-01031: insufficient privileges", bạn cần chạy
--    "GRANT CREATE VIEW TO <TEN_USER>;" bằng tài khoản admin (SYS/SYSTEM) trước.
-- 2. View chỉ select dữ liệu thô, không tính toán nghiệp vụ (5% phí nền tảng),
--    logic tính toán này được thực hiện tại tầng ứng dụng khi plot biểu đồ.
-- ====================================================================

-- 1. View phục vụ Biểu đồ "Doanh thu theo thể loại" (Doughnut Chart)
CREATE OR REPLACE VIEW V_ANALYTICS_CATEGORY AS
SELECT
    sk.MaSuKien,
    NVL(TRIM(sk.PhanLoai), N'Khác') AS THE_LOAI,
    NVL(dm_gross.GROSS_REVENUE, 0)   AS GROSS_REVENUE,
    NVL(ht.TONG_HOAN,           0)   AS TONG_HOAN_TRA
FROM SU_KIEN sk
LEFT JOIN (
    SELECT MaSuKien, SUM(TongTien) AS GROSS_REVENUE
    FROM DON_MUA
    WHERE TrangThaiThanhToan IN (N'Đã thanh toán', N'Đã hủy', N'Đã hoàn tiền')
    GROUP BY MaSuKien
) dm_gross ON sk.MaSuKien = dm_gross.MaSuKien
LEFT JOIN (
    SELECT MaSuKien, SUM(SoTienHoan) AS TONG_HOAN
    FROM LICH_SU_HOAN_TIEN
    GROUP BY MaSuKien
) ht ON sk.MaSuKien = ht.MaSuKien;

-- 2. View phục vụ Biểu đồ "Tăng trưởng doanh thu theo thời gian" (Line Chart)
CREATE OR REPLACE VIEW V_ANALYTICS_GROWTH AS
SELECT
    TRUNC(dm.ThoiDiemMua) AS NGAY_PHAT_SINH,
    TO_CHAR(dm.ThoiDiemMua, 'MM/YYYY') AS THANG_NAM,
    TO_CHAR(TRUNC(dm.ThoiDiemMua), 'DD/MM/YYYY') AS NGAY_THANG_NAM,
    dm.MaSuKien,
    dm.TongTien AS GROSS_TIEN,
    0 AS SO_TIEN_HOAN
FROM DON_MUA dm
WHERE dm.TrangThaiThanhToan IN (N'Đã thanh toán', N'Đã hủy', N'Đã hoàn tiền')
  AND dm.ThoiDiemMua IS NOT NULL
UNION ALL
SELECT
    TRUNC(ht.ThoiDiemHoan) AS NGAY_PHAT_SINH,
    TO_CHAR(ht.ThoiDiemHoan, 'MM/YYYY') AS THANG_NAM,
    TO_CHAR(TRUNC(ht.ThoiDiemHoan), 'DD/MM/YYYY') AS NGAY_THANG_NAM,
    ht.MaSuKien,
    0 AS GROSS_TIEN,
    ht.SoTienHoan
FROM LICH_SU_HOAN_TIEN ht
WHERE ht.ThoiDiemHoan IS NOT NULL;

-- 3. View phục vụ Biểu đồ "Top nhà tổ chức theo doanh thu" (Horizontal Bar Chart)
CREATE OR REPLACE VIEW V_ANALYTICS_ORGANIZER AS
SELECT
    sk.MaSuKien,
    sk.TenSuKien,
    CASE
        WHEN sk.MaNguoiTao IS NULL THEN N'Hệ thống'
        ELSE NVL(ntc.TenNhaToChuc, tk.TenDangNhap)
    END AS TEN_NHA_TO_CHUC,
    NVL(dm_gross.GROSS_REVENUE, 0) AS GROSS_REVENUE,
    NVL(ht.TONG_HOAN,           0) AS TONG_HOAN_TRA
FROM SU_KIEN sk
LEFT JOIN TAI_KHOAN tk ON sk.MaNguoiTao = tk.MaTaiKhoan
LEFT JOIN NHA_TO_CHUC ntc ON tk.MaTaiKhoan = ntc.MaTaiKhoan
LEFT JOIN (
    SELECT MaSuKien, SUM(TongTien) AS GROSS_REVENUE
    FROM DON_MUA
    WHERE TrangThaiThanhToan IN (N'Đã thanh toán', N'Đã hủy', N'Đã hoàn tiền')
    GROUP BY MaSuKien
) dm_gross ON sk.MaSuKien = dm_gross.MaSuKien
LEFT JOIN (
    SELECT MaSuKien, SUM(SoTienHoan) AS TONG_HOAN
    FROM LICH_SU_HOAN_TIEN
    GROUP BY MaSuKien
) ht ON sk.MaSuKien = ht.MaSuKien;
