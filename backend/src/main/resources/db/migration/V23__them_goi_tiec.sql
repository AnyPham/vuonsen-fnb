-- Mo rong danh sach goi tiec tu 3 len 8.
--
-- Ba goi ban dau deu la tiec lon kieu cuoi hoi va gala, khoang gia tu 2,9 den 6,8 trieu.
-- Bo loc vua lam ra gan nhu khong co tac dung tren ba dong, ma thuc te nha hang F&B con
-- nhan nhieu loai tiec khac han ve quy mo: lien hoan phong ban, day thang, tat nien cong
-- ty, hoi nghi khach hang. Them nam goi nay vua phu kin cac loai tiec do, vua giai rong
-- ca ba truc ma bo loc dung den: gia tu 1,8 den 8,5 trieu, so mon tu 5 den 12, thoi gian
-- dung khong gian tu 2 den 12 tieng.
--
-- Viet duoc cho ca MySQL lan H2 che do MySQL: chi dung INSERT va UPDATE, khong dung cu
-- phap rieng cua MySQL. Bai hoc tu V20, ban do gop nhieu ADD COLUMN vao mot ALTER nen
-- chay duoc tren MySQL nhung lam truot 106 kiem thu chay tren H2.

INSERT INTO party_packages (code, name, name_en, tagline, tagline_en, price_per_table, dish_count, hours_included, featured, sort_order, created_at) VALUES
('HOP-MAT',       N'Gói Họp Mặt',        'Get-together Package', N'Liên hoan phòng ban, họp lớp, sinh nhật nhỏ',   'Team dinners, class reunions and small birthdays',      1800000,  5,  2, FALSE, 1, CURRENT_TIMESTAMP),
('SEN-TRANG',     N'Gói Sen Trắng',      'White Lotus Package',  N'Đầy tháng, thôi nôi, mừng thọ',                 'Baby celebrations and birthday banquets for elders',    3600000,  7,  4, FALSE, 3, CURRENT_TIMESTAMP),
('TAT-NIEN',      N'Gói Tất Niên',       'Year-end Package',     N'Tiệc cuối năm, tri ân khách hàng',              'Year-end parties and client appreciation dinners',      3900000,  9,  4, FALSE, 4, CURRENT_TIMESTAMP),
('HOI-NGHI',      N'Gói Hội Nghị',       'Conference Package',   N'Hội thảo, hội nghị khách hàng, đào tạo',        'Seminars, client conferences and training days',        5200000,  8,  8, FALSE, 6, CURRENT_TIMESTAMP),
('CUOI-TRON-GOI', N'Gói Cưới Trọn Gói',  'Full Wedding Package', N'Tiệc cưới trọn gói từ đón khách đến tiễn khách','A complete wedding from welcome to farewell',           8500000, 12, 10, FALSE, 8, CURRENT_TIMESTAMP);

-- Xep lai thu tu hien thi cho ca tam goi chay theo gia tu thap len cao. Khong lam buoc
-- nay thi nam goi moi chen vao giua cac so thu tu cu va danh sach bay ra lon xon.
UPDATE party_packages SET sort_order = 2 WHERE code = 'DONG-QUE';
UPDATE party_packages SET sort_order = 5 WHERE code = 'SEN-VANG';
UPDATE party_packages SET sort_order = 7 WHERE code = 'THUONG-UYEN';

-- Hang muc trong tung goi. Cac cum da co trong tu dien NoiDungSongNgu duoc dung lai
-- nguyen van thay vi viet lai cho khac di, vi tu dien tra theo dung chuoi tieng Viet:
-- viet lech mot chu la mat ban dich tieng Anh cua dong do.
INSERT INTO package_features (package_id, feature, sort_order) VALUES
((SELECT id FROM party_packages WHERE code='HOP-MAT'), N'Thực đơn 5 món theo mùa',            1),
((SELECT id FROM party_packages WHERE code='HOP-MAT'), N'Trà đá & nước ngọt không giới hạn',  2),
((SELECT id FROM party_packages WHERE code='HOP-MAT'), N'Bàn ghế, khăn trải bàn tiêu chuẩn',  3),
((SELECT id FROM party_packages WHERE code='HOP-MAT'), N'Sử dụng không gian 2 tiếng',         4),

