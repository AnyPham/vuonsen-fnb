-- Bo sung anh cho khong gian, danh gia va thu vien.
--
-- Anh lay tu Pexels, giay phep cho dung mien phi ke ca muc dich thuong mai.
-- Danh sach tac gia tung anh xem doc/DANH-SACH-ANH.md
--
-- Truoc dot nay moi khong gian chi co mot anh dai dien va bang space_images
-- nam khong. Danh gia thi khong co cho luu anh, phai them bang o V7.

-- Sảnh Ven Sông
INSERT INTO space_images (space_id, url, caption, sort_order)
SELECT id, v.url, v.caption, v.ord FROM spaces, (
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591694/sanh-ven-song-01.jpg' AS url, N'Toàn cảnh sân cỏ ven sông' AS caption, 0 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591695/sanh-ven-song-02.jpg' AS url, N'Bàn tiệc nhìn ra mặt nước' AS caption, 1 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591696/sanh-ven-song-03.jpg' AS url, N'Sân khấu ngoài trời' AS caption, 2 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591697/sanh-ven-song-04.jpg' AS url, N'Lối vào trải hoa' AS caption, 3 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591700/sanh-ven-song-05.jpg' AS url, N'Tiệc tối có đèn dây' AS caption, 4 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591701/sanh-ven-song-06.jpg' AS url, N'Khu đón khách' AS caption, 5 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591703/sanh-ven-song-07.jpg' AS url, N'Bàn tròn phủ khăn trắng' AS caption, 6 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591705/sanh-ven-song-08.jpg' AS url, N'Góc chụp ảnh cưới' AS caption, 7 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591706/sanh-ven-song-09.jpg' AS url, N'Mái che di động' AS caption, 8 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591708/sanh-ven-song-10.jpg' AS url, N'Hoàng hôn trên sông' AS caption, 9 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591709/sanh-ven-song-11.jpg' AS url, N'Khu tiệc trà ngoài trời' AS caption, 10 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591710/sanh-ven-song-12.jpg' AS url, N'Bàn gallery đón khách' AS caption, 11 AS ord
) v WHERE spaces.code = 'SANH-VEN-SONG';

-- Sảnh Sen Vàng
INSERT INTO space_images (space_id, url, caption, sort_order)
SELECT id, v.url, v.caption, v.ord FROM spaces, (
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591713/sanh-sen-vang-01.jpg' AS url, N'Toàn cảnh sảnh trần cao' AS caption, 0 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591714/sanh-sen-vang-02.jpg' AS url, N'Sân khấu và màn LED' AS caption, 1 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591716/sanh-sen-vang-03.jpg' AS url, N'Bàn tiệc bày sẵn' AS caption, 2 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591717/sanh-sen-vang-04.jpg' AS url, N'Lối đi trung tâm' AS caption, 3 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591719/sanh-sen-vang-05.jpg' AS url, N'Hệ thống đèn trần' AS caption, 4 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591720/sanh-sen-vang-06.jpg' AS url, N'Khu vực đón khách' AS caption, 5 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591722/sanh-sen-vang-07.jpg' AS url, N'Bàn tiệc nhìn từ trên' AS caption, 6 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591723/sanh-sen-vang-08.jpg' AS url, N'Góc sân khấu' AS caption, 7 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591724/sanh-sen-vang-09.jpg' AS url, N'Trang trí hoa bàn' AS caption, 8 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591725/sanh-sen-vang-10.jpg' AS url, N'Bàn tiệc buổi tối' AS caption, 9 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591727/sanh-sen-vang-11.jpg' AS url, N'Khu sảnh chờ' AS caption, 10 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591728/sanh-sen-vang-12.jpg' AS url, N'Chi tiết bày bàn' AS caption, 11 AS ord
) v WHERE spaces.code = 'SANH-SEN-VANG';

