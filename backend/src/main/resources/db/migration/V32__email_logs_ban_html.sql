-- Lưu thêm bản HTML của thư vào nhật ký.
-- Cột body giữ bản chữ trơn để đọc ngay trong cơ sở dữ liệu; html_body giữ đúng lá thư
-- khách nhận được, để xem lại giao diện thư khi chưa bật máy chủ thư hoặc khi cần đối chiếu.
-- MEDIUMTEXT vì thư hóa đơn có bảng kê nhiều món có thể vượt giới hạn 64KB của TEXT.
ALTER TABLE email_logs ADD COLUMN html_body MEDIUMTEXT NULL AFTER body;
