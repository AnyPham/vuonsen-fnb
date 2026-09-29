-- Một trăm món bổ sung cho thực đơn, kèm bản tiếng Anh và ảnh minh họa.
--
-- Tệp này do cong-cu/du-lieu-mau/mon-an/sinh-migration.js sinh ra, đừng sửa tay:
-- sửa ở tệp nguồn rồi chạy lại, nếu không lần sinh sau sẽ ghi đè mất.
--
-- Ảnh lấy qua Openverse (phần lớn từ Flickr) và Wikimedia Commons, chỉ nhận giấy phép
-- CC0, phạm vi công cộng, CC BY và CC BY-SA. Cố ý loại nhóm giấy phép NC vì website
-- nhà hàng là mục đích thương mại. Bảng ghi nguồn từng ảnh: cong-cu/du-lieu-mau/mon-an/ghi-nguon-anh.md.
-- Món nào chưa có ảnh đúng chủ đề thì để trống, giao diện hiện khối giữ chỗ.
--
-- Dùng INSERT ... SELECT để lấy id danh mục theo mã thay vì ghi cứng số: số id phụ
-- thuộc thứ tự chạy migration, cơ sở dữ liệu dựng lại từ đầu có thể ra id khác.

-- Gỏi cuốn tôm thịt
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Gỏi cuốn tôm thịt', 'Fresh prawn and pork rolls', 'goi-cuon-tom-thit',
       'Bánh tráng cuốn tay, chấm tương đậu phộng', 'Hand-rolled rice paper with peanut dipping sauce',
       95000, 'https://live.staticflickr.com/2095/2393234452_5d279033cc_b.jpg', 0, 1, 100,
       'Bánh tráng, tôm sú luộc, thịt ba chỉ, bún tươi, hẹ, xà lách, rau thơm, tương đậu phộng.', 'Rice paper, boiled tiger prawn, pork belly, fresh vermicelli, garlic chives, lettuce, herbs, peanut dipping sauce.',
       'Cuốn tay từng cuốn khi khách gọi. Bánh tráng chỉ nhúng ẩm chứ không ngâm, để cuốn không bị nhão và vẫn nhìn rõ con tôm bên trong.', 'Rolled by hand to order. The rice paper is only dampened, not soaked, so the roll stays firm and the prawn shows through.',
       'Cuốn sẵn để lâu bánh tráng sẽ dai, bên mình chỉ cuốn khi khách gọi. Tương đậu phộng có đậu phộng, khách dị ứng báo trước để đổi nước chấm.', 'Rolled too far ahead the rice paper turns tough, so we roll to order. The dipping sauce contains peanuts; tell us if anyone is allergic and we will change it.',
       'Phần 10 cuốn', '10 rolls', 15,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'khaivi';

-- Bánh khọt Vũng Tàu
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Bánh khọt Vũng Tàu', 'Vung Tau mini savoury pancakes', 'banh-khot-vung-tau',
       'Đổ khuôn gang, tôm tươi nguyên con', 'Cooked in cast-iron moulds, topped with whole prawns',
       135000, 'https://live.staticflickr.com/2569/4171696432_5acdf19f1c_b.jpg', 0, 1, 101,
       'Bột gạo pha nước cốt dừa, tôm tươi, mỡ hành, tôm cháy, rau sống, nước mắm chua ngọt.', 'Rice batter with coconut milk, fresh prawn, scallion oil, dried prawn floss, garden herbs, sweet-and-sour fish sauce.',
       'Khuôn gang phải thật nóng và láng đủ dầu thì rìa bánh mới giòn còn lòng bánh vẫn mềm. Đổ từng mẻ mười hai cái, ra tới đâu dọn tới đó.', 'The cast-iron mould must be very hot and well oiled so the rim crisps while the centre stays soft. Cooked twelve at a time and served straight away.',
       'Bánh nguội sẽ mất độ giòn nên bên mình dọn theo mẻ. Có tôm, khách dị ứng hải sản báo trước.', 'They lose their crispness as they cool, so we serve them in batches. Contains prawn — please tell us about shellfish allergies.',
       'Phần 12 cái', '12 pieces', 20,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'khaivi';

-- Nem nướng Nha Trang
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Nem nướng Nha Trang', 'Nha Trang grilled pork skewers', 'nem-nuong-nha-trang',
       'Nướng than, cuốn bánh tráng, chấm tương nếp', 'Charcoal grilled, rolled in rice paper with fermented rice sauce',
       155000, 'https://upload.wikimedia.org/wikipedia/commons/a/a8/Nem_chua_nuong.JPG', 0, 1, 102,
       'Thịt heo quết tay, tỏi, tiêu, bánh tráng, bánh hỏi, rau sống, tương nếp Nha Trang.', 'Hand-pounded minced pork, garlic, pepper, rice paper, fine rice vermicelli sheets, herbs, Nha Trang fermented rice sauce.',
       'Thịt quết tay cho dai chứ không xay nhuyễn bằng máy, ướp qua đêm rồi nướng than hoa. Tương nếp nấu từ gạo nếp lên men, không dùng tương ớt đóng chai.', 'The pork is pounded by hand rather than machine-minced so it stays springy, marinated overnight and grilled over charcoal. The sauce is made from fermented glutinous rice, not bottled chilli sauce.',
       'Nướng than có khói, không bố trí được ở phòng máy lạnh kín. Món ăn kèm nhiều rau nên một phần đủ cho ba người ăn chơi.', 'Charcoal grilling produces smoke and cannot be set up in a sealed air-conditioned room. Served with plenty of herbs, so one portion is enough for three as a snack.',
       'Phần 8 xiên', '8 skewers', 25,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'khaivi';

-- Bánh bèo chén Huế
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Bánh bèo chén Huế', 'Hue steamed rice cakes', 'banh-beo-chen-hue',
       'Hấp từng chén nhỏ, tôm cháy và mỡ hành', 'Steamed in individual bowls with prawn floss and scallion oil',
       90000, NULL, 0, 1, 103,
       'Bột gạo, tôm cháy, mỡ hành, bánh mì chiên giòn, nước mắm pha loãng.', 'Rice flour, dried prawn floss, scallion oil, fried bread croutons, diluted fish sauce.',
       'Bột đổ vào từng chén sứ nhỏ rồi hấp, giữa mặt bánh lõm xuống một chút để giữ nhân. Rắc tôm cháy và chan mỡ hành ngay trước khi dọn.', 'The batter is steamed in small porcelain bowls with a dimple in the middle to hold the topping. Prawn floss and scallion oil go on just before serving.',
       'Bánh phải ăn nóng, để nguội sẽ cứng lại. Có tôm cháy, khách ăn chay báo trước để bên mình làm bản chay.', 'Best eaten warm; they firm up as they cool. Contains dried prawn — tell us in advance for a vegetarian version.',
       'Phần 12 chén', '12 bowls', 20,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'khaivi';

-- Bánh bột lọc lá chuối
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Bánh bột lọc lá chuối', 'Tapioca dumplings in banana leaf', 'banh-bot-loc-la-chuoi',
       'Gói lá chuối, nhân tôm thịt', 'Wrapped in banana leaf, filled with prawn and pork',
       85000, 'https://live.staticflickr.com/4078/4926523630_2e0e027732_b.jpg', 0, 1, 104,
       'Bột năng, tôm đất, thịt ba chỉ, lá chuối, hành phi, nước mắm ớt.', 'Tapioca starch, small river prawns, pork belly, banana leaf, fried shallots, chilli fish sauce.',
       'Bột nhồi nước sôi cho dẻo, gói lá chuối rồi hấp mười lăm phút. Lá chuối hơ lửa trước cho mềm, vừa dễ gói vừa để bánh thơm mùi lá.', 'The dough is kneaded with boiling water, wrapped in banana leaf and steamed for fifteen minutes. The leaf is passed over a flame first so it softens and perfumes the dumpling.',
       'Vỏ bánh dai nên trẻ nhỏ ăn cần cắt nhỏ. Có tôm, khách dị ứng hải sản báo trước.', 'The dough is chewy, so cut it up for small children. Contains prawn — please tell us about shellfish allergies.',
       'Phần 10 cái', '10 pieces', 25,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'khaivi';

-- Súp măng cua gà xé
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Súp măng cua gà xé', 'Crab and asparagus soup with shredded chicken', 'sup-mang-cua-ga-xe',
       'Măng tây tươi, gà xé sợi', 'Fresh asparagus with hand-shredded chicken',
       105000, 'https://live.staticflickr.com/7286/8736444970_b966dea186_b.jpg', 0, 1, 105,
       'Măng tây tươi, thịt cua, ức gà xé, trứng gà, nước hầm gà, tiêu, ngò rí.', 'Fresh asparagus, crab meat, shredded chicken breast, egg, chicken stock, pepper, coriander.',
       'Nước hầm gà lọc trong, măng tây cho vào cuối cùng để giữ màu xanh và độ giòn. Trứng đánh tan rót thành sợi mỏng chứ không khuấy vón cục.', 'The chicken stock is strained clear and the asparagus goes in last to keep its colour and bite. The egg is poured in a thin stream so it sets in ribbons rather than lumps.',
       'Có thịt cua, khách dị ứng hải sản báo trước để bên mình làm bản chỉ có gà.', 'Contains crab; tell us in advance and we will make a chicken-only version.',
       'Phần 4 chén', '4 bowls', 25,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'khaivi';

-- Gỏi ngó sen tôm thịt
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Gỏi ngó sen tôm thịt', 'Lotus stem salad with prawn and pork', 'goi-ngo-sen-tom-thit',
       'Ngó sen Đồng Tháp, giòn và trắng', 'Dong Thap lotus stems, crisp and pale',
       175000, 'https://live.staticflickr.com/1779/42037845750_3842c3ea0b_b.jpg', 0, 1, 106,
       'Ngó sen Đồng Tháp, tôm sú, thịt ba chỉ, cà rốt, rau răm, đậu phộng rang, nước mắm chua ngọt.', 'Dong Thap lotus stems, tiger prawn, pork belly, carrot, Vietnamese coriander, roasted peanuts, sweet-and-sour fish sauce.',
       'Ngó sen ngâm nước chanh loãng cho trắng và giòn, vắt ráo rồi mới trộn. Trộn tay nhẹ để ngó sen không gãy vụn.', 'The lotus stems soak in weak lemon water to stay pale and crisp, then are squeezed dry before dressing. Tossed gently by hand so they do not break.',
       'Món trộn nên dọn ngay, để lâu sẽ ra nước. Có đậu phộng và hải sản, khách dị ứng báo trước.', 'A tossed salad, served immediately; left standing it weeps. Contains peanuts and shellfish — please tell us about allergies.',
       'Phần 3-4 người', 'Serves 3-4', 20,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'khaivi';

-- Chả cá Lã Vọng
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Chả cá Lã Vọng', 'La Vong turmeric fish', 'cha-ca-la-vong',
       'Cá lăng ướp nghệ, rán trên chảo tại bàn', 'Turmeric-marinated catfish, pan-fried at your table',
       185000, 'https://live.staticflickr.com/4085/4837078209_a54697140c_b.jpg', 0, 1, 107,
       'Cá lăng phi lê, nghệ tươi, riềng, mẻ, thì là, hành lá, bún tươi, mắm tôm.', 'Catfish fillet, fresh turmeric, galangal, fermented rice starter, dill, spring onion, vermicelli, fermented shrimp paste.',
       'Cá ướp nghệ và mẻ nửa ngày cho thấm, rán sơ ở bếp rồi mang ra chảo nóng tại bàn, thả thì là và hành lá vào sau cùng.', 'The fish marinates for half a day in turmeric and fermented rice starter, is part-fried in the kitchen, then finished in a hot pan at your table with dill and spring onion thrown in last.',
       'Mắm tôm mùi rất đậm, bên mình có sẵn nước mắm thay thế nếu khách chưa quen. Chảo nóng đặt tại bàn nên cẩn thận với trẻ nhỏ.', 'The shrimp paste has a powerful aroma; we can serve fish sauce instead if you are not used to it. The hot pan sits on the table, so take care with small children.',
       'Phần 2-3 người', 'Serves 2-3', 25,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'khaivi';

-- Hoành thánh chiên giòn
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Hoành thánh chiên giòn', 'Crispy fried wontons', 'hoanh-thanh-chien-gion',
       'Nhân tôm thịt, chấm sốt chua ngọt', 'Prawn and pork filling with sweet-and-sour sauce',
       115000, 'https://live.staticflickr.com/4082/4739410312_1abbb43307_b.jpg', 0, 1, 108,
       'Vỏ hoành thánh, tôm, thịt nạc dăm, nấm mèo, hành tím, sốt chua ngọt.', 'Wonton wrappers, prawn, pork shoulder, wood-ear mushroom, shallot, sweet-and-sour sauce.',
       'Gói xòe cánh rồi chiên ngập dầu ở lửa vừa, vớt ra để ráo trên vỉ chứ không để trên giấy, tránh bánh bị ỉu.', 'Folded into open fans and deep-fried at medium heat, then drained on a rack rather than paper so they do not go soggy.',
       'Món chiên nhiều dầu, không hợp với khách đang ăn kiêng. Có tôm trong nhân.', 'Deep-fried and rich, not suitable if you are eating light. The filling contains prawn.',
       'Phần 12 cái', '12 pieces', 15,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'khaivi';

-- Gỏi xoài khô cá sặc
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Gỏi xoài khô cá sặc', 'Green mango salad with dried gourami', 'goi-xoai-kho-ca-sac',
       'Xoài xanh bào sợi, khô cá sặc nướng', 'Shredded green mango with grilled dried gourami',
       125000, 'https://upload.wikimedia.org/wikipedia/commons/d/d8/Yum_pladook_foo.jpg', 0, 1, 109,
       'Xoài xanh, khô cá sặc, hành tím, rau răm, ớt, đậu phộng rang, nước mắm chua ngọt.', 'Green mango, dried gourami fish, shallot, Vietnamese coriander, chilli, roasted peanuts, sweet-and-sour fish sauce.',
       'Khô cá sặc nướng than cho dậy mùi rồi xé sợi. Xoài bào ngay trước khi trộn để không bị thâm.', 'The dried fish is grilled over charcoal until fragrant, then torn into strips. The mango is shredded at the last moment so it does not brown.',
       'Khô cá mặn và có mùi đặc trưng, khách chưa quen nên gọi phần nhỏ. Món chua, không hợp người đau dạ dày.', 'The dried fish is salty with a strong aroma; order a small portion if it is new to you. The salad is sour and not kind to a sensitive stomach.',
       'Phần 3-4 người', 'Serves 3-4', 15,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'khaivi';