-- Nhà Rường Gỗ
INSERT INTO space_images (space_id, url, caption, sort_order)
SELECT id, v.url, v.caption, v.ord FROM spaces, (
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591731/nha-ruong-go-01.jpg' AS url, N'Gian giữa nhà rường' AS caption, 0 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591733/nha-ruong-go-02.jpg' AS url, N'Bàn ăn gỗ tự nhiên' AS caption, 1 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591734/nha-ruong-go-03.jpg' AS url, N'Chi tiết cột kèo' AS caption, 2 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591735/nha-ruong-go-04.jpg' AS url, N'Hiên nhà nhìn ra vườn' AS caption, 3 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591737/nha-ruong-go-05.jpg' AS url, N'Góc thờ truyền thống' AS caption, 4 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591738/nha-ruong-go-06.jpg' AS url, N'Bàn tiệc gia đình' AS caption, 5 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591740/nha-ruong-go-07.jpg' AS url, N'Cửa gỗ chạm khắc' AS caption, 6 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591741/nha-ruong-go-08.jpg' AS url, N'Ánh sáng buổi chiều' AS caption, 7 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591742/nha-ruong-go-09.jpg' AS url, N'Không gian ấm cúng' AS caption, 8 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591744/nha-ruong-go-10.jpg' AS url, N'Bàn trà gỗ' AS caption, 9 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591745/nha-ruong-go-11.jpg' AS url, N'Lối vào nhà rường' AS caption, 10 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591747/nha-ruong-go-12.jpg' AS url, N'Mái ngói nhìn từ sân' AS caption, 11 AS ord
) v WHERE spaces.code = 'NHA-RUONG-GO';

-- Phòng Hội Nghị Lúa
INSERT INTO space_images (space_id, url, caption, sort_order)
SELECT id, v.url, v.caption, v.ord FROM spaces, (
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591749/phong-hoi-nghi-01.jpg' AS url, N'Bố trí kiểu lớp học' AS caption, 0 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591750/phong-hoi-nghi-02.jpg' AS url, N'Bố trí chữ U' AS caption, 1 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591752/phong-hoi-nghi-03.jpg' AS url, N'Máy chiếu và màn hình' AS caption, 2 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591753/phong-hoi-nghi-04.jpg' AS url, N'Bàn chủ tọa' AS caption, 3 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591755/phong-hoi-nghi-05.jpg' AS url, N'Khu teabreak giữa giờ' AS caption, 4 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591756/phong-hoi-nghi-06.jpg' AS url, N'Ghế hội nghị' AS caption, 5 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591758/phong-hoi-nghi-07.jpg' AS url, N'Bảng trắng và bục phát biểu' AS caption, 6 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591759/phong-hoi-nghi-08.jpg' AS url, N'Ánh sáng phòng họp' AS caption, 7 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591760/phong-hoi-nghi-09.jpg' AS url, N'Khu đăng ký đại biểu' AS caption, 8 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591762/phong-hoi-nghi-10.jpg' AS url, N'Góc nhìn từ cuối phòng' AS caption, 9 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591763/phong-hoi-nghi-11.jpg' AS url, N'Bàn tài liệu' AS caption, 10 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591764/phong-hoi-nghi-12.jpg' AS url, N'Hệ thống âm thanh' AS caption, 11 AS ord
) v WHERE spaces.code = 'PHONG-HOI-NGHI';

-- Cụm Chòi Sen
INSERT INTO space_images (space_id, url, caption, sort_order)
SELECT id, v.url, v.caption, v.ord FROM spaces, (
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591767/cum-choi-sen-01.jpg' AS url, N'Cụm chòi trên mặt nước' AS caption, 0 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591769/cum-choi-sen-02.jpg' AS url, N'Một chòi nhìn gần' AS caption, 1 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591771/cum-choi-sen-03.jpg' AS url, N'Lối cầu gỗ nối các chòi' AS caption, 2 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591774/cum-choi-sen-04.jpg' AS url, N'Bàn ăn trong chòi' AS caption, 3 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591776/cum-choi-sen-05.jpg' AS url, N'Mái lá nhìn từ dưới' AS caption, 4 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591778/cum-choi-sen-06.jpg' AS url, N'Mặt nước buổi chiều' AS caption, 5 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591779/cum-choi-sen-07.jpg' AS url, N'Chòi lá lúc hoàng hôn' AS caption, 6 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591781/cum-choi-sen-08.jpg' AS url, N'Góc nhìn từ bờ' AS caption, 7 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591783/cum-choi-sen-09.jpg' AS url, N'Chòi đôi cho nhóm nhỏ' AS caption, 8 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591786/cum-choi-sen-10.jpg' AS url, N'Đèn lồng buổi tối' AS caption, 9 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591787/cum-choi-sen-11.jpg' AS url, N'Sen quanh chòi' AS caption, 10 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591789/cum-choi-sen-12.jpg' AS url, N'Toàn cảnh cụm chòi' AS caption, 11 AS ord
) v WHERE spaces.code = 'CUM-CHOI-SEN';

