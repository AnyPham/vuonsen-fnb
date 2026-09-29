/*
 * Tra ảnh minh họa cho 100 món mới trên Wikimedia Commons.
 *
 * Vì sao chọn Wikimedia Commons: không cần khóa API nên không phát sinh chi phí và không
 * phải đăng ký tài khoản, ảnh đều mang giấy phép tự do. Bộ ảnh cũ của dự án lấy từ Pexels
 * rồi tải lên Cloudinary, nhưng cách đó cần khóa tài khoản Cloudinary để tải lên, mà khóa
 * đó không có sẵn ở máy này.
 *
 * Chỉ nhận các giấy phép cho phép dùng lại: CC0, phạm vi công cộng, CC BY, CC BY-SA và
 * "Copyrighted free use". Ảnh không rõ giấy phép thì bỏ, thà để trống còn hơn đưa vào
 * tiểu luận một tấm không rõ nguồn gốc.
 *
 * Kết quả ghi ra anh-mon.json kèm tên tác giả và trang gốc, để lập bảng ghi nguồn trong
 * doc/DANH-SACH-ANH.md.
 *
 * Chạy: node cong-cu/du-lieu-mau/mon-an/tra-anh.js
 * Chạy lại thì tự bỏ qua những món đã có ảnh, nên đứt giữa chừng cũng chạy tiếp được.
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

const GIAY_PHEP_NHAN = [
  'cc0', 'public domain', 'cc by', 'cc by-sa', 'copyrighted free use', 'attribution',
];

const UA = 'VuonSenThesis/1.0 (tieu luan tot nghiep; lien he qua truong)';

/*
 * Giãn cách giữa hai lần gọi API.
 *
 * Lần chạy đầu gọi liên tục một trăm lần trong vài giây thì Commons chặn lại, chỉ mười
 * bốn món có ảnh. Máy chủ công cộng miễn phí thì phải gọi có chừng mực.
 */
const NGHI_MS = 1200;
const nghi = (ms) => new Promise((ok) => setTimeout(ok, ms));

const lay = (url) =>
  new Promise((ok, loi) => {
    https
      .get(url, { headers: { 'User-Agent': UA } }, (r) => {
        let d = '';
        r.on('data', (c) => (d += c));
        r.on('end', () => {
          if (r.statusCode === 429 || r.statusCode >= 500) {
            loi(new Error('may chu tra ma ' + r.statusCode));
            return;
          }
          try {
            ok(JSON.parse(d));
          } catch (e) {
            loi(new Error('khong doc duoc JSON'));
          }
        });
      })
      .on('error', loi);
  });

// Bỏ tham số theo dõi mà Commons gắn thêm vào đường dẫn ảnh
const donDuongDan = (url) => (url || '').split('?')[0];

const hopLe = (giayPhep) => {
  const g = (giayPhep || '').toLowerCase();
  return GIAY_PHEP_NHAN.some((x) => g.includes(x));
};

async function tim(tuKhoa) {
  const url =
    'https://commons.wikimedia.org/w/api.php?action=query&format=json&generator=search' +
    '&gsrsearch=' + encodeURIComponent('filetype:bitmap ' + tuKhoa) +
    '&gsrnamespace=6&gsrlimit=8&prop=imageinfo&iiprop=url|extmetadata&iiurlwidth=1000';
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

// Kiểm đường dẫn có tải được thật không, tránh ghi vào cơ sở dữ liệu một ảnh hỏng
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
  // Chạy tiếp từ kết quả lần trước, khỏi tra lại những món đã có ảnh
  const ketQua = fs.existsSync(tepRa) ? JSON.parse(fs.readFileSync(tepRa, 'utf8')) : {};
  const daDung = new Set(Object.values(ketQua).map((x) => x.url));

  for (let i = 0; i < MON.length; i++) {
    const m = MON[i];
    const thuTu = String(i + 1).padStart(3) + '/' + MON.length + ' ' + m.slug.padEnd(32);

    if (ketQua[m.slug]) {
      console.log(thuTu + 'da co');
      continue;
    }

    let chon = null;
    let loiCuoi = 'khong co ket qua hop giay phep';

    // Thử lại ba lần, mỗi lần nghỉ lâu hơn, vì lỗi hay gặp là bị chặn tạm thời
    for (let lan = 1; lan <= 3 && !chon; lan++) {
      try {
        const ds = await tim(m.tim);
        for (const a of ds) {
          if (daDung.has(a.url)) continue;
          if (!(await songKhong(a.url))) continue;
          chon = a;
          break;
        }
        break;
      } catch (e) {
        loiCuoi = e.message;
        await nghi(NGHI_MS * lan * 2);
      }
    }

    if (chon) {
      daDung.add(chon.url);
      ketQua[m.slug] = chon;
      console.log(thuTu + chon.giayPhep);
    } else {
      console.log(thuTu + 'KHONG CO - ' + loiCuoi);
    }

    // Ghi ngay sau mỗi món, đứt giữa chừng cũng không phải tra lại từ đầu
    fs.writeFileSync(tepRa, JSON.stringify(ketQua, null, 2), 'utf8');
    await nghi(NGHI_MS);
  }

  console.log('\nCo anh cho ' + Object.keys(ketQua).length + '/' + MON.length + ' mon.');
})();
