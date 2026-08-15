-- Dữ liệu mẫu để chạy lên là có nội dung sẵn.

-- Không gian sự kiện
INSERT INTO spaces (code, name, slug, space_type, short_desc, capacity_min, capacity_max, rental_fee, fee_unit, unit_capacity, latitude, longitude, sort_order, created_at) VALUES
('SANH-VEN-SONG', N'Sảnh Ven Sông',        'sanh-ven-song',   'OUTDOOR',    N'Sân cỏ nhìn thẳng ra sông, phù hợp tiệc cưới ngoài trời và gala tối. Có mái che di động phòng mưa.', 300, 800, 15000000, 'SESSION', NULL, 10.8231, 106.7300, 1, CURRENT_TIMESTAMP),
('SANH-SEN-VANG',  N'Sảnh Sen Vàng',        'sanh-sen-vang',   'INDOOR',     N'Sảnh máy lạnh trần cao 7m, hệ thống LED và âm thanh line-array. Lựa chọn an toàn cho mọi thời tiết.', 200, 500, 12000000, 'SESSION', NULL, 10.8231, 106.7300, 2, CURRENT_TIMESTAMP),
('NHA-RUONG-GO',   N'Nhà Rường Gỗ',         'nha-ruong-go',    'PRIVATE',    N'Nhà gỗ ba gian truyền thống, ấm cúng cho họp mặt gia đình, giỗ chạp, mừng thọ hoặc tiệc thân mật.',   20,  60,  3000000, 'SESSION', NULL, 10.8231, 106.7300, 3, CURRENT_TIMESTAMP),
('PHONG-HOI-NGHI', N'Phòng Hội Nghị Lúa',   'phong-hoi-nghi',  'CONFERENCE', N'Bố trí lớp học hoặc chữ U, máy chiếu 4K, wifi riêng 200Mbps. Có gói teabreak giữa giờ.',              40, 150,  6000000, 'SESSION', NULL, 10.8231, 106.7300, 4, CURRENT_TIMESTAMP),
('CUM-CHOI-SEN',   N'Cụm Chòi Sen',         'cum-choi-sen',    'HUT',        N'12 chòi lá trên mặt nước, mỗi chòi 8-12 khách. Đặt lẻ hằng ngày hoặc bao trọn cụm cho nhóm lớn.',      8, 144,   500000, 'HUT',       12, 10.8231, 106.7300, 5, CURRENT_TIMESTAMP),
('VUON-CAU',       N'Vườn Cau',             'vuon-cau',        'GARDEN',     N'Bãi cỏ dưới hàng cau, đẹp nhất lúc 16-18h. Lý tưởng cho lễ đính hôn, tiệc trà và chụp ảnh cưới.',   60, 150,  7000000, 'SESSION', NULL, 10.8231, 106.7300, 6, CURRENT_TIMESTAMP);

INSERT INTO space_amenities (space_id, amenity) VALUES
((SELECT id FROM spaces WHERE code = 'SANH-VEN-SONG'), N'300-800 khách'),
((SELECT id FROM spaces WHERE code = 'SANH-VEN-SONG'), N'Sân khấu 8m'),
((SELECT id FROM spaces WHERE code = 'SANH-VEN-SONG'), N'Có mái che'),
((SELECT id FROM spaces WHERE code = 'SANH-SEN-VANG'), N'200-500 khách'),
((SELECT id FROM spaces WHERE code = 'SANH-SEN-VANG'), N'Máy lạnh'),
((SELECT id FROM spaces WHERE code = 'SANH-SEN-VANG'), N'LED 6x3m'),
((SELECT id FROM spaces WHERE code = 'NHA-RUONG-GO'),  N'20-60 khách'),
((SELECT id FROM spaces WHERE code = 'NHA-RUONG-GO'),  N'Phòng kín'),
((SELECT id FROM spaces WHERE code = 'NHA-RUONG-GO'),  N'Karaoke'),
((SELECT id FROM spaces WHERE code = 'PHONG-HOI-NGHI'), N'40-150 khách'),
((SELECT id FROM spaces WHERE code = 'PHONG-HOI-NGHI'), N'Máy chiếu 4K'),
((SELECT id FROM spaces WHERE code = 'PHONG-HOI-NGHI'), N'Teabreak'),
((SELECT id FROM spaces WHERE code = 'CUM-CHOI-SEN'),  N'8-12 khách/chòi'),
((SELECT id FROM spaces WHERE code = 'CUM-CHOI-SEN'),  N'Trên mặt nước'),
((SELECT id FROM spaces WHERE code = 'CUM-CHOI-SEN'),  N'Đặt lẻ'),
((SELECT id FROM spaces WHERE code = 'VUON-CAU'),      N'60-150 khách'),
((SELECT id FROM spaces WHERE code = 'VUON-CAU'),      N'Tiệc đứng'),
((SELECT id FROM spaces WHERE code = 'VUON-CAU'),      N'Góc chụp đẹp');

