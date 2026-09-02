-- Gắn ảnh thật đã tải lên Cloudinary vào không gian, món ăn và thư viện ảnh.
-- Trước đây các cột này để trống, riêng thư viện trỏ tới /images/gallery/01.jpg
-- đến 06.jpg là những tệp chưa bao giờ tồn tại.
--
-- Ảnh lấy từ Pexels, giấy phép cho dùng miễn phí kể cả mục đích thương mại,
-- không bắt buộc ghi nguồn. Danh sách nguồn từng ảnh xem doc/DANH-SACH-ANH.md
--
-- Để trong migration chứ không chỉ nhập tay trên trang quản trị, để ai tải mã
-- nguồn về dựng lại cơ sở dữ liệu từ đầu cũng có ảnh, không phải nhập lại.
-- Thư viện ảnh chưa có màn hình quản trị nên càng cần đặt ở đây.

UPDATE spaces SET thumbnail_url = 'https://res.cloudinary.com/b59sgbhx/image/upload/sanh-ven-song.jpg' WHERE code = 'SANH-VEN-SONG';
UPDATE spaces SET thumbnail_url = 'https://res.cloudinary.com/b59sgbhx/image/upload/sanh-sen-vang.jpg' WHERE code = 'SANH-SEN-VANG';
UPDATE spaces SET thumbnail_url = 'https://res.cloudinary.com/b59sgbhx/image/upload/nha-ruong-go.jpg' WHERE code = 'NHA-RUONG-GO';
UPDATE spaces SET thumbnail_url = 'https://res.cloudinary.com/b59sgbhx/image/upload/phong-hoi-nghi-lua.jpg' WHERE code = 'PHONG-HOI-NGHI';
UPDATE spaces SET thumbnail_url = 'https://res.cloudinary.com/b59sgbhx/image/upload/cum-choi-sen.jpg' WHERE code = 'CUM-CHOI-SEN';
UPDATE spaces SET thumbnail_url = 'https://res.cloudinary.com/b59sgbhx/image/upload/vuon-cau.jpg' WHERE code = 'VUON-CAU';
UPDATE dishes SET image_url = 'https://res.cloudinary.com/b59sgbhx/image/upload/khai-vi-goi-cu-hu-dua.jpg' WHERE id = 1;
UPDATE dishes SET image_url = 'https://res.cloudinary.com/b59sgbhx/image/upload/khai-vi-cha-gio.jpg' WHERE id = 2;
UPDATE dishes SET image_url = 'https://res.cloudinary.com/b59sgbhx/image/upload/khai-vi-banh-xeo.jpg' WHERE id = 3;
UPDATE dishes SET image_url = 'https://res.cloudinary.com/b59sgbhx/image/upload/chinh-ca-loc-nuong.jpg' WHERE id = 7;
UPDATE dishes SET image_url = 'https://res.cloudinary.com/b59sgbhx/image/upload/nuong-tom-muc.jpg' WHERE id = 9;
UPDATE dishes SET image_url = 'https://res.cloudinary.com/b59sgbhx/image/upload/chinh-mam-com-viet.jpg' WHERE id = 11;
UPDATE dishes SET image_url = 'https://res.cloudinary.com/b59sgbhx/image/upload/chinh-ca-kho-to.jpg' WHERE id = 12;
UPDATE dishes SET image_url = 'https://res.cloudinary.com/b59sgbhx/image/upload/lau-mam-mien-tay.jpg' WHERE id = 13;
UPDATE dishes SET image_url = 'https://res.cloudinary.com/b59sgbhx/image/upload/lau-hai-san-nuong.jpg' WHERE id = 16;
UPDATE dishes SET image_url = 'https://res.cloudinary.com/b59sgbhx/image/upload/trang-mieng-che.jpg' WHERE id = 19;
UPDATE dishes SET image_url = 'https://res.cloudinary.com/b59sgbhx/image/upload/trang-mieng-banh-viet.jpg' WHERE id = 21;
UPDATE gallery_images SET url = 'https://res.cloudinary.com/b59sgbhx/image/upload/01-tiec-cuoi-ven-song.jpg' WHERE id = 1;
UPDATE gallery_images SET url = 'https://res.cloudinary.com/b59sgbhx/image/upload/02-gala-doanh-nghiep.jpg' WHERE id = 2;
UPDATE gallery_images SET url = 'https://res.cloudinary.com/b59sgbhx/image/upload/03-le-dinh-hon.jpg' WHERE id = 3;
UPDATE gallery_images SET url = 'https://res.cloudinary.com/b59sgbhx/image/upload/04-tiec-trong-sanh.jpg' WHERE id = 4;
UPDATE gallery_images SET url = 'https://res.cloudinary.com/b59sgbhx/image/upload/phong-hoi-nghi-lua.jpg' WHERE id = 5;
UPDATE gallery_images SET url = 'https://res.cloudinary.com/b59sgbhx/image/upload/06-khong-gian-ngoai-troi.jpg' WHERE id = 6;
