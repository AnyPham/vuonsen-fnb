-- Nới cột token của bảng refresh_tokens từ 255 lên 512 ký tự.
--
-- Refresh token là một chuỗi JWT, độ dài thay đổi theo độ dài email và theo các trường bên trong.
-- Với email dài hoặc khi thêm mã định danh riêng cho từng token, chuỗi vượt quá 255 ký tự và lần
-- đăng nhập đó hỏng. 512 ký tự đủ rộng cho cả email dài; ràng buộc duy nhất vẫn giữ nguyên vì
-- 512 ký tự utf8mb4 là 2048 byte, còn trong giới hạn 3072 byte của chỉ mục InnoDB.
ALTER TABLE refresh_tokens MODIFY COLUMN token VARCHAR(512) NOT NULL;