-- Danh mục và món ăn
INSERT INTO dish_categories (code, name, sort_order) VALUES
('khaivi',     N'Khai vị',       1),
('chinh',      N'Món chính',     2),
('lau',        N'Lẩu & nướng',   3),
('trangmieng', N'Tráng miệng',   4),
('douong',     N'Đồ uống',       5);

INSERT INTO dishes (category_id, name, description, price, price_note, best_seller, sort_order, created_at) VALUES
((SELECT id FROM dish_categories WHERE code='khaivi'), N'Gỏi củ hũ dừa tôm thịt', N'Củ hũ dừa Bến Tre, tôm sú, bánh phồng tôm', 185000, NULL, TRUE,  1, CURRENT_TIMESTAMP),
((SELECT id FROM dish_categories WHERE code='khaivi'), N'Chả giò rế hải sản',     N'Cuốn tay, chiên giòn, rau sống vườn nhà',   145000, NULL, FALSE, 2, CURRENT_TIMESTAMP),
((SELECT id FROM dish_categories WHERE code='khaivi'), N'Bánh xèo miền Tây',      N'Đổ tại bàn, 12 loại rau rừng',              120000, NULL, FALSE, 3, CURRENT_TIMESTAMP),
((SELECT id FROM dish_categories WHERE code='khaivi'), N'Bò lá lốt nướng than',   N'Bò tơ Củ Chi, mắm nêm pha nhà',            165000, NULL, FALSE, 4, CURRENT_TIMESTAMP),
((SELECT id FROM dish_categories WHERE code='khaivi'), N'Súp cua trứng bắc thảo', N'Nấu từ nước hầm gà 6 tiếng',                95000, NULL, FALSE, 5, CURRENT_TIMESTAMP),
((SELECT id FROM dish_categories WHERE code='khaivi'), N'Nham bắp cải tôm khô',   N'Món dân dã, chua ngọt nhẹ',                110000, NULL, FALSE, 6, CURRENT_TIMESTAMP),

((SELECT id FROM dish_categories WHERE code='chinh'), N'Cá lóc nướng trui cuốn bánh tráng', N'Cá lóc đồng 1,2kg, nướng rơm tại bàn', 395000, NULL, TRUE,  1, CURRENT_TIMESTAMP),
((SELECT id FROM dish_categories WHERE code='chinh'), N'Gà ta hấp lá chanh',                N'Gà thả vườn nguyên con, muối tiêu chanh', 450000, NULL, FALSE, 2, CURRENT_TIMESTAMP),
((SELECT id FROM dish_categories WHERE code='chinh'), N'Tôm càng nướng muối ớt',            N'Tôm càng xanh size 4 con/kg',            620000, NULL, FALSE, 3, CURRENT_TIMESTAMP),
((SELECT id FROM dish_categories WHERE code='chinh'), N'Sườn non kho tộ',                   N'Kho nước dừa xiêm, ăn kèm cơm cháy',     265000, NULL, FALSE, 4, CURRENT_TIMESTAMP),
((SELECT id FROM dish_categories WHERE code='chinh'), N'Cơm cháy chà bông kho quẹt',        N'Rau luộc theo mùa',                      135000, NULL, FALSE, 5, CURRENT_TIMESTAMP),
((SELECT id FROM dish_categories WHERE code='chinh'), N'Cá kèo kho rau răm',                N'Cá kèo tươi, kho tiêu xanh',             225000, NULL, FALSE, 6, CURRENT_TIMESTAMP),

