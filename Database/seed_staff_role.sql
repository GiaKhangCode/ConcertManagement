-- Thêm Nhóm quyền nhân viên soát vé
INSERT INTO NHOM_QUYEN (TenNhomQuyen, MoTa) 
VALUES (N'ROLE_STAFF', N'Nhân viên soát vé tại sự kiện');

-- Cấp quyền xem và soát vé cho ROLE_STAFF (Giả định MaChucNang cho vé là 1 hoặc tương ứng)
-- Lưu ý: Bạn có thể gán thủ công tài khoản nhân viên vào nhóm quyền này trong trang quản trị.
