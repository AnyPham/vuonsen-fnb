-- Nhat ky luot xem, de thu thap phan hoi ngam (implicit feedback).
--
-- Hai kenh thu thap thong tin khach: kenh tuong minh la nhung gi khach chu dong de lai,
-- tuc don dat va phieu thu; kenh ngam la nhung gi suy ra tu hanh vi duyet. Bang nay lo
-- kenh thu hai.
--
-- Tin hieu dang gia nhat lay tu bang nay la "xem nhieu lan ma chua dat": khach mo trang
-- mot sanh bon nam lan trong hai tuan nhung chua gui don nao thi do la y dinh ro rang,
-- chi con vuong gi do. Don dat chi noi duoc khach da quyet; luot xem noi duoc khach dang
-- can nhac gi, tuc noi som hon mot buoc.
--
-- user_id cho phep NULL vi khach chua dang nhap van xem trang. Ban ghi cua khach an danh
-- khong noi duoc voi lan truy cap sau, nhung van dem duoc vao con so "duoc xem nhieu nhat"
-- cua toan he thong.

CREATE TABLE luot_xem (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT       NULL,
    loai_doi_tuong  VARCHAR(20)  NOT NULL,
    doi_tuong_id    BIGINT       NOT NULL,
    xem_luc         DATETIME     NOT NULL,
    CONSTRAINT fk_luot_xem_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Truy van chinh: lay luot xem gan day cua mot khach, moi truoc. Chi muc theo dung thu tu do.
CREATE INDEX idx_luot_xem_khach ON luot_xem (user_id, xem_luc DESC);

-- Truy van phu: dem luot xem cua tung doi tuong de xep hang "duoc xem nhieu nhat".
CREATE INDEX idx_luot_xem_doi_tuong ON luot_xem (loai_doi_tuong, doi_tuong_id);
