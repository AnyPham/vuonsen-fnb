-- Bo sung noi dung chi tiet cho tung mon an va bang anh cho mon.
--
-- Truoc day mon an chi co mot dong mo ta ngan va mot anh. Khach muon biet
-- mon co nguyen lieu gi, lam the nao, co gi can luu y truoc khi dat thi khong
-- co cho nao tra loi, phai goi dien hoi.
--
-- Them cot slug de moi mon co duong dan rieng, vi truoc do khong co cach nao
-- tro toi mot mon cu the.

ALTER TABLE dishes ADD COLUMN slug VARCHAR(180);
ALTER TABLE dishes ADD COLUMN ingredients VARCHAR(1000);
ALTER TABLE dishes ADD COLUMN preparation VARCHAR(1500);
ALTER TABLE dishes ADD COLUMN order_note VARCHAR(800);
ALTER TABLE dishes ADD COLUMN portion_desc VARCHAR(120);
ALTER TABLE dishes ADD COLUMN prep_minutes INT;

UPDATE dishes SET
    slug         = 'goi-cu-hu-dua-tom-thit',
    portion_desc = N'Phần 3-4 người',
    prep_minutes = 20,
    ingredients  = N'Củ hũ dừa Bến Tre, tôm sú, thịt ba chỉ, hành tây, rau răm, đậu phộng rang, nước mắm chua ngọt.',
    preparation  = N'Củ hũ dừa bào mỏng ngâm nước đá cho giòn. Tôm luộc bóc vỏ, thịt ba chỉ luộc thái mỏng. Trộn đều với nước mắm pha chua ngọt ngay trước khi dọn để gỏi không ra nước.',
    order_note   = N'Trộn trước 30 phút là gỏi mềm và chảy nước, nên bên mình chỉ trộn khi khách đã ngồi vào bàn. Có đậu phộng, báo trước nếu khách dị ứng.'
  WHERE name = N'Gỏi củ hũ dừa tôm thịt';

UPDATE dishes SET
    slug         = 'cha-gio-re-hai-san',
    portion_desc = N'Phần 10 cuốn',
    prep_minutes = 25,
    ingredients  = N'Bánh rế, tôm, mực, thịt xay, khoai môn, nấm mèo, miến, cà rốt.',
    preparation  = N'Nhân trộn để nghỉ 20 phút cho thấm rồi cuốn bằng bánh rế. Chiên ngập dầu hai lửa: lửa vừa cho chín trong, vớt ra để nguội rồi chiên lại lửa lớn cho vỏ giòn lâu.',
    order_note   = N'Món có hải sản. Đặt trên 20 phần nên báo trước một ngày vì phải cuốn tay.'
  WHERE name = N'Chả giò rế hải sản';

UPDATE dishes SET
    slug         = 'banh-xeo-mien-tay',
    portion_desc = N'Phần 2 cái lớn',
    prep_minutes = 20,
    ingredients  = N'Bột gạo pha nước cốt dừa và nghệ, tôm, thịt ba chỉ, giá đỗ, đậu xanh, rau sống các loại.',
    preparation  = N'Bột pha loãng để nghỉ 2 tiếng cho bánh giòn. Đổ chảo gang nóng già, tráng mỏng, đậy nắp cho chín nhân rồi mở nắp cho vỏ giòn.',
    order_note   = N'Bánh phải ăn nóng ngay khi ra chảo nên bên mình đổ theo lượt, không đổ sẵn. Bàn đông sẽ dọn làm nhiều đợt.'
  WHERE name = N'Bánh xèo miền Tây';

UPDATE dishes SET
    slug         = 'bo-la-lot-nuong-than',
    portion_desc = N'Phần 10 cuốn',
    prep_minutes = 20,
    ingredients  = N'Thịt bò xay, lá lốt tươi, sả, tỏi, hành tím, mỡ chài.',
    preparation  = N'Thịt bò ướp sả tỏi băm để ngấm 1 tiếng, cuốn lá lốt rồi nướng than hoa. Trở đều tay cho lá xém cạnh mà thịt bên trong còn mọng.',
    order_note   = N'Nướng than nên có khói, không đặt được cho phòng máy lạnh kín. Bên mình nướng ở khu bếp ngoài rồi mang vào.'
  WHERE name = N'Bò lá lốt nướng than';

