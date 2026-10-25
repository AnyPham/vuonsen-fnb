-- Danh dau mon chay de khach loc duoc thuc don va tro ly tra loi dung.
--
-- Truoc do co so du lieu khong co cho nao ghi mon nao chay. Tro ly tu doan theo ten
-- mon va doan sai: no xep Nom hoa chuoi vao danh sach chay, trong khi nguyen lieu co
-- nuoc mam chua ngot, tuc nuoc mam thuong. Voi khach an chay truong thi do la sai
-- nghiem trong, nen phai co du lieu that chu khong de mo hinh doan.
--
-- Tieu chi: khong thit, hai san, nuoc mam thuong, dau hao, trung, mo hay nuoc dung
-- tu dong vat. Van cho phep sua va hanh toi, dung muc chay thong dung cua nha hang
-- Viet chu khong phai chay thuan hay chay nha Phat. Do uong co con khong tinh la
-- chay du khong co thit, vi an chay kieng ca do co con.
--
-- Sinh bang cong-cu, phan loai doc tu truong nguyen lieu cua tung mon.

ALTER TABLE dishes ADD COLUMN vegetarian BOOLEAN NOT NULL DEFAULT FALSE;

-- 41 mon chay tren tong 126 mon

-- Khai vi (3)
UPDATE dishes SET vegetarian = TRUE WHERE slug = 'ca-tim-nuong-mo-hanh';
UPDATE dishes SET vegetarian = TRUE WHERE slug = 'dau-hu-chien-sa-ot';
UPDATE dishes SET vegetarian = TRUE WHERE slug = 'khoai-lang-ken-chien';

-- Mon chinh (3)
UPDATE dishes SET vegetarian = TRUE WHERE slug = 'rau-muong-xao-toi';
UPDATE dishes SET vegetarian = TRUE WHERE slug = 'dau-hu-sot-ca-chua';
UPDATE dishes SET vegetarian = TRUE WHERE slug = 'nam-kho-tieu-chay';

-- Lau va nuong (2)
UPDATE dishes SET vegetarian = TRUE WHERE slug = 'lau-nam-chay';
UPDATE dishes SET vegetarian = TRUE WHERE slug = 'nam-nuong-sa-te';

-- Trang mieng (13)
UPDATE dishes SET vegetarian = TRUE WHERE slug = 'che-ba-mau';
UPDATE dishes SET vegetarian = TRUE WHERE slug = 'che-troi-nuoc';
UPDATE dishes SET vegetarian = TRUE WHERE slug = 'che-hat-sen-nhan-nhuc';
UPDATE dishes SET vegetarian = TRUE WHERE slug = 'che-khoai-deo';
UPDATE dishes SET vegetarian = TRUE WHERE slug = 'suong-sa-hat-luu';
UPDATE dishes SET vegetarian = TRUE WHERE slug = 'banh-chuoi-hap-nuoc-cot-dua';
UPDATE dishes SET vegetarian = TRUE WHERE slug = 'kem-dua-trai-dua';
UPDATE dishes SET vegetarian = TRUE WHERE slug = 'sinh-to-bo-kem';
UPDATE dishes SET vegetarian = TRUE WHERE slug = 'tau-hu-nuoc-duong';
UPDATE dishes SET vegetarian = TRUE WHERE slug = 'xoi-xoai-nuoc-cot-dua';
UPDATE dishes SET vegetarian = TRUE WHERE slug = 'che-thai-sau-rieng';
UPDATE dishes SET vegetarian = TRUE WHERE slug = 'banh-cam-nhan-dau-xanh';
UPDATE dishes SET vegetarian = TRUE WHERE slug = 'suu-chua-nep-cam';

-- Do uong (14)
UPDATE dishes SET vegetarian = TRUE WHERE slug = 'ca-phe-sua-da';
UPDATE dishes SET vegetarian = TRUE WHERE slug = 'ca-phe-den-da';
UPDATE dishes SET vegetarian = TRUE WHERE slug = 'ca-phe-muoi';
UPDATE dishes SET vegetarian = TRUE WHERE slug = 'tra-sen-vang';
UPDATE dishes SET vegetarian = TRUE WHERE slug = 'tra-dao-cam-sa';
UPDATE dishes SET vegetarian = TRUE WHERE slug = 'tra-tac-mat-ong';
UPDATE dishes SET vegetarian = TRUE WHERE slug = 'nuoc-mia-tac';
UPDATE dishes SET vegetarian = TRUE WHERE slug = 'nuoc-chanh-day';
UPDATE dishes SET vegetarian = TRUE WHERE slug = 'sinh-to-xoai';
UPDATE dishes SET vegetarian = TRUE WHERE slug = 'nuoc-ep-dua-hau';
UPDATE dishes SET vegetarian = TRUE WHERE slug = 'nuoc-rau-ma-dau-xanh';
UPDATE dishes SET vegetarian = TRUE WHERE slug = 'tra-atiso-da-lat';
UPDATE dishes SET vegetarian = TRUE WHERE slug = 'soda-chanh-bac-ha';
UPDATE dishes SET vegetarian = TRUE WHERE slug = 'nuoc-dua-tac-nong';

-- Mon co san tu dau (6)
UPDATE dishes SET vegetarian = TRUE WHERE slug = 'che-buoi-can-tho';
UPDATE dishes SET vegetarian = TRUE WHERE slug = 'rau-cau-dua-la-dua';
UPDATE dishes SET vegetarian = TRUE WHERE slug = 'banh-da-lon-hap-la-dua';
UPDATE dishes SET vegetarian = TRUE WHERE slug = 'trai-cay-theo-mua';
UPDATE dishes SET vegetarian = TRUE WHERE slug = 'nuoc-sam-la-dua-nha-nau';
UPDATE dishes SET vegetarian = TRUE WHERE slug = 'dua-tuoi-ben-tre';
