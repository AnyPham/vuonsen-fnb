-- Bản tiếng Anh cho phần nội dung còn lại: món ăn, danh mục món, gói tiệc, thư viện ảnh.
--
-- Cùng quy tắc với V20: mỗi cột một câu ALTER riêng, không dùng mệnh đề AFTER, để hồ sơ
-- test chạy trên H2 cũng nạp được. Cột để trống thì giao diện tự lùi về bản tiếng Việt.

ALTER TABLE dishes ADD COLUMN name_en        VARCHAR(150) NULL;
ALTER TABLE dishes ADD COLUMN description_en VARCHAR(500) NULL;
ALTER TABLE dishes ADD COLUMN price_note_en  VARCHAR(150) NULL;
ALTER TABLE dishes ADD COLUMN portion_desc_en VARCHAR(150) NULL;
ALTER TABLE dishes ADD COLUMN order_note_en  TEXT NULL;
ALTER TABLE dishes ADD COLUMN ingredients_en TEXT NULL;
ALTER TABLE dishes ADD COLUMN preparation_en TEXT NULL;

ALTER TABLE dish_categories ADD COLUMN name_en VARCHAR(100) NULL;

ALTER TABLE party_packages ADD COLUMN name_en    VARCHAR(150) NULL;
ALTER TABLE party_packages ADD COLUMN tagline_en VARCHAR(255) NULL;

ALTER TABLE gallery_images ADD COLUMN caption_en VARCHAR(255) NULL;

-- ---------- Danh mục món ----------
UPDATE dish_categories SET name_en = 'Starters'   WHERE code = 'khaivi';
UPDATE dish_categories SET name_en = 'Mains'      WHERE code = 'chinh';
UPDATE dish_categories SET name_en = 'Hotpot & grill' WHERE code = 'lau';
UPDATE dish_categories SET name_en = 'Desserts'   WHERE code = 'trangmieng';
UPDATE dish_categories SET name_en = 'Drinks'     WHERE code = 'douong';

-- ---------- Gói tiệc ----------
UPDATE party_packages SET
    name_en = 'Countryside Package',
    tagline_en = 'Family gatherings and informal parties'
WHERE code = 'DONG-QUE';

UPDATE party_packages SET
    name_en = 'Golden Lotus Package',
    tagline_en = 'Weddings, birthdays and year-end parties'
WHERE code = 'SEN-VANG';

UPDATE party_packages SET
    name_en = 'Royal Garden Package',
    tagline_en = 'Premium weddings and corporate galas'
WHERE code = 'THUONG-UYEN';

-- ---------- Món ăn: khai vị ----------
UPDATE dishes SET
    name_en = 'Coconut heart salad with prawn and pork',
    description_en = 'Ben Tre coconut heart, tiger prawn, prawn crackers',
    portion_desc_en = 'Serves 3-4',
    ingredients_en = 'Ben Tre coconut heart, tiger prawn, pork belly, onion, Vietnamese coriander, roasted peanuts, sweet-and-sour fish sauce.',
    preparation_en = 'The coconut heart is shaved thin and chilled in iced water so it stays crisp. Prawns are boiled and peeled, pork belly boiled and thinly sliced. Everything is tossed with sweet-and-sour fish sauce only just before serving, so the salad does not weep.',
    order_note_en = 'Dressed thirty minutes early the salad turns soft and watery, so we only toss it once you are seated. Contains peanuts — please tell us if anyone has an allergy.'
WHERE slug = 'goi-cu-hu-dua-tom-thit';

UPDATE dishes SET
    name_en = 'Seafood net spring rolls',
    description_en = 'Hand-rolled, deep-fried crisp, served with garden herbs',
    portion_desc_en = '10 rolls',
    ingredients_en = 'Rice net wrappers, prawn, squid, crab meat, wood-ear mushroom, glass noodles, garden herbs, sweet-and-sour dipping sauce.',
    preparation_en = 'The filling is rolled by hand in rice net wrappers, then fried twice: once at low heat to cook through, once hot to crisp the shell just before serving.',
    order_note_en = 'Contains seafood. For more than twenty portions please order a day ahead, as every roll is made by hand.'
WHERE slug = 'cha-gio-re-hai-san';

