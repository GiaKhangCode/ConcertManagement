ALTER TABLE SU_KIEN ADD AnhBiaURL NVARCHAR2(1000);
ALTER TABLE SU_KIEN ADD PhanLoai VARCHAR2(50);

-- Cập nhật hình ảnh động cho Concert Phạm Duy
UPDATE SU_KIEN 
SET AnhBiaURL = 'https://images.tkbcdn.com/2/614/350/ts/ds/f9/4b/b5/e1a68f8ebcd5aefdfe871a2a68f40f28.png',
    PhanLoai = 'music'
WHERE TenSuKien LIKE '%PHAM DUY%';

-- Cập nhật hình ảnh độc quyền cho Hà Anh Tuấn
UPDATE SU_KIEN 
SET AnhBiaURL = 'https://ticketbox.vn/_next/image?url=https%3A%2F%2Fimages.tkbcdn.com%2F2%2F360%2F479%2Fts%2Fds%2Fb7%2F30%2F1d%2F3f7701e1975850532979f17c0640e056.jpg&w=640&q=75',
    PhanLoai = 'music'
WHERE TenSuKien LIKE '%HÀ ANH TUẤN%';

COMMIT;