-- Vườn Cau
INSERT INTO space_images (space_id, url, caption, sort_order)
SELECT id, v.url, v.caption, v.ord FROM spaces, (
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591791/vuon-cau-01.jpg' AS url, N'Bãi cỏ dưới hàng cau' AS caption, 0 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591793/vuon-cau-02.jpg' AS url, N'Cổng hoa lễ đính hôn' AS caption, 1 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591794/vuon-cau-03.jpg' AS url, N'Bàn tiệc trà ngoài trời' AS caption, 2 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591796/vuon-cau-04.jpg' AS url, N'Lối đi giữa hàng cau' AS caption, 3 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591797/vuon-cau-05.jpg' AS url, N'Ánh nắng chiều xuyên tán' AS caption, 4 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591798/vuon-cau-06.jpg' AS url, N'Khu chụp ảnh cưới' AS caption, 5 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591800/vuon-cau-07.jpg' AS url, N'Bàn ghế mây trắng' AS caption, 6 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591801/vuon-cau-08.jpg' AS url, N'Trang trí hoa tươi' AS caption, 7 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591803/vuon-cau-09.jpg' AS url, N'Góc nhìn toàn cảnh vườn' AS caption, 8 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591805/vuon-cau-10.jpg' AS url, N'Đèn dây buổi tối' AS caption, 9 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591806/vuon-cau-11.jpg' AS url, N'Khu đón khách ngoài trời' AS caption, 10 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591808/vuon-cau-12.jpg' AS url, N'Thảm cỏ và hàng ghế' AS caption, 11 AS ord
) v WHERE spaces.code = 'VUON-CAU';

