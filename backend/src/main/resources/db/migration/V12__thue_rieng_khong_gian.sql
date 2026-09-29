-- Cho phép đặt không gian mà không kèm gói tiệc.
--
-- Khách chỉ cần mặt bằng (tự lo ăn uống, hoặc sự kiện không có tiệc) thì đơn không
-- có gói, nên package_id phải được để trống.
--
-- Các cột table_count, unit_price, food_amount vẫn giữ NOT NULL: đơn chỉ thuê không
-- gian ghi 0 vào ba cột này. Để 0 thay vì NULL thì các phép cộng tiền ở báo cáo và
-- thống kê không phải xử lý giá trị rỗng.
ALTER TABLE bookings MODIFY COLUMN package_id BIGINT NULL;
