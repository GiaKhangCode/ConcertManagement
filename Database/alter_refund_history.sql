-- ============================================================
-- Migration: Thêm bảng LICH_SU_HOAN_TIEN
-- Mục đích: Lưu lại từng lần hoàn tiền (theo yêu cầu người dùng
--           hoặc khi BTC hủy sự kiện) để phục vụ quyết toán chính xác.
-- ============================================================

CREATE TABLE LICH_SU_HOAN_TIEN (
    MaHoanTien    NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    MaVe          NUMBER        NOT NULL REFERENCES VE(MaVe),
    MaSuKien      NUMBER        NOT NULL REFERENCES SU_KIEN(MaSuKien),
    MaTaiKhoan    NUMBER        NOT NULL REFERENCES TAI_KHOAN(MaTaiKhoan),
    SoTienHoan    NUMBER(15, 2) NOT NULL,
    LyDoHoan      VARCHAR2(500),
    LoaiHoan      VARCHAR2(100) DEFAULT 'Yêu cầu người dùng'
                  CHECK (LoaiHoan IN ('Yêu cầu người dùng', 'Hủy sự kiện')),
    ThoiDiemHoan  TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

-- Index để tăng tốc query tổng hợp theo sự kiện
CREATE INDEX IDX_LSHT_SUKIEN ON LICH_SU_HOAN_TIEN(MaSuKien);
CREATE INDEX IDX_LSHT_TAIKHOAN ON LICH_SU_HOAN_TIEN(MaTaiKhoan);