((SELECT id FROM dish_categories WHERE code='lau'), N'Lẩu mắm miền Tây',    N'Mắm cá linh, 15 loại rau đồng',         495000, NULL,        TRUE,  1, CURRENT_TIMESTAMP),
((SELECT id FROM dish_categories WHERE code='lau'), N'Lẩu cá kèo lá giang', N'Chua thanh, ăn kèm bún tươi',           425000, NULL,        FALSE, 2, CURRENT_TIMESTAMP),
((SELECT id FROM dish_categories WHERE code='lau'), N'Lẩu gà lá é',         N'Gà ta, măng tươi, lá é Phú Yên',        455000, NULL,        FALSE, 3, CURRENT_TIMESTAMP),
((SELECT id FROM dish_categories WHERE code='lau'), N'Combo nướng than hoa',N'Bò, heo, hải sản - 6 món, cho 4 người', 690000, NULL,        FALSE, 4, CURRENT_TIMESTAMP),
((SELECT id FROM dish_categories WHERE code='lau'), N'Heo quay giòn bì',    N'Nguyên con quay lu, đặt trước 24h',       NULL, N'Theo cân', FALSE, 5, CURRENT_TIMESTAMP),
((SELECT id FROM dish_categories WHERE code='lau'), N'Dê nướng ngũ vị',     N'Ăn kèm bánh mì và chao',                385000, NULL,        FALSE, 6, CURRENT_TIMESTAMP),

((SELECT id FROM dish_categories WHERE code='trangmieng'), N'Chè bưởi Cần Thơ',         N'Nấu mỗi ngày, không phẩm màu',  45000, NULL, FALSE, 1, CURRENT_TIMESTAMP),
((SELECT id FROM dish_categories WHERE code='trangmieng'), N'Rau câu dừa lá dứa',       N'Đổ trong trái dừa tươi',        55000, NULL, FALSE, 2, CURRENT_TIMESTAMP),
((SELECT id FROM dish_categories WHERE code='trangmieng'), N'Bánh da lợn hấp lá dứa',   N'Làm theo công thức gia truyền', 40000, NULL, FALSE, 3, CURRENT_TIMESTAMP),
((SELECT id FROM dish_categories WHERE code='trangmieng'), N'Trái cây theo mùa',        N'Đĩa lớn cho bàn 10 khách',     150000, NULL, FALSE, 4, CURRENT_TIMESTAMP),

((SELECT id FROM dish_categories WHERE code='douong'), N'Nước sâm lá dứa nhà nấu',      N'Bình 1,5 lít',                        85000, NULL, FALSE, 1, CURRENT_TIMESTAMP),
((SELECT id FROM dish_categories WHERE code='douong'), N'Dừa tươi Bến Tre',             N'Ướp lạnh, phục vụ nguyên trái',       45000, NULL, FALSE, 2, CURRENT_TIMESTAMP),
((SELECT id FROM dish_categories WHERE code='douong'), N'Rượu nếp than ủ 12 tháng',     N'Chai 500ml',                         220000, NULL, FALSE, 3, CURRENT_TIMESTAMP),
((SELECT id FROM dish_categories WHERE code='douong'), N'Bia & nước ngọt',              N'Đủ nhãn phổ biến, tính theo lon dùng thật', 25000, NULL, FALSE, 4, CURRENT_TIMESTAMP);

-- Gói tiệc
INSERT INTO party_packages (code, name, tagline, price_per_table, dish_count, hours_included, featured, sort_order, created_at) VALUES
('DONG-QUE',      N'Gói Đồng Quê',     N'Họp mặt gia đình, tiệc thân mật',       2900000,  7, 3, FALSE, 1, CURRENT_TIMESTAMP),
('SEN-VANG',      N'Gói Sen Vàng',     N'Tiệc cưới, sinh nhật, tất niên',        4500000,  8, 5, TRUE,  2, CURRENT_TIMESTAMP),
('THUONG-UYEN',   N'Gói Thượng Uyển',  N'Tiệc cưới cao cấp, gala doanh nghiệp',  6800000, 10, 12, FALSE, 3, CURRENT_TIMESTAMP);

