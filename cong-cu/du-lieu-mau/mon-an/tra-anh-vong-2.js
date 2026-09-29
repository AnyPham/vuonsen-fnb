/*
 * Vòng tra ảnh thứ hai cho những món vòng một không tìm được.
 *
 * Lý do phải có vòng hai: từ khóa vòng một bám sát tên món tiếng Việt, mà Commons phần
 * lớn gắn thẻ bằng tiếng Anh nên nhiều món không ra kết quả nào. Vòng này dùng từ khóa
 * mô tả kiểu món chứ không cố tìm đúng tên: một tấm ảnh lẩu bò đàng hoàng vẫn hơn là để
 * trống ô ảnh.
 *
 * Ghi thêm vào đúng tệp anh-mon.json của vòng một, không ghi đè những món đã có.
 */
const fs = require('fs');
const path = require('path');
const https = require('https');

// Từ khóa thay thế, chọn theo kiểu món chứ không theo tên riêng
const TU_KHOA_THAY = {
  'banh-beo-chen-hue': 'banh beo',
  'goi-ngo-sen-tom-thit': 'Vietnamese salad shrimp',
  'tai-heo-cuon-ngu-sac': 'Vietnamese pork salad',
  'ngheu-hap-sa': 'clams dish',
  'khoai-lang-ken-chien': 'fried sweet potato snack',
  'thit-kho-trung-nuoc-dua': 'thit kho',
  'suon-ram-man': 'pork ribs dish',
  'ga-kho-gung': 'chicken dish Vietnamese',
  'com-nieu-dap-nieu': 'rice clay pot',
  'bun-thit-nuong-cha-gio': 'bun thit nuong',
  'mi-quang-ga': 'mi Quang',
  'ca-hap-hong-kong': 'steamed fish dish',
  'ech-xao-lan': 'frog meat dish',
  'luon-um-nuoc-dua': 'eel dish Vietnamese',
  'bo-nuong-la-cach': 'grilled beef rolls',
  'heo-rung-xao-sa-ot': 'pork stir fry Vietnamese',
  'nam-kho-tieu-chay': 'mushroom dish clay',
  'canh-kho-qua-nhoi-thit': 'bitter gourd stuffed',
  'chim-cut-roti': 'quail dish',
  'lau-thai-hai-san': 'tom yum soup',
  'lau-de-tiem-thuoc-bac': 'goat meat soup',
  'lau-vit-nau-chao': 'duck soup Vietnamese',
  'lau-ech-mang-cay': 'spicy hot pot',
  'suon-nuong-bbq': 'barbecue ribs',
  'ba-chi-nuong-sa': 'grilled pork slices',
  'che-hat-sen-nhan-nhuc': 'lotus seed soup sweet',
  'che-khoai-deo': 'taro balls dessert',
  'kem-dua-trai-dua': 'coconut ice cream',
  'suu-chua-nep-cam': 'yogurt dessert rice',
  'ca-phe-sua-da': 'ca phe sua da',
  'tra-atiso-da-lat': 'herbal tea cup',
  'soda-chanh-bac-ha': 'mojito lime mint drink',
};

const GIAY_PHEP_NHAN = ['cc0', 'public domain', 'cc by', 'cc by-sa', 'copyrighted free use', 'attribution'];
const UA = 'VuonSenThesis/1.0 (tieu luan tot nghiep; lien he qua truong)';
const NGHI_MS = 1500;
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

const donDuongDan = (url) => (url || '').split('?')[0];
const hopLe = (g) => GIAY_PHEP_NHAN.some((x) => (g || '').toLowerCase().includes(x));

async function tim(tuKhoa) {
  const url =
    'https://commons.wikimedia.org/w/api.php?action=query&format=json&generator=search' +
    '&gsrsearch=' + encodeURIComponent('filetype:bitmap ' + tuKhoa) +
    '&gsrnamespace=6&gsrlimit=10&prop=imageinfo&iiprop=url|extmetadata&iiurlwidth=1000';
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
        url: donDuongDan(ii.thumburl || ii.url),
        trangGoc: donDuongDan(ii.descriptionurl),
        giayPhep: boThe(m.LicenseShortName && m.LicenseShortName.value) || '?',
        tacGia: boThe(m.Artist && m.Artist.value).slice(0, 80) || 'khong ro',
      };
    })
    .filter((x) => x && x.url && hopLe(x.giayPhep));
}

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
  const ketQua = JSON.parse(fs.readFileSync(tepRa, 'utf8'));
  const daDung = new Set(Object.values(ketQua).map((x) => x.url));

  const canTra = Object.keys(TU_KHOA_THAY).filter((s) => !ketQua[s]);
  console.log('Can tra lai ' + canTra.length + ' mon\n');

  for (const slug of canTra) {
    let chon = null;
    for (let lan = 1; lan <= 3 && !chon; lan++) {
      try {
        for (const a of await tim(TU_KHOA_THAY[slug])) {
          if (daDung.has(a.url)) continue;
          if (!(await songKhong(a.url))) continue;
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
      fs.writeFileSync(tepRa, JSON.stringify(ketQua, null, 2), 'utf8');
    }
    console.log(slug.padEnd(32) + (chon ? chon.giayPhep : 'VAN KHONG CO'));
    await nghi(NGHI_MS);
  }

  console.log('\nTong cong co anh: ' + Object.keys(ketQua).length + '/100');
})();