UPDATE dishes SET
    name_en = 'Mekong-style sizzling crepe',
    description_en = 'Cooked at your table, served with twelve wild herbs',
    portion_desc_en = '2 large crepes',
    ingredients_en = 'Rice batter with turmeric and coconut milk, pork belly, prawn, mung bean, bean sprouts, twelve kinds of wild herbs.',
    preparation_en = 'The batter goes into a very hot pan so the edges turn lacy and crisp. We cook it at your table and serve each crepe the moment it leaves the pan.',
    order_note_en = 'The crepe has to be eaten straight off the pan, so we cook them in rounds rather than in advance. Larger tables are served in several waves.'
WHERE slug = 'banh-xeo-mien-tay';

UPDATE dishes SET
    name_en = 'Beef in betel leaf, charcoal grilled',
    description_en = 'Cu Chi young beef, house-made fermented anchovy sauce',
    portion_desc_en = '10 rolls',
    ingredients_en = 'Cu Chi young beef, wild betel leaf, lemongrass, garlic, roasted peanuts, house-made fermented anchovy sauce.',
    preparation_en = 'Minced beef seasoned with lemongrass and garlic is wrapped in betel leaf and grilled over charcoal until the leaf chars and releases its scent.',
    order_note_en = 'Charcoal grilling produces smoke, so this dish cannot be served in a sealed air-conditioned room. We grill it in the outdoor kitchen and bring it in.'
WHERE slug = 'bo-la-lot-nuong-than';

UPDATE dishes SET
    name_en = 'Crab soup with century egg',
    description_en = 'Built on a six-hour chicken stock',
    portion_desc_en = '4 bowls',
    ingredients_en = 'Fresh crab meat, century egg, quail egg, straw mushroom, sweetcorn, six-hour chicken stock, coriander.',
    preparation_en = 'The chicken stock simmers for six hours and is skimmed clear. Crab meat is folded in at the end so it keeps its texture rather than breaking into threads.',
    order_note_en = 'Contains century egg, which children may find unfamiliar. We can prepare portions without it — please say so when ordering.'
WHERE slug = 'sup-cua-trung-bac-thao';

UPDATE dishes SET
    name_en = 'Cabbage salad with dried shrimp',
    description_en = 'A homely dish, lightly sweet and sour',
    portion_desc_en = 'Serves 3-4',
    ingredients_en = 'Cabbage, dried shrimp, carrot, Vietnamese coriander, roasted peanuts, sweet-and-sour fish sauce.',
    preparation_en = 'Cabbage is shredded and salted briefly to draw out water, then squeezed dry and tossed with dried shrimp and dressing so it stays crunchy.',
    order_note_en = 'A tossed salad, so it is served immediately. Contains dried shrimp — guests with a shellfish allergy should tell us and we will swap the dish.'
WHERE slug = 'nham-bap-cai-tom-kho';

-- ---------- Món ăn: món chính ----------
UPDATE dishes SET
    name_en = 'Straw-grilled snakehead fish with rice paper',
    description_en = 'A 1.2kg wild snakehead, grilled on straw at your table',
    portion_desc_en = 'Serves 4-6',
    ingredients_en = 'Wild snakehead fish about 1.2kg, rice straw, rice paper, vermicelli, garden herbs, scallion oil, roasted peanuts, tamarind dipping sauce.',
    preparation_en = 'The whole fish is skewered on a bamboo stick and buried in burning rice straw. The skin chars away completely, leaving the flesh underneath steamed in its own moisture and scented with straw smoke.',
    order_note_en = 'Straw grilling takes about forty minutes, so please book at least one session ahead. The fish keeps its fine bones — take care with young children.'
WHERE slug = 'ca-loc-nuong-trui-cuon-banh-trang';

UPDATE dishes SET
    name_en = 'Steamed free-range chicken with lime leaf',
    description_en = 'Whole free-range bird, salt-pepper-lime dip',
    portion_desc_en = 'Whole bird, serves 4-6',
    ingredients_en = 'Free-range chicken, kaffir lime leaf, ginger, salt, green pepper, lime.',
    preparation_en = 'The whole bird is steamed with lime leaf and ginger for forty-five minutes, rested, then chopped to order so the meat does not dry out.',
    order_note_en = 'Free-range chicken is firmer than farmed chicken — that is the point of the dish, not a sign that it is undercooked. Steaming takes forty-five minutes, so please order ahead.'
