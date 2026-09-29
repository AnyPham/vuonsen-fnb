-- Bo sung thu vien anh cho tung mon an.
--
-- Truoc day moi mon chi co dung mot anh dai dien. Trang chi tiet mon can
-- nhieu goc hon: phan don tai ban, nguyen lieu, can canh, do an kem.
--
-- Anh lay tu Pexels, giay phep cho dung mien phi. Tac gia tung anh ghi trong
-- doc/NGUON-ANH-BO-SUNG.md

-- Gỏi củ hũ dừa tôm thịt
INSERT INTO dish_images (dish_id, url, caption, sort_order)
SELECT id, v.url, v.caption, v.ord FROM dishes, (
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593079/goi-cu-hu-dua-tom-thit-a1.jpg' AS url, N'Phần dọn tại bàn' AS caption, 0 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593080/goi-cu-hu-dua-tom-thit-a2.jpg' AS url, N'Nguyên liệu chính' AS caption, 1 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593082/goi-cu-hu-dua-tom-thit-a3.jpg' AS url, N'Cận cảnh món' AS caption, 2 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593083/goi-cu-hu-dua-tom-thit-a4.jpg' AS url, N'Bày trong mâm tiệc' AS caption, 3 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593085/goi-cu-hu-dua-tom-thit-a5.jpg' AS url, N'Đồ ăn kèm' AS caption, 4 AS ord
) v WHERE dishes.slug = 'goi-cu-hu-dua-tom-thit';

-- Chả giò rế hải sản
INSERT INTO dish_images (dish_id, url, caption, sort_order)
SELECT id, v.url, v.caption, v.ord FROM dishes, (
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593087/cha-gio-re-hai-san-a1.jpg' AS url, N'Phần dọn tại bàn' AS caption, 0 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593088/cha-gio-re-hai-san-a2.jpg' AS url, N'Nguyên liệu chính' AS caption, 1 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593089/cha-gio-re-hai-san-a3.jpg' AS url, N'Cận cảnh món' AS caption, 2 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593090/cha-gio-re-hai-san-a4.jpg' AS url, N'Bày trong mâm tiệc' AS caption, 3 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593091/cha-gio-re-hai-san-a5.jpg' AS url, N'Đồ ăn kèm' AS caption, 4 AS ord
) v WHERE dishes.slug = 'cha-gio-re-hai-san';

-- Bánh xèo miền Tây
INSERT INTO dish_images (dish_id, url, caption, sort_order)
SELECT id, v.url, v.caption, v.ord FROM dishes, (
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593094/banh-xeo-mien-tay-a1.jpg' AS url, N'Phần dọn tại bàn' AS caption, 0 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593096/banh-xeo-mien-tay-a2.jpg' AS url, N'Nguyên liệu chính' AS caption, 1 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593097/banh-xeo-mien-tay-a3.jpg' AS url, N'Cận cảnh món' AS caption, 2 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593098/banh-xeo-mien-tay-a4.jpg' AS url, N'Bày trong mâm tiệc' AS caption, 3 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593100/banh-xeo-mien-tay-a5.jpg' AS url, N'Đồ ăn kèm' AS caption, 4 AS ord
) v WHERE dishes.slug = 'banh-xeo-mien-tay';

-- Bò lá lốt nướng than
INSERT INTO dish_images (dish_id, url, caption, sort_order)
SELECT id, v.url, v.caption, v.ord FROM dishes, (
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593101/bo-la-lot-nuong-than-a1.jpg' AS url, N'Phần dọn tại bàn' AS caption, 0 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593102/bo-la-lot-nuong-than-a2.jpg' AS url, N'Nguyên liệu chính' AS caption, 1 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593104/bo-la-lot-nuong-than-a3.jpg' AS url, N'Cận cảnh món' AS caption, 2 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593105/bo-la-lot-nuong-than-a4.jpg' AS url, N'Bày trong mâm tiệc' AS caption, 3 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593106/bo-la-lot-nuong-than-a5.jpg' AS url, N'Đồ ăn kèm' AS caption, 4 AS ord
) v WHERE dishes.slug = 'bo-la-lot-nuong-than';

