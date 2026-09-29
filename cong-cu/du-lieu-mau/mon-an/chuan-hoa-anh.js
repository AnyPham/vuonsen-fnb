/*
 * Chuẩn hóa và kiểm tra đường dẫn ảnh đã tra được.
 *
 * Làm hai việc:
 *
 * 1. Đổi máy chủ thumb.wikimedia.org về upload.wikimedia.org. API trả về tên miền thứ
 *    nhất, tên miền chính thức là tên miền thứ hai; cả hai đều chạy nhưng ghi vào cơ sở
 *    dữ liệu thì nên ghi tên miền chính.
 *
 * 2. Tải thử từng ảnh, đo dung lượng và báo ảnh nào nặng bất thường.
 *
 * Cố ý KHÔNG tự dựng đường dẫn thu nhỏ cho ảnh gốc. Đã thử và sai: Commons chỉ sinh bản
 * thu nhỏ khi ảnh gốc rộng hơn cỡ yêu cầu; ảnh nào đã nhỏ sẵn thì API trả về chính ảnh
 * gốc, và dựng tay một đường dẫn /thumb/.../1000px- cho nó thì máy chủ trả mã 400. Nghĩa
 * là những đường dẫn ảnh gốc còn lại trong danh sách đều là ảnh dưới 1000px, dùng thẳng
 * được chứ không phải ảnh nhiều chục MB như lo ban đầu.
 *
 * Chạy: node cong-cu/du-lieu-mau/mon-an/chuan-hoa-anh.js
 */
const fs = require('fs');
const path = require('path');
const https = require('https');

const TEP = path.join(__dirname, 'anh-mon.json');
const UA = 'VuonSenThesis/1.0 (tieu luan tot nghiep)';
const NGUONG_NANG_KB = 800;

/*
 * Giãn cách giữa hai lần gọi.
 *
 * Gọi liên tiếp một trăm lần thì Wikimedia chặn lại và trả mã lỗi, nhìn vào tưởng cả
 * trăm đường dẫn đều hỏng trong khi thật ra chỉ là bị chặn vì gọi quá dày.
 */
const NGHI_MS = 700;
const nghi = (ms) => new Promise((ok) => setTimeout(ok, ms));

const chuanHoa = (url) => url.replace('https://thumb.wikimedia.org/', 'https://upload.wikimedia.org/');

const doKichThuoc = (url) =>
  new Promise((ok) => {
    const req = https.request(url, { method: 'HEAD', headers: { 'User-Agent': UA } }, (r) => {
      ok(r.statusCode >= 200 && r.statusCode < 300 ? Number(r.headers['content-length'] || 0) : -1);
    });
    req.on('error', () => ok(-1));
    req.setTimeout(20000, () => {
      req.destroy();
      ok(-1);
    });
    req.end();
  });

(async () => {
  const kq = JSON.parse(fs.readFileSync(TEP, 'utf8'));
  let doiTenMien = 0;
  let hong = 0;
  let nang = 0;
  let tongByte = 0;

  for (const [slug, a] of Object.entries(kq)) {
    const moi = chuanHoa(a.url);
    if (moi !== a.url) doiTenMien++;
    a.url = moi;

    const co = await doKichThuoc(moi);
    if (co < 0) {
      console.log(`${slug.padEnd(32)} HONG`);
      hong++;
      await nghi(NGHI_MS);
      continue;
    }

    const kb = Math.round(co / 1024);
    tongByte += co;
    if (kb > NGUONG_NANG_KB) {
      nang++;
      console.log(`${slug.padEnd(32)} ${String(kb).padStart(5)} KB  <-- nang`);
    }
    await nghi(NGHI_MS);
  }

  fs.writeFileSync(TEP, JSON.stringify(kq, null, 2), 'utf8');
  const so = Object.keys(kq).length;
  console.log(`\nDoi ten mien: ${doiTenMien}/${so}. Hong: ${hong}. Nang hon ${NGUONG_NANG_KB}KB: ${nang}.`);
  console.log(`Trung binh ${Math.round(tongByte / (so - hong) / 1024)} KB moi anh.`);
})();
