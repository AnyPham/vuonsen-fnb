-- Bản tiếng Anh cho nội dung do cơ sở dữ liệu quản lý.
--
-- Để cột riêng thay vì bảng dịch chung: nội dung song ngữ ở đây chỉ có hai thứ tiếng và
-- gắn chặt với từng dòng, tách ra bảng riêng thì mỗi lần lấy danh sách phải nối thêm một
-- bảng nữa mà không được lợi gì. Cột để trống thì giao diện tự lùi về bản tiếng Việt,
-- nên thêm không gian mới chưa kịp dịch cũng không vỡ trang.
--
-- Mỗi cột một câu ALTER riêng và không dùng mệnh đề AFTER: gộp nhiều ADD COLUMN vào một
-- câu lệnh và chỉ định vị trí cột là cú pháp riêng của MySQL, hồ sơ test chạy trên H2 sẽ
-- không hiểu và cả bộ kiểm thử đơn vị hỏng theo. Thứ tự cột chỉ là chuyện nhìn cho đẹp.

ALTER TABLE spaces ADD COLUMN name_en       VARCHAR(120) NULL;
ALTER TABLE spaces ADD COLUMN short_desc_en VARCHAR(500) NULL;
ALTER TABLE spaces ADD COLUMN description_en TEXT        NULL;

UPDATE spaces SET
    name_en = 'Riverside Lawn',
    short_desc_en = 'A lawn opening straight onto the river, ideal for outdoor weddings and evening galas. A retractable canopy covers the space if it rains.'
WHERE code = 'SANH-VEN-SONG';

UPDATE spaces SET
    name_en = 'Golden Lotus Hall',
    short_desc_en = 'An air-conditioned hall with a 7m ceiling, an LED wall and line-array sound. The safe choice in any weather.'
WHERE code = 'SANH-SEN-VANG';

UPDATE spaces SET
    name_en = 'Traditional Timber House',
    short_desc_en = 'A traditional three-bay timber house, intimate enough for family gatherings, ancestral anniversaries, longevity celebrations and small private dinners.'
WHERE code = 'NHA-RUONG-GO';

UPDATE spaces SET
    name_en = 'Lua Conference Room',
    short_desc_en = 'Classroom or U-shape seating, a 4K projector and a dedicated 200Mbps wifi line. A tea break package is available.'
WHERE code = 'PHONG-HOI-NGHI';

UPDATE spaces SET
    name_en = 'Lotus Water Huts',
    short_desc_en = 'Twelve thatched huts over the water, each seating 8 to 12 guests. Book a single hut by the day or reserve the whole cluster for a large group.'
WHERE code = 'CUM-CHOI-SEN';

UPDATE spaces SET
    name_en = 'Areca Palm Garden',
    short_desc_en = 'A lawn beneath a row of areca palms, at its best between 4 and 6 PM. Ideal for engagement ceremonies, tea parties and wedding photography.'
WHERE code = 'VUON-CAU';