-- Súp cua trứng bắc thảo
INSERT INTO dish_images (dish_id, url, caption, sort_order)
SELECT id, v.url, v.caption, v.ord FROM dishes, (
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593108/sup-cua-trung-bac-thao-a1.jpg' AS url, N'Phần dọn tại bàn' AS caption, 0 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593109/sup-cua-trung-bac-thao-a2.jpg' AS url, N'Nguyên liệu chính' AS caption, 1 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593111/sup-cua-trung-bac-thao-a3.jpg' AS url, N'Cận cảnh món' AS caption, 2 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593112/sup-cua-trung-bac-thao-a4.jpg' AS url, N'Bày trong mâm tiệc' AS caption, 3 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593114/sup-cua-trung-bac-thao-a5.jpg' AS url, N'Đồ ăn kèm' AS caption, 4 AS ord
) v WHERE dishes.slug = 'sup-cua-trung-bac-thao';

-- Nham bắp cải tôm khô
INSERT INTO dish_images (dish_id, url, caption, sort_order)
SELECT id, v.url, v.caption, v.ord FROM dishes, (
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593115/nham-bap-cai-tom-kho-a1.jpg' AS url, N'Phần dọn tại bàn' AS caption, 0 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593116/nham-bap-cai-tom-kho-a2.jpg' AS url, N'Nguyên liệu chính' AS caption, 1 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593117/nham-bap-cai-tom-kho-a3.jpg' AS url, N'Cận cảnh món' AS caption, 2 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593118/nham-bap-cai-tom-kho-a4.jpg' AS url, N'Bày trong mâm tiệc' AS caption, 3 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593119/nham-bap-cai-tom-kho-a5.jpg' AS url, N'Đồ ăn kèm' AS caption, 4 AS ord
) v WHERE dishes.slug = 'nham-bap-cai-tom-kho';

-- Cá lóc nướng trui cuốn bánh tráng
INSERT INTO dish_images (dish_id, url, caption, sort_order)
SELECT id, v.url, v.caption, v.ord FROM dishes, (
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593122/ca-loc-nuong-trui-cuon-banh-trang-a1.jpg' AS url, N'Phần dọn tại bàn' AS caption, 0 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593123/ca-loc-nuong-trui-cuon-banh-trang-a2.jpg' AS url, N'Nguyên liệu chính' AS caption, 1 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593125/ca-loc-nuong-trui-cuon-banh-trang-a3.jpg' AS url, N'Cận cảnh món' AS caption, 2 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593126/ca-loc-nuong-trui-cuon-banh-trang-a4.jpg' AS url, N'Bày trong mâm tiệc' AS caption, 3 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593127/ca-loc-nuong-trui-cuon-banh-trang-a5.jpg' AS url, N'Đồ ăn kèm' AS caption, 4 AS ord
) v WHERE dishes.slug = 'ca-loc-nuong-trui-cuon-banh-trang';

-- Gà ta hấp lá chanh
INSERT INTO dish_images (dish_id, url, caption, sort_order)
SELECT id, v.url, v.caption, v.ord FROM dishes, (
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593129/ga-ta-hap-la-chanh-a1.jpg' AS url, N'Phần dọn tại bàn' AS caption, 0 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593130/ga-ta-hap-la-chanh-a2.jpg' AS url, N'Nguyên liệu chính' AS caption, 1 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593132/ga-ta-hap-la-chanh-a3.jpg' AS url, N'Cận cảnh món' AS caption, 2 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593133/ga-ta-hap-la-chanh-a4.jpg' AS url, N'Bày trong mâm tiệc' AS caption, 3 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593135/ga-ta-hap-la-chanh-a5.jpg' AS url, N'Đồ ăn kèm' AS caption, 4 AS ord
) v WHERE dishes.slug = 'ga-ta-hap-la-chanh';