UPDATE dishes SET
    slug         = 'sup-cua-trung-bac-thao',
    portion_desc = N'Phần 4 chén',
    prep_minutes = 15,
    ingredients  = N'Thịt cua, trứng bắc thảo, trứng cút, nấm đông cô, bắp non, trứng gà đánh tan.',
    preparation  = N'Nước dùng gà hầm trong, nêm nhạt rồi thả thịt cua và nấm. Rót trứng đánh thành sợi ở lửa nhỏ, cuối cùng mới cho trứng bắc thảo để không bị nát.',
    order_note   = N'Có trứng bắc thảo, vị hơi lạ với trẻ nhỏ. Đặt riêng phần không trứng bắc thảo được, báo trước khi gọi món.'
  WHERE name = N'Súp cua trứng bắc thảo';

UPDATE dishes SET
    slug         = 'nham-bap-cai-tom-kho',
    portion_desc = N'Phần 3-4 người',
    prep_minutes = 15,
    ingredients  = N'Bắp cải bào sợi, tôm khô, thịt ba chỉ, rau răm, hành phi, nước mắm chua ngọt.',
    preparation  = N'Tôm khô ngâm nở rồi rang thơm. Bắp cải bào sợi ngâm nước đá cho giòn, vắt ráo, trộn cùng tôm khô và thịt luộc thái sợi.',
    order_note   = N'Món trộn nên dọn ngay. Có tôm khô, khách dị ứng hải sản báo trước để đổi món khác.'
  WHERE name = N'Nham bắp cải tôm khô';

UPDATE dishes SET
    slug         = 'ca-loc-nuong-trui-cuon-banh-trang',
    portion_desc = N'Phần 4-6 người',
    prep_minutes = 40,
    ingredients  = N'Cá lóc đồng nguyên con, rơm nướng, bánh tráng, bún, rau sống, chuối chát, khế, mắm nêm.',
    preparation  = N'Cá lóc để nguyên vảy, xiên que tre rồi vùi rơm nướng cho đến khi lớp vảy cháy đen. Cạo lớp cháy, thịt bên trong trắng ngọt, dọn kèm mâm rau và bánh tráng cuốn.',
    order_note   = N'Nướng rơm mất khoảng 40 phút, nên đặt trước ít nhất một buổi. Cá còn xương dăm, cẩn thận với trẻ nhỏ.'
  WHERE name = N'Cá lóc nướng trui cuốn bánh tráng';

UPDATE dishes SET
    slug         = 'ga-ta-hap-la-chanh',
    portion_desc = N'Phần nguyên con, 4-6 người',
    prep_minutes = 45,
    ingredients  = N'Gà ta thả vườn, lá chanh, gừng, hành, muối tiêu chanh.',
    preparation  = N'Gà xát muối gừng rồi hấp cách thủy cùng lá chanh, hấp vừa chín tới để da giòn và thịt còn ngọt. Chặt miếng, rắc lá chanh thái chỉ.',
    order_note   = N'Gà ta dai hơn gà công nghiệp, đó là đặc trưng chứ không phải chưa chín. Hấp mất 45 phút nên đặt trước.'
  WHERE name = N'Gà ta hấp lá chanh';

UPDATE dishes SET
    slug         = 'tom-cang-nuong-muoi-ot',
    portion_desc = N'Phần 4 con lớn',
    prep_minutes = 25,
    ingredients  = N'Tôm càng xanh, muối hột, ớt hiểm, sả, hành lá, mỡ hành.',
    preparation  = N'Tôm càng chẻ lưng, ướp muối ớt sả giã thô rồi nướng than hoa. Rưới mỡ hành lúc gần chín để thịt tôm bóng và thơm.',
    order_note   = N'Món cay vừa, có thể làm không cay nếu báo trước. Giá theo tôm tươi trong ngày nên có thể chênh so với bảng giá.'
  WHERE name = N'Tôm càng nướng muối ớt';

