/*
 * Sổ thanh toán.
 *
 * Trước đây tiền chỉ được ghi thành hai cột trên đơn đặt tiệc: phải cọc bao nhiêu, đã thu
 * bao nhiêu. Cách đó không trả lời được những câu hỏi bình thường của một nhà hàng: khoản
 * này thu ngày nào, ai thu, thu bằng tiền mặt hay chuyển khoản, nội dung chuyển khoản là gì,
 * và khách đã bấm chuyển khoản nhưng kế toán chưa đối soát thì nằm ở đâu.
 *
 * Bảng payments là sổ ghi từng lần thu. Số tiền đã thu trên đơn vẫn giữ để đọc nhanh, nhưng
 * nguồn sự thật là các dòng trong sổ này.
 */
CREATE TABLE payments (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    code           VARCHAR(30)   NOT NULL,      -- TT-B-20260927-0001

    -- Một phiếu thu thuộc về đúng một đơn, hoặc đơn đặt tiệc hoặc đơn đặt món
    order_type     VARCHAR(20)   NOT NULL,      -- BOOKING / DISH_ORDER
    booking_id     BIGINT        NULL,
    dish_order_id  BIGINT        NULL,

    purpose        VARCHAR(20)   NOT NULL,      -- DEPOSIT / BALANCE / FULL
    amount         DECIMAL(15,2) NOT NULL,
    method         VARCHAR(20)   NOT NULL,      -- CASH / TRANSFER / VIETQR / COD
    status         VARCHAR(20)   NOT NULL,      -- PENDING / CONFIRMED / CANCELLED

    -- Nội dung chuyển khoản hoặc mã giao dịch ngân hàng, dùng để đối soát
    reference      VARCHAR(120)  NULL,
    note           VARCHAR(500)  NULL,

    confirmed_by   VARCHAR(160)  NULL,
    confirmed_at   DATETIME      NULL,
    created_at     DATETIME      NOT NULL,
    updated_at     DATETIME      NULL,

    CONSTRAINT uk_payment_code UNIQUE (code),
    CONSTRAINT fk_payment_booking    FOREIGN KEY (booking_id)    REFERENCES bookings (id)    ON DELETE CASCADE,
    CONSTRAINT fk_payment_dish_order FOREIGN KEY (dish_order_id) REFERENCES dish_orders (id) ON DELETE CASCADE,
    CONSTRAINT ck_payment_amount CHECK (amount > 0)
);

-- Màn hình đối soát luôn lọc theo trạng thái rồi sắp theo thời gian
CREATE INDEX idx_payment_status ON payments (status, created_at);

/*
 * Đơn đặt món trước đây không có chỗ ghi tiền, vì mặc định coi như trả khi nhận.
 * Nay khách chọn được hình thức nên phải lưu lại đã thu bao nhiêu và chọn cách nào.
 */
ALTER TABLE dish_orders ADD COLUMN paid_amount    DECIMAL(15,2) NOT NULL DEFAULT 0;
ALTER TABLE dish_orders ADD COLUMN payment_method VARCHAR(20)   NULL;   -- COD / VIETQR / CASH

/*
 * Dựng lại phiếu thu cho những khoản đã ghi nhận trước khi có bảng này, để sổ thanh toán
 * không bắt đầu từ con số không. Mã phiếu suy từ mã đơn nên chắc chắn không trùng nhau.
 */
INSERT INTO payments (code, order_type, booking_id, purpose, amount, method, status,
                      reference, note, confirmed_by, confirmed_at, created_at)
SELECT CONCAT('TT-B-', SUBSTRING(b.code, 4)),
       'BOOKING', b.id, 'DEPOSIT', b.deposit_paid,
       COALESCE(b.deposit_method, 'TRANSFER'), 'CONFIRMED',
       b.code, 'Dựng lại từ khoản cọc đã ghi nhận trước khi có sổ thanh toán',
       'admin@vuonsen.vn', COALESCE(b.deposit_paid_at, b.created_at), COALESCE(b.deposit_paid_at, b.created_at)
FROM bookings b
WHERE b.deposit_paid > 0;

-- Đơn đặt món đã hoàn thành nghĩa là khách đã nhận món và đã trả tiền
UPDATE dish_orders SET paid_amount = total, payment_method = 'CASH' WHERE status = 'COMPLETED';

INSERT INTO payments (code, order_type, dish_order_id, purpose, amount, method, status,
                      reference, note, confirmed_by, confirmed_at, created_at)
SELECT CONCAT('TT-M-', SUBSTRING(d.code, 4)),
       'DISH_ORDER', d.id, 'FULL', d.total, 'CASH', 'CONFIRMED',
       d.code, 'Dựng lại từ đơn đã hoàn thành trước khi có sổ thanh toán',
       'admin@vuonsen.vn', d.serve_at, d.serve_at
FROM dish_orders d
WHERE d.status = 'COMPLETED';