-- Tôm càng nướng muối ớt
INSERT INTO dish_images (dish_id, url, caption, sort_order)
SELECT id, v.url, v.caption, v.ord FROM dishes, (
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593136/tom-cang-nuong-muoi-ot-a1.jpg' AS url, N'Phần dọn tại bàn' AS caption, 0 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593138/tom-cang-nuong-muoi-ot-a2.jpg' AS url, N'Nguyên liệu chính' AS caption, 1 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593139/tom-cang-nuong-muoi-ot-a3.jpg' AS url, N'Cận cảnh món' AS caption, 2 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593140/tom-cang-nuong-muoi-ot-a4.jpg' AS url, N'Bày trong mâm tiệc' AS caption, 3 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593142/tom-cang-nuong-muoi-ot-a5.jpg' AS url, N'Đồ ăn kèm' AS caption, 4 AS ord
) v WHERE dishes.slug = 'tom-cang-nuong-muoi-ot';

-- Sườn non kho tộ
INSERT INTO dish_images (dish_id, url, caption, sort_order)
SELECT id, v.url, v.caption, v.ord FROM dishes, (
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593144/suon-non-kho-to-a1.jpg' AS url, N'Phần dọn tại bàn' AS caption, 0 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593145/suon-non-kho-to-a2.jpg' AS url, N'Nguyên liệu chính' AS caption, 1 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593146/suon-non-kho-to-a3.jpg' AS url, N'Cận cảnh món' AS caption, 2 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593147/suon-non-kho-to-a4.jpg' AS url, N'Bày trong mâm tiệc' AS caption, 3 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593148/suon-non-kho-to-a5.jpg' AS url, N'Đồ ăn kèm' AS caption, 4 AS ord
) v WHERE dishes.slug = 'suon-non-kho-to';

-- Cơm cháy chà bông kho quẹt
INSERT INTO dish_images (dish_id, url, caption, sort_order)
SELECT id, v.url, v.caption, v.ord FROM dishes, (
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593150/com-chay-cha-bong-kho-quet-a1.jpg' AS url, N'Phần dọn tại bàn' AS caption, 0 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593151/com-chay-cha-bong-kho-quet-a2.jpg' AS url, N'Nguyên liệu chính' AS caption, 1 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593152/com-chay-cha-bong-kho-quet-a3.jpg' AS url, N'Cận cảnh món' AS caption, 2 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593153/com-chay-cha-bong-kho-quet-a4.jpg' AS url, N'Bày trong mâm tiệc' AS caption, 3 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593155/com-chay-cha-bong-kho-quet-a5.jpg' AS url, N'Đồ ăn kèm' AS caption, 4 AS ord
) v WHERE dishes.slug = 'com-chay-cha-bong-kho-quet';

-- Cá kèo kho rau răm
INSERT INTO dish_images (dish_id, url, caption, sort_order)
SELECT id, v.url, v.caption, v.ord FROM dishes, (
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593158/ca-keo-kho-rau-ram-a1.jpg' AS url, N'Phần dọn tại bàn' AS caption, 0 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593159/ca-keo-kho-rau-ram-a2.jpg' AS url, N'Nguyên liệu chính' AS caption, 1 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593160/ca-keo-kho-rau-ram-a3.jpg' AS url, N'Cận cảnh món' AS caption, 2 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593161/ca-keo-kho-rau-ram-a4.jpg' AS url, N'Bày trong mâm tiệc' AS caption, 3 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593162/ca-keo-kho-rau-ram-a5.jpg' AS url, N'Đồ ăn kèm' AS caption, 4 AS ord
) v WHERE dishes.slug = 'ca-keo-kho-rau-ram';

-- Lẩu mắm miền Tây
INSERT INTO dish_images (dish_id, url, caption, sort_order)
SELECT id, v.url, v.caption, v.ord FROM dishes, (
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593164/lau-mam-mien-tay-a1.jpg' AS url, N'Phần dọn tại bàn' AS caption, 0 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593166/lau-mam-mien-tay-a2.jpg' AS url, N'Nguyên liệu chính' AS caption, 1 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593167/lau-mam-mien-tay-a3.jpg' AS url, N'Cận cảnh món' AS caption, 2 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593168/lau-mam-mien-tay-a4.jpg' AS url, N'Bày trong mâm tiệc' AS caption, 3 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593171/lau-mam-mien-tay-a5.png' AS url, N'Đồ ăn kèm' AS caption, 4 AS ord
) v WHERE dishes.slug = 'lau-mam-mien-tay';