-- Chạo tôm bọc mía
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Chạo tôm bọc mía', 'Prawn paste on sugarcane', 'chao-tom-boc-mia',
       'Tôm quết bọc quanh khúc mía, nướng than', 'Pounded prawn wrapped around sugarcane and charcoal grilled',
       165000, 'https://live.staticflickr.com/4021/4239380430_3f0892fb38_b.jpg', 0, 1, 110,
       'Tôm sú quết, mỡ heo, mía tươi, bánh tráng, rau sống, nước chấm đậu phộng.', 'Pounded tiger prawn, pork fat, fresh sugarcane, rice paper, garden herbs, peanut dipping sauce.',
       'Tôm quết tay cùng một ít mỡ heo cho kết dính, bọc quanh khúc mía rồi nướng than. Mía vừa làm que vừa tiết nước ngọt vào phần tôm khi nướng.', 'The prawn is pounded by hand with a little pork fat to bind, moulded around the cane and grilled over charcoal. The cane doubles as a skewer and sweetens the prawn as it cooks.',
       'Khúc mía dùng để cầm và nhai lấy nước chứ không nuốt bã. Có tôm và đậu phộng.', 'The cane is there to hold and to chew for its juice, not to swallow. Contains prawn and peanuts.',
       'Phần 8 khúc', '8 skewers', 25,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'khaivi';

-- Bánh ướt thịt nướng
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Bánh ướt thịt nướng', 'Steamed rice sheets with grilled pork', 'banh-uot-thit-nuong',
       'Bánh tráng ướt cuốn thịt nướng than', 'Soft rice sheets rolled around charcoal-grilled pork',
       120000, 'https://upload.wikimedia.org/wikipedia/commons/thumb/2/21/Nh%C3%A0_m%C3%ACnh_L%E1%BB%85_30th4n2023_%28b%C3%A1nh_%C6%B0%E1%BB%9Bt_Ph%C6%B0%C6%A1ng_Lang%2C_th%E1%BB%8Bt_heo_n%C6%B0%E1%BB%9Bng_%C4%83n_k%C3%A8m%29_%281%29.jpg/1280px-Nh%C3%A0_m%C3%ACnh_L%E1%BB%85_30th4n2023_%28b%C3%A1nh_%C6%B0%E1%BB%9Bt_Ph%C6%B0%C6%A1ng_Lang%2C_th%E1%BB%8Bt_heo_n%C6%B0%E1%BB%9Bng_%C4%83n_k%C3%A8m%29_%281%29.jpg', 0, 1, 111,
       'Bánh ướt, thịt nạc vai nướng, xà lách, dưa leo, rau thơm, nước chấm đậu phộng.', 'Fresh rice sheets, grilled pork shoulder, lettuce, cucumber, herbs, peanut dipping sauce.',
       'Thịt ướp sả tỏi nướng than cho xém cạnh, cuốn ngay vào bánh ướt còn ấm để bánh không bị khô và dính vào nhau.', 'The pork is marinated with lemongrass and garlic, grilled until the edges char, and rolled straight into warm rice sheets so they do not dry or stick together.',
       'Bánh ướt để lâu sẽ dính, bên mình cuốn khi khách gọi. Nước chấm có đậu phộng.', 'The rice sheets stick together if left standing, so we roll them to order. The dipping sauce contains peanuts.',
       'Phần 8 cuốn', '8 rolls', 20,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'khaivi';

-- Tai heo cuốn ngũ sắc
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Tai heo cuốn ngũ sắc', 'Five-colour pork ear rolls', 'tai-heo-cuon-ngu-sac',
       'Tai heo luộc giòn cuốn rau củ', 'Crisp boiled pork ear rolled with vegetables',
       135000, NULL, 0, 1, 112,
       'Tai heo, cà rốt, dưa leo, ớt chuông, hẹ, bánh tráng, nước mắm gừng.', 'Pork ear, carrot, cucumber, bell pepper, garlic chives, rice paper, ginger fish sauce.',
       'Tai heo luộc vừa chín tới rồi ngâm nước đá cho giòn, thái mỏng. Cuốn cùng rau củ thái sợi năm màu, chấm nước mắm gừng.', 'The pork ear is boiled just until done, shocked in iced water to stay crunchy, then finely sliced and rolled with vegetables cut in five colours, served with ginger fish sauce.',
       'Tai heo có độ sần đặc trưng, khách chưa quen nên thử trước. Món nguội, không hâm lại được.', 'Pork ear has a distinctive crunch that not everyone expects; try a piece first. Served cold and cannot be reheated.',
       'Phần 10 cuốn', '10 rolls', 20,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'khaivi';

-- Mực chiên nước mắm
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Mực chiên nước mắm', 'Squid fried in fish sauce', 'muc-chien-nuoc-mam',
       'Mực ống tươi, sốt mắm tỏi sánh', 'Fresh squid in a thick garlic fish-sauce glaze',
       195000, 'https://live.staticflickr.com/2167/2369430695_1a5d337b0d_b.jpg', 0, 1, 113,
       'Mực ống tươi, tỏi, ớt, nước mắm, đường thốt nốt, tiêu, hành lá.', 'Fresh squid, garlic, chilli, fish sauce, palm sugar, pepper, spring onion.',
       'Mực chiên nhanh ở lửa lớn cho săn rồi mới rưới sốt mắm tỏi đã thắng sánh, đảo vài lượt là tắt bếp để mực không dai.', 'The squid is flash-fried over high heat, then tossed briefly in a reduced garlic fish-sauce glaze so it does not turn rubbery.',
       'Món mặn đậm, hợp ăn với cơm trắng. Có hải sản, khách dị ứng báo trước.', 'Deliberately salty and meant to be eaten with plain rice. Contains shellfish — please tell us about allergies.',
       'Phần 3-4 người', 'Serves 3-4', 20,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'khaivi';

-- Sò điệp nướng mỡ hành
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Sò điệp nướng mỡ hành', 'Grilled scallops with scallion oil', 'so-diep-nuong-mo-hanh',
       'Sò điệp nguyên vỏ, đậu phộng rang', 'Scallops in the shell with roasted peanuts',
       245000, 'https://upload.wikimedia.org/wikipedia/commons/thumb/a/ac/Scallop-Sausages-DSC2415.jpg/1280px-Scallop-Sausages-DSC2415.jpg', 0, 1, 114,
       'Sò điệp tươi nguyên vỏ, mỡ hành, đậu phộng rang giã, muối tiêu chanh.', 'Fresh scallops in the shell, scallion oil, crushed roasted peanuts, salt-pepper-lime dip.',
       'Sò nướng nguyên vỏ trên than để giữ nước ngọt, chan mỡ hành và rắc đậu phộng ngay khi vừa mở miệng.', 'Grilled in the shell over charcoal so the juices stay in, then finished with scallion oil and peanuts the moment the shells open.',
       'Giá theo hàng tươi trong ngày nên có thể chênh so với bảng giá. Có hải sản và đậu phộng.', 'Priced on the day market rate, so it may differ from the listed figure. Contains shellfish and peanuts.',
       'Phần 6 con', '6 pieces', 20,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'khaivi';

-- Cà tím nướng mỡ hành
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Cà tím nướng mỡ hành', 'Grilled aubergine with scallion oil', 'ca-tim-nuong-mo-hanh',
       'Món chay, cà tím nướng than nguyên trái', 'Vegetarian, whole aubergine grilled over charcoal',
       95000, 'https://upload.wikimedia.org/wikipedia/commons/thumb/b/bb/Grilled_eggplant_appetizer.jpg/1280px-Grilled_eggplant_appetizer.jpg', 0, 1, 115,
       'Cà tím, hành lá, dầu ăn, đậu phộng rang, nước mắm chay hoặc nước tương.', 'Aubergine, spring onion, oil, roasted peanuts, vegetarian fish sauce or soy sauce.',
       'Cà tím nướng nguyên trái trên than đến khi vỏ cháy xém, bóc vỏ rồi xé dọc thớ, chan mỡ hành lúc còn nóng.', 'The whole aubergine is grilled over charcoal until the skin blackens, then peeled and torn along the grain, with scallion oil poured over while still hot.',
       'Món chay hoàn toàn nếu dùng nước tương, báo trước khi gọi. Có đậu phộng.', 'Fully vegetarian when served with soy sauce — say so when ordering. Contains peanuts.',
       'Phần 2-3 người', 'Serves 2-3', 20,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'khaivi';

-- Đậu hũ chiên sả ớt
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Đậu hũ chiên sả ớt', 'Tofu fried with lemongrass and chilli', 'dau-hu-chien-sa-ot',
       'Món chay, đậu hũ non chiên giòn rìa', 'Vegetarian, silken tofu fried crisp at the edges',
       75000, 'https://live.staticflickr.com/7216/7306390296_63d54bc718.jpg', 0, 1, 116,
       'Đậu hũ non, sả băm, ớt, nước tương, đường, tiêu, hành lá.', 'Silken tofu, minced lemongrass, chilli, soy sauce, sugar, pepper, spring onion.',
       'Đậu hũ để ráo thật kỹ rồi mới chiên, nếu còn ướt dầu sẽ bắn và rìa không giòn. Sả băm phi riêng cho vàng rồi mới trộn vào.', 'The tofu is drained thoroughly before frying — still wet, the oil spits and the edges will not crisp. The lemongrass is fried separately until golden, then folded in.',
       'Món chay hoàn toàn. Cay vừa, bên mình làm không cay nếu khách báo trước.', 'Fully vegetarian. Mildly spicy; we can leave out the chilli if you say so in advance.',
       'Phần 2-3 người', 'Serves 2-3', 15,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'khaivi';

-- Nộm hoa chuối
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Nộm hoa chuối', 'Banana blossom salad', 'nom-hoa-chuoi',
       'Hoa chuối bào, lạc rang, rau thơm', 'Shaved banana blossom with roasted peanuts and herbs',
       105000, 'https://live.staticflickr.com/50/141663065_bc9305b794.jpg', 0, 1, 117,
       'Hoa chuối, cà rốt, rau kinh giới, lạc rang, hành phi, nước mắm chua ngọt.', 'Banana blossom, carrot, Vietnamese balm, roasted peanuts, fried shallots, sweet-and-sour fish sauce.',
       'Hoa chuối bào xong thả ngay vào nước chanh loãng cho khỏi thâm, vắt ráo rồi trộn. Rắc lạc và hành phi sau cùng để giữ độ giòn.', 'The blossom goes straight into weak lemon water after shaving so it does not darken, then is squeezed dry and dressed. Peanuts and shallots go on last to stay crisp.',
       'Hoa chuối có vị chát nhẹ, đó là đặc trưng của món. Có lạc, khách dị ứng báo trước.', 'Banana blossom carries a faint astringency, which is characteristic of the dish. Contains peanuts — please tell us about allergies.',
       'Phần 3-4 người', 'Serves 3-4', 20,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'khaivi';

-- Nghêu hấp sả
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Nghêu hấp sả', 'Clams steamed with lemongrass', 'ngheu-hap-sa',
       'Nghêu Cần Giờ, nước dùng ngọt tự nhiên', 'Can Gio clams in their own sweet broth',
       145000, 'https://live.staticflickr.com/3241/3032559724_97930809d4_b.jpg', 0, 1, 118,
       'Nghêu Cần Giờ, sả đập dập, lá chanh, ớt, rau răm, muối tiêu chanh.', 'Can Gio clams, bruised lemongrass, kaffir lime leaf, chilli, Vietnamese coriander, salt-pepper-lime dip.',
       'Nghêu ngâm nước vo gạo cho nhả cát, hấp cùng sả đến khi vừa mở miệng là bắc ra, hấp quá thì thịt teo lại.', 'The clams soak in rice-washing water to purge sand, then steam with lemongrass only until they open; any longer and the meat shrinks.',
       'Có hải sản, khách dị ứng báo trước. Nghêu theo con nước nên có ngày hết hàng, bên mình sẽ báo khi nhận đặt.', 'Contains shellfish — please tell us about allergies. Clams follow the tides and some days sell out; we will say so when you book.',
       'Phần 3-4 người', 'Serves 3-4', 15,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'khaivi';

-- Khoai lang kén chiên
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Khoai lang kén chiên', 'Fried sweet potato croquettes', 'khoai-lang-ken-chien',
       'Món chay, ngoài giòn trong dẻo', 'Vegetarian, crisp outside and chewy within',
       65000, 'https://upload.wikimedia.org/wikipedia/commons/7/73/Fried_Sweet_Potato_Balls%2C_Mar_2026.jpg', 0, 1, 119,
       'Khoai lang mật, bột năng, nước cốt dừa, đường, mè trắng.', 'Honey sweet potato, tapioca starch, coconut milk, sugar, white sesame.',
       'Khoai hấp chín tán nhuyễn trộn bột năng, nặn hình thoi rồi chiên hai lửa: lửa nhỏ cho chín trong, lửa lớn cho giòn ngoài.', 'The sweet potato is steamed, mashed with tapioca starch, shaped into diamonds and fried twice: low heat to cook through, high heat to crisp the shell.',
       'Món chay hoàn toàn. Ăn nóng mới ngon, để nguội sẽ cứng lại.', 'Fully vegetarian. Best eaten hot; they harden as they cool.',
       'Phần 12 viên', '12 pieces', 15,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'khaivi';

-- Cá tai tượng chiên xù
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Cá tai tượng chiên xù', 'Whole crispy elephant-ear fish', 'ca-tai-tuong-chien-xu',
       'Chiên nguyên con, cuốn bánh tráng rau sống', 'Fried whole, rolled in rice paper with herbs',
       385000, 'https://upload.wikimedia.org/wikipedia/commons/8/87/Flash_fried_whole_scup.jpg', 0, 1, 120,
       'Cá tai tượng nguyên con, bánh tráng, bún tươi, xoài xanh, chuối chát, rau sống, nước mắm me.', 'Whole elephant-ear fish, rice paper, vermicelli, green mango, green banana, garden herbs, tamarind fish sauce.',
       'Cá để nguyên vảy chiên ngập dầu, dựng đứng trên đĩa. Vảy cá phồng lên giòn tan, thịt bên trong vẫn mềm và ngọt.', 'The fish is deep-fried with the scales on and stood upright on the plate. The scales puff into crisp shards while the flesh underneath stays soft and sweet.',
       'Chiên mất khoảng nửa tiếng nên đặt trước. Cá có xương, cẩn thận với trẻ nhỏ.', 'Frying takes about half an hour, so please order ahead. The fish has bones; take care with small children.',
       'Phần 4-6 người', 'Serves 4-6', 30,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'chinh';

-- Cá kho tộ miền Tây
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Cá kho tộ miền Tây', 'Mekong clay-pot braised fish', 'ca-kho-to-mien-tay',
       'Cá basa kho nước dừa, tiêu xanh', 'Basa fish braised in coconut water with green peppercorns',
       245000, 'https://upload.wikimedia.org/wikipedia/commons/thumb/b/b4/C%C3%A1_kho_t%E1%BB%99.JPG/1280px-C%C3%A1_kho_t%E1%BB%99.JPG', 0, 1, 121,
       'Cá basa cắt khúc, nước dừa xiêm, nước màu dừa, tiêu xanh, hành tím, ớt, hành lá.', 'Basa fish steaks, young coconut water, coconut caramel, green peppercorns, shallot, chilli, spring onion.',
       'Cá ướp nước màu dừa rồi kho trong tộ đất ở lửa liu riu bốn mươi phút, đến khi nước kho sánh lại bám quanh miếng cá.', 'The fish is marinated in coconut caramel, then braised in a clay pot over a low flame for forty minutes until the sauce reduces and clings to each piece.',
       'Kho tộ cần bốn mươi phút, đặt sát giờ sẽ phải chờ. Món mặn đậm, ăn kèm cơm trắng và canh chua.', 'Clay-pot braising needs forty minutes, so ordering at the last minute means waiting. Deliberately salty and meant to be eaten with plain rice and a sour soup.',
       'Phần 3-4 người', 'Serves 3-4', 40,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'chinh';