WHERE slug = 'ga-ta-hap-la-chanh';

UPDATE dishes SET
    name_en = 'Grilled river prawns with chilli salt',
    description_en = 'Giant river prawns, four to a kilo',
    portion_desc_en = '4 large prawns',
    ingredients_en = 'Giant freshwater prawns (four per kilo), chilli salt, spring onion, lime.',
    preparation_en = 'The prawns are split along the back, brushed with chilli salt and grilled over charcoal shell-side down so the roe in the head sets rather than running out.',
    order_note_en = 'Mildly spicy, and we can make it without chilli if you tell us in advance. The price follows the day market rate for live prawns, so it may differ from the listed figure.'
WHERE slug = 'tom-cang-nuong-muoi-ot';

UPDATE dishes SET
    name_en = 'Pork ribs braised in a clay pot',
    description_en = 'Braised in young coconut water, served with crisp rice',
    portion_desc_en = 'Serves 3-4',
    ingredients_en = 'Pork spare ribs, young coconut water, caramel sauce, shallot, garlic, black pepper, spring onion.',
    preparation_en = 'The ribs are seared in caramel, then braised in coconut water in a clay pot over low heat for forty minutes until the sauce reduces and coats the meat.',
    order_note_en = 'Clay-pot braising needs forty minutes, so ordering at the last minute means waiting. The seasoning is deliberately salty and is meant to be eaten with plain rice.'
WHERE slug = 'suon-non-kho-to';

UPDATE dishes SET
    name_en = 'Crisp rice with pork floss and kho quet dip',
    description_en = 'Served with seasonal boiled vegetables',
    portion_desc_en = 'Serves 3-4',
    ingredients_en = 'Crisp rice crackers, pork floss, dried shrimp, pork belly, fish sauce, black pepper, seasonal boiled vegetables.',
    preparation_en = 'Rice is pressed into sheets and fried until it puffs and crackles. The kho quet dip is reduced in a clay pot until thick enough to cling to a vegetable stem.',
    order_note_en = 'This is a snack rather than a main course and is usually ordered alongside other dishes. The dip is quite salty; we can make it milder on request.'
WHERE slug = 'com-chay-cha-bong-kho-quet';

UPDATE dishes SET
    name_en = 'Goby fish braised with Vietnamese coriander',
    description_en = 'Fresh goby fish braised with green peppercorns',
    portion_desc_en = 'Serves 3-4',
    ingredients_en = 'Fresh goby fish, green peppercorns, Vietnamese coriander, caramel sauce, chilli, spring onion.',
    preparation_en = 'The fish go into the pot live so they stay whole, and are braised with caramel and green peppercorns until the sauce thickens around them.',
    order_note_en = 'Goby fish carry a faint bitterness in the gut, which is characteristic of the dish. If that is not to your taste, choose something else.'
WHERE slug = 'ca-keo-kho-rau-ram';

UPDATE dishes SET
    name_en = 'Crispy-skin roast suckling pig',
    description_en = 'Whole pig roasted in a clay oven, order 24 hours ahead',
    price_note_en = 'By weight',
    portion_desc_en = 'By weight, one kilo minimum',
    ingredients_en = 'Whole suckling pig, five-spice, fermented bean curd, honey, rice vinegar, steamed buns, pickled vegetables.',
    preparation_en = 'The skin is scalded and air-dried overnight, then the pig roasts for about three hours in a clay oven so the skin blisters into glass while the meat stays moist.',
    order_note_en = 'Roasting takes about three hours, so this must be ordered at least one day in advance. It is billed by actual weight after roasting, which may differ from the first estimate.'
WHERE slug = 'heo-quay-gion-bi';

-- ---------- Món ăn: lẩu và nướng ----------
UPDATE dishes SET
    name_en = 'Mekong fermented fish hotpot',
    description_en = 'Linh fish sauce base with fifteen field vegetables',
    portion_desc_en = 'Pot for 4-6',
    ingredients_en = 'Fermented linh fish, pork belly, prawn, squid, aubergine, lemongrass, chilli, fifteen kinds of field vegetables, fresh rice noodles.',
    preparation_en = 'The fermented fish is simmered and strained so only the broth remains, then built up with lemongrass and pork bones. Vegetables go in at the table, a handful at a time.',
    order_note_en = 'The fermented fish aroma is very strong — guests who have not tried it before should think twice. The smallest pot serves four and cannot be split further.'
