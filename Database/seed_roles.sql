-- Seed data cho bảng NHOM_QUYEN (System Roles)
-- Chạy script này để khởi tạo 3 nhóm quyền cơ bản cho hệ thống RBAC

-- Kiểm tra và insert nếu chưa tồn tại
INSERT INTO NHOM_QUYEN (TenNhomQuyen, MoTa) 
SELECT 'ROLE_CUSTOMER', 'Khách hàng - Quyền mua vé và xem sự kiện' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM NHOM_QUYEN WHERE TenNhomQuyen = 'ROLE_CUSTOMER');

INSERT INTO NHOM_QUYEN (TenNhomQuyen, MoTa) 
SELECT 'ROLE_ORGANIZER', 'Nhà tổ chức - Quyền tạo sự kiện và xem doanh thu' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM NHOM_QUYEN WHERE TenNhomQuyen = 'ROLE_ORGANIZER');

INSERT INTO NHOM_QUYEN (TenNhomQuyen, MoTa) 
SELECT 'ROLE_ADMIN', 'Quản trị viên - Quyền duyệt sự kiện và quản lý hệ thống' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM NHOM_QUYEN WHERE TenNhomQuyen = 'ROLE_ADMIN');

COMMIT;

-- Để nâng cấp quyền cho một tài khoản thành ORGANIZER hoặc ADMIN, sử dụng:
-- INSERT INTO PHAN_QUYEN_NHOM (MaTaiKhoan, MaNhomQuyen) 
-- VALUES (<MaTaiKhoan>, (SELECT MaNhomQuyen FROM NHOM_QUYEN WHERE TenNhomQuyen = 'ROLE_ORGANIZER'));
-- 
-- INSERT INTO PHAN_QUYEN_NHOM (MaTaiKhoan, MaNhomQuyen) 
-- VALUES (<MaTaiKhoan>, (SELECT MaNhomQuyen FROM NHOM_QUYEN WHERE TenNhomQuyen = 'ROLE_ADMIN'));