-- Canh chua cá lóc
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Canh chua cá lóc', 'Sour soup with snakehead fish', 'canh-chua-ca-loc',
       'Me chín, bạc hà, đậu bắp, giá', 'Ripe tamarind with taro stem, okra and bean sprouts',
       215000, 'https://upload.wikimedia.org/wikipedia/commons/a/a0/Canhchua2.jpg', 0, 1, 122,
       'Cá lóc đồng, me chín, bạc hà, đậu bắp, cà chua, thơm, giá đỗ, ngò om, tỏi phi.', 'Wild snakehead fish, ripe tamarind, taro stem, okra, tomato, pineapple, bean sprouts, rice paddy herb, fried garlic.',
       'Vị chua lấy từ me chín dầm chứ không dùng giấm. Rau thả vào sau cùng, vừa chín tới là tắt bếp để bạc hà còn giòn.', 'The sourness comes from mashed ripe tamarind, never vinegar. The vegetables go in last and the pot comes off the heat the moment they are done, so the taro stem keeps its crunch.',
       'Cá lóc đồng có xương dăm. Món chua, khách đau dạ dày nên cân nhắc.', 'Wild snakehead carries fine bones. The soup is sour and may not suit a sensitive stomach.',
       'Nồi 4-6 người', 'Pot for 4-6', 25,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'chinh';

-- Thịt kho trứng nước dừa
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Thịt kho trứng nước dừa', 'Pork and eggs braised in coconut water', 'thit-kho-trung-nuoc-dua',
       'Ba chỉ kho rục, trứng vịt thấm màu', 'Pork belly braised soft with colour-stained duck eggs',
       225000, 'https://upload.wikimedia.org/wikipedia/commons/c/ca/Vietnamese_thit_kho%2C_braised_pork_belly_and_boiled_eggs_-_47693228262.jpg', 0, 1, 123,
       'Thịt ba chỉ, trứng vịt luộc, nước dừa xiêm, nước màu, hành tím, tiêu, dưa giá ăn kèm.', 'Pork belly, boiled duck eggs, young coconut water, caramel, shallot, pepper, pickled bean sprouts on the side.',
       'Thịt kho trong nước dừa bốn mươi lăm phút cho mỡ trong và mềm rục. Trứng luộc bóc vỏ kho cùng nửa tiếng cuối để thấm màu đều.', 'The pork braises in coconut water for forty-five minutes until the fat turns clear and yields. The peeled eggs join for the last half hour so they take on an even colour.',
       'Món nhiều mỡ, khách kiêng dầu mỡ nên chọn món khác. Kho lâu nên cần đặt trước.', 'A rich, fatty dish — choose something else if you are avoiding fat. The long braise means it must be ordered ahead.',
       'Phần 4-6 người', 'Serves 4-6', 45,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'chinh';

-- Sườn ram mặn
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Sườn ram mặn', 'Caramelised pork ribs', 'suon-ram-man',
       'Sườn cốt lết ram đường thốt nốt', 'Pork chop ribs caramelised in palm sugar',
       235000, NULL, 0, 1, 124,
       'Sườn cốt lết, đường thốt nốt, nước mắm, tỏi, hành tím, tiêu, hành lá.', 'Pork chop ribs, palm sugar, fish sauce, garlic, shallot, pepper, spring onion.',
       'Sườn chiên sơ cho vàng cạnh rồi ram với đường thốt nốt thắng cháy, đảo liên tục cho nước sốt bám đều và bóng.', 'The ribs are seared golden, then tossed in burnt palm sugar caramel and stirred constantly so the glaze coats them evenly and shines.',
       'Món ngọt mặn đậm, ăn kèm cơm trắng. Sườn có xương.', 'Sweet and salty, meant for plain rice. The ribs are bone-in.',
       'Phần 3-4 người', 'Serves 3-4', 30,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'chinh';

-- Gà kho gừng
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Gà kho gừng', 'Chicken braised with ginger', 'ga-kho-gung',
       'Gà ta chặt miếng, gừng non thái sợi', 'Free-range chicken with shredded young ginger',
       265000, NULL, 0, 1, 125,
       'Gà ta, gừng non, nước mắm, đường, tiêu, hành tím, ớt.', 'Free-range chicken, young ginger, fish sauce, sugar, pepper, shallot, chilli.',
       'Gừng non thái sợi phi thơm trước, sau đó mới cho gà vào kho để mùi gừng ngấm vào thịt chứ không nổi riêng một bên.', 'The young ginger is fried until fragrant before the chicken goes in, so its warmth runs through the meat instead of sitting on top.',
       'Gà ta dai hơn gà công nghiệp, đó là đặc trưng. Gừng cay nồng, trẻ nhỏ có thể không hợp.', 'Free-range chicken is firmer than farmed chicken, which is the point. The ginger is pungent and may not suit young children.',
       'Phần 3-4 người', 'Serves 3-4', 35,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'chinh';

-- Vịt quay lá mắc mật
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Vịt quay lá mắc mật', 'Roast duck with mac mat leaves', 'vit-quay-la-mac-mat',
       'Vịt nguyên con, da giòn, lá mắc mật Lạng Sơn', 'Whole duck with crisp skin and Lang Son mac mat leaves',
       520000, 'https://live.staticflickr.com/2224/2418785280_4a045965bd_b.jpg', 0, 1, 126,
       'Vịt cỏ nguyên con, lá mắc mật, mật ong, hoa hồi, quế, xì dầu, nước chấm mắc mật.', 'Whole free-range duck, mac mat leaves, honey, star anise, cinnamon, soy sauce, mac mat dipping sauce.',
       'Nhồi lá mắc mật vào bụng vịt, phết mật ong lên da rồi quay năm mươi phút. Da phồng giòn còn lá bên trong hãm mùi hôi và tỏa hương vào thịt.', 'Mac mat leaves are packed into the cavity, honey brushed over the skin, and the duck roasts for fifty minutes. The skin blisters crisp while the leaves inside tame the gaminess and scent the meat.',
       'Quay mất gần một tiếng, bắt buộc đặt trước ít nhất một buổi. Thịt vịt có mùi đặc trưng.', 'Roasting takes nearly an hour and must be ordered at least one session ahead. Duck has a distinctive aroma.',
       'Nguyên con, 4-6 người', 'Whole bird, serves 4-6', 50,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'chinh';

-- Bò lúc lắc khoai tây
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Bò lúc lắc khoai tây', 'Shaking beef with potatoes', 'bo-luc-lac-khoai-tay',
       'Thăn bò cắt vuông, áp chảo lửa lớn', 'Cubed beef tenderloin seared over high heat',
       345000, 'https://live.staticflickr.com/3600/3322431400_90671f4172_b.jpg', 0, 1, 127,
       'Thăn bò, khoai tây chiên, hành tây, ớt chuông, xà lách xoong, nước sốt tiêu đen.', 'Beef tenderloin, fried potato, onion, bell pepper, watercress, black pepper sauce.',
       'Chảo phải thật nóng, đảo nhanh từng mẻ nhỏ để bò xém ngoài mà trong còn hồng. Đảo nhiều quá thì bò ra nước và dai.', 'The pan must be searing hot and the beef tossed quickly in small batches so it chars outside and stays pink within. Overcrowd it and the beef weeps and toughens.',
       'Bên mình làm chín vừa theo mặc định, khách muốn chín kỹ báo trước. Món có hành tây.', 'Cooked medium by default; tell us if you would like it well done. Contains onion.',
       'Phần 3-4 người', 'Serves 3-4', 20,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'chinh';

-- Cơm chiên hải sản trái thơm
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Cơm chiên hải sản trái thơm', 'Seafood fried rice in a pineapple', 'com-chien-hai-san-trai-thom',
       'Dọn trong nửa trái thơm nướng', 'Served in half a roasted pineapple',
       285000, 'https://live.staticflickr.com/5235/7090014075_1675dc6a35_b.jpg', 0, 1, 128,
       'Cơm nguội, tôm, mực, chả cá, trứng, thơm, đậu Hà Lan, hành lá, nước mắm.', 'Day-old rice, prawn, squid, fish cake, egg, pineapple, green peas, spring onion, fish sauce.',
       'Dùng cơm nguội để hạt rời chứ không bết. Chiên lửa lớn từng mẻ, dọn trong nửa trái thơm đã khoét ruột và nướng sơ.', 'Made with day-old rice so the grains stay separate. Fried in batches over high heat and served in a hollowed, lightly roasted pineapple half.',
       'Có hải sản, khách dị ứng báo trước để bên mình đổi sang cơm chiên gà.', 'Contains seafood; tell us in advance and we will make it with chicken instead.',
       'Phần 3-4 người', 'Serves 3-4', 25,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'chinh';

-- Cơm niêu đập niêu
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Cơm niêu đập niêu', 'Clay-pot rice cracked at the table', 'com-nieu-dap-nieu',
       'Nấu niêu đất, đập niêu ngay tại bàn', 'Cooked in a clay pot and cracked open at your table',
       165000, NULL, 0, 1, 129,
       'Gạo thơm, niêu đất, mỡ hành, nước mắm kho quẹt, cá khô ăn kèm.', 'Fragrant rice, clay pot, scallion oil, kho quet dipping sauce, dried fish on the side.',
       'Cơm nấu trong niêu đất bốn mươi phút cho đáy niêu đóng lớp cháy vàng. Nhân viên đập niêu ngay tại bàn để khách thấy lớp cơm cháy còn nguyên.', 'The rice cooks in a clay pot for forty minutes until a golden crust forms at the base. A server cracks the pot at your table so you see the crust intact.',
       'Nấu mất bốn mươi phút, nên gọi ngay từ đầu bữa. Đập niêu có tiếng động lớn, báo trước nếu bàn có trẻ nhỏ hay người lớn tuổi.', 'Forty minutes to cook, so order it at the start of the meal. Cracking the pot is loud — tell us if there are small children or elderly guests at the table.',
       'Niêu 4-6 người', 'Pot for 4-6', 40,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'chinh';

-- Bún thịt nướng chả giò
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Bún thịt nướng chả giò', 'Vermicelli with grilled pork and spring rolls', 'bun-thit-nuong-cha-gio',
       'Bún tươi, thịt nướng than, chả giò cắt khúc', 'Fresh vermicelli, charcoal-grilled pork and sliced spring rolls',
       125000, 'https://live.staticflickr.com/2553/3730861055_7249a18df5.jpg', 0, 1, 130,
       'Bún tươi, thịt nạc vai nướng, chả giò, xà lách, giá, dưa leo, đậu phộng, nước mắm chua ngọt.', 'Fresh vermicelli, grilled pork shoulder, spring rolls, lettuce, bean sprouts, cucumber, peanuts, sweet-and-sour fish sauce.',
       'Thịt ướp sả tỏi nướng than cho xém cạnh. Chả giò chiên lại lần hai ngay trước khi dọn để giữ độ giòn khi chan nước mắm.', 'The pork is marinated with lemongrass and garlic and grilled until the edges char. The spring rolls are fried a second time just before serving so they stay crisp under the dressing.',
       'Món một người ăn, không phải món chia bàn. Có đậu phộng.', 'A single-serve bowl, not a sharing dish. Contains peanuts.',
       'Phần 1 tô lớn', 'One large bowl', 20,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'chinh';

-- Mì Quảng gà
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Mì Quảng gà', 'Quang noodles with chicken', 'mi-quang-ga',
       'Sợi mì vàng, nước dùng xăm xắp, bánh tráng mè', 'Yellow noodles in a shallow broth with sesame rice crackers',
       115000, 'https://live.staticflickr.com/4093/4926723822_ce8873d50b_b.jpg', 0, 1, 131,
       'Mì Quảng sợi vàng, gà ta, nghệ tươi, đậu phộng rang, bánh tráng mè, rau sống, hành phi.', 'Yellow Quang noodles, free-range chicken, fresh turmeric, roasted peanuts, sesame rice cracker, garden herbs, fried shallots.',
       'Nước dùng nấu đậm và chỉ chan xăm xắp chứ không ngập như phở, để sợi mì thấm chứ không loãng. Bánh tráng mè bẻ vào ăn cùng.', 'The broth is cooked strong and ladled only halfway up the bowl rather than covering the noodles, so the flavour clings instead of thinning. The sesame cracker is broken in as you eat.',
       'Ít nước dùng là đúng kiểu Quảng Nam, không phải bên mình chan thiếu. Có đậu phộng.', 'The shallow broth is how Quang Nam serves it, not a short measure. Contains peanuts.',
       'Phần 1 tô lớn', 'One large bowl', 25,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'chinh';

-- Hủ tiếu Nam Vang
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Hủ tiếu Nam Vang', 'Nam Vang noodle soup', 'hu-tieu-nam-vang',
       'Nước trong, tôm thịt, gan và trứng cút', 'Clear broth with prawn, pork, liver and quail eggs',
       105000, 'https://live.staticflickr.com/4079/4735655334_6236f51f41_b.jpg', 0, 1, 132,
       'Hủ tiếu dai, tôm, thịt bằm, gan heo, trứng cút, tỏi phi, hẹ, nước hầm xương.', 'Chewy rice noodles, prawn, minced pork, pork liver, quail eggs, fried garlic, garlic chives, pork bone broth.',
       'Xương hầm bốn tiếng rồi lọc kỹ cho nước thật trong. Tôm và gan trụng riêng từng phần để không làm đục nồi nước dùng.', 'The bones simmer four hours and the broth is strained until clear. Prawn and liver are blanched separately for each bowl so they do not cloud the pot.',
       'Có hải sản và nội tạng, khách kiêng báo trước để bên mình bỏ bớt.', 'Contains shellfish and offal; tell us in advance and we will leave them out.',
       'Phần 1 tô lớn', 'One large bowl', 25,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'chinh';

-- Bánh canh cua
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Bánh canh cua', 'Thick noodle soup with crab', 'banh-canh-cua',
       'Sợi bột gạo, nước dùng sánh, thịt cua tươi', 'Thick rice noodles in a glossy broth with fresh crab',
       135000, 'https://live.staticflickr.com/4138/4735655640_b4452fdf01_b.jpg', 0, 1, 133,
       'Bánh canh bột gạo, thịt cua, chả cua, huyết, trứng cút, hành lá, tiêu, nước hầm xương.', 'Thick rice noodles, crab meat, crab cake, blood cake, quail eggs, spring onion, pepper, pork bone broth.',
       'Nước dùng làm sánh bằng chính bột gạo của sợi bánh canh chứ không pha bột năng. Thịt cua gỡ tay, thả vào sau cùng để không nát.', 'The broth is thickened by the starch from the noodles themselves rather than added tapioca. The crab is picked by hand and folded in last so it does not break up.',
       'Có hải sản. Món có huyết, khách không dùng báo trước để bên mình bỏ ra.', 'Contains shellfish. The bowl includes blood cake; tell us if you would rather it was left out.',
       'Phần 1 tô lớn', 'One large bowl', 30,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'chinh';