UPDATE dishes SET
    slug         = 'suon-non-kho-to',
    portion_desc = N'Phần 3-4 người',
    prep_minutes = 40,
    ingredients  = N'Sườn non, nước màu dừa, nước mắm, hành tím, tiêu xanh, ớt.',
    preparation  = N'Sườn chần sơ rồi kho trong tộ đất với nước màu dừa, lửa liu riu cho thịt mềm và nước kho sánh lại bám quanh miếng sườn.',
    order_note   = N'Kho tộ cần 40 phút, đặt sát giờ sẽ phải chờ. Món mặn đậm, hợp ăn với cơm trắng.'
  WHERE name = N'Sườn non kho tộ';

UPDATE dishes SET
    slug         = 'com-chay-cha-bong-kho-quet',
    portion_desc = N'Phần 3-4 người',
    prep_minutes = 20,
    ingredients  = N'Cơm cháy chiên giòn, chà bông, tóp mỡ, tôm khô, nước mắm kho quẹt, rau củ luộc.',
    preparation  = N'Cơm cháy chiên vàng giòn. Kho quẹt nấu từ nước mắm, đường và tóp mỡ đến khi sánh đặc, dọn trong tộ nóng kèm rau củ luộc chấm.',
    order_note   = N'Món ăn chơi, thường gọi kèm chứ không đủ no cho bữa chính. Kho quẹt khá mặn, có thể làm nhạt hơn nếu báo trước.'
  WHERE name = N'Cơm cháy chà bông kho quẹt';

UPDATE dishes SET
    slug         = 'ca-keo-kho-rau-ram',
    portion_desc = N'Phần 3-4 người',
    prep_minutes = 30,
    ingredients  = N'Cá kèo tươi, rau răm, nước màu, ớt, tiêu.',
    preparation  = N'Cá kèo để nguyên con làm sạch nhớt, kho lửa nhỏ với nước màu và rau răm cho thấm, không đảo mạnh để cá không nát.',
    order_note   = N'Cá kèo có vị đắng nhẹ ở phần ruột, đó là đặc trưng món này. Ai không quen thì nên chọn món khác.'
  WHERE name = N'Cá kèo kho rau răm';

UPDATE dishes SET
    slug         = 'lau-mam-mien-tay',
    portion_desc = N'Nồi 4-6 người',
    prep_minutes = 45,
    ingredients  = N'Mắm cá linh, mắm cá sặc, cá basa, tôm, mực, thịt quay, cà tím, và mâm rau đồng hơn 10 loại.',
    preparation  = N'Mắm nấu rã lọc lấy nước trong, nêm cùng sả và nước dừa. Nước lẩu đun sôi mới thả hải sản và rau theo thứ tự để mỗi thứ chín vừa tới.',
    order_note   = N'Mùi mắm rất đậm, khách chưa quen nên cân nhắc. Nồi nhỏ nhất phục vụ 4 người, không tách phần lẻ được.'
  WHERE name = N'Lẩu mắm miền Tây';

UPDATE dishes SET
    slug         = 'lau-ca-keo-la-giang',
    portion_desc = N'Nồi 4-6 người',
    prep_minutes = 35,
    ingredients  = N'Cá kèo tươi, lá giang, măng chua, cà chua, rau muống, bún.',
    preparation  = N'Lá giang vò nhẹ cho ra chất chua tự nhiên, nấu cùng nước dùng xương. Cá kèo thả vào khi nước đang sôi mạnh để cá không tanh.',
    order_note   = N'Vị chua đến từ lá giang chứ không dùng me hay giấm. Cá kèo còn nguyên con, có xương nhỏ.'
  WHERE name = N'Lẩu cá kèo lá giang';

UPDATE dishes SET
    slug         = 'lau-ga-la-e',
    portion_desc = N'Nồi 4-6 người',
    prep_minutes = 40,
    ingredients  = N'Gà ta, lá é, măng tươi, ớt xiêm xanh, nấm.',
    preparation  = N'Gà chặt miếng nấu với măng tươi, nêm ớt xiêm xanh giã. Lá é chỉ thả vào lúc gần ăn để giữ mùi thơm đặc trưng.',
    order_note   = N'Món cay theo kiểu Phú Yên, độ cay điều chỉnh được nếu báo trước. Lá é theo mùa, hết mùa bên mình sẽ báo trước khi nhận đặt.'
  WHERE name = N'Lẩu gà lá é';