WHERE slug = 'lau-mam-mien-tay';

UPDATE dishes SET
    name_en = 'Goby fish hotpot with giang leaf',
    description_en = 'Clean sourness, served with fresh rice noodles',
    portion_desc_en = 'Pot for 4-6',
    ingredients_en = 'Fresh goby fish, giang leaf, okra, bean sprouts, water spinach, chilli, fresh rice noodles.',
    preparation_en = 'The sourness comes from crushed giang leaf simmered in the broth. The fish are added whole at the table so they do not overcook.',
    order_note_en = 'The sourness comes from giang leaf, not from tamarind or vinegar. The goby fish are served whole and contain small bones.'
WHERE slug = 'lau-ca-keo-la-giang';

UPDATE dishes SET
    name_en = 'Chicken hotpot with e leaf',
    description_en = 'Free-range chicken, fresh bamboo shoot, Phu Yen e leaf',
    portion_desc_en = 'Pot for 4-6',
    ingredients_en = 'Free-range chicken, fresh bamboo shoot, Phu Yen e leaf, bird chilli, ginger, fresh rice noodles.',
    preparation_en = 'The chicken is simmered with bamboo shoot until the broth turns sweet, and the e leaf is added only at the table so its scent does not cook away.',
    order_note_en = 'Spicy in the Phu Yen style; the heat can be adjusted if you tell us in advance. E leaf is seasonal — when it is out of season we will say so before taking the booking.'
WHERE slug = 'lau-ga-la-e';

UPDATE dishes SET
    name_en = 'Charcoal grill platter',
    description_en = 'Beef, pork and seafood — six items, for four people',
    portion_desc_en = 'Serves 4-6',
    ingredients_en = 'Beef short rib, pork neck, prawn, squid, okra, king oyster mushroom, chilli salt and lime dip.',
    preparation_en = 'Everything is marinated in the kitchen and grilled by you at the table, so each item comes off the charcoal at the moment you want it.',
    order_note_en = 'The charcoal burner sits on the table, so this cannot be set up in a sealed air-conditioned room or where small children are running about.'
WHERE slug = 'combo-nuong-than-hoa';

UPDATE dishes SET
    name_en = 'Five-spice grilled goat',
    description_en = 'Served with baguette and fermented bean curd',
    portion_desc_en = 'Serves 3-4',
    ingredients_en = 'Goat leg, five-spice, lemongrass, fermented bean curd, baguette, cucumber, garden herbs.',
    preparation_en = 'The goat is marinated overnight with five-spice and lemongrass to temper its gaminess, then grilled over charcoal and sliced across the grain.',
    order_note_en = 'Goat keeps a distinctive aroma even after the marinade. If you have never eaten goat, order a small portion first.'
WHERE slug = 'de-nuong-ngu-vi';

-- ---------- Món ăn: tráng miệng ----------
UPDATE dishes SET
    name_en = 'Can Tho pomelo sweet soup',
    description_en = 'Cooked fresh each day, no artificial colouring',
    portion_desc_en = '2 bowls',
    ingredients_en = 'Pomelo pith, mung bean, tapioca starch, coconut milk, pandan leaf, palm sugar.',
    preparation_en = 'The pomelo pith is washed repeatedly in salted water to draw out the bitterness, then coated in tapioca starch so it stays springy in the syrup.',
    order_note_en = 'Moderately sweet, and we can cut the sugar if you tell us beforehand. Better served cold than warm.'
WHERE slug = 'che-buoi-can-tho';

UPDATE dishes SET
    name_en = 'Pandan coconut jelly',
    description_en = 'Set inside a fresh coconut',
    portion_desc_en = '4 pieces',
    ingredients_en = 'Fresh coconut, coconut milk, pandan leaf, agar, palm sugar.',
    preparation_en = 'The jelly is poured straight into the hollowed coconut in two layers and chilled until set, so it takes on the shape and scent of the fruit.',
    order_note_en = 'Served chilled and needs half a day''s notice so the jelly sets properly.'