-- Bò kho bánh mì
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Bò kho bánh mì', 'Beef stew with baguette', 'bo-kho-banh-mi',
       'Bắp bò hầm mềm, cà rốt, sả quế', 'Beef shank stewed soft with carrot, lemongrass and cinnamon',
       145000, 'https://live.staticflickr.com/8658/16479866370_c6e7f9e8a5_b.jpg', 0, 1, 134,
       'Bắp bò, cà rốt, sả, quế, hoa hồi, cà chua, nước dừa, bánh mì nóng, rau quế.', 'Beef shank, carrot, lemongrass, cinnamon, star anise, tomato, coconut water, warm baguette, Thai basil.',
       'Bắp bò hầm bốn mươi lăm phút với sả quế cho mềm mà vẫn giữ thớ. Cà rốt cho vào nửa tiếng cuối để không bị nát.', 'The shank stews for forty-five minutes with lemongrass and cinnamon until tender but still fibrous. The carrot goes in for the last half hour so it does not collapse.',
       'Hầm lâu nên đặt trước. Bánh mì nướng lại khi dọn, hết bánh bên mình sẽ báo.', 'The long stew means ordering ahead. The baguette is re-toasted on serving; if we run out we will say so.',
       'Phần 2-3 người', 'Serves 2-3', 45,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'chinh';

-- Tôm sú hấp nước dừa
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Tôm sú hấp nước dừa', 'Tiger prawns steamed in coconut water', 'tom-su-hap-nuoc-dua',
       'Hấp trong trái dừa, giữ nguyên vị ngọt', 'Steamed inside the coconut to keep the sweetness',
       485000, NULL, 0, 1, 135,
       'Tôm sú, nước dừa xiêm, sả, lá chanh, muối tiêu chanh, rau răm.', 'Tiger prawn, young coconut water, lemongrass, kaffir lime leaf, salt-pepper-lime dip, Vietnamese coriander.',
       'Tôm hấp trong nước dừa cùng sả và lá chanh, canh đúng lúc vỏ vừa chuyển đỏ là bắc ra, hấp quá thì thịt bở.', 'The prawns steam in coconut water with lemongrass and lime leaf, and come off the heat the moment the shells turn red; any longer and the flesh goes mealy.',
       'Giá theo tôm tươi trong ngày nên có thể chênh so với bảng giá. Có hải sản.', 'Priced on the day market rate for live prawns, so it may differ from the listed figure. Contains shellfish.',
       'Phần 8 con lớn', '8 large prawns', 20,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'chinh';

-- Cua rang me
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Cua rang me', 'Crab in tamarind sauce', 'cua-rang-me',
       'Cua thịt Cà Mau, sốt me chua ngọt', 'Ca Mau mud crab in a sweet-sour tamarind glaze',
       650000, 'https://live.staticflickr.com/2687/4444630274_b5aa8c65ca_b.jpg', 0, 1, 136,
       'Cua thịt Cà Mau, me chín, đường thốt nốt, tỏi, ớt, hành tây, bánh mì ăn kèm.', 'Ca Mau mud crab, ripe tamarind, palm sugar, garlic, chilli, onion, baguette on the side.',
       'Cua chiên sơ cho săn rồi rang với sốt me thắng sánh. Sốt phải đủ đặc để bám vào mai chứ không chảy xuống đáy đĩa.', 'The crab is flash-fried to firm the meat, then tossed in a reduced tamarind glaze thick enough to cling to the shell rather than pool on the plate.',
       'Giá theo cân cua tươi trong ngày. Ăn cua cần dùng tay và kẹp, bên mình dọn kèm khăn và chén nước chanh.', 'Priced by the weight of live crab on the day. Eating it means hands and crackers, so we bring a towel and a lemon bowl.',
       'Phần 2-3 người', 'Serves 2-3', 30,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'chinh';

-- Cá chẽm hấp Hồng Kông
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Cá chẽm hấp Hồng Kông', 'Hong Kong style steamed sea bass', 'ca-hap-hong-kong',
       'Cá chẽm nguyên con, xì dầu và gừng hành', 'Whole sea bass with soy, ginger and spring onion',
       560000, NULL, 0, 1, 137,
       'Cá chẽm nguyên con, gừng, hành lá, xì dầu, dầu mè, nấm đông cô, cải bẹ xanh.', 'Whole sea bass, ginger, spring onion, soy sauce, sesame oil, shiitake mushroom, mustard greens.',
       'Cá hấp đúng mười hai phút, rưới xì dầu nóng và dầu mè sôi lên gừng hành thái sợi để dậy mùi ngay tại bàn.', 'The fish steams for exactly twelve minutes, then hot soy and smoking sesame oil are poured over shredded ginger and spring onion so the aroma lifts at the table.',
       'Cá tươi sống chọn trong bể, giá theo cân thực tế. Cá có xương.', 'The fish is chosen live from the tank and billed by actual weight. It has bones.',
       'Phần 4-6 người', 'Serves 4-6', 30,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'chinh';

-- Mực nhồi thịt hấp
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Mực nhồi thịt hấp', 'Steamed squid stuffed with pork', 'muc-nhoi-thit-hap',
       'Mực ống nhồi thịt bằm, nấm mèo', 'Squid tubes stuffed with minced pork and wood-ear',
       315000, 'https://upload.wikimedia.org/wikipedia/commons/f/f1/Red_Wine_%26_Stuffed_Squid%2C_Calamari.jpg', 0, 1, 138,
       'Mực ống, thịt bằm, nấm mèo, miến, hành tím, tiêu, nước mắm gừng.', 'Squid tubes, minced pork, wood-ear mushroom, glass noodles, shallot, pepper, ginger fish sauce.',
       'Nhồi nhân vừa tay, chừa chỗ cho nhân nở, xăm vài lỗ trên thân mực để hơi thoát ra chứ không làm nứt mực khi hấp.', 'The tubes are filled loosely to leave room for the stuffing to swell, and pricked a few times so steam escapes instead of splitting the squid.',
       'Có hải sản. Hấp mất ba mươi phút nên đặt trước.', 'Contains shellfish. Thirty minutes to steam, so please order ahead.',
       'Phần 3-4 người', 'Serves 3-4', 30,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'chinh';

-- Ếch xào lăn
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Ếch xào lăn', 'Frog sautéed in coconut curry', 'ech-xao-lan',
       'Ếch đồng, nước cốt dừa, cà ri và sả', 'Field frog with coconut milk, curry and lemongrass',
       295000, NULL, 0, 1, 139,
       'Ếch đồng, nước cốt dừa, bột cà ri, sả băm, đậu phộng rang, rau răm, bánh mì.', 'Field frog, coconut milk, curry powder, minced lemongrass, roasted peanuts, Vietnamese coriander, baguette.',
       'Ếch chiên sơ cho săn rồi xào với sả và cà ri, chan nước cốt dừa vào cuối và chỉ đun nhỏ lửa để nước cốt không bị tách dầu.', 'The frog is seared, sautéed with lemongrass and curry, then finished with coconut milk over a low flame so the cream does not split.',
       'Thịt ếch có nhiều xương nhỏ, cẩn thận với trẻ nhỏ. Có đậu phộng.', 'Frog has many small bones; take care with young children. Contains peanuts.',
       'Phần 3-4 người', 'Serves 3-4', 25,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'chinh';

-- Lươn um nước dừa
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Lươn um nước dừa', 'Eel braised in coconut milk', 'luon-um-nuoc-dua',
       'Lươn đồng um rau ngổ, nước cốt dừa', 'Field eel braised with rice paddy herb and coconut milk',
       325000, NULL, 0, 1, 140,
       'Lươn đồng, nước cốt dừa, rau ngổ, sả, nghệ, đậu phộng, bún tươi.', 'Field eel, coconut milk, rice paddy herb, lemongrass, turmeric, peanuts, fresh vermicelli.',
       'Lươn tuốt nhớt bằng tro và muối chứ không cạo, giữ được lớp da. Um nhỏ lửa bốn mươi phút cho thịt mềm mà không nát.', 'The eel is cleaned of slime with ash and salt rather than scraped, which keeps the skin intact, then braised gently for forty minutes until tender but whole.',
       'Um lâu nên đặt trước. Lươn có mùi đặc trưng, khách chưa ăn bao giờ nên gọi phần nhỏ.', 'The long braise means ordering ahead. Eel has a distinctive aroma; order a small portion if it is new to you.',
       'Phần 3-4 người', 'Serves 3-4', 40,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'chinh';

-- Gà nướng muối ớt
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Gà nướng muối ớt', 'Chicken grilled with chilli salt', 'ga-nuong-muoi-ot',
       'Gà ta nguyên con, nướng than hoa', 'Whole free-range chicken over charcoal',
       420000, 'https://live.staticflickr.com/4042/4372059601_c91f5eb8f0_b.jpg', 0, 1, 141,
       'Gà ta nguyên con, muối hột, ớt hiểm, sả, mật ong, lá chanh, muối tiêu chanh.', 'Whole free-range chicken, coarse salt, bird chilli, lemongrass, honey, kaffir lime leaf, salt-pepper-lime dip.',
       'Gà ướp muối ớt và sả qua đêm, nướng than xoay đều bốn mươi lăm phút. Phết mật ong ở mười phút cuối cho da vàng bóng mà không cháy.', 'The bird marinates overnight in chilli salt and lemongrass, then turns over charcoal for forty-five minutes. Honey is brushed on in the last ten minutes so the skin glazes without burning.',
       'Nướng gần một tiếng, bắt buộc đặt trước. Món cay, bên mình làm không cay nếu báo trước.', 'Nearly an hour on the grill, so it must be ordered ahead. Spicy by default; we can leave the chilli out on request.',
       'Nguyên con, 4-6 người', 'Whole bird, serves 4-6', 45,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'chinh';

-- Gà hấp lá sen
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Gà hấp lá sen', 'Chicken steamed in lotus leaf', 'ga-hap-la-sen',
       'Gà ta gói lá sen, hấp cách thủy', 'Free-range chicken wrapped in lotus leaf and steamed',
       460000, 'https://upload.wikimedia.org/wikipedia/commons/2/23/Steamed_rice_with_minced_chicken_and_pork_dim_sum.jpg', 0, 1, 142,
       'Gà ta, lá sen tươi, hạt sen, nấm đông cô, gừng, rượu trắng, muối tiêu chanh.', 'Free-range chicken, fresh lotus leaf, lotus seeds, shiitake mushroom, ginger, rice wine, salt-pepper-lime dip.',
       'Gà nhồi hạt sen và nấm, gói kín trong lá sen tươi rồi hấp năm mươi phút. Lá sen giữ hơi nước lại và để mùi sen ngấm vào thịt.', 'The bird is stuffed with lotus seeds and mushroom, sealed in fresh lotus leaf and steamed for fifty minutes. The leaf traps the steam and lends its scent to the meat.',
       'Hấp gần một tiếng, bắt buộc đặt trước. Lá sen theo mùa, hết mùa bên mình sẽ báo trước.', 'Nearly an hour to steam, so it must be ordered ahead. Lotus leaf is seasonal and we will tell you when it is unavailable.',
       'Nguyên con, 4-6 người', 'Whole bird, serves 4-6', 50,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'chinh';

-- Bò nướng lá cách
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Bò nướng lá cách', 'Beef grilled in premna leaf', 'bo-nuong-la-cach',
       'Bò tơ cuốn lá cách, nướng vỉ than', 'Young beef wrapped in premna leaf over charcoal',
       275000, NULL, 0, 1, 143,
       'Bò tơ, lá cách, sả, tỏi, mỡ chài, đậu phộng, mắm nêm.', 'Young beef, premna leaf, lemongrass, garlic, caul fat, peanuts, fermented anchovy sauce.',
       'Thịt bò ướp sả tỏi cuốn lá cách, bọc thêm lớp mỡ chài mỏng để khi nướng mỡ chảy ra giữ cho thịt không khô.', 'The seasoned beef is wrapped in premna leaf and a thin layer of caul fat, which melts on the grill and keeps the meat from drying.',
       'Nướng than có khói, không đặt được ở phòng máy lạnh kín. Mắm nêm mùi đậm, bên mình có nước mắm thay thế.', 'Charcoal grilling produces smoke and cannot be set up in a sealed air-conditioned room. The anchovy sauce is pungent; fish sauce is available instead.',
       'Phần 10 cuốn', '10 rolls', 25,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'chinh';

-- Heo rừng xào sả ớt
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Heo rừng xào sả ớt', 'Wild boar sautéed with lemongrass and chilli', 'heo-rung-xao-sa-ot',
       'Thịt heo rừng nuôi, da dày và giòn', 'Farmed wild boar with thick, crunchy skin',
       355000, NULL, 0, 1, 144,
       'Thịt heo rừng nuôi, sả băm, ớt hiểm, lá chanh, hành tây, nước mắm, tiêu.', 'Farmed wild boar, minced lemongrass, bird chilli, kaffir lime leaf, onion, fish sauce, pepper.',
       'Thịt thái mỏng ngang thớ rồi xào lửa lớn thật nhanh, sả phi vàng riêng để không bị cháy đắng khi xào chung.', 'The meat is sliced thin across the grain and flashed over high heat; the lemongrass is fried golden separately so it does not scorch bitter in the pan.',
       'Thịt heo rừng dai hơn heo thường, đó là đặc trưng. Món cay, điều chỉnh được nếu báo trước.', 'Wild boar is chewier than farmed pork, which is the point. Spicy by default and adjustable on request.',
       'Phần 3-4 người', 'Serves 3-4', 25,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'chinh';

-- Rau muống xào tỏi
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Rau muống xào tỏi', 'Water spinach stir-fried with garlic', 'rau-muong-xao-toi',
       'Món chay, xào lửa lớn giữ độ giòn', 'Vegetarian, flashed over high heat to stay crisp',
       75000, 'https://live.staticflickr.com/65535/48831098842_d1dbbf3592_b.jpg', 0, 1, 145,
       'Rau muống, tỏi, dầu ăn, nước mắm chay hoặc nước tương, tiêu.', 'Water spinach, garlic, oil, vegetarian fish sauce or soy sauce, pepper.',
       'Chảo phải nóng già, xào từng mẻ nhỏ trong chưa đầy hai phút. Xào lâu rau sẽ ra nước và mất màu xanh.', 'The wok must be scorching and each small batch takes under two minutes. Cook it longer and the greens weep and lose their colour.',
       'Món chay hoàn toàn nếu dùng nước tương, báo trước khi gọi.', 'Fully vegetarian when made with soy sauce — say so when ordering.',
       'Phần 3-4 người', 'Serves 3-4', 10,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'chinh';