UPDATE dishes SET
    slug         = 'combo-nuong-than-hoa',
    portion_desc = N'Phần 4-6 người',
    prep_minutes = 30,
    ingredients  = N'Ba chỉ bò, sườn heo, tôm sú, mực lá, nấm, rau củ, các loại nước chấm.',
    preparation  = N'Nguyên liệu ướp sẵn theo từng loại, khách tự nướng trên bếp than hoa đặt giữa bàn. Nhân viên hỗ trợ thay than và trở đồ nếu cần.',
    order_note   = N'Bếp than đặt tại bàn nên không bố trí được ở phòng máy lạnh kín và khu vực có trẻ nhỏ chạy nhảy.'
  WHERE name = N'Combo nướng than hoa';

UPDATE dishes SET
    slug         = 'heo-quay-gion-bi',
    portion_desc = N'Theo cân, tối thiểu 1kg',
    prep_minutes = 180,
    ingredients  = N'Heo sữa, ngũ vị hương, giấm, muối, bánh hỏi và rau sống ăn kèm.',
    preparation  = N'Da heo phơi khô rồi quay lò than trở đều nhiều giờ cho bì phồng giòn đều, thịt bên trong vẫn giữ được nước.',
    order_note   = N'Quay mất khoảng 3 tiếng nên bắt buộc đặt trước ít nhất một ngày. Tính tiền theo cân thực tế sau khi quay, có thể lệch so với ước tính ban đầu.'
  WHERE name = N'Heo quay giòn bì';

UPDATE dishes SET
    slug         = 'de-nuong-ngu-vi',
    portion_desc = N'Phần 3-4 người',
    prep_minutes = 30,
    ingredients  = N'Thịt dê, ngũ vị hương, sả, riềng, chao, rau răm, chuối chát.',
    preparation  = N'Thịt dê khử mùi bằng rượu gừng, ướp ngũ vị và riềng sả rồi nướng than. Dọn kèm chao pha và rau thơm để cân vị.',
    order_note   = N'Thịt dê có mùi đặc trưng dù đã khử. Khách chưa ăn dê bao giờ nên gọi phần nhỏ thử trước.'
  WHERE name = N'Dê nướng ngũ vị';

UPDATE dishes SET
    slug         = 'che-buoi-can-tho',
    portion_desc = N'Phần 2 chén',
    prep_minutes = 15,
    ingredients  = N'Cùi bưởi, đậu xanh cà vỏ, nước cốt dừa, bột năng, đường phèn.',
    preparation  = N'Cùi bưởi bóp muối nhiều lần cho hết the, luộc rồi áo bột năng để có độ giòn sần sật. Nấu cùng đậu xanh, chan nước cốt dừa khi dọn.',
    order_note   = N'Chè ngọt vừa, có thể giảm đường nếu báo trước. Dọn lạnh ngon hơn dọn nóng.'
  WHERE name = N'Chè bưởi Cần Thơ';

UPDATE dishes SET
    slug         = 'rau-cau-dua-la-dua',
    portion_desc = N'Phần 4 miếng',
    prep_minutes = 10,
    ingredients  = N'Nước dừa tươi, lá dứa, bột rau câu, nước cốt dừa, đường.',
    preparation  = N'Đổ hai lớp: lớp lá dứa và lớp nước cốt dừa, chờ lớp dưới se mặt mới đổ lớp trên để hai màu không lẫn vào nhau.',
    order_note   = N'Món để lạnh, cần báo trước nửa ngày cho rau câu đông đủ độ.'
  WHERE name = N'Rau câu dừa lá dứa';

UPDATE dishes SET
    slug         = 'banh-da-lon-hap-la-dua',
    portion_desc = N'Phần 4 miếng',
    prep_minutes = 15,
    ingredients  = N'Bột năng, bột gạo, lá dứa, đậu xanh, nước cốt dừa.',
    preparation  = N'Hấp từng lớp mỏng xen kẽ lá dứa và đậu xanh, mỗi lớp chờ chín mới đổ lớp kế để bánh tách lớp rõ ràng.',
    order_note   = N'Bánh mềm dẻo, ăn trong ngày là ngon nhất, để tủ lạnh sẽ cứng lại.'
  WHERE name = N'Bánh da lợn hấp lá dứa';

