-- Gợi ý thực đơn: chấm mức phù hợp của từng món với từng loại sự kiện.
-- Đề cương mục tiêu 6 yêu cầu gợi ý cả không gian, thực đơn và gói dịch vụ.
-- Trước đây hệ thống mới gợi ý không gian và gói tiệc, thiếu phần thực đơn.

CREATE TABLE dish_event_types (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    dish_id     BIGINT      NOT NULL,
    event_type  VARCHAR(40) NOT NULL,
    -- Chấm từ 1 đến 5, giống cách chấm của bảng space_event_types
    suitability INT         NOT NULL DEFAULT 3,
    CONSTRAINT fk_det_dish FOREIGN KEY (dish_id) REFERENCES dishes (id) ON DELETE CASCADE,
    CONSTRAINT uk_det UNIQUE (dish_id, event_type)
);

CREATE INDEX idx_det_event ON dish_event_types (event_type);

-- Điểm đặt theo danh mục món chứ không đặt riêng từng món, vì trong cùng một
-- danh mục thì mức hợp với loại tiệc gần như nhau. Muốn chỉnh riêng một món
-- nào đó thì sửa dòng của món đó trong bảng này.
--
-- Lý do đặt điểm, theo cách nhà hàng tiệc hay làm:
--   - Lẩu và nướng chấm thấp cho tiệc cưới và hội nghị: khó phục vụ đồng loạt
--     cho nhiều bàn, bàn tiệc dễ lộn xộn. Ngược lại rất hợp họp mặt gia đình.
--   - Tráng miệng chấm cao cho sinh nhật và tiệc cưới vì cần món ngọt kết thúc.
--   - Đồ uống chấm cao cho hội nghị vì khách ngồi lâu, uống nhiều.

INSERT INTO dish_event_types (dish_id, event_type, suitability)
SELECT d.id, 'WEDDING',
       CASE c.code WHEN 'khaivi' THEN 5 WHEN 'chinh' THEN 5 WHEN 'lau' THEN 2
                   WHEN 'trangmieng' THEN 5 ELSE 4 END
FROM dishes d JOIN dish_categories c ON c.id = d.category_id;

INSERT INTO dish_event_types (dish_id, event_type, suitability)
SELECT d.id, 'CORPORATE',
       CASE c.code WHEN 'khaivi' THEN 5 WHEN 'chinh' THEN 5 WHEN 'lau' THEN 2
                   WHEN 'trangmieng' THEN 4 ELSE 5 END
FROM dishes d JOIN dish_categories c ON c.id = d.category_id;

INSERT INTO dish_event_types (dish_id, event_type, suitability)
SELECT d.id, 'BIRTHDAY',
       CASE c.code WHEN 'khaivi' THEN 4 WHEN 'chinh' THEN 4 WHEN 'lau' THEN 4
                   WHEN 'trangmieng' THEN 5 ELSE 4 END
FROM dishes d JOIN dish_categories c ON c.id = d.category_id;

INSERT INTO dish_event_types (dish_id, event_type, suitability)
SELECT d.id, 'FAMILY',
       CASE c.code WHEN 'khaivi' THEN 4 WHEN 'chinh' THEN 5 WHEN 'lau' THEN 5
                   WHEN 'trangmieng' THEN 3 ELSE 4 END
FROM dishes d JOIN dish_categories c ON c.id = d.category_id;

INSERT INTO dish_event_types (dish_id, event_type, suitability)
SELECT d.id, 'OTHER', 4
FROM dishes d JOIN dish_categories c ON c.id = d.category_id;