-- Đậu hũ sốt cà chua
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Đậu hũ sốt cà chua', 'Tofu in tomato sauce', 'dau-hu-sot-ca-chua',
       'Món chay, cà chua chín nấu nhừ', 'Vegetarian, made with ripe tomatoes cooked down',
       85000, NULL, 0, 1, 146,
       'Đậu hũ chiên, cà chua chín, hành lá, thì là, nước tương, đường, tiêu.', 'Fried tofu, ripe tomato, spring onion, dill, soy sauce, sugar, pepper.',
       'Cà chua nấu nhừ thành sốt sánh trước, sau đó mới thả đậu hũ vào rim nhẹ để đậu thấm mà không nát.', 'The tomatoes are cooked down to a thick sauce first, then the tofu is simmered gently in it so it takes on flavour without falling apart.',
       'Món chay hoàn toàn. Vị nhẹ, hợp ăn kèm với các món đậm.', 'Fully vegetarian. Mild in flavour and best alongside the stronger dishes.',
       'Phần 3-4 người', 'Serves 3-4', 20,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'chinh';

-- Nấm kho tiêu chay
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Nấm kho tiêu chay', 'Mushrooms braised with pepper', 'nam-kho-tieu-chay',
       'Món chay, nấm đùi gà kho tộ', 'Vegetarian, king oyster mushrooms in a clay pot',
       115000, NULL, 0, 1, 147,
       'Nấm đùi gà, nấm rơm, tiêu xanh, nước tương, đường thốt nốt, hành boa rô.', 'King oyster mushroom, straw mushroom, green peppercorns, soy sauce, palm sugar, leek.',
       'Nấm xé sợi rồi áp chảo cho ráo nước trước khi kho, nếu kho ngay thì nấm ra nước và nồi kho bị loãng.', 'The mushrooms are torn into strips and dry-seared to drive off moisture before braising; skip that and they water down the pot.',
       'Món chay hoàn toàn. Tiêu xanh cay nồng, giảm được nếu khách báo trước.', 'Fully vegetarian. The green peppercorns are pungent and can be reduced on request.',
       'Phần 3-4 người', 'Serves 3-4', 25,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'chinh';

-- Canh khổ qua nhồi thịt
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Canh khổ qua nhồi thịt', 'Bitter melon soup stuffed with pork', 'canh-kho-qua-nhoi-thit',
       'Món Tết quen thuộc, vị đắng dịu', 'A Lunar New Year staple with a gentle bitterness',
       125000, 'https://upload.wikimedia.org/wikipedia/commons/a/ae/Soup_made_from_stuffed_bitter_melon%2C_Canh_Kho_Qua.jpg', 0, 1, 148,
       'Khổ qua, thịt bằm, mộc nhĩ, miến, hành lá, nước hầm xương, tiêu.', 'Bitter melon, minced pork, wood-ear mushroom, glass noodles, spring onion, pork bone broth, pepper.',
       'Khổ qua móc ruột, nhồi thịt rồi hầm nhỏ lửa ba mươi lăm phút cho vỏ mềm và vị đắng dịu lại chứ không gắt.', 'The melon is cored, stuffed and simmered gently for thirty-five minutes until the skin softens and the bitterness mellows rather than bites.',
       'Vị đắng là đặc trưng của món, trẻ nhỏ thường không hợp. Hầm lâu nên đặt trước.', 'The bitterness is the point of the dish and children rarely take to it. The long simmer means ordering ahead.',
       'Nồi 4-6 người', 'Pot for 4-6', 35,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'chinh';

-- Chim cút rô ti
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Chim cút rô ti', 'Roasted quail', 'chim-cut-roti',
       'Cút nguyên con rô ti sốt sánh', 'Whole quail roasted in a thick glaze',
       265000, 'https://live.staticflickr.com/3010/2631820544_0aa712ba87_b.jpg', 0, 1, 149,
       'Chim cút, ngũ vị hương, mật ong, nước tương, tỏi, gừng, xôi trắng ăn kèm.', 'Quail, five-spice, honey, soy sauce, garlic, ginger, plain sticky rice on the side.',
       'Cút ướp ngũ vị nửa ngày, chiên sơ cho vàng rồi rô ti trong nước sốt đến khi sốt sánh bám quanh mình chim.', 'The quail marinate half a day in five-spice, are seared golden, then roasted in the sauce until it reduces and coats each bird.',
       'Chim cút xương nhỏ và nhiều, cẩn thận với trẻ nhỏ. Ăn bằng tay là hợp nhất.', 'Quail bones are small and plentiful; take care with young children. Best eaten with your hands.',
       'Phần 6 con', '6 birds', 30,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'chinh';

-- Lẩu Thái hải sản
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Lẩu Thái hải sản', 'Thai-style seafood hotpot', 'lau-thai-hai-san',
       'Chua cay, tôm mực nghêu và nấm', 'Hot and sour with prawn, squid, clams and mushrooms',
       545000, 'https://live.staticflickr.com/7051/6928723901_dd26ef45cf_b.jpg', 0, 1, 150,
       'Tôm, mực, nghêu, nấm rơm, sả, riềng, lá chanh, ớt, cà chua, bún tươi.', 'Prawn, squid, clams, straw mushroom, lemongrass, galangal, kaffir lime leaf, chilli, tomato, fresh vermicelli.',
       'Nước dùng nấu từ xương và đầu tôm, dậy mùi bằng sả riềng lá chanh đập dập chứ không dùng gói gia vị pha sẵn.', 'The broth is built on bones and prawn heads, lifted with bruised lemongrass, galangal and lime leaf rather than a packet mix.',
       'Món cay, độ cay điều chỉnh được nếu báo trước. Có hải sản, khách dị ứng báo trước.', 'Spicy, and the heat can be adjusted on request. Contains shellfish — please tell us about allergies.',
       'Nồi 4-6 người', 'Pot for 4-6', 30,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'lau';

-- Lẩu bò nhúng dấm
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Lẩu bò nhúng dấm', 'Beef hotpot with vinegar broth', 'lau-bo-nhung-dam',
       'Bò tơ thái mỏng, nhúng nước dấm dừa', 'Thinly sliced young beef dipped in coconut vinegar broth',
       595000, NULL, 0, 1, 151,
       'Bò tơ thái mỏng, dấm dừa, nước dừa, hành tây, sả, bánh tráng, rau sống, mắm nêm.', 'Thinly sliced young beef, coconut vinegar, coconut water, onion, lemongrass, rice paper, garden herbs, fermented anchovy sauce.',
       'Nước nhúng pha dấm dừa với nước dừa tươi cho chua thanh. Bò thái mỏng nhúng vài giây là chín, để lâu sẽ dai.', 'The dipping broth balances coconut vinegar with fresh coconut water for a clean sourness. The beef is sliced thin and needs only seconds; longer and it toughens.',
       'Mắm nêm mùi đậm, bên mình có nước mắm thay thế. Bò nhúng tái, khách muốn chín kỹ nhúng lâu hơn.', 'The anchovy sauce is pungent; fish sauce is available instead. The beef is meant to be rare — dip it longer if you prefer it cooked through.',
       'Nồi 4-6 người', 'Pot for 4-6', 25,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'lau';

-- Lẩu cá bớp lá giang
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Lẩu cá bớp lá giang', 'Cobia hotpot with giang leaf', 'lau-ca-bop-la-giang',
       'Cá bớp cắt khúc, chua thanh lá giang', 'Cobia steaks in a clean giang-leaf sourness',
       565000, 'https://live.staticflickr.com/5497/10639477323_aca39e00d2_b.jpg', 0, 1, 152,
       'Cá bớp, lá giang, măng chua, cà chua, ớt, rau muống, bún tươi.', 'Cobia, giang leaf, pickled bamboo shoot, tomato, chilli, water spinach, fresh vermicelli.',
       'Lá giang vò nhẹ rồi thả vào nồi, không đun sôi lâu vì lá nấu quá sẽ chuyển sang đắng thay vì chua.', 'The giang leaf is lightly crushed and dropped in, never boiled long — overcooked it turns bitter instead of sour.',
       'Cá bớp có xương sống lớn, dễ gỡ. Món chua, khách đau dạ dày nên cân nhắc.', 'Cobia has a large central bone that lifts out easily. The broth is sour and may not suit a sensitive stomach.',
       'Nồi 4-6 người', 'Pot for 4-6', 30,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'lau';

-- Lẩu riêu cua đồng
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Lẩu riêu cua đồng', 'Field crab hotpot', 'lau-riu-rieu-cua-dong',
       'Riêu cua đồng giã tay, đậu rán và cà chua', 'Hand-pounded field crab with fried tofu and tomato',
       485000, 'https://live.staticflickr.com/4001/4687454915_8c959aa3c6_b.jpg', 0, 1, 153,
       'Cua đồng giã lọc, đậu phụ rán, cà chua, dấm bỗng, hành lá, rau sống, bún tươi.', 'Pounded and strained field crab, fried tofu, tomato, fermented rice vinegar, spring onion, garden herbs, fresh vermicelli.',
       'Cua giã tay lọc lấy nước rồi đun lửa vừa cho riêu nổi thành tảng. Khuấy mạnh lúc này thì riêu vỡ vụn, không đóng bánh được.', 'The crab is pounded, strained and heated gently so the protein rises in soft cakes. Stir hard at this point and it breaks into crumbs.',
       'Có hải sản nước ngọt. Nồi nhỏ nhất phục vụ bốn người, không tách phần lẻ.', 'Contains freshwater shellfish. The smallest pot serves four and cannot be split further.',
       'Nồi 4-6 người', 'Pot for 4-6', 35,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'lau';

-- Lẩu dê tiềm thuốc bắc
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Lẩu dê tiềm thuốc bắc', 'Goat hotpot with Chinese herbs', 'lau-de-tiem-thuoc-bac',
       'Dê hầm thuốc bắc, khoai môn và tàu hũ ky', 'Goat stewed with herbs, taro and tofu skin',
       625000, NULL, 0, 1, 154,
       'Thịt dê, thuốc bắc, khoai môn, tàu hũ ky, chao, rau tần ô, mì tươi.', 'Goat meat, Chinese herbs, taro, tofu skin, fermented bean curd, garland chrysanthemum, fresh noodles.',
       'Dê hầm cùng thuốc bắc bốn mươi lăm phút cho mềm và bớt mùi. Khoai môn cho vào sau để không tan vào nước.', 'The goat stews with the herb mix for forty-five minutes to tenderise and tame its aroma. The taro goes in later so it does not dissolve.',
       'Hầm lâu nên bắt buộc đặt trước. Thịt dê có mùi đặc trưng dù đã khử.', 'The long stew means it must be ordered ahead. Goat keeps a distinctive aroma even after the herbs.',
       'Nồi 4-6 người', 'Pot for 4-6', 45,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'lau';

-- Lẩu nấm chay
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Lẩu nấm chay', 'Vegetarian mushroom hotpot', 'lau-nam-chay',
       'Món chay, tám loại nấm và rau củ', 'Vegetarian, eight kinds of mushroom with vegetables',
       385000, 'https://live.staticflickr.com/8233/8586109722_3ceaab89d9_b.jpg', 0, 1, 155,
       'Nấm đùi gà, nấm kim châm, nấm đông cô, nấm bào ngư, nấm rơm, củ sen, bắp non, tàu hũ non, mì tươi.', 'King oyster, enoki, shiitake, oyster, straw mushroom, lotus root, baby corn, silken tofu, fresh noodles.',
       'Nước dùng ninh từ củ sen, bắp và nấm đông cô khô trong hai tiếng, ngọt tự nhiên không dùng bột nêm.', 'The broth simmers two hours from lotus root, corn and dried shiitake, sweet on its own without stock powder.',
       'Món chay hoàn toàn. Nấm kim châm dễ chín, nhúng lâu sẽ nhũn.', 'Fully vegetarian. Enoki cooks in moments and goes limp if left in the pot.',
       'Nồi 4-6 người', 'Pot for 4-6', 25,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'lau';

-- Lẩu hải sản chua cay
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Lẩu hải sản chua cay', 'Hot and sour seafood hotpot', 'lau-hai-san-chua-cay',
       'Tôm, mực, cá phi lê, nước dùng me', 'Prawn, squid and fish fillet in a tamarind broth',
       575000, NULL, 0, 1, 156,
       'Tôm, mực, cá phi lê, nghêu, me chín, dứa, cà chua, ớt, rau muống, bún tươi.', 'Prawn, squid, fish fillet, clams, ripe tamarind, pineapple, tomato, chilli, water spinach, fresh vermicelli.',
       'Nước dùng nấu từ đầu tôm và xương cá, chua bằng me chín dầm. Hải sản để riêng đĩa, khách nhúng tới đâu chín tới đó.', 'The broth is built on prawn heads and fish bones and soured with mashed ripe tamarind. The seafood comes on a separate plate to be dipped as you go.',
       'Có hải sản. Món cay và chua, khách đau dạ dày nên cân nhắc.', 'Contains shellfish. Hot and sour, and may not suit a sensitive stomach.',
       'Nồi 4-6 người', 'Pot for 4-6', 30,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'lau';

-- Lẩu vịt nấu chao
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Lẩu vịt nấu chao', 'Duck hotpot with fermented bean curd', 'lau-vit-nau-chao',
       'Vịt xiêm, chao đỏ, khoai môn', 'Muscovy duck with red fermented bean curd and taro',
       465000, 'https://upload.wikimedia.org/wikipedia/commons/0/0e/Ginger_duck_hot_pot.jpg', 0, 1, 157,
       'Vịt xiêm, chao đỏ, khoai môn, nước dừa, sả, rau muống, bún tươi.', 'Muscovy duck, red fermented bean curd, taro, coconut water, lemongrass, water spinach, fresh vermicelli.',
       'Vịt ướp chao và sả một tiếng rồi hầm nước dừa. Khoai môn chiên sơ trước khi thả vào để giữ hình mà vẫn bở.', 'The duck marinates an hour in fermented bean curd and lemongrass, then stews in coconut water. The taro is lightly fried first so it holds its shape while staying floury.',
       'Chao có mùi đậm, khách chưa quen nên cân nhắc. Hầm lâu nên đặt trước.', 'Fermented bean curd has a strong aroma; think twice if it is new to you. The long stew means ordering ahead.',
       'Nồi 4-6 người', 'Pot for 4-6', 40,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'lau';

-- Lẩu cá hồi măng chua
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Lẩu cá hồi măng chua', 'Salmon hotpot with pickled bamboo', 'lau-ca-hoi-mang-chua',
       'Đầu và xương cá hồi, măng chua tự muối', 'Salmon head and bones with house-pickled bamboo',
       655000, NULL, 0, 1, 158,
       'Đầu và xương cá hồi, phi lê cá hồi, măng chua, cà chua, thì là, hành lá, bún tươi.', 'Salmon head and bones, salmon fillet, pickled bamboo shoot, tomato, dill, spring onion, fresh vermicelli.',
       'Đầu và xương cá nướng sơ trước khi ninh để nước dùng thơm mà không tanh. Phi lê để riêng, nhúng khi ăn.', 'The head and bones are roasted before simmering so the broth smells sweet rather than fishy. The fillet is kept separate and dipped as you eat.',
       'Có hải sản. Măng chua tự muối nên độ chua thay đổi theo mẻ.', 'Contains fish. The bamboo is pickled in house, so the sourness varies a little between batches.',
       'Nồi 4-6 người', 'Pot for 4-6', 30,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'lau';

