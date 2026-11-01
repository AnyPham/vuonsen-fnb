-- Xác thực email khi đăng ký tài khoản.
--
-- Mã xác nhận lưu ngay trên bảng users thay vì dựng một bảng riêng, vì mỗi tài khoản chỉ
-- có nhiều nhất một mã đang chờ: mã mới cấp thì mã cũ hết giá trị. Bảng riêng chỉ đáng
-- làm khi cần giữ lịch sử nhiều mã cùng lúc, ở đây không có nhu cầu đó.
--
-- Hai cột đều cho phép rỗng: tài khoản đã xác thực xong thì xóa mã đi, không giữ lại.

ALTER TABLE users ADD COLUMN verification_code VARCHAR(10) NULL;

-- Tách thành hai câu ALTER riêng. Gộp nhiều mệnh đề ADD COLUMN vào một câu tuy hợp lệ
-- với MySQL nhưng H2 không chấp nhận, mà bộ kiểm thử đơn vị lại chạy trên H2.
ALTER TABLE users ADD COLUMN verification_expires_at DATETIME NULL;

-- Tài khoản lập trước khi có tính năng này coi như đã xác thực, không bắt xác thực lại.
UPDATE users SET verification_code = NULL, verification_expires_at = NULL;
