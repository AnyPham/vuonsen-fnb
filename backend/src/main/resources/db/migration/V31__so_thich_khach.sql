-- So thich khach tu khai trong trang ho so, phuc vu goi y thong minh.
--
-- Ho so de goi y gom hai phan: phan he thong tu ghi (don dat tiec, don dat mon, luot xem)
-- va phan khach tu ke khai. Bang nay giu phan thu hai. Khach moi dang ky chua dat gi thi
-- chi can khai so thich la trang chu da goi y rieng, khong phai doi toi lan dat dau tien.
--
-- Moi khach mot dong. Moi cot deu cho phep trong: khach khai toi dau dung toi do.
--   loai_tiec        danh sach loai tiec hay to chuc, ngan cach dau phay (WEDDING,FAMILY)
--   so_khach         so khach thuong moi
--   kieu_khong_gian  TRONG_NHA hoac NGOAI_TROI
--   tam_gia          TIET_KIEM, VUA_PHAI hoac CAO_CAP
--   an_chay          uu tien mon chay
--
-- Khong luu ho ten hay gioi tinh lam thuoc tinh so khop: ten khong noi gi ve nhu cau, con suy
-- gioi tinh hay tuoi tu ten la suy dien thieu can cu.

CREATE TABLE so_thich_khach (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id          BIGINT       NOT NULL,
    loai_tiec        VARCHAR(120) NULL,
    so_khach         INT          NULL,
    kieu_khong_gian  VARCHAR(20)  NULL,
    tam_gia          VARCHAR(20)  NULL,
    an_chay          BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at       DATETIME     NOT NULL,
    updated_at       DATETIME     NULL,
    CONSTRAINT uk_so_thich_khach_user UNIQUE (user_id),
    CONSTRAINT fk_so_thich_khach_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
