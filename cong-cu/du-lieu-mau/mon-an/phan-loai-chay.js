/*
 * Phân loại 126 món trong thực đơn thành chay và không chay, dựa trên nguyên liệu thật.
 *
 * Gom nguyên liệu từ hai nguồn: 100 món mới nằm trong các tệp dữ liệu của cong-cu, và
 * 26 món gốc nằm trong migration V9. Không gọi API vì backend không phải lúc nào cũng
 * chạy, mà hai nguồn này mới là chỗ sinh ra dữ liệu.
 *
 * Mốc phân loại: món chay là món không có thịt, hải sản, nước mắm thường, dầu hào,
 * trứng, mỡ hay nước dùng từ động vật. Đây là mức "chay" thông dụng của nhà hàng.
 * Lưu ý riêng ở cuối: chay theo nhà Phật còn kiêng ngũ vị tân, tức hành tỏi hẹ kiệu.
 */
const fs = require('fs');
const path = require('path');

const GOC = 'E:/vuonsen-fnb';
const THU_MUC_MON = __dirname;

/*
 * Từ khóa loại trừ, nhóm riêng để báo cáo nói rõ món bị loại vì thứ gì.
 *
 * Không dùng \b được. Trong JavaScript, \b chỉ tính theo [A-Za-z0-9_] nên chữ có dấu
 * đứng ngay cạnh đã bị coi là ranh giới: mẫu /\bcá\b/ không khớp chuỗi "cá lóc". Bản
 * đầu viết như vậy và cho ra kết quả vô lý, xếp cả sò điệp, cá kho tộ, gà nướng và ếch
 * xào lăn vào nhóm chay. Thay bằng ranh giới nhận biết chữ cái Unicode.
 */
const tu = (...cac) => new RegExp('(?<!\\p{L})(?:' + cac.join('|') + ')(?!\\p{L})', 'iu');

const LOAI_TRU = {
  'thịt': tu('thịt', 'ba chỉ', 'nạc vai', 'nạc dăm', 'thăn', 'sườn', 'giò heo', 'chân giò',
    'bắp bò', 'gân bò', 'đuôi bò', 'xương', 'giò sống', 'chả lụa', 'nem chua', 'lạp xưởng'),
  'gia cầm': tu('gà', 'vịt', 'chim cút', 'ngan', 'bồ câu'),
  'hải sản': tu('tôm', 'cá', 'mực', 'nghêu', 'sò', 'ốc', 'cua', 'ghẹ', 'hàu', 'lươn',
    'hến', 'bạch tuộc', 'tép', 'cá cơm', 'khô cá', 'sò điệp', 'cá basa', 'cá lóc',
    'cá chẽm', 'cá nục', 'cá bớp', 'cá hồi', 'cá thu', 'tôm sú', 'tôm thẻ'),
  'thịt khác': tu('ếch', 'dê', 'bò', 'heo', 'lợn'),
  'nước mắm': /nước mắm(?! chay)|mắm tôm|mắm nêm|mắm ruốc|mắm cá/iu,
  'dầu hào': /dầu hào/iu,
  'trứng': tu('trứng', 'trứng cút', 'trứng muối', 'lòng đỏ', 'lòng trắng'),
  'mỡ động vật': /mỡ heo|mỡ gà|tóp mỡ|mỡ bò/iu,
  'nước dùng thịt': /nước dùng (gà|heo|bò|xương)|hầm xương|nước hầm xương|nước lèo (gà|heo|bò)/iu,
};

/*
 * Các cụm có tên con vật nhưng bản thân là nguyên liệu thực vật.
 *
 * "Nấm đùi gà" là tên một loại nấm, không phải thịt gà, mà dò chữ "gà" thì dính ngay:
 * ba món vốn có sẵn chữ chay trong tên là lẩu nấm chay, nấm kho tiêu chay và nấm nướng
 * sa tế đều bị xếp nhầm sang không chay. "Bơ sáp Đắk Lắk" là trái bơ chứ không phải
 * bơ sữa. Che các cụm này đi trước khi dò từ khóa loại trừ.
 */
const CUM_THUC_VAT = /nấm đùi gà|nấm bào ngư|bơ sáp|trái bơ|sữa đậu nành|sữa hạt|sa tế chay|chả chay|giò chay|thịt chay/giu;

function xetMot(ten, nguyenLieu) {
  const nl = String(nguyenLieu || '');
  const deDo = nl.replace(CUM_THUC_VAT, ' ___ ');
  const lyDo = [];
  for (const [nhan, re] of Object.entries(LOAI_TRU)) {
    if (re.test(deDo)) lyDo.push(nhan);
  }
  return { ten, nguyenLieu: nl, chay: lyDo.length === 0, lyDo };
}

/* ---- Nguồn 1: 100 món mới trong tệp dữ liệu ---- */
const mon = [];
for (const t of ['khai-vi', 'mon-chinh-1', 'mon-chinh-2', 'lau-nuong', 'trang-mieng-do-uong']) {
  for (const m of require(path.join(THU_MUC_MON, t + '.js'))) {
    mon.push({ slug: m.slug, ten: m.ten, dm: m.dm, nguyenLieu: m.nguyenLieu, nguon: 'V22' });
  }
}

/* ---- Nguồn 2: 26 món gốc trong migration V9 ---- */
const sqlV9 = fs.readFileSync(path.join(GOC, 'backend/src/main/resources/db/migration/V9__chi_tiet_mon_an.sql'), 'utf8');
for (const khoi of sqlV9.split(/UPDATE\s+dishes\s+SET/i).slice(1)) {
  const slug = (khoi.match(/slug\s*=\s*'([^']+)'/) || [])[1];
  const nl = (khoi.match(/ingredients\s*=\s*N'((?:[^']|'')*)'/) || [])[1];
  if (slug && nl) {
    mon.push({ slug, ten: slug, dm: '?', nguyenLieu: nl.replace(/''/g, "'"), nguon: 'V9' });
  }
}

/* Lấy tên thật của 26 món gốc từ V2 */
const sqlV2 = fs.readFileSync(path.join(GOC, 'backend/src/main/resources/db/migration/V2__seed_data.sql'), 'utf8');
const tenTheoSlug = {};
for (const m of sqlV9.split(/UPDATE\s+dishes\s+SET/i).slice(1)) {
  const slug = (m.match(/slug\s*=\s*'([^']+)'/) || [])[1];
  const dieuKien = (m.match(/WHERE\s+name\s*=\s*N'((?:[^']|'')*)'/i) || [])[1];
  if (slug && dieuKien) tenTheoSlug[slug] = dieuKien.replace(/''/g, "'");
}
mon.forEach((m) => { if (tenTheoSlug[m.slug]) m.ten = tenTheoSlug[m.slug]; });

/* ---- Phân loại ---- */
const ketQua = mon.map((m) => ({ ...m, ...xetMot(m.ten, m.nguyenLieu) }));
const chay = ketQua.filter((m) => m.chay);
const khongChay = ketQua.filter((m) => !m.chay);

console.log('Tong so mon xet: ' + ketQua.length);
console.log('  chay      : ' + chay.length);
console.log('  khong chay: ' + khongChay.length);
console.log();
console.log('===== DANH SACH MON DUOC XEP LA CHAY =====');
chay.forEach((m) => {
  console.log('  ' + m.slug.padEnd(32) + m.ten);
  console.log('      ' + m.nguyenLieu.slice(0, 110));
});

fs.writeFileSync(path.join(__dirname, 'ket-qua-chay.json'), JSON.stringify(ketQua, null, 1));
console.log();
console.log('(da ghi ket-qua-chay.json de dung cho buoc sinh migration)');
