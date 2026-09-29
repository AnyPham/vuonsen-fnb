-- Đặt món lẻ: giao tận nhà hoặc đặt trước rồi tới ăn tại chỗ.
--
-- Đây là luồng đặt thứ hai, tách hẳn khỏi bảng bookings. Lý do không dùng chung:
-- đơn đặt tiệc gắn với một không gian và một gói tiệc trọn gói, tính tiền theo mâm;
-- còn đơn đặt món tính theo từng phần khách chọn, có thể không cần không gian nào.
-- Nhét cả hai vào một bảng thì quá nửa số cột luôn để trống.
--
-- Mục đích nghiệp vụ: nhận nhóm khách nhỏ chưa cần đặt trọn sảnh tiệc lớn, và nhận
-- khách chỉ muốn mua món mang về.

CREATE TABLE dish_orders (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    code             VARCHAR(30)  NOT NULL,

    -- DELIVERY: giao tận nhà. DINE_IN: đặt trước rồi tới ăn tại chỗ.
    fulfillment_type VARCHAR(20)  NOT NULL,

    customer_name    VARCHAR(120) NOT NULL,
    customer_phone   VARCHAR(20)  NOT NULL,
    customer_email   VARCHAR(160),

    -- Chỉ đơn giao tận nhà mới có địa chỉ
    delivery_address VARCHAR(400),

    -- Chỉ đơn ăn tại chỗ mới có số khách
    guest_count      INT,

    -- Thời điểm khách muốn nhận món hoặc tới ăn
    serve_at         DATETIME     NOT NULL,
    note             VARCHAR(600),

    -- Tiền chốt tại thời điểm đặt, không tính lại khi bảng giá đổi
    subtotal         DECIMAL(15,2) NOT NULL,
    delivery_fee     DECIMAL(15,2) NOT NULL DEFAULT 0,
    vat_amount       DECIMAL(15,2) NOT NULL,
    total            DECIMAL(15,2) NOT NULL,

    status           VARCHAR(20)  NOT NULL,
    user_id          BIGINT,
    created_at       DATETIME     NOT NULL,
    updated_at       DATETIME,

    CONSTRAINT uk_dish_order_code UNIQUE (code),
    CONSTRAINT fk_dish_order_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL
);

CREATE INDEX idx_dish_order_status ON dish_orders (status);
CREATE INDEX idx_dish_order_serve  ON dish_orders (serve_at);

-- Từng dòng món trong đơn.
--
-- Cố ý chép lại tên món và đơn giá vào đây thay vì chỉ giữ khóa ngoại. Bảng giá thay
-- đổi theo thời gian, mà đơn cũ phải giữ đúng con số đã chốt với khách lúc đặt. Nếu
-- chỉ nối sang bảng dishes thì hôm sau tăng giá là hóa đơn cũ tự đổi theo, tra cứu
-- lại sẽ lệch với số tiền khách đã trả.
CREATE TABLE dish_order_items (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id   BIGINT       NOT NULL,
    dish_id    BIGINT,
    dish_name  VARCHAR(160) NOT NULL,
    unit_price DECIMAL(15,2) NOT NULL,
    quantity   INT          NOT NULL,
    line_total DECIMAL(15,2) NOT NULL,

    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES dish_orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_order_item_dish  FOREIGN KEY (dish_id)  REFERENCES dishes (id)      ON DELETE SET NULL
);

CREATE INDEX idx_order_item_order ON dish_order_items (order_id);
