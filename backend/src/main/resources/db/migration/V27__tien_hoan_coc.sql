-- Số tiền hoàn lại cho khách khi hủy đơn đã đóng cọc.
--
-- Trước đây hệ thống chỉ ghi một câu mô tả chính sách vào lịch sử trạng thái, ví dụ
-- "hoàn 50% tiền cọc", mà không tính ra con số. Nhân viên phải tự nhẩm, và khách không
-- có chỗ nào tra được mình được nhận lại bao nhiêu.
--
-- Chốt cả tỉ lệ lẫn số tiền tại thời điểm hủy: nhà hàng đổi chính sách hoàn tiền về sau
-- thì đơn đã hủy vẫn giữ nguyên con số hai bên đã thống nhất.
ALTER TABLE bookings ADD COLUMN refund_rate DECIMAL(5, 2) NULL;
ALTER TABLE bookings ADD COLUMN refund_amount DECIMAL(15, 2) NULL;
ALTER TABLE bookings ADD COLUMN refund_note VARCHAR(255) NULL;