-- Anh khach chup tai tiec, chia deu cho cac danh gia da duyet
INSERT INTO review_images (review_id, url, sort_order)
SELECT id, 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591812/danh-gia-01.jpg', 0 FROM reviews ORDER BY id LIMIT 1 OFFSET 0;
INSERT INTO review_images (review_id, url, sort_order)
SELECT id, 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591813/danh-gia-02.jpg', 0 FROM reviews ORDER BY id LIMIT 1 OFFSET 1;
INSERT INTO review_images (review_id, url, sort_order)
SELECT id, 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591816/danh-gia-03.jpg', 0 FROM reviews ORDER BY id LIMIT 1 OFFSET 2;
INSERT INTO review_images (review_id, url, sort_order)
SELECT id, 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591817/danh-gia-04.jpg', 0 FROM reviews ORDER BY id LIMIT 1 OFFSET 3;
INSERT INTO review_images (review_id, url, sort_order)
SELECT id, 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591818/danh-gia-05.jpg', 0 FROM reviews ORDER BY id LIMIT 1 OFFSET 4;
INSERT INTO review_images (review_id, url, sort_order)
SELECT id, 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591819/danh-gia-06.jpg', 0 FROM reviews ORDER BY id LIMIT 1 OFFSET 5;
INSERT INTO review_images (review_id, url, sort_order)
SELECT id, 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591821/danh-gia-07.jpg', 0 FROM reviews ORDER BY id LIMIT 1 OFFSET 6;
INSERT INTO review_images (review_id, url, sort_order)
SELECT id, 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591822/danh-gia-08.jpg', 0 FROM reviews ORDER BY id LIMIT 1 OFFSET 7;
INSERT INTO review_images (review_id, url, sort_order)
SELECT id, 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591823/danh-gia-09.jpg', 0 FROM reviews ORDER BY id LIMIT 1 OFFSET 8;
INSERT INTO review_images (review_id, url, sort_order)
SELECT id, 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591825/danh-gia-10.jpg', 1 FROM reviews ORDER BY id LIMIT 1 OFFSET 0;
INSERT INTO review_images (review_id, url, sort_order)
SELECT id, 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591826/danh-gia-11.jpg', 1 FROM reviews ORDER BY id LIMIT 1 OFFSET 1;
INSERT INTO review_images (review_id, url, sort_order)
SELECT id, 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591828/danh-gia-12.jpg', 1 FROM reviews ORDER BY id LIMIT 1 OFFSET 2;
INSERT INTO review_images (review_id, url, sort_order)
SELECT id, 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591829/danh-gia-13.jpg', 1 FROM reviews ORDER BY id LIMIT 1 OFFSET 3;
INSERT INTO review_images (review_id, url, sort_order)
SELECT id, 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591831/danh-gia-14.jpg', 1 FROM reviews ORDER BY id LIMIT 1 OFFSET 4;
INSERT INTO review_images (review_id, url, sort_order)
SELECT id, 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591832/danh-gia-15.jpg', 1 FROM reviews ORDER BY id LIMIT 1 OFFSET 5;
INSERT INTO review_images (review_id, url, sort_order)
SELECT id, 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591835/danh-gia-16.jpg', 1 FROM reviews ORDER BY id LIMIT 1 OFFSET 6;
INSERT INTO review_images (review_id, url, sort_order)
SELECT id, 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591836/danh-gia-17.jpg', 1 FROM reviews ORDER BY id LIMIT 1 OFFSET 7;
INSERT INTO review_images (review_id, url, sort_order)
SELECT id, 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591838/danh-gia-18.jpg', 1 FROM reviews ORDER BY id LIMIT 1 OFFSET 8;
INSERT INTO review_images (review_id, url, sort_order)
SELECT id, 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591839/danh-gia-19.jpg', 2 FROM reviews ORDER BY id LIMIT 1 OFFSET 0;
INSERT INTO review_images (review_id, url, sort_order)
SELECT id, 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591841/danh-gia-20.jpg', 2 FROM reviews ORDER BY id LIMIT 1 OFFSET 1;
INSERT INTO review_images (review_id, url, sort_order)
SELECT id, 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591842/danh-gia-21.jpg', 2 FROM reviews ORDER BY id LIMIT 1 OFFSET 2;
INSERT INTO review_images (review_id, url, sort_order)
SELECT id, 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591843/danh-gia-22.jpg', 2 FROM reviews ORDER BY id LIMIT 1 OFFSET 3;
INSERT INTO review_images (review_id, url, sort_order)
SELECT id, 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591845/danh-gia-23.jpg', 2 FROM reviews ORDER BY id LIMIT 1 OFFSET 4;
INSERT INTO review_images (review_id, url, sort_order)
SELECT id, 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591846/danh-gia-24.jpg', 2 FROM reviews ORDER BY id LIMIT 1 OFFSET 5;
INSERT INTO review_images (review_id, url, sort_order)
SELECT id, 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591848/danh-gia-25.jpg', 2 FROM reviews ORDER BY id LIMIT 1 OFFSET 6;
INSERT INTO review_images (review_id, url, sort_order)
SELECT id, 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591850/danh-gia-26.jpg', 2 FROM reviews ORDER BY id LIMIT 1 OFFSET 7;
INSERT INTO review_images (review_id, url, sort_order)
SELECT id, 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591852/danh-gia-27.jpg', 2 FROM reviews ORDER BY id LIMIT 1 OFFSET 8;

-- Thu vien: WEDDING
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591856/thu-vien-wedding-01.jpg', NULL, 'WEDDING', 100, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591857/thu-vien-wedding-02.jpg', NULL, 'WEDDING', 101, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591859/thu-vien-wedding-03.jpg', NULL, 'WEDDING', 102, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591860/thu-vien-wedding-04.png', NULL, 'WEDDING', 103, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591862/thu-vien-wedding-05.jpg', NULL, 'WEDDING', 104, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591863/thu-vien-wedding-06.jpg', NULL, 'WEDDING', 105, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591865/thu-vien-wedding-07.jpg', NULL, 'WEDDING', 106, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591866/thu-vien-wedding-08.jpg', NULL, 'WEDDING', 107, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591869/thu-vien-wedding-09.jpg', NULL, 'WEDDING', 108, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591870/thu-vien-wedding-10.jpg', NULL, 'WEDDING', 109, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591872/thu-vien-wedding-11.jpg', NULL, 'WEDDING', 110, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591873/thu-vien-wedding-12.jpg', NULL, 'WEDDING', 111, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591874/thu-vien-wedding-13.jpg', NULL, 'WEDDING', 112, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591876/thu-vien-wedding-14.jpg', NULL, 'WEDDING', 113, TRUE, CURRENT_TIMESTAMP);

