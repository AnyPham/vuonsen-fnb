/*
 * Rà mọi component xem có chỗ nào gọi hàm dịch t() mà quên lấy nó từ useI18n().
 *
 * Vì sao cần: gọi t() ở một hàm không có hook thì Vite build vẫn qua, vì đó là lỗi lúc
 * chạy chứ không phải lỗi cú pháp. Trang chỉ vỡ khi người dùng bấm đúng vào nhánh đó, mà
 * bấm thử bằng tay thì rất dễ sót. Đã gặp đúng lỗi này ở BookingPage: hàm validateStep
 * nằm ngoài component nên không có t, chỉ lộ ra khi khách bỏ trống một ô bắt buộc.
 *
 * Chạy: node cong-cu/kiem-tra/kiem-hook-dich.js
 */
const fs = require('fs');
const path = require('path');

const GOC = path.join(__dirname, '..', '..', 'frontend', 'src');

const liet = (thuMuc) =>
  fs.readdirSync(thuMuc, { withFileTypes: true }).flatMap((muc) => {
    const duong = path.join(thuMuc, muc.name);
    if (muc.isDirectory()) return liet(duong);
    return muc.name.endsWith('.jsx') ? [duong] : [];
  });

// Gọi t với một khóa dạng chuỗi: t('a.b') hoặc t("a.b")
const GOI_T = /\bt\(\s*['"]/;

let soLoi = 0;
let soTep = 0;

for (const tep of liet(GOC)) {
  const nguon = fs.readFileSync(tep, 'utf8');
  if (!GOI_T.test(nguon)) continue;
  soTep++;

  // Cắt tệp thành từng hàm khai báo ở mức ngoài cùng
  const ham = [...nguon.matchAll(/^(?:export default )?function (\w+)\(([^)]*)\)/gm)]
    .map((m) => ({ ten: m[1], thamSo: m[2], viTri: m.index }));
  ham.push({ ten: '', thamSo: '', viTri: nguon.length });

  for (let i = 0; i < ham.length - 1; i++) {
    const than = nguon.slice(ham[i].viTri, ham[i + 1].viTri);
    if (!GOI_T.test(than)) continue;
    if (than.includes('useI18n()')) continue;
    // Hàm thường thì nhận t qua tham số, cũng hợp lệ
    if (/\bt\b/.test(ham[i].thamSo)) continue;

    console.error(`THIẾU HOOK: ${path.relative(GOC, tep)} → ${ham[i].ten}()`);
    soLoi++;
  }
}

console.log(`Đã rà ${soTep} tệp có gọi t().`);
if (soLoi > 0) {
  console.error(`Không đạt: ${soLoi} chỗ gọi t() mà không có useI18n() và cũng không nhận t qua tham số.`);
  process.exit(1);
}
console.log('Đạt: mọi chỗ gọi t() đều lấy được hàm dịch.');