-- Lẩu cá kèo lá giang
INSERT INTO dish_images (dish_id, url, caption, sort_order)
SELECT id, v.url, v.caption, v.ord FROM dishes, (
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593174/lau-ca-keo-la-giang-a1.jpg' AS url, N'Phần dọn tại bàn' AS caption, 0 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593175/lau-ca-keo-la-giang-a2.jpg' AS url, N'Nguyên liệu chính' AS caption, 1 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593176/lau-ca-keo-la-giang-a3.jpg' AS url, N'Cận cảnh món' AS caption, 2 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593177/lau-ca-keo-la-giang-a4.jpg' AS url, N'Bày trong mâm tiệc' AS caption, 3 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593178/lau-ca-keo-la-giang-a5.jpg' AS url, N'Đồ ăn kèm' AS caption, 4 AS ord
) v WHERE dishes.slug = 'lau-ca-keo-la-giang';

-- Lẩu gà lá é
INSERT INTO dish_images (dish_id, url, caption, sort_order)
SELECT id, v.url, v.caption, v.ord FROM dishes, (
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593180/lau-ga-la-e-a1.jpg' AS url, N'Phần dọn tại bàn' AS caption, 0 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593181/lau-ga-la-e-a2.jpg' AS url, N'Nguyên liệu chính' AS caption, 1 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593183/lau-ga-la-e-a3.jpg' AS url, N'Cận cảnh món' AS caption, 2 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593184/lau-ga-la-e-a4.jpg' AS url, N'Bày trong mâm tiệc' AS caption, 3 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593185/lau-ga-la-e-a5.jpg' AS url, N'Đồ ăn kèm' AS caption, 4 AS ord
) v WHERE dishes.slug = 'lau-ga-la-e';

-- Combo nướng than hoa
INSERT INTO dish_images (dish_id, url, caption, sort_order)
SELECT id, v.url, v.caption, v.ord FROM dishes, (
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593187/combo-nuong-than-hoa-a1.jpg' AS url, N'Phần dọn tại bàn' AS caption, 0 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593188/combo-nuong-than-hoa-a2.jpg' AS url, N'Nguyên liệu chính' AS caption, 1 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593189/combo-nuong-than-hoa-a3.jpg' AS url, N'Cận cảnh món' AS caption, 2 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593190/combo-nuong-than-hoa-a4.jpg' AS url, N'Bày trong mâm tiệc' AS caption, 3 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593191/combo-nuong-than-hoa-a5.jpg' AS url, N'Đồ ăn kèm' AS caption, 4 AS ord
) v WHERE dishes.slug = 'combo-nuong-than-hoa';

-- Heo quay giòn bì
INSERT INTO dish_images (dish_id, url, caption, sort_order)
SELECT id, v.url, v.caption, v.ord FROM dishes, (
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593192/heo-quay-gion-bi-a1.jpg' AS url, N'Phần dọn tại bàn' AS caption, 0 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593193/heo-quay-gion-bi-a2.jpg' AS url, N'Nguyên liệu chính' AS caption, 1 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593195/heo-quay-gion-bi-a3.jpg' AS url, N'Cận cảnh món' AS caption, 2 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593196/heo-quay-gion-bi-a4.jpg' AS url, N'Bày trong mâm tiệc' AS caption, 3 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593197/heo-quay-gion-bi-a5.jpg' AS url, N'Đồ ăn kèm' AS caption, 4 AS ord
) v WHERE dishes.slug = 'heo-quay-gion-bi';