-- Lẩu ếch măng cay
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Lẩu ếch măng cay', 'Frog hotpot with bamboo and chilli', 'lau-ech-mang-cay',
       'Ếch đồng, măng tươi, cay nồng', 'Field frog with fresh bamboo, distinctly hot',
       495000, NULL, 0, 1, 159,
       'Ếch đồng, măng tươi, ớt hiểm, sa tế, rau muống, tía tô, bún tươi.', 'Field frog, fresh bamboo shoot, bird chilli, chilli oil, water spinach, perilla, fresh vermicelli.',
       'Ếch ướp sa tế rồi xào săn trước khi cho nước vào, cách này giữ thịt ếch chắc thay vì bở ra trong nồi.', 'The frog is tossed in chilli oil and seared before the broth goes in, which keeps the meat firm instead of falling apart in the pot.',
       'Món rất cay, độ cay điều chỉnh được nếu báo trước. Thịt ếch nhiều xương nhỏ.', 'Very spicy, and the heat can be adjusted on request. Frog has many small bones.',
       'Nồi 4-6 người', 'Pot for 4-6', 30,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'lau';

-- Sườn nướng BBQ mật ong
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Sườn nướng BBQ mật ong', 'Honey BBQ pork ribs', 'suon-nuong-bbq',
       'Sườn cây nướng chậm, phết mật ong', 'Rib racks slow-grilled and brushed with honey',
       385000, 'https://live.staticflickr.com/1432/659524476_1561f96a48_b.jpg', 0, 1, 160,
       'Sườn cây heo, mật ong, xì dầu, tỏi, ớt bột, tiêu, khoai tây chiên.', 'Pork rib rack, honey, soy sauce, garlic, chilli powder, pepper, fried potato.',
       'Sườn ướp qua đêm, nướng chậm ở lửa nhỏ bốn mươi phút rồi mới phết mật ong ở mười phút cuối để lớp áo bóng mà không cháy khét.', 'The ribs marinate overnight and grill slowly for forty minutes, with the honey brushed on only in the last ten so the glaze shines instead of scorching.',
       'Nướng chậm nên đặt trước. Món ngọt đậm, trẻ nhỏ thường thích.', 'Slow-grilled, so please order ahead. Sweet and rich, usually a hit with children.',
       'Phần 3-4 người', 'Serves 3-4', 40,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'lau';

-- Ba chỉ nướng sả
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Ba chỉ nướng sả', 'Grilled pork belly with lemongrass', 'ba-chi-nuong-sa',
       'Ba chỉ thái dày, nướng vỉ tại bàn', 'Thick-cut pork belly grilled at your table',
       265000, 'https://upload.wikimedia.org/wikipedia/commons/thumb/8/89/Grilled_kurobuta_pork_belly_02.jpg/1280px-Grilled_kurobuta_pork_belly_02.jpg', 0, 1, 161,
       'Ba chỉ heo, sả băm, tỏi, mật ong, nước mắm, rau sống, bánh tráng, mắm nêm.', 'Pork belly, minced lemongrass, garlic, honey, fish sauce, garden herbs, rice paper, fermented anchovy sauce.',
       'Ba chỉ thái dày một phân, ướp sả tỏi bốn tiếng. Nướng vỉ tại bàn để khách canh độ chín theo ý mình.', 'The belly is cut a centimetre thick and marinates four hours in lemongrass and garlic, then grills at your table so you judge the doneness.',
       'Bếp nướng đặt tại bàn, không bố trí được ở phòng máy lạnh kín. Món nhiều mỡ.', 'The grill sits on the table and cannot be set up in a sealed air-conditioned room. A fatty dish.',
       'Phần 3-4 người', 'Serves 3-4', 25,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'lau';

-- Mực nướng sa tế
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Mực nướng sa tế', 'Grilled squid with chilli oil', 'muc-nuong-sa-te',
       'Mực ống nguyên con, nướng than', 'Whole squid tubes over charcoal',
       325000, 'https://live.staticflickr.com/4109/4997841338_86f3aaa01e_b.jpg', 0, 1, 162,
       'Mực ống tươi, sa tế, sả, tỏi, muối tiêu chanh, rau răm.', 'Fresh squid tubes, chilli oil, lemongrass, garlic, salt-pepper-lime dip, Vietnamese coriander.',
       'Mực khứa chéo thân rồi nướng nhanh ở lửa lớn, nướng quá tay thì mực co lại và dai như cao su.', 'The tubes are scored on the diagonal and grilled fast over high heat; overdone, squid shrinks and turns rubbery.',
       'Có hải sản. Món cay, làm không cay được nếu báo trước.', 'Contains shellfish. Spicy, and can be made without chilli on request.',
       'Phần 3-4 người', 'Serves 3-4', 20,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'lau';

-- Cá nục nướng giấy bạc
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Cá nục nướng giấy bạc', 'Mackerel grilled in foil', 'ca-nuc-nuong-giay-bac',
       'Cá nục tươi, hành tây và ớt chuông', 'Fresh mackerel with onion and bell pepper',
       215000, 'https://live.staticflickr.com/3044/2321144530_528500d30d_b.jpg', 0, 1, 163,
       'Cá nục tươi, hành tây, ớt chuông, sả, nghệ, bơ, chanh.', 'Fresh mackerel, onion, bell pepper, lemongrass, turmeric, butter, lime.',
       'Cá gói giấy bạc cùng rau củ rồi nướng than, hơi nước giữ lại bên trong nên thịt cá mềm chứ không khô như nướng trần.', 'The fish is sealed in foil with the vegetables and grilled over charcoal; the trapped steam keeps the flesh moist instead of drying as it would on an open grill.',
       'Cá nục nhiều xương dăm, cẩn thận với trẻ nhỏ.', 'Mackerel has many fine bones; take care with small children.',
       'Phần 3-4 người', 'Serves 3-4', 25,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'lau';

-- Gà nướng lu
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Gà nướng lu', 'Chicken roasted in a clay urn', 'ga-nuong-lu',
       'Treo trong lu đất, da giòn đều', 'Hung inside a clay urn for evenly crisp skin',
       395000, 'https://upload.wikimedia.org/wikipedia/commons/c/c1/Tandoori_Chicken_in_restaurent.jpg', 0, 1, 164,
       'Gà ta nguyên con, ngũ vị hương, mật ong, sả, muối hột, lá chanh.', 'Whole free-range chicken, five-spice, honey, lemongrass, coarse salt, kaffir lime leaf.',
       'Gà treo trong lu đất kín, chín bằng hơi nóng phản xạ từ thành lu nên da vàng đều mọi mặt mà không cần trở.', 'The bird hangs inside a sealed clay urn and cooks by heat reflected from the walls, so the skin browns evenly without turning.',
       'Nướng lu mất bốn mươi lăm phút, bắt buộc đặt trước một buổi.', 'Urn roasting takes forty-five minutes and must be ordered one session ahead.',
       'Nguyên con, 4-6 người', 'Whole bird, serves 4-6', 45,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'lau';

-- Bò bít tết tiêu đen
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Bò bít tết tiêu đen', 'Beef steak with black pepper', 'bo-bit-tet-tieu-den',
       'Thăn ngoại áp chảo, sốt tiêu đen', 'Pan-seared sirloin in black pepper sauce',
       425000, 'https://live.staticflickr.com/7034/6523198789_3746417f7c_b.jpg', 0, 1, 165,
       'Thăn ngoại bò, tiêu đen xay thô, bơ, tỏi, khoai tây nghiền, rau củ áp chảo.', 'Beef sirloin, coarsely ground black pepper, butter, garlic, mashed potato, pan-seared vegetables.',
       'Thịt để về nhiệt độ phòng trước khi áp chảo, nghỉ năm phút sau khi chín để nước thịt phân bố đều chứ không chảy hết ra đĩa.', 'The steak comes to room temperature before searing and rests five minutes afterwards so the juices settle instead of running onto the plate.',
       'Bên mình làm chín vừa theo mặc định, khách muốn độ chín khác báo trước khi gọi.', 'Cooked medium by default; tell us when ordering if you would like it otherwise.',
       'Phần 1-2 người', 'Serves 1-2', 20,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'lau';

-- Tôm hùm nướng phô mai
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Tôm hùm nướng phô mai', 'Lobster grilled with cheese', 'tom-hum-nuong-pho-mai',
       'Tôm hùm xanh, phô mai mozzarella', 'Green lobster with mozzarella',
       1250000, 'https://live.staticflickr.com/8246/8558909573_cd5a71f0fa_b.jpg', 0, 1, 166,
       'Tôm hùm xanh, phô mai mozzarella, bơ tỏi, tiêu, chanh, rau củ nướng.', 'Green lobster, mozzarella, garlic butter, pepper, lime, grilled vegetables.',
       'Tôm bổ đôi, phết bơ tỏi rồi nướng tới khi thịt vừa đục, phủ phô mai và nướng thêm ba phút cho vàng mặt.', 'The lobster is halved, brushed with garlic butter and grilled until the flesh turns opaque, then topped with cheese and given three more minutes to brown.',
       'Giá theo tôm sống trong bể, tính theo cân thực tế. Có hải sản và sữa.', 'Priced from the live tank and billed by actual weight. Contains shellfish and dairy.',
       'Phần 2-3 người', 'Serves 2-3', 30,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'lau';

-- Hàu nướng mỡ hành
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Hàu nướng mỡ hành', 'Grilled oysters with scallion oil', 'hau-nuong-mo-hanh',
       'Hàu sữa nguyên vỏ, đậu phộng rang', 'Milk oysters in the shell with roasted peanuts',
       235000, NULL, 0, 1, 167,
       'Hàu sữa, mỡ hành, đậu phộng rang giã, hành phi, muối tiêu chanh.', 'Milk oysters, scallion oil, crushed roasted peanuts, fried shallots, salt-pepper-lime dip.',
       'Hàu tách vỏ giữ nguyên nước, nướng than chừng ba phút rồi chan mỡ hành, không nướng lâu vì hàu sẽ teo lại.', 'The oysters are shucked with their liquor kept, grilled about three minutes and finished with scallion oil; longer and they shrink away.',
       'Có hải sản và đậu phộng. Hàu theo con nước, có ngày hết hàng.', 'Contains shellfish and peanuts. Oysters follow the tides and some days sell out.',
       'Phần 12 con', '12 pieces', 20,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'lau';

-- Nấm nướng sa tế
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Nấm nướng sa tế', 'Grilled mushrooms with chilli oil', 'nam-nuong-sa-te',
       'Món chay, nấm đùi gà và bào ngư', 'Vegetarian, king oyster and oyster mushrooms',
       145000, NULL, 0, 1, 168,
       'Nấm đùi gà, nấm bào ngư, sa tế chay, sả, dầu ăn, muối tiêu chanh.', 'King oyster mushroom, oyster mushroom, vegetarian chilli oil, lemongrass, oil, salt-pepper-lime dip.',
       'Nấm cắt dày rồi nướng vỉ cho xém cạnh, phết sa tế ở cuối để ớt không cháy đắng trên than.', 'The mushrooms are cut thick and grilled until the edges char, with the chilli oil brushed on at the end so it does not burn bitter over the coals.',
       'Món chay hoàn toàn nếu dùng sa tế chay, báo trước khi gọi. Món cay.', 'Fully vegetarian when made with vegetarian chilli oil — say so when ordering. Spicy.',
       'Phần 2-3 người', 'Serves 2-3', 20,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'lau';

-- Combo lẩu nướng gia đình
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Combo lẩu nướng gia đình', 'Family hotpot and grill set', 'combo-lau-nuong-gia-dinh',
       'Một nồi lẩu và tám món nướng, cho 6 người', 'One hotpot plus eight grill items, for six people',
       890000, 'https://live.staticflickr.com/4066/4210126172_4e3789dab3_b.jpg', 0, 1, 169,
       'Nồi lẩu chua cay, bò, ba chỉ, tôm, mực, nấm, bắp, rau nhúng, bún tươi.', 'Hot-and-sour hotpot, beef, pork belly, prawn, squid, mushroom, corn, dipping greens, fresh vermicelli.',
       'Bố trí bếp lẩu và vỉ nướng cùng lúc trên một bàn, đồ nướng ướp sẵn từ bếp để khách chỉ việc đặt lên vỉ.', 'A hotpot burner and a grill are set up side by side on one table, with everything pre-marinated in the kitchen so you only have to lay it on.',
       'Cần bàn rộng và khu vực thoáng khói, không bố trí được ở phòng máy lạnh kín. Có hải sản.', 'Needs a large table and a well-ventilated area; cannot be set up in a sealed air-conditioned room. Contains shellfish.',
       'Phần 6-8 người', 'Serves 6-8', 35,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'lau';

-- Chè ba màu
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Chè ba màu', 'Three-colour sweet soup', 'che-ba-mau',
       'Đậu đỏ, đậu xanh, thạch và nước cốt dừa', 'Red bean, mung bean, jelly and coconut cream',
       45000, 'https://upload.wikimedia.org/wikipedia/commons/thumb/7/75/Chendol2.jpg/1280px-Chendol2.jpg', 0, 1, 170,
       'Đậu đỏ, đậu xanh đánh nhuyễn, thạch lá dứa, nước cốt dừa, đá bào, đường.', 'Red bean, whipped mung bean, pandan jelly, coconut cream, shaved ice, sugar.',
       'Ba lớp múc riêng để giữ đúng màu, nước cốt dừa chan trên cùng ngay trước khi dọn để lớp màu không bị hòa lẫn.', 'The three layers are spooned in separately to keep their colours, and the coconut cream is poured last so the layers do not blur.',
       'Món ngọt nhiều, giảm đường được nếu báo trước. Dọn lạnh, để lâu đá tan sẽ nhạt.', 'Quite sweet, and we can cut the sugar on request. Served cold; as the ice melts it thins.',
       'Phần 2 ly', '2 glasses', 10,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'trangmieng';

-- Chè trôi nước
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Chè trôi nước', 'Glutinous rice balls in ginger syrup', 'che-troi-nuoc',
       'Viên nếp nhân đậu xanh, nước gừng ấm', 'Mung bean filled rice balls in warm ginger syrup',
       50000, 'https://upload.wikimedia.org/wikipedia/commons/thumb/f/fd/M%C3%B3n_ch%C3%A8_%E1%BB%9F_B%C3%ACnh_Long_n%C4%83m_2020.jpg/1280px-M%C3%B3n_ch%C3%A8_%E1%BB%9F_B%C3%ACnh_Long_n%C4%83m_2020.jpg', 0, 1, 171,
       'Bột nếp, đậu xanh, gừng tươi, đường thốt nốt, nước cốt dừa, mè rang.', 'Glutinous rice flour, mung bean, fresh ginger, palm sugar, coconut cream, toasted sesame.',
       'Viên nặn tay rồi luộc đến khi nổi lên mặt nước là chín. Nước gừng nấu riêng với đường thốt nốt cho thơm mà không bị đục.', 'The balls are shaped by hand and boiled until they float. The ginger syrup is cooked separately with palm sugar so it stays clear and fragrant.',
       'Dọn ấm mới ngon. Nếp dẻo và dai, trẻ nhỏ ăn cần cắt đôi.', 'Best served warm. The rice balls are soft and chewy; halve them for small children.',
       'Phần 4 viên', '4 pieces', 25,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'trangmieng';

