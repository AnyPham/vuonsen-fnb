/*
 * Bổ sung ảnh cho những món vòng lọc kỹ chưa tìm được.
 *
 * Vòng lọc kỹ đòi tên tệp phải chứa từ khóa của món, nên loại rất nhiều ảnh đúng chủ đề
 * chỉ vì Commons đặt tên bằng tiếng Anh còn từ khóa lại là tên món tiếng Việt. Vòng này
 * đổi cách: từ khóa viết bằng tiếng Anh mô tả đúng món ăn đó là gì, rồi chỉ giữ lại điều
 * kiện thể loại phải là đồ ăn thức uống.
 *
 * Bỏ điều kiện khớp tên vì từ khóa lúc này đã tả đúng món, còn điều kiện thể loại vẫn giữ
 * vì đó là thứ chặn được tem, tranh và ảnh nhà nghỉ lọt vào thực đơn.
 *
 * Ảnh ở đây là ảnh minh họa đúng loại món chứ không phải ảnh chụp chính món của nhà hàng.
 * Bảng ghi nguồn trong doc/DANH-SACH-ANH.md nói rõ điều này.
 *
 * Chạy: node cong-cu/du-lieu-mau/mon-an/tra-anh-bo-sung.js
 */
const fs = require('fs');
const path = require('path');
const https = require('https');

// Từ khóa tiếng Anh mô tả đúng món, viết tay cho từng món còn thiếu
const TU_KHOA = {
  'banh-khot-vung-tau': 'mini rice pancakes shrimp',
  'banh-beo-chen-hue': 'steamed rice cake small bowls',
  'sup-mang-cua-ga-xe': 'thick soup bowl chicken',
  'goi-ngo-sen-tom-thit': 'lotus root salad shrimp',
  'cha-ca-la-vong': 'turmeric fish dill pan',
  'chao-tom-boc-mia': 'shrimp paste skewer grilled',
  'tai-heo-cuon-ngu-sac': 'pork salad vegetables plate',
  'muc-chien-nuoc-mam': 'squid stir fried plate',
  'ngheu-hap-sa': 'steamed clams bowl',
  'khoai-lang-ken-chien': 'fried sweet potato balls',
  'thit-kho-trung-nuoc-dua': 'braised pork belly eggs',
  'suon-ram-man': 'braised pork ribs plate',
  'ga-kho-gung': 'braised chicken pieces pot',
  'com-nieu-dap-nieu': 'rice earthenware pot cooked',
  'bun-thit-nuong-cha-gio': 'rice vermicelli grilled pork bowl',
  'mi-quang-ga': 'yellow noodle bowl chicken',
  'tom-su-hap-nuoc-dua': 'steamed prawns plate',
  'ca-hap-hong-kong': 'steamed whole fish ginger',
  'ech-xao-lan': 'frog legs cooked plate',
  'luon-um-nuoc-dua': 'eel dish plate cooked',
  'ga-hap-la-sen': 'steamed chicken whole plate',
  'bo-nuong-la-cach': 'grilled beef rolls leaves',
  'heo-rung-xao-sa-ot': 'stir fried pork chilli',
  'nam-kho-tieu-chay': 'braised mushrooms pot',
  'lau-thai-hai-san': 'seafood hot pot soup',
  'lau-bo-nhung-dam': 'beef hot pot table',
  'lau-de-tiem-thuoc-bac': 'mutton stew pot herbs',
  'lau-nam-chay': 'mushroom hot pot vegetables',
  'lau-vit-nau-chao': 'duck stew pot',
  'lau-ech-mang-cay': 'spicy hot pot bamboo',
  'suon-nuong-bbq': 'barbecue pork ribs grilled',
  'tom-hum-nuong-pho-mai': 'grilled lobster half',
  'hau-nuong-mo-hanh': 'grilled oysters shell',
  'combo-lau-nuong-gia-dinh': 'barbecue table grill meat',
  'che-hat-sen-nhan-nhuc': 'lotus seed sweet soup bowl',
  'suong-sa-hat-luu': 'shaved ice dessert coconut',
  'kem-dua-trai-dua': 'coconut ice cream served',
  'sinh-to-bo-kem': 'avocado smoothie glass',
  'tau-hu-nuoc-duong': 'soybean pudding syrup bowl',
  'banh-cam-nhan-dau-xanh': 'sesame balls fried dessert',
  'suu-chua-nep-cam': 'yogurt dessert glass',
  'tra-sen-vang': 'iced tea glass drink',
  'tra-tac-mat-ong': 'honey lemon tea glass',
  'nuoc-mia-tac': 'sugarcane juice glass',
  'nuoc-rau-ma-dau-xanh': 'green drink glass herbal',
};

