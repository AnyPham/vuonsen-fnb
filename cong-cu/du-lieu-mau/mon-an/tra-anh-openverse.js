/*
 * Tra ảnh cho 100 món bằng Openverse.
 *
 * Vì sao đổi nguồn: ba vòng tra trên Wikimedia Commons chỉ cho ra khoảng một nửa số món
 * có ảnh đúng chủ đề. Commons là kho tư liệu bách khoa, ảnh món ăn thường ngày rất mỏng,
 * nên tra "bánh khọt" hay "nước mía" thường không có gì, còn nới từ khóa ra thì lại nhận
 * về tranh vẽ, con tem hay ảnh nhà hàng.
 *
 * Openverse gom ảnh giấy phép mở từ nhiều nguồn, trong đó có Flickr — nơi có rất nhiều
 * ảnh món ăn thật do người đi ăn chụp. Tra thử "banh khot" ra 41 kết quả đúng món, trong
 * khi Commons không có kết quả nào dùng được. API không cần khóa nên vẫn không tốn phí.
 *
 * Giấy phép: chỉ nhận cc0, pdm, by và by-sa. Cố ý loại nhóm nc (phi thương mại) vì website
 * nhà hàng là mục đích thương mại, dùng ảnh nc vào đó là sai giấy phép.
 *
 * Chạy: node cong-cu/du-lieu-mau/mon-an/tra-anh-openverse.js
 */
const fs = require('fs');
const path = require('path');
const https = require('https');

const MON = [
  ...require('./khai-vi.js'),
  ...require('./mon-chinh-1.js'),
  ...require('./mon-chinh-2.js'),
  ...require('./lau-nuong.js'),
  ...require('./trang-mieng-do-uong.js'),
];