-- Thu vien: CORPORATE
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591878/thu-vien-corporate-01.jpg', NULL, 'CORPORATE', 100, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591880/thu-vien-corporate-02.jpg', NULL, 'CORPORATE', 101, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591881/thu-vien-corporate-03.jpg', NULL, 'CORPORATE', 102, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591882/thu-vien-corporate-04.jpg', NULL, 'CORPORATE', 103, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591883/thu-vien-corporate-05.jpg', NULL, 'CORPORATE', 104, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591885/thu-vien-corporate-06.jpg', NULL, 'CORPORATE', 105, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591887/thu-vien-corporate-07.jpg', NULL, 'CORPORATE', 106, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591888/thu-vien-corporate-08.jpg', NULL, 'CORPORATE', 107, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591889/thu-vien-corporate-09.jpg', NULL, 'CORPORATE', 108, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591891/thu-vien-corporate-10.jpg', NULL, 'CORPORATE', 109, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591892/thu-vien-corporate-11.jpg', NULL, 'CORPORATE', 110, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591894/thu-vien-corporate-12.jpg', NULL, 'CORPORATE', 111, TRUE, CURRENT_TIMESTAMP);

-- Thu vien: FAMILY
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591896/thu-vien-family-01.jpg', NULL, 'FAMILY', 100, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591897/thu-vien-family-02.jpg', NULL, 'FAMILY', 101, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591898/thu-vien-family-03.jpg', NULL, 'FAMILY', 102, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591900/thu-vien-family-04.jpg', NULL, 'FAMILY', 103, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591902/thu-vien-family-05.jpg', NULL, 'FAMILY', 104, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591903/thu-vien-family-06.jpg', NULL, 'FAMILY', 105, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591905/thu-vien-family-07.jpg', NULL, 'FAMILY', 106, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591906/thu-vien-family-08.jpg', NULL, 'FAMILY', 107, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591908/thu-vien-family-09.jpg', NULL, 'FAMILY', 108, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591909/thu-vien-family-10.jpg', NULL, 'FAMILY', 109, TRUE, CURRENT_TIMESTAMP);

-- Thu vien: CONFERENCE
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591911/thu-vien-conference-01.jpg', NULL, 'CONFERENCE', 100, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591912/thu-vien-conference-02.jpg', NULL, 'CONFERENCE', 101, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591913/thu-vien-conference-03.jpg', NULL, 'CONFERENCE', 102, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591914/thu-vien-conference-04.jpg', NULL, 'CONFERENCE', 103, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591915/thu-vien-conference-05.jpg', NULL, 'CONFERENCE', 104, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591916/thu-vien-conference-06.jpg', NULL, 'CONFERENCE', 105, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591917/thu-vien-conference-07.jpg', NULL, 'CONFERENCE', 106, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591918/thu-vien-conference-08.jpg', NULL, 'CONFERENCE', 107, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591920/thu-vien-conference-09.jpg', NULL, 'CONFERENCE', 108, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591921/thu-vien-conference-10.jpg', NULL, 'CONFERENCE', 109, TRUE, CURRENT_TIMESTAMP);

-- Thu vien: SPACE
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591923/thu-vien-space-01.jpg', NULL, 'SPACE', 100, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591925/thu-vien-space-02.jpg', NULL, 'SPACE', 101, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591926/thu-vien-space-03.jpg', NULL, 'SPACE', 102, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591927/thu-vien-space-04.jpg', NULL, 'SPACE', 103, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591928/thu-vien-space-05.jpg', NULL, 'SPACE', 104, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591930/thu-vien-space-06.jpg', NULL, 'SPACE', 105, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591931/thu-vien-space-07.jpg', NULL, 'SPACE', 106, TRUE, CURRENT_TIMESTAMP);
INSERT INTO gallery_images (url, caption, category, sort_order, active, created_at) VALUES ('https://res.cloudinary.com/b59sgbhx/image/upload/v1788591933/thu-vien-space-08.jpg', NULL, 'SPACE', 107, TRUE, CURRENT_TIMESTAMP);

UPDATE dishes SET image_url = 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591935/mon-nham-bap-cai-tom-kho-01.jpg' WHERE name = N'Nham bắp cải tôm khô';
UPDATE dishes SET image_url = 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788591937/mon-ruou-nep-than-u-12-thang-01.jpg' WHERE name = N'Rượu nếp than ủ 12 tháng';