-- Dê nướng ngũ vị
INSERT INTO dish_images (dish_id, url, caption, sort_order)
SELECT id, v.url, v.caption, v.ord FROM dishes, (
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593200/de-nuong-ngu-vi-a1.jpg' AS url, N'Phần dọn tại bàn' AS caption, 0 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593201/de-nuong-ngu-vi-a2.jpg' AS url, N'Nguyên liệu chính' AS caption, 1 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593202/de-nuong-ngu-vi-a3.jpg' AS url, N'Cận cảnh món' AS caption, 2 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593203/de-nuong-ngu-vi-a4.jpg' AS url, N'Bày trong mâm tiệc' AS caption, 3 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593204/de-nuong-ngu-vi-a5.jpg' AS url, N'Đồ ăn kèm' AS caption, 4 AS ord
) v WHERE dishes.slug = 'de-nuong-ngu-vi';

-- Chè bưởi Cần Thơ
INSERT INTO dish_images (dish_id, url, caption, sort_order)
SELECT id, v.url, v.caption, v.ord FROM dishes, (
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593206/che-buoi-can-tho-a1.jpg' AS url, N'Phần dọn tại bàn' AS caption, 0 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593207/che-buoi-can-tho-a2.png' AS url, N'Nguyên liệu chính' AS caption, 1 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593208/che-buoi-can-tho-a3.jpg' AS url, N'Cận cảnh món' AS caption, 2 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593209/che-buoi-can-tho-a4.jpg' AS url, N'Bày trong mâm tiệc' AS caption, 3 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593210/che-buoi-can-tho-a5.jpg' AS url, N'Đồ ăn kèm' AS caption, 4 AS ord
) v WHERE dishes.slug = 'che-buoi-can-tho';

-- Rau câu dừa lá dứa
INSERT INTO dish_images (dish_id, url, caption, sort_order)
SELECT id, v.url, v.caption, v.ord FROM dishes, (
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593212/rau-cau-dua-la-dua-a1.jpg' AS url, N'Phần dọn tại bàn' AS caption, 0 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593214/rau-cau-dua-la-dua-a2.jpg' AS url, N'Nguyên liệu chính' AS caption, 1 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593215/rau-cau-dua-la-dua-a3.jpg' AS url, N'Cận cảnh món' AS caption, 2 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593216/rau-cau-dua-la-dua-a4.jpg' AS url, N'Bày trong mâm tiệc' AS caption, 3 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593216/rau-cau-dua-la-dua-a5.jpg' AS url, N'Đồ ăn kèm' AS caption, 4 AS ord
) v WHERE dishes.slug = 'rau-cau-dua-la-dua';

-- Bánh da lợn hấp lá dứa
INSERT INTO dish_images (dish_id, url, caption, sort_order)
SELECT id, v.url, v.caption, v.ord FROM dishes, (
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593218/banh-da-lon-hap-la-dua-a1.jpg' AS url, N'Phần dọn tại bàn' AS caption, 0 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593219/banh-da-lon-hap-la-dua-a2.jpg' AS url, N'Nguyên liệu chính' AS caption, 1 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593220/banh-da-lon-hap-la-dua-a3.jpg' AS url, N'Cận cảnh món' AS caption, 2 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593221/banh-da-lon-hap-la-dua-a4.jpg' AS url, N'Bày trong mâm tiệc' AS caption, 3 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593222/banh-da-lon-hap-la-dua-a5.jpg' AS url, N'Đồ ăn kèm' AS caption, 4 AS ord
) v WHERE dishes.slug = 'banh-da-lon-hap-la-dua';

-- Trái cây theo mùa
INSERT INTO dish_images (dish_id, url, caption, sort_order)
SELECT id, v.url, v.caption, v.ord FROM dishes, (
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593224/trai-cay-theo-mua-a1.jpg' AS url, N'Phần dọn tại bàn' AS caption, 0 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593225/trai-cay-theo-mua-a2.jpg' AS url, N'Nguyên liệu chính' AS caption, 1 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593226/trai-cay-theo-mua-a3.jpg' AS url, N'Cận cảnh món' AS caption, 2 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593227/trai-cay-theo-mua-a4.jpg' AS url, N'Bày trong mâm tiệc' AS caption, 3 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593229/trai-cay-theo-mua-a5.jpg' AS url, N'Đồ ăn kèm' AS caption, 4 AS ord
) v WHERE dishes.slug = 'trai-cay-theo-mua';

