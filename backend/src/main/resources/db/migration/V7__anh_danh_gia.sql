-- Bảng lưu ảnh khách gửi kèm đánh giá.
--
-- Trước đây bảng reviews không có chỗ nào cho ảnh, nên khách chỉ gửi được chữ.
-- Ảnh thật của khách là thứ thuyết phục người đọc hơn hẳn lời khen suông, nên
-- tách thành bảng con để một đánh giá kèm được nhiều ảnh.
--
-- Cấu trúc cố ý giống space_images cho dễ đọc: khóa ngoại xóa theo bản ghi cha,
-- thêm cột thứ tự để giữ đúng trình tự khách tải lên.
CREATE TABLE review_images (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    review_id  BIGINT       NOT NULL,
    url        VARCHAR(500) NOT NULL,
    sort_order INT          NOT NULL DEFAULT 0,
    CONSTRAINT fk_reviewimg_review FOREIGN KEY (review_id) REFERENCES reviews (id) ON DELETE CASCADE
);

CREATE INDEX idx_reviewimg_review ON review_images (review_id);
