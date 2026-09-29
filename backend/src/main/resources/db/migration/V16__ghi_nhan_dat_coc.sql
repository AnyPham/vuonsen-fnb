/*
 * Ghi nhận tiền đặt cọc của đơn đặt tiệc.
 *
 * Website chưa nối cổng thanh toán, khách vẫn chuyển khoản hoặc đóng tiền mặt tại nhà hàng.
 * Trước đây khoản cọc chỉ nằm trong báo giá rồi thôi, thu xong ghi ra sổ giấy, nên mở đơn
 * lên không biết đơn nào đã cọc. Ba cột dưới đây cho quản trị ghi lại ngay trên hệ thống.
 *
 * deposit_amount là số phải đóng, chốt tại thời điểm đặt theo tỉ lệ trong cấu hình, giữ lại
 * để sau này đổi tỉ lệ thì đơn cũ không bị tính lại.
 */
ALTER TABLE bookings ADD COLUMN deposit_amount  DECIMAL(15,2) NOT NULL DEFAULT 0;
ALTER TABLE bookings ADD COLUMN deposit_paid    DECIMAL(15,2) NOT NULL DEFAULT 0;
ALTER TABLE bookings ADD COLUMN deposit_paid_at DATETIME      NULL;
ALTER TABLE bookings ADD COLUMN deposit_method  VARCHAR(20)   NULL;   -- CASH / TRANSFER

-- Đơn đã có trước khi có ba cột này: suy ra khoản phải cọc từ tổng tiền, 30% theo cấu hình
UPDATE bookings SET deposit_amount = ROUND(total_amount * 0.3, 0);

/*
 * Đơn đã xác nhận nghĩa là nhà hàng đã giữ ngày cho khách, mà giữ ngày thì phải có cọc.
 * Đánh dấu các đơn đó đã thu đủ, lấy mốc thời gian là lúc đơn được tạo.
 */
UPDATE bookings
SET deposit_paid    = deposit_amount,
    deposit_paid_at = created_at,
    deposit_method  = 'TRANSFER'
WHERE status IN ('CONFIRMED', 'COMPLETED');