((SELECT id FROM party_packages WHERE code='SEN-TRANG'), N'Thực đơn 7 món, có món chay tùy chọn',       1),
((SELECT id FROM party_packages WHERE code='SEN-TRANG'), N'Trang trí bàn tiệc theo tông màu chọn trước', 2),
((SELECT id FROM party_packages WHERE code='SEN-TRANG'), N'Bánh kem và nước ngọt cho khách nhỏ tuổi',    3),
((SELECT id FROM party_packages WHERE code='SEN-TRANG'), N'Nhân viên phục vụ 1 người/2 bàn',             4),
((SELECT id FROM party_packages WHERE code='SEN-TRANG'), N'Sử dụng không gian 4 tiếng',                  5),

((SELECT id FROM party_packages WHERE code='TAT-NIEN'), N'Thực đơn 9 món, có lẩu cuối tiệc',             1),
((SELECT id FROM party_packages WHERE code='TAT-NIEN'), N'Bia và nước ngọt không giới hạn trong 2 tiếng', 2),
((SELECT id FROM party_packages WHERE code='TAT-NIEN'), N'Màn hình LED chiếu video tổng kết năm',        3),
((SELECT id FROM party_packages WHERE code='TAT-NIEN'), N'Âm thanh, ánh sáng sân khấu',                  4),
((SELECT id FROM party_packages WHERE code='TAT-NIEN'), N'Bốc thăm trúng thưởng, quà cho nhân viên',     5),
((SELECT id FROM party_packages WHERE code='TAT-NIEN'), N'Sử dụng không gian 4 tiếng',                   6),

((SELECT id FROM party_packages WHERE code='HOI-NGHI'), N'Thực đơn 8 món phục vụ theo suất',          1),
((SELECT id FROM party_packages WHERE code='HOI-NGHI'), N'Hai lần teabreak giữa giờ',                 2),
((SELECT id FROM party_packages WHERE code='HOI-NGHI'), N'Máy chiếu, màn chiếu và bục phát biểu',     3),
((SELECT id FROM party_packages WHERE code='HOI-NGHI'), N'Micro không dây và kỹ thuật viên trực',     4),
((SELECT id FROM party_packages WHERE code='HOI-NGHI'), N'Bảng tên, bàn đón tiếp và nước suối tại chỗ', 5),
((SELECT id FROM party_packages WHERE code='HOI-NGHI'), N'Sử dụng không gian trọn ngày',              6),

((SELECT id FROM party_packages WHERE code='CUOI-TRON-GOI'), N'Thực đơn 12 món, bàn thử món trước ngày cưới',        1),
((SELECT id FROM party_packages WHERE code='CUOI-TRON-GOI'), N'Cổng hoa, backdrop & bàn gallery',                    2),
((SELECT id FROM party_packages WHERE code='CUOI-TRON-GOI'), N'Trang trí concept riêng, hoa nhập',                   3),
((SELECT id FROM party_packages WHERE code='CUOI-TRON-GOI'), N'MC + ban nhạc acoustic 3 người',                      4),
((SELECT id FROM party_packages WHERE code='CUOI-TRON-GOI'), N'Quay phim & chụp ảnh phóng sự',                       5),
((SELECT id FROM party_packages WHERE code='CUOI-TRON-GOI'), N'Xe hoa và phòng chờ riêng cho cô dâu chú rể',         6),
((SELECT id FROM party_packages WHERE code='CUOI-TRON-GOI'), N'Điều phối viên theo sát từ đón khách đến tiễn khách', 7),
((SELECT id FROM party_packages WHERE code='CUOI-TRON-GOI'), N'Sử dụng không gian trọn ngày',                        8);
