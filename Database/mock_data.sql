-- Bạn có thể chạy đoạn Script này trong Oracle SQL Developer để thêm dữ liệu mẫu vào nhé!

INSERT INTO DIA_DIEM (TenDiaDiem, SucChua, TinhThanh, PhuongXa, SoNhaTenDuong) 
VALUES ('Sân vận động Quốc gia Mỹ Đình', 40000, 'Hà Nội', 'Mỹ Đình 1', 'Đường Lê Đức Thọ, Nam Từ Liêm');

INSERT INTO SU_KIEN (MaDiaDiem, TenSuKien, ThoiGianBD, ThoiGianKT, ThoiGianMoBanVe, ThoiGianNgungBanVe, TrangThai)
VALUES (1, 'CONCERT PHAM DUY - ĐƯỜNG MÂY QUA...', TIMESTAMP '2026-11-15 20:00:00', TIMESTAMP '2026-11-15 23:00:00', TIMESTAMP '2026-10-01 12:00:00', TIMESTAMP '2026-11-15 19:00:00', 'Đang diễn ra');

INSERT INTO SU_KIEN (MaDiaDiem, TenSuKien, ThoiGianBD, ThoiGianKT, ThoiGianMoBanVe, ThoiGianNgungBanVe, TrangThai)
VALUES (1, 'LIVESHOW HÀ ANH TUẤN - CHÂN TRỜI RỰC RỠ', TIMESTAMP '2026-12-24 19:30:00', TIMESTAMP '2026-12-24 22:30:00', TIMESTAMP '2026-11-01 12:00:00', TIMESTAMP '2026-12-24 19:00:00', 'Sắp diễn ra');

INSERT INTO HANG_VE (MaSuKien, TenHangVe, GiaNiemYet, TongSoLuong) VALUES (1, 'GA Standing', 500000, 2000);
INSERT INTO HANG_VE (MaSuKien, TenHangVe, GiaNiemYet, TongSoLuong) VALUES (1, 'VIP Lounge', 1500000, 200);

INSERT INTO HANG_VE (MaSuKien, TenHangVe, GiaNiemYet, TongSoLuong) VALUES (2, 'VÉ THƯỜNG', 1200000, 5000);
INSERT INTO HANG_VE (MaSuKien, TenHangVe, GiaNiemYet, TongSoLuong) VALUES (2, 'VVIP SEATING', 5000000, 500);

-- BẮT BUỘC PHẢI CHẠY LỆNH NÀY ĐỂ LƯU DỮ LIỆU
COMMIT;
