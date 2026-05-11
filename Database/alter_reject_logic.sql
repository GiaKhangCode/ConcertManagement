-- Script bổ sung logic từ chối sự kiện
-- 1. Thêm cột LyDoTuChoi vào bảng SU_KIEN
ALTER TABLE SU_KIEN ADD (LyDoTuChoi NVARCHAR2(1000));

-- 2. Cập nhật ràng buộc TrangThai để bao gồm 'Bị từ chối'
-- Lưu ý: Cần tìm đúng tên constraint để DROP. Thường là SYS_Cxxxxxx.
-- Ở đây chúng ta sẽ tạo một constraint mới có tên tường minh để dễ quản lý.

-- Đầu tiên, tìm và xóa constraint cũ (nếu biết tên, nếu không có thể bỏ qua bước DROP nếu nó không xung đột hoặc dùng script động)
-- Giả sử chúng ta thêm constraint mới, Oracle sẽ kiểm tra cả hai. Tốt nhất là xóa cái cũ.
-- Vì không biết tên chính xác (nó là hệ thống tự sinh), chúng ta có thể dùng câu lệnh sau để vô hiệu hóa các check constraint trên cột TrangThai.

BEGIN
  FOR r IN (SELECT constraint_name 
            FROM user_constraints 
            WHERE table_name = 'SU_KIEN' 
              AND constraint_type = 'C' 
              AND search_condition_vc LIKE '%TrangThai%')
  LOOP
    EXECUTE IMMEDIATE 'ALTER TABLE SU_KIEN DROP CONSTRAINT ' || r.constraint_name;
  END LOOP;
END;
/

-- Thêm constraint mới với đầy đủ trạng thái
ALTER TABLE SU_KIEN ADD CONSTRAINT CK_SU_KIEN_TRANG_THAI 
CHECK (TrangThai IN (N'Chờ phê duyệt', N'Sắp diễn ra', N'Đang diễn ra', N'Đã kết thúc', N'Đã hủy', N'Bị từ chối'));

COMMIT;