const GIAY_PHEP_NHAN = ['cc0', 'public domain', 'cc by', 'cc by-sa', 'copyrighted free use', 'attribution'];

const THE_LOAI_DO_AN = [
  'food', 'cuisine', 'dish', 'cooking', 'cookery', 'meal', 'restaurant',
  'drink', 'beverage', 'cocktail', 'coffee', 'tea ', 'juice', 'soup',
  'rice', 'noodle', 'meat', 'seafood', 'fish dishes', 'desserts', 'cakes',
  'salads', 'grilled', 'fried', 'steamed', 'hot pot', 'hotpot', 'pork',
  'chicken', 'beef', 'shrimp', 'prawn', 'mushroom', 'smoothie', 'ice cream',
];

const UA = 'VuonSenThesis/1.0 (tieu luan tot nghiep; lien he qua truong)';
const NGHI_MS = 1300;
const nghi = (ms) => new Promise((ok) => setTimeout(ok, ms));

const lay = (url) =>
  new Promise((ok, loi) => {
    https
      .get(url, { headers: { 'User-Agent': UA } }, (r) => {
        let d = '';
        r.on('data', (c) => (d += c));
        r.on('end', () => {
          if (r.statusCode === 429 || r.statusCode >= 500) return loi(new Error('ma ' + r.statusCode));
          try {
            ok(JSON.parse(d));
          } catch (e) {
            loi(new Error('khong doc duoc JSON'));
          }
        });
      })
      .on('error', loi);
  });

const khongDau = (s) =>
  (s || '').normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase();

const hopGiayPhep = (g) => GIAY_PHEP_NHAN.some((x) => (g || '').toLowerCase().includes(x));
const laDoAn = (theLoai) => {
  const gop = khongDau(theLoai.join(' | '));
  return THE_LOAI_DO_AN.some((x) => gop.includes(x));
};

async function tim(tuKhoa) {
  const url =
    'https://commons.wikimedia.org/w/api.php?action=query&format=json&generator=search' +
    '&gsrsearch=' + encodeURIComponent('filetype:bitmap ' + tuKhoa) +
    '&gsrnamespace=6&gsrlimit=14&prop=imageinfo|categories&cllimit=60' +
    '&iiprop=url|extmetadata&iiurlwidth=1000';
  const j = await lay(url);
  const trang = j.query && j.query.pages ? Object.values(j.query.pages) : [];
  return trang
    .map((p) => {
      const ii = p.imageinfo && p.imageinfo[0];
      if (!ii) return null;
      const m = ii.extmetadata || {};
      const boThe = (s) => (s || '').replace(/<[^>]+>/g, '').trim();
      return {
        tep: p.title,
        theLoai: (p.categories || []).map((c) => c.title),
        url: (ii.thumburl || ii.url).split('?')[0].replace('https://thumb.wikimedia.org/', 'https://upload.wikimedia.org/'),
        trangGoc: (ii.descriptionurl || '').split('?')[0],
        giayPhep: boThe(m.LicenseShortName && m.LicenseShortName.value) || '?',
        tacGia: boThe(m.Artist && m.Artist.value).slice(0, 80) || 'khong ro',
        minhHoa: true,
      };
    })
    .filter(Boolean);
}

(async () => {
  const tepRa = path.join(__dirname, 'anh-mon.json');
  const ketQua = JSON.parse(fs.readFileSync(tepRa, 'utf8'));
  const daDung = new Set(Object.values(ketQua).map((x) => x.url));

  const canTra = Object.keys(TU_KHOA).filter((s) => !ketQua[s]);
  console.log(`Can bo sung ${canTra.length} mon\n`);

  let them = 0;
  for (const slug of canTra) {
    let chon = null;
    for (let lan = 1; lan <= 3 && !chon; lan++) {
      try {
        for (const a of await tim(TU_KHOA[slug])) {
          if (daDung.has(a.url)) continue;
          if (!hopGiayPhep(a.giayPhep)) continue;
          if (!laDoAn(a.theLoai)) continue;
          chon = a;
          break;
        }
        break;
      } catch (e) {
        await nghi(NGHI_MS * lan * 2);
      }
    }

    if (chon) {
      daDung.add(chon.url);
      ketQua[slug] = chon;
      them++;
    }
    console.log(
      slug.padEnd(30) + (chon ? decodeURIComponent(chon.tep).replace('File:', '').slice(0, 46) : '(van de trong)'),
    );
    fs.writeFileSync(tepRa, JSON.stringify(ketQua, null, 2), 'utf8');
    await nghi(NGHI_MS);
  }

  console.log(`\nBo sung them ${them} anh. Tong cong ${Object.keys(ketQua).length}/100 mon co anh.`);
})();
