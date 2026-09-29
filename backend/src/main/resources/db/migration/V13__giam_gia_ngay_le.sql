-- Giảm giá vào các dịp lễ lớn trong năm.
--
-- Ngày khách tới (ngày tổ chức tiệc, ngày nhận món) rơi vào khoảng một dịp lễ đang bật
-- thì được giảm theo mức của dịp đó. Mỗi dòng lưu một khoảng ngày dương lịch cụ thể của
-- một năm, không lưu kiểu "mùng 1 Tết" hay "10/3 âm lịch": lễ âm lịch mỗi năm rơi vào
-- một ngày dương khác nhau, quản trị nhập thẳng ngày dương là rõ ràng nhất, và việc tra
-- cứu lúc tính giá chỉ còn là so sánh ngày.
CREATE TABLE holiday_discounts (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(120) NOT NULL,
    start_date    DATE         NOT NULL,
    end_date      DATE         NOT NULL,

    -- Tỉ lệ giảm, 0.1500 nghĩa là 15%. Chính sách chỉ cho phép từ 10% đến 20%.
    discount_rate DECIMAL(5,4) NOT NULL,

    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    DATETIME     NOT NULL,
    updated_at    DATETIME,

    CONSTRAINT ck_holiday_range CHECK (end_date >= start_date),
    CONSTRAINT ck_holiday_rate  CHECK (discount_rate BETWEEN 0.10 AND 0.20)
);

CREATE INDEX idx_holiday_dates ON holiday_discounts (start_date, end_date);

-- Các ngày nghỉ lễ chính thức năm 2026, 2027 và 2028.
-- Ngày dương của Tết Nguyên Đán và Giỗ Tổ Hùng Vương đổi từ âm lịch theo múi giờ Việt Nam.
INSERT INTO holiday_discounts (name, start_date, end_date, discount_rate, active, created_at) VALUES
    ('Tết Dương lịch',                         '2026-01-01', '2026-01-01', 0.10, TRUE, CURRENT_TIMESTAMP),
    ('Tết Nguyên Đán',                         '2026-02-17', '2026-02-19', 0.20, TRUE, CURRENT_TIMESTAMP),
    ('Giỗ Tổ Hùng Vương',                      '2026-04-26', '2026-04-26', 0.10, TRUE, CURRENT_TIMESTAMP),
    ('Giải phóng miền Nam và Quốc tế Lao động', '2026-04-30', '2026-05-01', 0.15, TRUE, CURRENT_TIMESTAMP),
    ('Quốc khánh',                             '2026-09-02', '2026-09-02', 0.15, TRUE, CURRENT_TIMESTAMP),
    ('Tết Dương lịch',                         '2027-01-01', '2027-01-01', 0.10, TRUE, CURRENT_TIMESTAMP),
    ('Tết Nguyên Đán',                         '2027-02-06', '2027-02-08', 0.20, TRUE, CURRENT_TIMESTAMP),
    ('Giỗ Tổ Hùng Vương',                      '2027-04-16', '2027-04-16', 0.10, TRUE, CURRENT_TIMESTAMP),
    ('Giải phóng miền Nam và Quốc tế Lao động', '2027-04-30', '2027-05-01', 0.15, TRUE, CURRENT_TIMESTAMP),
    ('Quốc khánh',                             '2027-09-02', '2027-09-02', 0.15, TRUE, CURRENT_TIMESTAMP),
    ('Tết Dương lịch',                         '2028-01-01', '2028-01-01', 0.10, TRUE, CURRENT_TIMESTAMP),
    ('Tết Nguyên Đán',                         '2028-01-26', '2028-01-28', 0.20, TRUE, CURRENT_TIMESTAMP),
    ('Giỗ Tổ Hùng Vương',                      '2028-04-04', '2028-04-04', 0.10, TRUE, CURRENT_TIMESTAMP),
    ('Giải phóng miền Nam và Quốc tế Lao động', '2028-04-30', '2028-05-01', 0.15, TRUE, CURRENT_TIMESTAMP),
    ('Quốc khánh',                             '2028-09-02', '2028-09-02', 0.15, TRUE, CURRENT_TIMESTAMP);

-- Đơn đặt món cũng được giảm giá dịp lễ nên cần chỗ lưu số tiền giảm đã chốt lúc đặt.
-- Đơn cũ không được giảm gì nên mặc định 0.
ALTER TABLE dish_orders ADD COLUMN discount_amount DECIMAL(15,2) NOT NULL DEFAULT 0;
