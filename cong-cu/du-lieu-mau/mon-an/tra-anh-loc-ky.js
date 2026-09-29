/*
 * Tra ảnh món có lọc kỹ, thay cho hai vòng tra trước.
 *
 * Vì sao phải làm lại: hai vòng đầu chỉ tra theo từ khóa rồi lấy kết quả đầu tiên hợp
 * giấy phép. Cách đó cho ra khá nhiều ảnh sai hẳn chủ đề — một bức tranh của Bruegel cho
 * món cá tai tượng chiên xù, đĩa sushi cho lẩu nấm chay, con tem bưu chính cho soda chanh
 * bạc hà (vì tên tệp có chữ "mint"). Thực đơn có ảnh sai còn tệ hơn thực đơn không ảnh.
 *
 * Hai điều kiện lọc, ảnh phải qua cả hai:
 *
 * 1. Tên tệp phải chứa ít nhất một từ khóa của món. So khớp sau khi bỏ dấu và hạ chữ
 *    thường, vì tên tệp trên Commons lẫn lộn tiếng Việt có dấu và tiếng Anh.
 *
 * 2. Ảnh phải nằm trong ít nhất một thể loại liên quan tới đồ ăn thức uống. Đây là bộ lọc
 *    mạnh nhất: con tem nằm trong thể loại tem, bức tranh nằm trong thể loại hội họa, cả
 *    hai đều bị loại ngay mà không cần đoán theo tên.
 *
 * Món nào không tìm được ảnh đạt cả hai điều kiện thì để trống. Giao diện đã có sẵn khối
 * giữ chỗ kèm biểu tượng, nhìn vẫn gọn, và thà để trống còn hơn dán ảnh sai lên thực đơn.
 *
 * Chạy: node cong-cu/du-lieu-mau/mon-an/tra-anh-loc-ky.js
 * Kết quả ghi đè anh-mon.json.
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

const GIAY_PHEP_NHAN = ['cc0', 'public domain', 'cc by', 'cc by-sa', 'copyrighted free use', 'attribution'];

// Thể loại cho thấy đây thật sự là ảnh đồ ăn thức uống
const THE_LOAI_DO_AN = [
  'food', 'cuisine', 'dish', 'cooking', 'cookery', 'meal', 'restaurant',
  'drink', 'beverage', 'cocktail', 'coffee', 'tea ', 'juice', 'soup',
  'rice', 'noodle', 'meat', 'seafood', 'fish dishes', 'desserts', 'cakes',
  'salads', 'grilled', 'fried', 'steamed', 'hot pot', 'hotpot',
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

// Bỏ dấu tiếng Việt và hạ chữ thường, để so khớp tên tệp không phụ thuộc cách gõ
const khongDau = (s) =>
  (s || '')
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .replace(/đ/g, 'd')
    .replace(/Đ/g, 'd')
    .toLowerCase();

const BO_QUA = new Set(['the', 'and', 'with', 'for', 'style', 'dish', 'vietnamese', 'vietnam', 'food']);

// Các từ đáng kể trong từ khóa tra, dùng để đối chiếu với tên tệp
const tuKhoaManh = (cum) =>
  khongDau(cum)
    .split(/[^a-z0-9]+/)
    .filter((t) => t.length >= 3 && !BO_QUA.has(t));

const hopGiayPhep = (g) => GIAY_PHEP_NHAN.some((x) => (g || '').toLowerCase().includes(x));

const laDoAn = (theLoai) => {
  const gop = khongDau(theLoai.join(' | '));
  return THE_LOAI_DO_AN.some((x) => gop.includes(x));
};

async function tim(tuKhoa) {
  const url =
    'https://commons.wikimedia.org/w/api.php?action=query&format=json&generator=search' +
    '&gsrsearch=' + encodeURIComponent('filetype:bitmap ' + tuKhoa) +
    '&gsrnamespace=6&gsrlimit=12&prop=imageinfo|categories&cllimit=60' +
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
      };
    })
    .filter(Boolean);
}

(async () => {
  const tepRa = path.join(__dirname, 'anh-mon.json');
  const ketQua = {};
  const daDung = new Set();

  let dat = 0;
  let loaiViTen = 0;
  let loaiViTheLoai = 0;

  for (let i = 0; i < MON.length; i++) {
    const m = MON[i];
    const tu = tuKhoaManh(m.tim);
    let chon = null;

    for (let lan = 1; lan <= 3 && !chon; lan++) {
      try {
        for (const a of await tim(m.tim)) {
          if (daDung.has(a.url)) continue;
          if (!hopGiayPhep(a.giayPhep)) continue;

          const ten = khongDau(decodeURIComponent(a.tep));
          if (!tu.some((t) => ten.includes(t))) {
            loaiViTen++;
            continue;
          }
          if (!laDoAn(a.theLoai)) {
            loaiViTheLoai++;
            continue;
          }
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
      ketQua[m.slug] = chon;
      dat++;
    }

    const tenTep = chon ? decodeURIComponent(chon.tep).replace('File:', '').slice(0, 44) : '(de trong)';
    console.log(`${String(i + 1).padStart(3)}/${MON.length} ${m.slug.padEnd(30)} ${tenTep}`);

    fs.writeFileSync(tepRa, JSON.stringify(ketQua, null, 2), 'utf8');
    await nghi(NGHI_MS);
  }

  console.log(`\nDat: ${dat}/${MON.length} mon co anh dung chu de.`);
  console.log(`Loai vi ten khong khop tu khoa: ${loaiViTen}. Loai vi khong phai anh do an: ${loaiViTheLoai}.`);
})();