-- Chè hạt sen nhãn nhục
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Chè hạt sen nhãn nhục', 'Lotus seed and longan sweet soup', 'che-hat-sen-nhan-nhuc',
       'Hạt sen Huế, nhãn nhục, dọn lạnh', 'Hue lotus seeds with dried longan, served chilled',
       65000, 'https://live.staticflickr.com/2151/2103310712_bb744fb156_b.jpg', 0, 1, 172,
       'Hạt sen tươi, nhãn nhục, đường phèn, lá dứa.', 'Fresh lotus seeds, dried longan, rock sugar, pandan leaf.',
       'Hạt sen bỏ tâm rồi hầm đường phèn ba mươi phút cho bở mà vẫn nguyên hạt. Nhãn nhục ngâm nở, cho vào sau cùng.', 'The lotus seeds are cored and simmered in rock sugar for thirty minutes until floury but whole. The longan is soaked and added last.',
       'Hạt sen tươi theo mùa, hết mùa bên mình dùng hạt sen khô và sẽ báo trước.', 'Fresh lotus seeds are seasonal; out of season we use dried and will tell you.',
       'Phần 2 chén', '2 bowls', 30,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'trangmieng';

-- Chè khoai dẻo
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Chè khoai dẻo', 'Chewy sweet potato balls', 'che-khoai-deo',
       'Viên khoai ba màu, nước cốt dừa', 'Three-colour sweet potato balls in coconut cream',
       48000, 'https://live.staticflickr.com/4145/5082856547_fce57739fd_b.jpg', 0, 1, 173,
       'Khoai lang tím, khoai lang vàng, khoai môn, bột năng, nước cốt dừa, đường thốt nốt.', 'Purple sweet potato, yellow sweet potato, taro, tapioca starch, coconut cream, palm sugar.',
       'Ba loại khoai hấp riêng rồi nhồi với bột năng để giữ đúng màu tự nhiên, luộc từng mẻ cho viên dẻo đều.', 'Each root is steamed and kneaded with tapioca starch separately so the natural colours stay true, then boiled in small batches for an even chew.',
       'Viên khoai dai, ăn nóng ngon hơn. Món chay hoàn toàn.', 'The balls are chewy and better warm. Fully vegetarian.',
       'Phần 2 chén', '2 bowls', 25,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'trangmieng';

-- Sương sa hạt lựu
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Sương sa hạt lựu', 'Jelly and water chestnut dessert', 'suong-sa-hat-luu',
       'Hạt lựu giòn, sương sa và nước cốt dừa', 'Crunchy water chestnut with jelly and coconut cream',
       45000, 'https://upload.wikimedia.org/wikipedia/commons/f/f6/Tub_tim_krob_in_Singapore_-_20050520.jpg', 0, 1, 174,
       'Củ năng bọc bột năng, sương sa, đậu xanh, nước cốt dừa, đá bào, lá dứa.', 'Water chestnut coated in tapioca, agar jelly, mung bean, coconut cream, shaved ice, pandan leaf.',
       'Củ năng thái hạt lựu, lăn bột năng rồi luộc nhanh để lớp vỏ trong mà ruột vẫn giòn sần sật.', 'The water chestnut is diced, rolled in tapioca starch and boiled briefly so the coating turns clear while the centre keeps its crunch.',
       'Dọn lạnh. Món chay hoàn toàn.', 'Served cold. Fully vegetarian.',
       'Phần 2 ly', '2 glasses', 20,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'trangmieng';

-- Bánh flan cà phê
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Bánh flan cà phê', 'Coffee caramel custard', 'banh-flan-ca-phe',
       'Flan hấp, rưới cà phê đen đá', 'Steamed custard topped with iced black coffee',
       40000, 'https://upload.wikimedia.org/wikipedia/commons/thumb/4/43/Homemade_Flan.jpg/1280px-Homemade_Flan.jpg', 0, 1, 175,
       'Trứng gà, sữa tươi, sữa đặc, đường caramel, cà phê phin, đá bào.', 'Egg, fresh milk, condensed milk, caramel, drip coffee, shaved ice.',
       'Hấp cách thủy ở lửa nhỏ và đậy khăn lên nắp để hơi nước không nhỏ xuống làm rỗ mặt bánh.', 'Steamed gently in a water bath with a cloth under the lid so condensation does not pit the surface.',
       'Có trứng và sữa. Cà phê có caffeine, trẻ nhỏ nên dùng bản không cà phê.', 'Contains egg and dairy. The coffee has caffeine; children should have the plain version.',
       'Phần 2 cái', '2 pieces', 15,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'trangmieng';

-- Bánh chuối hấp nước cốt dừa
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Bánh chuối hấp nước cốt dừa', 'Steamed banana cake with coconut cream', 'banh-chuoi-hap-nuoc-cot-dua',
       'Chuối sứ chín, bột năng, mè rang', 'Ripe plantain, tapioca and toasted sesame',
       42000, 'https://upload.wikimedia.org/wikipedia/commons/b/b4/Steamed_banana_cake.jpg', 0, 1, 176,
       'Chuối sứ chín, bột năng, nước cốt dừa, đường, mè rang, muối.', 'Ripe plantain, tapioca starch, coconut cream, sugar, toasted sesame, salt.',
       'Chuối phải thật chín mới đủ ngọt để không cần nhiều đường. Hấp hai mươi lăm phút cho bánh trong và dẻo.', 'The plantain must be fully ripe to be sweet enough without much added sugar. Twenty-five minutes of steaming turns the cake translucent and chewy.',
       'Món chay hoàn toàn. Ăn trong ngày là ngon nhất.', 'Fully vegetarian. Best eaten the same day.',
       'Phần 4 miếng', '4 pieces', 25,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'trangmieng';

-- Kem dừa trái dừa
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Kem dừa trái dừa', 'Coconut ice cream in the shell', 'kem-dua-trai-dua',
       'Kem dừa dọn trong trái dừa, đậu phộng', 'Coconut ice cream served in the shell with peanuts',
       75000, 'https://live.staticflickr.com/2493/3843771409_f16640362d_b.jpg', 0, 1, 177,
       'Kem dừa, cơm dừa nạo, đậu phộng rang, mứt dừa, trái dừa tươi.', 'Coconut ice cream, shredded coconut flesh, roasted peanuts, candied coconut, fresh coconut.',
       'Dừa chặt lấy nước và giữ nguyên gáo làm chén, múc kem vào rồi rắc cơm dừa và đậu phộng lên trên.', 'The coconut is opened, the water reserved and the shell kept as a bowl, then filled with ice cream and topped with coconut flesh and peanuts.',
       'Có sữa và đậu phộng. Kem tan nhanh nên dọn là ăn ngay.', 'Contains dairy and peanuts. The ice cream melts fast, so eat it as soon as it arrives.',
       'Phần 1 trái', 'One coconut', 15,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'trangmieng';

-- Sinh tố bơ kem
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Sinh tố bơ kem', 'Avocado smoothie with ice cream', 'sinh-to-bo-kem',
       'Bơ sáp Đắk Lắk, sữa đặc', 'Dak Lak butter avocado with condensed milk',
       55000, 'https://live.staticflickr.com/4525/38768201132_cc6cccb1a9.jpg', 0, 1, 178,
       'Bơ sáp Đắk Lắk, sữa đặc, sữa tươi, kem tươi, đá.', 'Dak Lak butter avocado, condensed milk, fresh milk, cream, ice.',
       'Bơ chín tới xay cùng đá và sữa, xay vừa đủ để giữ độ sánh, xay lâu quá thì hỗn hợp loãng ra.', 'Ripe avocado is blended with ice and milk just long enough to stay thick; over-blend it and the mixture thins.',
       'Có sữa. Bơ theo mùa, hết mùa bên mình sẽ báo trước.', 'Contains dairy. Avocado is seasonal and we will tell you when it is unavailable.',
       'Phần 1 ly lớn', 'One large glass', 10,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'trangmieng';

-- Tàu hũ nước đường
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Tàu hũ nước đường', 'Silken tofu in ginger syrup', 'tau-hu-nuoc-duong',
       'Tàu hũ non, nước đường gừng', 'Silken tofu in warm ginger syrup',
       35000, 'https://live.staticflickr.com/5109/5567285168_428757f673.jpg', 0, 1, 179,
       'Tàu hũ non, gừng tươi, đường thốt nốt, lá dứa, nước cốt dừa.', 'Silken tofu, fresh ginger, palm sugar, pandan leaf, coconut cream.',
       'Tàu hũ hớt từng lát mỏng bằng muỗng dẹt chứ không múc cục, để miếng tàu hũ mịn và không vỡ.', 'The tofu is skimmed in thin sheets with a flat spoon rather than scooped, so it stays smooth and unbroken.',
       'Món chay hoàn toàn. Dọn ấm, gừng cay nhẹ.', 'Fully vegetarian. Served warm, with a gentle ginger heat.',
       'Phần 2 chén', '2 bowls', 15,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'trangmieng';

-- Xôi xoài nước cốt dừa
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Xôi xoài nước cốt dừa', 'Mango sticky rice', 'xoi-xoai-nuoc-cot-dua',
       'Xôi nếp dẻo, xoài cát Hòa Lộc', 'Sticky rice with Hoa Loc mango',
       85000, 'https://live.staticflickr.com/575/22525075347_d687b6c71e_b.jpg', 0, 1, 180,
       'Nếp cái hoa vàng, xoài cát Hòa Lộc, nước cốt dừa, đường, muối, đậu xanh rang.', 'Premium glutinous rice, Hoa Loc mango, coconut cream, sugar, salt, toasted mung bean.',
       'Nếp ngâm bốn tiếng rồi hấp, trộn nước cốt dừa lúc còn nóng để hạt nếp ngấm đều và bóng.', 'The rice soaks four hours, is steamed, then folded with coconut cream while still hot so every grain absorbs it and shines.',
       'Xoài theo mùa, hết mùa bên mình sẽ báo trước. Món ngọt nhiều.', 'Mango is seasonal and we will tell you when it is unavailable. Quite sweet.',
       'Phần 2 người', 'Serves 2', 30,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'trangmieng';

-- Bánh tét lá cẩm
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Bánh tét lá cẩm', 'Magenta sticky rice cake', 'banh-tet-la-cam',
       'Nếp nhuộm lá cẩm, nhân đậu xanh thịt', 'Rice dyed with magenta leaf, mung bean and pork filling',
       60000, 'https://live.staticflickr.com/7443/16545390385_d29abdbb4b.jpg', 0, 1, 181,
       'Nếp, lá cẩm, đậu xanh, thịt ba chỉ, lá chuối, dưa món ăn kèm.', 'Glutinous rice, magenta leaf, mung bean, pork belly, banana leaf, pickled vegetables on the side.',
       'Nếp ngâm nước lá cẩm cho lên màu tím tự nhiên, gói lá chuối rồi luộc tám tiếng để nhân và vỏ quyện vào nhau.', 'The rice soaks in magenta leaf water for its natural purple, is wrapped in banana leaf and boiled eight hours so filling and rice become one.',
       'Có thịt heo nên không phải món chay. Bánh luộc sẵn, dọn hâm nóng lại.', 'Contains pork, so not vegetarian. The cake is boiled in advance and reheated to serve.',
       'Phần 4 khoanh', '4 slices', 20,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'trangmieng';

-- Chè Thái sầu riêng
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Chè Thái sầu riêng', 'Thai-style dessert with durian', 'che-thai-sau-rieng',
       'Sầu riêng Ri6, thạch và mít', 'Ri6 durian with jelly and jackfruit',
       70000, 'https://upload.wikimedia.org/wikipedia/commons/thumb/6/69/Durian_Gelato.JPG/1280px-Durian_Gelato.JPG', 0, 1, 182,
       'Sầu riêng Ri6, mít, thạch rau câu, hạt é, nước cốt dừa, sữa đặc, đá bào.', 'Ri6 durian, jackfruit, jelly, basil seeds, coconut cream, condensed milk, shaved ice.',
       'Sầu riêng tách múi để nguyên chứ không xay, trộn nhẹ với các thành phần còn lại ngay trước khi dọn.', 'The durian is segmented whole rather than blended, and folded gently with everything else just before serving.',
       'Sầu riêng mùi rất đậm, không phù hợp với phòng kín hoặc bàn có khách không ăn được sầu riêng. Có sữa.', 'Durian has a powerful aroma and does not suit an enclosed room or a table where someone dislikes it. Contains dairy.',
       'Phần 1 ly lớn', 'One large glass', 15,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'trangmieng';

-- Bánh cam nhân đậu xanh
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Bánh cam nhân đậu xanh', 'Sesame balls with mung bean', 'banh-cam-nhan-dau-xanh',
       'Vỏ nếp phủ mè, chiên vàng', 'Sesame-coated glutinous shell, fried golden',
       38000, 'https://live.staticflickr.com/4838/44062921970_90ba668638.jpg', 0, 1, 183,
       'Bột nếp, đậu xanh, mè trắng, đường, dầu ăn.', 'Glutinous rice flour, mung bean, white sesame, sugar, oil.',
       'Chiên ở lửa nhỏ và đảo liên tục để bánh nở đều thành hình tròn, lửa lớn thì vỏ cháy mà trong còn sống.', 'Fried at low heat and turned constantly so the ball puffs evenly; too hot and the shell burns while the inside stays raw.',
       'Món chay hoàn toàn. Ăn nóng mới giòn, để nguội vỏ sẽ dai.', 'Fully vegetarian. Crisp only while hot; the shell toughens as it cools.',
       'Phần 4 cái', '4 pieces', 20,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'trangmieng';

-- Sữa chua nếp cẩm
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Sữa chua nếp cẩm', 'Yoghurt with black glutinous rice', 'suu-chua-nep-cam',
       'Sữa chua nhà làm, nếp cẩm ủ men', 'House-made yoghurt with fermented black rice',
       45000, NULL, 0, 1, 184,
       'Sữa chua nhà làm, nếp cẩm ủ men, nước cốt dừa, đường.', 'House-made yoghurt, fermented black glutinous rice, coconut cream, sugar.',
       'Nếp cẩm ủ men hai ngày cho dậy mùi rượu nhẹ rồi trộn nước cốt dừa, múc lên trên lớp sữa chua khi dọn.', 'The black rice ferments two days until it carries a faint wine note, is folded with coconut cream and spooned over the yoghurt on serving.',
       'Có sữa. Nếp cẩm lên men có cồn rất nhẹ, không đáng kể nhưng vẫn báo để khách biết.', 'Contains dairy. The fermented rice holds a trace of alcohol — negligible, but we mention it.',
       'Phần 1 ly', 'One glass', 15,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'trangmieng';