// Từ khóa tra, viết tay cho từng món để tả đúng thứ trên đĩa
const TU_KHOA = {
  'goi-cuon-tom-thit': 'goi cuon vietnamese spring rolls',
  'banh-khot-vung-tau': 'banh khot',
  'nem-nuong-nha-trang': 'nem nuong grilled pork',
  'banh-beo-chen-hue': 'banh beo hue',
  'banh-bot-loc-la-chuoi': 'banh bot loc',
  'sup-mang-cua-ga-xe': 'crab asparagus soup',
  'goi-ngo-sen-tom-thit': 'lotus stem salad shrimp',
  'cha-ca-la-vong': 'cha ca la vong turmeric fish',
  'hoanh-thanh-chien-gion': 'fried wonton crispy',
  'goi-xoai-kho-ca-sac': 'green mango salad dried fish',
  'chao-tom-boc-mia': 'chao tom shrimp sugarcane',
  'banh-uot-thit-nuong': 'banh uot thit nuong',
  'tai-heo-cuon-ngu-sac': 'pig ear salad vietnamese',
  'muc-chien-nuoc-mam': 'fried squid fish sauce',
  'so-diep-nuong-mo-hanh': 'grilled scallops scallion',
  'ca-tim-nuong-mo-hanh': 'grilled eggplant scallion oil',
  'dau-hu-chien-sa-ot': 'fried tofu lemongrass chilli',
  'nom-hoa-chuoi': 'banana flower salad',
  'ngheu-hap-sa': 'steamed clams lemongrass',
  'khoai-lang-ken-chien': 'fried sweet potato balls',

  'ca-tai-tuong-chien-xu': 'crispy fried whole fish vietnamese',
  'ca-kho-to-mien-tay': 'ca kho to braised fish clay pot',
  'canh-chua-ca-loc': 'canh chua vietnamese sour soup',
  'thit-kho-trung-nuoc-dua': 'thit kho braised pork eggs',
  'suon-ram-man': 'caramelized pork ribs',
  'ga-kho-gung': 'braised chicken ginger vietnamese',
  'vit-quay-la-mac-mat': 'roast duck crispy skin',
  'bo-luc-lac-khoai-tay': 'bo luc lac shaking beef',
  'com-chien-hai-san-trai-thom': 'pineapple fried rice',
  'com-nieu-dap-nieu': 'clay pot rice com nieu',
  'bun-thit-nuong-cha-gio': 'bun thit nuong',
  'mi-quang-ga': 'mi quang',
  'hu-tieu-nam-vang': 'hu tieu nam vang',
  'banh-canh-cua': 'banh canh cua',
  'bo-kho-banh-mi': 'bo kho beef stew vietnamese',

  'tom-su-hap-nuoc-dua': 'steamed tiger prawns',
  'cua-rang-me': 'tamarind crab',
  'ca-hap-hong-kong': 'steamed fish ginger scallion',
  'muc-nhoi-thit-hap': 'stuffed squid steamed',
  'ech-xao-lan': 'frog legs coconut curry',
  'luon-um-nuoc-dua': 'eel coconut milk dish',
  'ga-nuong-muoi-ot': 'grilled chicken chilli salt',
  'ga-hap-la-sen': 'chicken steamed lotus leaf',
  'bo-nuong-la-cach': 'grilled beef betel leaf',
  'heo-rung-xao-sa-ot': 'stir fried pork lemongrass chilli',
  'rau-muong-xao-toi': 'stir fried water spinach garlic',
  'dau-hu-sot-ca-chua': 'tofu tomato sauce vietnamese',
  'nam-kho-tieu-chay': 'braised mushrooms clay pot',
  'canh-kho-qua-nhoi-thit': 'stuffed bitter melon soup',
  'chim-cut-roti': 'roasted quail',

  'lau-thai-hai-san': 'tom yum seafood hot pot',
  'lau-bo-nhung-dam': 'beef hot pot vietnamese',
  'lau-ca-bop-la-giang': 'fish hot pot vietnamese',
  'lau-riu-rieu-cua-dong': 'bun rieu crab',
  'lau-de-tiem-thuoc-bac': 'goat hot pot herbal',
  'lau-nam-chay': 'mushroom hot pot vegetarian',
  'lau-hai-san-chua-cay': 'seafood hot pot spicy',
  'lau-vit-nau-chao': 'duck hot pot',
  'lau-ca-hoi-mang-chua': 'salmon head hot pot',
  'lau-ech-mang-cay': 'frog hot pot spicy',
  'suon-nuong-bbq': 'bbq pork ribs honey glazed',
  'ba-chi-nuong-sa': 'grilled pork belly lemongrass',
  'muc-nuong-sa-te': 'grilled squid satay',
  'ca-nuc-nuong-giay-bac': 'grilled mackerel foil',
  'ga-nuong-lu': 'roasted chicken clay oven',
  'bo-bit-tet-tieu-den': 'beef steak black pepper sauce',
  'tom-hum-nuong-pho-mai': 'grilled lobster cheese',
  'hau-nuong-mo-hanh': 'grilled oysters scallion',
  'nam-nuong-sa-te': 'grilled mushroom skewers',
  'combo-lau-nuong-gia-dinh': 'korean bbq table grill',

  'che-ba-mau': 'che ba mau three colour dessert',
  'che-troi-nuoc': 'che troi nuoc glutinous rice balls',
  'che-hat-sen-nhan-nhuc': 'lotus seed longan dessert',
  'che-khoai-deo': 'taro balls dessert',
  'suong-sa-hat-luu': 'tub tim krob water chestnut dessert',
  'banh-flan-ca-phe': 'vietnamese flan coffee',
  'banh-chuoi-hap-nuoc-cot-dua': 'steamed banana cake coconut',
  'kem-dua-trai-dua': 'coconut ice cream in shell',
  'sinh-to-bo-kem': 'avocado smoothie vietnamese',
  'tau-hu-nuoc-duong': 'tofu pudding ginger syrup',
  'xoi-xoai-nuoc-cot-dua': 'mango sticky rice',
  'banh-tet-la-cam': 'banh tet vietnamese rice cake',
  'che-thai-sau-rieng': 'durian dessert che thai',
  'banh-cam-nhan-dau-xanh': 'sesame balls banh cam',
  'suu-chua-nep-cam': 'yogurt black sticky rice',

  'ca-phe-sua-da': 'ca phe sua da vietnamese iced coffee',
  'ca-phe-den-da': 'vietnamese black iced coffee phin',
  'ca-phe-muoi': 'salt coffee vietnamese',
  'tra-sen-vang': 'lotus tea drink glass',
  'tra-dao-cam-sa': 'peach iced tea lemongrass',
  'tra-tac-mat-ong': 'kumquat honey tea',
  'nuoc-mia-tac': 'sugarcane juice glass',
  'nuoc-chanh-day': 'passion fruit juice glass',
  'sinh-to-xoai': 'mango smoothie glass',
  'nuoc-ep-dua-hau': 'watermelon juice glass',
  'nuoc-rau-ma-dau-xanh': 'pennywort drink green',
  'tra-atiso-da-lat': 'artichoke tea drink',
  'soda-chanh-bac-ha': 'mint lime soda drink',
  'nuoc-dua-tac-nong': 'ginger honey tea cup',
  'vang-do-ly': 'glass of red wine',
};

