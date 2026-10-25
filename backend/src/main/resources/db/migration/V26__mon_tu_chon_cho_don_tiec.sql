-- Món tự chọn của đơn đặt tiệc.
--
-- Dành cho tiệc nhỏ ở sảnh nhỏ: khách không lấy gói tiệc cố định mà tự chọn từng món.
-- Tách bảng riêng thay vì nhét vào bookings, vì một đơn có nhiều món.
--
-- Giá đơn vị chép lại tại thời điểm đặt chứ không tham chiếu sang bảng dishes: nhà hàng
-- đổi giá món về sau thì đơn đã chốt vẫn giữ nguyên số tiền khách đã đồng ý.
CREATE TABLE booking_dishes (
    id         BIGINT         NOT NULL AUTO_INCREMENT,
    booking_id BIGINT         NOT NULL,
    dish_id    BIGINT         NOT NULL,
    ten_mon    VARCHAR(255)   NOT NULL,
    so_luong   INT            NOT NULL,
    don_gia    DECIMAL(15, 2) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_booking_dishes_booking FOREIGN KEY (booking_id) REFERENCES bookings (id),
    CONSTRAINT fk_booking_dishes_dish FOREIGN KEY (dish_id) REFERENCES dishes (id)
);

CREATE INDEX ix_booking_dishes_booking ON booking_dishes (booking_id);
