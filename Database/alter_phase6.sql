-- Script bổ sung cột MaNguoiTao vào bảng SU_KIEN
-- Dùng để liên kết sự kiện với tài khoản nhà tổ chức đã tạo nó
-- Cần thiết cho tính năng Phase 6: Báo cáo doanh thu theo nhà tổ chức

-- Thêm cột MaNguoiTao (FK -> TAI_KHOAN) vào bảng SU_KIEN
ALTER TABLE SU_KIEN ADD (MaNguoiTao NUMBER REFERENCES TAI_KHOAN(MaTaiKhoan));

-- Thêm cột AnhBiaURL vào bảng SU_KIEN (nếu chưa có)
-- ALTER TABLE SU_KIEN ADD (AnhBiaURL VARCHAR2(500));

-- Thêm cột PhanLoai vào bảng SU_KIEN (nếu chưa có)
-- ALTER TABLE SU_KIEN ADD (PhanLoai NVARCHAR2(100));

-- Thêm trạng thái "Bị từ chối" vào constraint TrangThai nếu cần
-- (Cần DROP constraint cũ và tạo lại)
-- ALTER TABLE SU_KIEN DROP CONSTRAINT SYS_C00xxxx; -- (tên constraint thực tế)
-- ALTER TABLE SU_KIEN ADD CONSTRAINT CK_SU_KIEN_TRANG_THAI CHECK (
--     TrangThai IN (N'Chờ phê duyệt', N'Sắp diễn ra', N'Đang diễn ra', N'Đã kết thúc', N'Đã hủy', N'Bị từ chối')
-- );

COMMIT;