WHERE slug = 'rau-cau-dua-la-dua';

UPDATE dishes SET
    name_en = 'Steamed pandan layer cake',
    description_en = 'Made to a family recipe',
    portion_desc_en = '4 pieces',
    ingredients_en = 'Tapioca starch, rice flour, mung bean, coconut milk, pandan leaf, sugar.',
    preparation_en = 'Nine alternating layers are steamed one at a time, each set before the next is poured, which is why the cake takes close to two hours.',
    order_note_en = 'The cake is soft and chewy and is at its best the same day; refrigerating it makes it firm.'
WHERE slug = 'banh-da-lon-hap-la-dua';

UPDATE dishes SET
    name_en = 'Seasonal fruit platter',
    description_en = 'Large platter for a table of ten',
    portion_desc_en = 'Platter for 4-6',
    ingredients_en = 'Whatever is in season: pomelo, dragon fruit, watermelon, mango, rambutan, longan.',
    preparation_en = 'Fruit is cut to order rather than in advance, so the cut surfaces do not dry out or darken.',
    order_note_en = 'The fruit changes with the season; we will tell you what is available when you order.'
WHERE slug = 'trai-cay-theo-mua';

-- ---------- Món ăn: đồ uống ----------
UPDATE dishes SET
    name_en = 'House-brewed pandan herbal tea',
    description_en = '1.5 litre jug',
    portion_desc_en = '2 litre jug',
    ingredients_en = 'Pandan leaf, sugarcane, dried longan, monk fruit, rock sugar.',
    preparation_en = 'Brewed each morning in one batch, cooled naturally and chilled without ice so it does not turn watery.',
    order_note_en = 'One batch is brewed each day and when it runs out it is gone. Large jugs should be ordered in advance.'
WHERE slug = 'nuoc-sam-la-dua-nha-nau';

UPDATE dishes SET
    name_en = 'Ben Tre fresh coconut',
    description_en = 'Chilled and served in the whole fruit',
    portion_desc_en = 'Whole fruit',
    ingredients_en = 'Fresh Ben Tre coconut.',
    preparation_en = 'Chilled whole and opened only when ordered.',
    order_note_en = 'Coconut water sours once it is exposed to air, so we only open the fruit when you order it.'
WHERE slug = 'dua-tuoi-ben-tre';

UPDATE dishes SET
    name_en = 'Black glutinous rice wine, aged 12 months',
    description_en = '500ml bottle',
    portion_desc_en = '500ml bottle',
    ingredients_en = 'Black glutinous rice, yeast starter, aged twelve months.',
    preparation_en = 'Fermented from black glutinous rice and left to age for twelve months in earthenware.',
    order_note_en = 'Contains alcohol; not served to anyone under eighteen or to drivers. Quantities are limited by batch.'
WHERE slug = 'ruou-nep-than-u-12-thang';

UPDATE dishes SET
    name_en = 'Beer and soft drinks',
    description_en = 'Common brands, charged by the can actually opened',
    portion_desc_en = 'Per can or bottle',
    ingredients_en = 'Common Vietnamese and imported beer brands, carbonated soft drinks, mineral water.',
    preparation_en = 'Served chilled from the cabinet; unopened cans are not charged.',
    order_note_en = 'Charged by the number of cans actually drunk. Beer contains alcohol and is not served to anyone under eighteen.'
WHERE slug = 'bia-nuoc-ngot';

-- ---------- Chú thích ảnh thư viện ----------
UPDATE gallery_images SET caption_en = CASE caption
    WHEN N'Tiệc cưới ven sông'          THEN 'Riverside wedding'
    WHEN N'Sảnh tiệc trong nhà'         THEN 'Indoor banquet hall'
    WHEN N'Không gian nhà rường'        THEN 'Traditional timber house'
    WHEN N'Chòi lá trên mặt nước'       THEN 'Thatched huts over the water'
    WHEN N'Bàn tiệc bày sẵn'            THEN 'Table set for service'
    WHEN N'Vườn cau buổi chiều'         THEN 'Areca palm garden in the afternoon'
    ELSE caption_en END;
