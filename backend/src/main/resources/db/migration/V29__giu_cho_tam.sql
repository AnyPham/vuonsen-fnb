-- Giu cho tam co han gio cho don dat tiec.
--
-- Van de truoc day: don moi gui o trang thai cho xac nhan thi KHONG chiem cho. Nghia la
-- ba khach cung chon mot sanh, mot ngay, mot buoi deu gui don duoc, roi ca ba cung cho,
-- va chi mot nguoi duoc nhan. Hai nguoi kia cho vo ich.
--
-- Nhung khoa cho ngay luc khach chon thi con hong hon: ai cung khoa chua duoc, khong ton
-- dong nao, va khach dong tab giua chung thi khong ai mo khoa.
--
-- Cach giai: giu cho TAM, co han gio. Khach gui don thi buoi do bi giu lai mot khoang
-- ngan de khach kip tra coc. Tra coc trong khoang do thi cho duoc giu that; het gio ma
-- chua tra thi tu tha ra cho nguoi khac. Day la cach hang bay va khach san van lam.
--
-- Khong can tac vu chay nen de quet don het han: moi lan kiem buoi con trong, he thong
-- so giu_cho_den voi gio hien tai. Het han la tu dong khong tinh nua. Cach nay dung ngay
-- ca khi may chu vua khoi dong lai, trong khi tac vu quet thi co the bo sot.

ALTER TABLE bookings ADD COLUMN giu_cho_den DATETIME NULL
    COMMENT 'Thoi diem het han giu cho tam. NULL = khong giu cho.';

-- Truy van kiem trung lich loc theo khong gian, ngay, trang thai roi moi doc cot nay,
-- nen danh chi muc theo dung thu tu do.
CREATE INDEX idx_booking_giu_cho ON bookings (space_id, event_date, giu_cho_den);

-- Don cu da nam trong CSDL thi khong giu cho nua: don nao dang cho xac nhan tu truoc
-- deu da qua lau, giu lai chi lam ket cho vo co.
UPDATE bookings SET giu_cho_den = NULL;