// Chỉ nhận giấy phép cho dùng lại vào mục đích thương mại
const GIAY_PHEP_NHAN = new Set(['cc0', 'pdm', 'by', 'by-sa']);

const UA = 'VuonSenThesis/1.0 (tieu luan tot nghiep; lien he qua truong)';
const NGHI_MS = 900;
const nghi = (ms) => new Promise((ok) => setTimeout(ok, ms));

const lay = (url) =>
  new Promise((ok, loi) => {
    https
      .get(url, { headers: { 'User-Agent': UA } }, (r) => {
        let d = '';
        r.on('data', (c) => (d += c));
        r.on('end', () => {
          if (r.statusCode !== 200) return loi(new Error('ma ' + r.statusCode));
          try {
            ok(JSON.parse(d));
          } catch (e) {
            loi(new Error('JSON hong'));
          }
        });
      })
      .on('error', loi);
  });

const songKhong = (url) =>
  new Promise((ok) => {
    const req = https.request(url, { method: 'HEAD', headers: { 'User-Agent': UA } }, (r) =>
      ok(r.statusCode >= 200 && r.statusCode < 300),
    );
    req.on('error', () => ok(false));
    req.setTimeout(20000, () => {
      req.destroy();
      ok(false);
    });
    req.end();
  });

(async () => {
  const tepRa = path.join(__dirname, 'anh-mon.json');
  const ketQua = {};
  const daDung = new Set();
  let dat = 0;

  for (let i = 0; i < MON.length; i++) {
    const m = MON[i];
    const tu = TU_KHOA[m.slug];
    if (!tu) throw new Error('Thieu tu khoa cho ' + m.slug);

    let chon = null;
    for (let lan = 1; lan <= 3 && !chon; lan++) {
      try {
        const j = await lay(
          'https://api.openverse.org/v1/images/?q=' + encodeURIComponent(tu) +
          '&license=cc0,pdm,by,by-sa&page_size=8',
        );
        for (const r of j.results || []) {
          if (!r.url || !GIAY_PHEP_NHAN.has(r.license)) continue;
          if (daDung.has(r.url)) continue;
          if (!(await songKhong(r.url))) continue;
          chon = {
            tep: r.title || '(khong ten)',
            url: r.url,
            trangGoc: r.foreign_landing_url || r.detail_url || '',
            giayPhep: (r.license || '').toUpperCase() + ' ' + (r.license_version || ''),
            tacGia: (r.creator || 'khong ro').slice(0, 80),
            nguon: r.source || 'openverse',
          };
          break;
        }
        break;
      } catch (e) {
        await nghi(NGHI_MS * lan * 2);
      }
    }

    if (chon) {
      daDung.add(chon.url);
      ketQua[m.slug] = chon;
      dat++;
    }
    console.log(
      `${String(i + 1).padStart(3)}/${MON.length} ${m.slug.padEnd(30)} ${chon ? chon.tep.slice(0, 46) : '(de trong)'}`,
    );
    fs.writeFileSync(tepRa, JSON.stringify(ketQua, null, 2), 'utf8');
    await nghi(NGHI_MS);
  }

  console.log(`\nDat ${dat}/${MON.length} mon co anh.`);
})();
