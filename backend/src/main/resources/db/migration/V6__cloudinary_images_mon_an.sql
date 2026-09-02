-- Bổ sung ảnh cho 13 món ăn còn lại.
--
-- Tách thành V6 chứ không sửa V5 vì V5 đã chạy trên cơ sở dữ liệu thật rồi.
-- Sửa tệp cũ sẽ làm sai mã kiểm tra của Flyway và ứng dụng không khởi động được.
--
-- Sau đợt này 24 trên 26 món có ảnh. Hai món chưa có là "Nham bắp cải tôm khô"
-- và "Rượu nếp than ủ 12 tháng", chưa tìm được ảnh đúng chủ đề nên để trống,
-- giao diện sẽ hiện khối màu giữ chỗ thay vì gán ảnh sai.

UPDATE dishes SET image_url = 'https://res.cloudinary.com/b59sgbhx/image/upload/khai-vi-bo-la-lot.jpg' WHERE id = 4;
UPDATE dishes SET image_url = 'https://res.cloudinary.com/b59sgbhx/image/upload/khai-vi-sup-cua.jpg' WHERE id = 5;
UPDATE dishes SET image_url = 'https://res.cloudinary.com/b59sgbhx/image/upload/chinh-ga-hap-la-chanh.jpg' WHERE id = 8;
UPDATE dishes SET image_url = 'https://res.cloudinary.com/b59sgbhx/image/upload/chinh-suon-kho-to.jpg' WHERE id = 10;
UPDATE dishes SET image_url = 'https://res.cloudinary.com/b59sgbhx/image/upload/lau-ca-keo-la-giang.jpg' WHERE id = 14;
UPDATE dishes SET image_url = 'https://res.cloudinary.com/b59sgbhx/image/upload/lau-ga-la-e.jpg' WHERE id = 15;
UPDATE dishes SET image_url = 'https://res.cloudinary.com/b59sgbhx/image/upload/nuong-heo-quay-gion-bi.jpg' WHERE id = 17;
UPDATE dishes SET image_url = 'https://res.cloudinary.com/b59sgbhx/image/upload/nuong-de-ngu-vi.jpg' WHERE id = 18;
UPDATE dishes SET image_url = 'https://res.cloudinary.com/b59sgbhx/image/upload/trang-mieng-rau-cau-dua.jpg' WHERE id = 20;
UPDATE dishes SET image_url = 'https://res.cloudinary.com/b59sgbhx/image/upload/trang-mieng-trai-cay.jpg' WHERE id = 22;
UPDATE dishes SET image_url = 'https://res.cloudinary.com/b59sgbhx/image/upload/douong-nuoc-sam.jpg' WHERE id = 23;
UPDATE dishes SET image_url = 'https://res.cloudinary.com/b59sgbhx/image/upload/douong-dua-tuoi.jpg' WHERE id = 24;
UPDATE dishes SET image_url = 'https://res.cloudinary.com/b59sgbhx/image/upload/douong-bia-nuoc-ngot.jpg' WHERE id = 26;
