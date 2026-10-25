-- Lưu đăng ký nhận thông báo đẩy của trình duyệt.
--
-- Mỗi trình duyệt trên mỗi máy là một bản ghi riêng, định danh bằng endpoint do dịch vụ
-- push của hãng trình duyệt cấp. Không gắn với tài khoản, vì khách vãng lai cũng đăng ký
-- nhận khuyến mãi được mà không cần tạo tài khoản.
CREATE TABLE push_subscriptions (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    endpoint    VARCHAR(512) NOT NULL,
    p256dh      VARCHAR(255) NOT NULL,
    auth        VARCHAR(255) NOT NULL,
    ngon_ngu    VARCHAR(8)   NOT NULL DEFAULT 'vi',
    tao_luc     DATETIME     NOT NULL,
    gui_loi_luc DATETIME     NULL,
    PRIMARY KEY (id)
);

-- Endpoint là định danh duy nhất của một đăng ký. Khóa duy nhất để khách bấm đăng ký
-- nhiều lần trên cùng một máy cũng chỉ sinh một bản ghi, tránh gửi trùng thông báo.
CREATE UNIQUE INDEX ux_push_endpoint ON push_subscriptions (endpoint);