-- Nước sâm lá dứa nhà nấu
INSERT INTO dish_images (dish_id, url, caption, sort_order)
SELECT id, v.url, v.caption, v.ord FROM dishes, (
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593231/nuoc-sam-la-dua-nha-nau-a1.jpg' AS url, N'Phần dọn tại bàn' AS caption, 0 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593232/nuoc-sam-la-dua-nha-nau-a2.jpg' AS url, N'Nguyên liệu chính' AS caption, 1 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593233/nuoc-sam-la-dua-nha-nau-a3.jpg' AS url, N'Cận cảnh món' AS caption, 2 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593234/nuoc-sam-la-dua-nha-nau-a4.jpg' AS url, N'Bày trong mâm tiệc' AS caption, 3 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593236/nuoc-sam-la-dua-nha-nau-a5.jpg' AS url, N'Đồ ăn kèm' AS caption, 4 AS ord
) v WHERE dishes.slug = 'nuoc-sam-la-dua-nha-nau';

-- Dừa tươi Bến Tre
INSERT INTO dish_images (dish_id, url, caption, sort_order)
SELECT id, v.url, v.caption, v.ord FROM dishes, (
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593238/dua-tuoi-ben-tre-a1.jpg' AS url, N'Phần dọn tại bàn' AS caption, 0 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593239/dua-tuoi-ben-tre-a2.jpg' AS url, N'Nguyên liệu chính' AS caption, 1 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593240/dua-tuoi-ben-tre-a3.jpg' AS url, N'Cận cảnh món' AS caption, 2 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593241/dua-tuoi-ben-tre-a4.jpg' AS url, N'Bày trong mâm tiệc' AS caption, 3 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593242/dua-tuoi-ben-tre-a5.jpg' AS url, N'Đồ ăn kèm' AS caption, 4 AS ord
) v WHERE dishes.slug = 'dua-tuoi-ben-tre';

-- Rượu nếp than ủ 12 tháng
INSERT INTO dish_images (dish_id, url, caption, sort_order)
SELECT id, v.url, v.caption, v.ord FROM dishes, (
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593244/ruou-nep-than-u-12-thang-a1.jpg' AS url, N'Phần dọn tại bàn' AS caption, 0 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593245/ruou-nep-than-u-12-thang-a2.jpg' AS url, N'Nguyên liệu chính' AS caption, 1 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593246/ruou-nep-than-u-12-thang-a3.jpg' AS url, N'Cận cảnh món' AS caption, 2 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593247/ruou-nep-than-u-12-thang-a4.jpg' AS url, N'Bày trong mâm tiệc' AS caption, 3 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593248/ruou-nep-than-u-12-thang-a5.jpg' AS url, N'Đồ ăn kèm' AS caption, 4 AS ord
) v WHERE dishes.slug = 'ruou-nep-than-u-12-thang';

-- Bia & nước ngọt
INSERT INTO dish_images (dish_id, url, caption, sort_order)
SELECT id, v.url, v.caption, v.ord FROM dishes, (
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593250/bia-nuoc-ngot-a1.jpg' AS url, N'Phần dọn tại bàn' AS caption, 0 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593251/bia-nuoc-ngot-a2.jpg' AS url, N'Nguyên liệu chính' AS caption, 1 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593252/bia-nuoc-ngot-a3.png' AS url, N'Cận cảnh món' AS caption, 2 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593252/bia-nuoc-ngot-a4.jpg' AS url, N'Bày trong mâm tiệc' AS caption, 3 AS ord
  UNION ALL
  SELECT 'https://res.cloudinary.com/b59sgbhx/image/upload/v1788593254/bia-nuoc-ngot-a5.jpg' AS url, N'Đồ ăn kèm' AS caption, 4 AS ord
) v WHERE dishes.slug = 'bia-nuoc-ngot';