UPDATE dishes SET
    slug         = 'trai-cay-theo-mua',
    portion_desc = N'Đĩa 4-6 người',
    prep_minutes = 10,
    ingredients  = N'Trái cây miền Tây theo mùa: chôm chôm, măng cụt, xoài cát, dưa hấu, thanh long.',
    preparation  = N'Chọn trái chín tới trong ngày, gọt và bày đĩa ngay trước khi dọn để không bị thâm và mất nước.',
    order_note   = N'Loại trái thay đổi theo mùa, bên mình sẽ báo trước loại có sẵn khi khách đặt.'
  WHERE name = N'Trái cây theo mùa';

UPDATE dishes SET
    slug         = 'nuoc-sam-la-dua-nha-nau',
    portion_desc = N'Bình 2 lít',
    prep_minutes = 20,
    ingredients  = N'Mía lau, rễ tranh, râu bắp, lá dứa, đường phèn.',
    preparation  = N'Các loại rễ và lá nấu lửa nhỏ khoảng 1 tiếng, lọc trong rồi nêm đường phèn. Để nguội hẳn mới cho vào bình mát.',
    order_note   = N'Nấu mỗi ngày một mẻ, hết là hết. Đặt bình lớn nên báo trước.'
  WHERE name = N'Nước sâm lá dứa nhà nấu';

UPDATE dishes SET
    slug         = 'dua-tuoi-ben-tre',
    portion_desc = N'Trái nguyên',
    prep_minutes = 5,
    ingredients  = N'Dừa xiêm Bến Tre.',
    preparation  = N'Dừa chặt tại chỗ khi khách gọi, giữ nguyên trái để nước không mất mùi.',
    order_note   = N'Nước dừa để lâu ngoài không khí sẽ chua, nên bên mình chỉ chặt khi khách gọi.'
  WHERE name = N'Dừa tươi Bến Tre';

UPDATE dishes SET
    slug         = 'ruou-nep-than-u-12-thang',
    portion_desc = N'Bình 500ml',
    prep_minutes = 5,
    ingredients  = N'Nếp than, men truyền thống, ủ 12 tháng.',
    preparation  = N'Nếp than nấu chín trộn men, ủ kín 12 tháng trong chum sành cho rượu lên màu tím sẫm và vị dịu.',
    order_note   = N'Đồ uống có cồn, không phục vụ người dưới 18 tuổi và người lái xe. Số lượng có hạn theo mẻ ủ.'
  WHERE name = N'Rượu nếp than ủ 12 tháng';

UPDATE dishes SET
    slug         = 'bia-nuoc-ngot',
    portion_desc = N'Lon/chai',
    prep_minutes = 2,
    ingredients  = N'Các loại bia lon, nước ngọt, nước suối.',
    preparation  = N'Ướp lạnh sẵn, dọn theo yêu cầu của bàn.',
    order_note   = N'Tính theo số lon thực uống. Bia là đồ uống có cồn, không phục vụ người dưới 18 tuổi.'
  WHERE name = N'Bia & nước ngọt';

-- Slug dung lam duong dan nen phai duy nhat. Khong dat NOT NULL o day vi
-- cu phap doi cot khac nhau giua MySQL va H2, ma rang buoc duy nhat da du chac.
CREATE UNIQUE INDEX uk_dish_slug ON dishes (slug);

-- Thu vien anh cua mon, cau truc giong space_images cho de doc
CREATE TABLE dish_images (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    dish_id    BIGINT       NOT NULL,
    url        VARCHAR(500) NOT NULL,
    caption    VARCHAR(255),
    sort_order INT          NOT NULL DEFAULT 0,
    CONSTRAINT fk_dishimg_dish FOREIGN KEY (dish_id) REFERENCES dishes (id) ON DELETE CASCADE
);

CREATE INDEX idx_dishimg_dish ON dish_images (dish_id);