INSERT INTO package_features (package_id, feature, sort_order) VALUES
((SELECT id FROM party_packages WHERE code='DONG-QUE'), N'Thực đơn 6 món + tráng miệng',        1),
((SELECT id FROM party_packages WHERE code='DONG-QUE'), N'Nước sâm & trà đá không giới hạn',    2),
((SELECT id FROM party_packages WHERE code='DONG-QUE'), N'Trang trí bàn cơ bản, hoa tươi',      3),
((SELECT id FROM party_packages WHERE code='DONG-QUE'), N'Nhân viên phục vụ 1 người/2 bàn',     4),
((SELECT id FROM party_packages WHERE code='DONG-QUE'), N'Sử dụng không gian 3 tiếng',          5),

((SELECT id FROM party_packages WHERE code='SEN-VANG'), N'Thực đơn 8 món có hải sản',           1),
((SELECT id FROM party_packages WHERE code='SEN-VANG'), N'Cổng hoa, backdrop & bàn gallery',    2),
((SELECT id FROM party_packages WHERE code='SEN-VANG'), N'MC dẫn chương trình 2 tiếng',         3),
((SELECT id FROM party_packages WHERE code='SEN-VANG'), N'Âm thanh, ánh sáng sân khấu',         4),
((SELECT id FROM party_packages WHERE code='SEN-VANG'), N'Bánh kem & tháp ly champagne',        5),
((SELECT id FROM party_packages WHERE code='SEN-VANG'), N'Sử dụng không gian 5 tiếng',          6),

((SELECT id FROM party_packages WHERE code='THUONG-UYEN'), N'Thực đơn 10 món chọn theo yêu cầu',  1),
((SELECT id FROM party_packages WHERE code='THUONG-UYEN'), N'Trang trí concept riêng, hoa nhập',  2),
((SELECT id FROM party_packages WHERE code='THUONG-UYEN'), N'MC + ban nhạc acoustic 3 người',     3),
((SELECT id FROM party_packages WHERE code='THUONG-UYEN'), N'Quay phim & chụp ảnh phóng sự',      4),
((SELECT id FROM party_packages WHERE code='THUONG-UYEN'), N'Xe điện đón khách, lễ tân riêng',    5),
((SELECT id FROM party_packages WHERE code='THUONG-UYEN'), N'Sử dụng không gian trọn ngày',       6);

-- Đánh giá mẫu
INSERT INTO reviews (customer_name, rating, content, event_type, approved, created_at) VALUES
(N'Chị Ngọc Hân', 5, N'Tiệc cưới 45 mâm ở Sảnh Ven Sông. Trời mưa nhẹ lúc 17h nhưng đội ngũ dựng mái che rất nhanh, khách không ai bị ướt.', 'WEDDING',   TRUE, CURRENT_TIMESTAMP),
(N'Anh Minh Đức', 5, N'Công ty tổ chức tất niên 200 khách. Báo giá rõ ràng từng khoản, không phát sinh khi thanh toán.',                     'CORPORATE', TRUE, CURRENT_TIMESTAMP),
(N'Cô Bảy Lành',  5, N'Đặt Nhà Rường mừng thọ cho ba. Món ăn đúng vị miền Tây, người lớn tuổi ăn được hết.',                                 'FAMILY',    TRUE, CURRENT_TIMESTAMP);

-- Thư viện ảnh
INSERT INTO gallery_images (url, caption, category, sort_order, created_at) VALUES
('/images/gallery/01.jpg', N'Tiệc cưới ven sông - 45 mâm',      'WEDDING',   1, CURRENT_TIMESTAMP),
('/images/gallery/02.jpg', N'Gala tất niên doanh nghiệp',       'CORPORATE', 2, CURRENT_TIMESTAMP),
('/images/gallery/03.jpg', N'Lễ đính hôn tại Vườn Cau',         'WEDDING',   3, CURRENT_TIMESTAMP),
('/images/gallery/04.jpg', N'Họp mặt gia đình - Nhà Rường Gỗ',  'FAMILY',    4, CURRENT_TIMESTAMP),
('/images/gallery/05.jpg', N'Hội nghị 120 khách',               'CONFERENCE',5, CURRENT_TIMESTAMP),
('/images/gallery/06.jpg', N'Cụm Chòi Sen buổi chiều',          'SPACE',     6, CURRENT_TIMESTAMP);