-- Cà phê sữa đá
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Cà phê sữa đá', 'Iced coffee with condensed milk', 'ca-phe-sua-da',
       'Pha phin, cà phê Robusta Đắk Lắk', 'Drip-brewed Dak Lak robusta',
       35000, 'https://live.staticflickr.com/297/18863269318_2c4b93d0dc_b.jpg', 0, 1, 185,
       'Cà phê Robusta Đắk Lắk, sữa đặc, đá viên.', 'Dak Lak robusta coffee, condensed milk, ice.',
       'Pha phin từng ly, nước sôi chan hai lần: lần đầu ủ cho bột nở, lần sau mới chiết lấy nước cốt đậm.', 'Brewed one cup at a time in a phin, with water added twice: first to bloom the grounds, then to draw the concentrated brew.',
       'Cà phê Robusta rất đậm và nhiều caffeine, khách nhạy caffeine nên gọi ly nhỏ. Có sữa.', 'Robusta is strong and high in caffeine; order a small one if you are sensitive. Contains dairy.',
       'Phần 1 ly', 'One glass', 8,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'douong';

-- Cà phê đen đá
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Cà phê đen đá', 'Iced black coffee', 'ca-phe-den-da',
       'Pha phin, không sữa', 'Drip-brewed, no milk',
       30000, 'https://upload.wikimedia.org/wikipedia/commons/thumb/4/45/A_small_cup_of_coffee.JPG/1280px-A_small_cup_of_coffee.JPG', 0, 1, 186,
       'Cà phê Robusta Đắk Lắk, đường (tùy chọn), đá viên.', 'Dak Lak robusta coffee, sugar (optional), ice.',
       'Pha phin từng ly, để khách tự thêm đường theo ý chứ bên mình không pha sẵn đường.', 'Brewed one cup at a time; sugar is served on the side so you sweeten it to taste.',
       'Rất đậm và đắng, khách quen cà phê nhạt nên gọi cà phê sữa. Nhiều caffeine.', 'Strong and bitter; if you are used to milder coffee, order it with milk. High in caffeine.',
       'Phần 1 ly', 'One glass', 8,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'douong';

-- Cà phê muối
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Cà phê muối', 'Salt coffee', 'ca-phe-muoi',
       'Kem muối đánh bông phủ trên cà phê', 'Whipped salted cream over coffee',
       40000, 'https://pd.w.org/2025/09/568bab0b319be82.36307954-1536x2048.jpg', 0, 1, 187,
       'Cà phê phin, kem tươi, sữa đặc, muối biển, đá viên.', 'Drip coffee, cream, condensed milk, sea salt, ice.',
       'Kem đánh bông với chút muối biển rồi đổ nhẹ lên mặt cà phê, uống không khuấy để nếm được hai lớp riêng biệt.', 'The cream is whipped with a pinch of sea salt and floated on top; drink it unstirred to taste the two layers separately.',
       'Có sữa và kem. Vị mặn ngọt lạ, khách chưa thử nên gọi một ly chung trước.', 'Contains dairy and cream. The salty-sweet combination is unusual; share one glass first if it is new to you.',
       'Phần 1 ly', 'One glass', 10,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'douong';

-- Trà sen vàng
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Trà sen vàng', 'Golden lotus tea', 'tra-sen-vang',
       'Trà xanh ướp sen, hạt sen và củ năng', 'Lotus-scented green tea with lotus seeds and water chestnut',
       55000, NULL, 0, 1, 188,
       'Trà xanh ướp hoa sen, hạt sen, củ năng, sữa tươi, đường, đá.', 'Lotus-scented green tea, lotus seeds, water chestnut, fresh milk, sugar, ice.',
       'Trà ủ ba phút ở tám mươi độ chứ không dùng nước sôi già, nước sôi sẽ làm trà chát và át mất mùi sen.', 'The tea steeps three minutes at eighty degrees rather than boiling water, which would turn it astringent and bury the lotus scent.',
       'Có sữa. Trà có caffeine nhẹ.', 'Contains dairy. The tea carries a little caffeine.',
       'Phần 1 ly lớn', 'One large glass', 10,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'douong';

-- Trà đào cam sả
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Trà đào cam sả', 'Peach tea with orange and lemongrass', 'tra-dao-cam-sa',
       'Đào ngâm, cam tươi, sả đập dập', 'Preserved peach, fresh orange and bruised lemongrass',
       50000, 'https://live.staticflickr.com/3589/3421019933_c67db24087.jpg', 0, 1, 189,
       'Trà đen, đào ngâm, cam tươi, sả, mật ong, đá.', 'Black tea, preserved peach, fresh orange, lemongrass, honey, ice.',
       'Sả đập dập ngâm cùng trà nóng mười phút cho ra tinh dầu, sau đó mới thêm đào và cam để không bị đắng vỏ.', 'The bruised lemongrass steeps in the hot tea for ten minutes to release its oils; the peach and orange go in afterwards so the peel does not turn it bitter.',
       'Món ngọt, giảm đường được nếu báo trước. Có caffeine nhẹ từ trà đen.', 'Sweet, and the sugar can be reduced on request. A little caffeine from the black tea.',
       'Phần 1 ly lớn', 'One large glass', 10,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'douong';

-- Trà tắc mật ong
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Trà tắc mật ong', 'Calamansi honey tea', 'tra-tac-mat-ong',
       'Tắc tươi vắt tay, mật ong rừng', 'Hand-squeezed calamansi with forest honey',
       40000, 'https://live.staticflickr.com/7680/16874980828_a65eee95fa_b.jpg', 0, 1, 190,
       'Trà xanh, tắc tươi, mật ong rừng, đá viên.', 'Green tea, fresh calamansi, forest honey, ice.',
       'Tắc vắt tay và bỏ hạt ngay, để hạt ngâm lâu nước sẽ đắng. Mật ong pha khi trà đã nguội để giữ hương.', 'The calamansi is squeezed by hand and the seeds removed at once, since they turn the drink bitter. The honey goes in once the tea has cooled, to keep its aroma.',
       'Món chua, khách đau dạ dày nên uống sau bữa ăn. Có mật ong, không dùng cho trẻ dưới một tuổi.', 'Sour, so drink it after eating if your stomach is sensitive. Contains honey, which is not for children under one.',
       'Phần 1 ly', 'One glass', 8,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'douong';

-- Nước mía tắc
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Nước mía tắc', 'Sugarcane juice with calamansi', 'nuoc-mia-tac',
       'Ép tại chỗ, vắt thêm tắc tươi', 'Pressed to order with fresh calamansi',
       30000, 'https://live.staticflickr.com/3030/2892843607_75266c1abb_b.jpg', 0, 1, 191,
       'Mía tươi, tắc, đá viên.', 'Fresh sugarcane, calamansi, ice.',
       'Ép mía ngay khi khách gọi, nước mía để quá mười lăm phút sẽ đổi màu và mất vị ngọt thanh.', 'Pressed the moment you order; sugarcane juice left more than fifteen minutes darkens and loses its clean sweetness.',
       'Ngọt tự nhiên nhưng khá cao đường, khách tiểu đường nên cân nhắc.', 'Naturally sweet but high in sugar; something to consider if you are diabetic.',
       'Phần 1 ly lớn', 'One large glass', 5,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'douong';

-- Nước chanh dây
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Nước chanh dây', 'Passion fruit juice', 'nuoc-chanh-day',
       'Chanh dây tươi, giữ nguyên hạt', 'Fresh passion fruit with the seeds kept in',
       35000, 'https://live.staticflickr.com/2832/34488746945_1260832462_b.jpg', 0, 1, 192,
       'Chanh dây tươi, đường, nước lọc, đá viên.', 'Fresh passion fruit, sugar, water, ice.',
       'Giữ nguyên hạt chứ không lọc, hạt chanh dây giòn và là phần khách hay thích nhất.', 'The seeds are left in rather than strained out; they are crunchy and usually the part people like best.',
       'Món chua, khách đau dạ dày nên uống sau bữa ăn.', 'Sour, so drink it after eating if your stomach is sensitive.',
       'Phần 1 ly', 'One glass', 5,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'douong';

-- Sinh tố xoài
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Sinh tố xoài', 'Mango smoothie', 'sinh-to-xoai',
       'Xoài cát chín, sữa tươi', 'Ripe cat mango with fresh milk',
       50000, 'https://live.staticflickr.com/3546/3872794615_2e252d3ed0.jpg', 0, 1, 193,
       'Xoài cát chín, sữa tươi, sữa đặc, đá.', 'Ripe cat mango, fresh milk, condensed milk, ice.',
       'Xoài chín cây xay cùng đá, không thêm si rô để giữ đúng vị trái.', 'Tree-ripened mango blended with ice and no syrup, so the fruit speaks for itself.',
       'Có sữa. Xoài theo mùa, hết mùa bên mình sẽ báo trước.', 'Contains dairy. Mango is seasonal and we will tell you when it is unavailable.',
       'Phần 1 ly lớn', 'One large glass', 8,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'douong';

-- Nước ép dưa hấu
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Nước ép dưa hấu', 'Watermelon juice', 'nuoc-ep-dua-hau',
       'Ép nguyên trái, không thêm đường', 'Pressed from whole fruit with no added sugar',
       40000, 'https://live.staticflickr.com/4073/4782853161_74f25c86f4_b.jpg', 0, 1, 194,
       'Dưa hấu ruột đỏ, chanh, đá viên.', 'Red watermelon, lime, ice.',
       'Ép nguyên trái, vắt thêm chút chanh để vị không bị ngọt gắt và giữ màu đỏ tươi lâu hơn.', 'Pressed from whole fruit with a squeeze of lime, which keeps the sweetness from cloying and holds the red colour longer.',
       'Không thêm đường. Ép xong nên uống ngay, để lâu sẽ tách nước.', 'No added sugar. Best drunk straight away, as it separates on standing.',
       'Phần 1 ly lớn', 'One large glass', 8,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'douong';

-- Nước rau má đậu xanh
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Nước rau má đậu xanh', 'Pennywort and mung bean drink', 'nuoc-rau-ma-dau-xanh',
       'Rau má xay, đậu xanh và nước cốt dừa', 'Blended pennywort with mung bean and coconut cream',
       38000, 'https://live.staticflickr.com/5185/5749930962_0883a04e65_b.jpg', 0, 1, 195,
       'Rau má tươi, đậu xanh nấu nhuyễn, nước cốt dừa, đường, đá.', 'Fresh pennywort, cooked mung bean, coconut cream, sugar, ice.',
       'Rau má xay với nước lọc rồi vắt bỏ bã, nếu để cả bã thì uống sẽ đắng và lợn cợn.', 'The pennywort is blended with water and strained; left unstrained the drink turns bitter and gritty.',
       'Rau má có tính hàn, không nên uống nhiều khi đang bụng đói. Món chay hoàn toàn.', 'Pennywort is considered cooling and is best not drunk on an empty stomach. Fully vegetarian.',
       'Phần 1 ly lớn', 'One large glass', 10,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'douong';

-- Trà atiso Đà Lạt
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Trà atiso Đà Lạt', 'Da Lat artichoke tea', 'tra-atiso-da-lat',
       'Hoa atiso khô, không caffeine', 'Dried artichoke flower, caffeine free',
       35000, 'https://live.staticflickr.com/5159/5872068364_fea1a605e2_b.jpg', 0, 1, 196,
       'Hoa atiso khô Đà Lạt, đường phèn (tùy chọn), nước lọc.', 'Dried Da Lat artichoke flower, rock sugar (optional), water.',
       'Hãm hai mươi phút cho ra hết chất, dọn nóng hoặc để nguội tùy khách. Không dùng nước sôi già để tránh vị chát.', 'Steeped twenty minutes to draw everything out, served hot or cooled as you prefer. Not boiled hard, which would make it astringent.',
       'Không có caffeine, hợp cho người lớn tuổi và trẻ nhỏ. Vị nhạt, thêm đường phèn nếu khách muốn.', 'Caffeine free, suitable for older guests and children. Mild in flavour; rock sugar on request.',
       'Bình 1 lít', 'One litre pot', 10,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'douong';

-- Soda chanh bạc hà
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Soda chanh bạc hà', 'Lime and mint soda', 'soda-chanh-bac-ha',
       'Soda, chanh tươi, lá bạc hà dầm', 'Soda with fresh lime and muddled mint',
       45000, 'https://live.staticflickr.com/4147/5084101811_9f84644414_b.jpg', 0, 1, 197,
       'Soda, chanh tươi, lá bạc hà, si rô đường, đá viên.', 'Soda water, fresh lime, mint leaves, sugar syrup, ice.',
       'Bạc hà dầm nhẹ cho ra tinh dầu chứ không giã nát, giã nát thì lá ra vị đắng.', 'The mint is muddled lightly to release its oils, not crushed — crushed leaves turn bitter.',
       'Món có ga, khách đầy bụng nên cân nhắc. Không cồn.', 'Carbonated, so think twice if you feel bloated. Non-alcoholic.',
       'Phần 1 ly lớn', 'One large glass', 8,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'douong';

-- Nước gừng mật ong nóng
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Nước gừng mật ong nóng', 'Hot ginger honey drink', 'nuoc-dua-tac-nong',
       'Gừng tươi đập dập, mật ong rừng', 'Bruised fresh ginger with forest honey',
       35000, 'https://live.staticflickr.com/4026/4384541378_10f294fc92_b.jpg', 0, 1, 198,
       'Gừng tươi, mật ong rừng, chanh, nước nóng.', 'Fresh ginger, forest honey, lime, hot water.',
       'Gừng đập dập hãm nước nóng mười phút, mật ong cho vào khi nước đã hạ nhiệt để không mất dưỡng chất.', 'The bruised ginger steeps ten minutes in hot water, and the honey goes in once it has cooled a little so its properties are not lost.',
       'Gừng cay nồng, trẻ nhỏ nên dùng loãng. Có mật ong, không dùng cho trẻ dưới một tuổi.', 'Ginger is pungent; dilute it for young children. Contains honey, which is not for children under one.',
       'Phần 1 ly', 'One glass', 8,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'douong';

-- Vang đỏ ly
INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,
                    price, image_url, best_seller, available, sort_order,
                    ingredients, ingredients_en, preparation, preparation_en,
                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,
                    created_at, updated_at)
SELECT c.id,
       'Vang đỏ ly', 'Red wine by the glass', 'vang-do-ly',
       'Vang Chile, rót theo ly 150ml', 'Chilean red, poured in 150ml glasses',
       145000, 'https://live.staticflickr.com/2551/3665139018_3c3c7b33f7_b.jpg', 0, 1, 199,
       'Vang đỏ Chile, nhãn thay đổi theo lô nhập.', 'Chilean red wine; the label changes with each shipment.',
       'Rót theo ly từ chai đã mở trong ngày, chai mở quá một ngày bên mình không phục vụ nữa.', 'Poured from bottles opened the same day; we do not serve from a bottle opened more than a day ago.',
       'Đồ uống có cồn, không phục vụ người dưới 18 tuổi và người lái xe. Nhãn thay đổi theo lô nhập, bên mình báo khi khách gọi.', 'Contains alcohol; not served to anyone under eighteen or to drivers. The label changes with each shipment and we will tell you what is open.',
       'Ly 150ml', '150ml glass', 5,
       NOW(), NOW()
FROM dish_categories c WHERE c.code = 'douong';
