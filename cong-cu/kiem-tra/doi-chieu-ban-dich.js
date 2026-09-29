/*
 * Đối chiếu hai tệp ngôn ngữ xem có khóa nào lệch nhau không.
 *
 * Lỗi hay gặp nhất khi làm đa ngôn ngữ không phải dịch sai chữ, mà là thêm một câu vào
 * tệp tiếng Việt rồi quên thêm vào tệp tiếng Anh. Lúc chạy thì không báo lỗi gì cả, chỗ
 * đó lặng lẽ hiện ra tiếng Việt giữa trang tiếng Anh, tự xem bằng mắt rất khó bắt.
 *
 * Chạy: node cong-cu/kiem-tra/doi-chieu-ban-dich.js
 * Trả mã thoát khác 0 khi có lệch, để cắm được vào quy trình kiểm tra tự động.
 */
const fs = require('fs');
const path = require('path');

const THU_MUC = path.join(__dirname, '..', '..', 'frontend', 'src', 'i18n');

// Trải đối tượng lồng nhau thành danh sách khóa phẳng: { nav: { spaces: 'x' } } -> ['nav.spaces']
const traiKhoa = (doiTuong, tienTo = '') =>
  Object.entries(doiTuong).flatMap(([khoa, giaTri]) => {
    const duongDan = tienTo ? `${tienTo}.${khoa}` : khoa;
    return typeof giaTri === 'object' && giaTri !== null ? traiKhoa(giaTri, duongDan) : [duongDan];
  });

const doc = (ma) => JSON.parse(fs.readFileSync(path.join(THU_MUC, `${ma}.json`), 'utf8'));

const vi = traiKhoa(doc('vi'));
const en = traiKhoa(doc('en'));

const thieuTiengAnh = vi.filter((k) => !en.includes(k));
const duTiengAnh = en.filter((k) => !vi.includes(k));

// Khóa có nhưng để chuỗi rỗng cũng là thiếu, chỉ là thiếu kiểu khó thấy hơn
const trong = (ma) => {
  const nguon = doc(ma);
  const tra = (k) => k.split('.').reduce((o, p) => (o == null ? undefined : o[p]), nguon);
  return traiKhoa(nguon).filter((k) => String(tra(k)).trim() === '');
};

console.log(`Tiếng Việt: ${vi.length} khóa`);
console.log(`Tiếng Anh:  ${en.length} khóa`);

let hong = false;

const bao = (nhan, ds) => {
  if (ds.length === 0) return;
  hong = true;
  console.error(`\n${nhan} (${ds.length}):`);
  ds.forEach((k) => console.error(`  - ${k}`));
};

bao('Thiếu trong en.json', thieuTiengAnh);
bao('Thừa trong en.json (không có bên tiếng Việt)', duTiengAnh);
bao('Để trống trong vi.json', trong('vi'));
bao('Để trống trong en.json', trong('en'));

if (hong) {
  console.error('\nKhông đạt: hai tệp ngôn ngữ chưa khớp nhau.');
  process.exit(1);
}

console.log('\nĐạt: hai tệp ngôn ngữ khớp nhau, không khóa nào bỏ trống.');
