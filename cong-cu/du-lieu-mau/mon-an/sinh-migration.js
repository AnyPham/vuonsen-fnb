/*
 * Sinh migration V22 từ danh mục 100 món và bảng ảnh đã tra được.
 *
 * Sinh bằng mã thay vì gõ tay 100 câu INSERT: dữ liệu nằm ở một chỗ duy nhất, sửa mô tả
 * một món thì chạy lại là ra tệp SQL mới, không lo hai nơi lệch nhau. Lệch nhau kiểu đó
 * là thứ rất khó phát hiện vì SQL vẫn chạy trót lọt.
 *
 * Chạy: node cong-cu/du-lieu-mau/mon-an/sinh-migration.js
 */
const fs = require('fs');
const path = require('path');

const MON = [
  ...require('./khai-vi.js'),
  ...require('./mon-chinh-1.js'),
  ...require('./mon-chinh-2.js'),
  ...require('./lau-nuong.js'),
  ...require('./trang-mieng-do-uong.js'),
];

const ANH = JSON.parse(fs.readFileSync(path.join(__dirname, 'anh-mon.json'), 'utf8'));

// Nháy đơn trong chuỗi SQL phải nhân đôi, nếu không câu lệnh sẽ đứt giữa chừng
const q = (s) => (s == null ? 'NULL' : "'" + String(s).replace(/'/g, "''") + "'");

const dong = [];
dong.push('-- Một trăm món bổ sung cho thực đơn, kèm bản tiếng Anh và ảnh minh họa.');
dong.push('--');
dong.push('-- Tệp này do cong-cu/du-lieu-mau/mon-an/sinh-migration.js sinh ra, đừng sửa tay:');
dong.push('-- sửa ở tệp nguồn rồi chạy lại, nếu không lần sinh sau sẽ ghi đè mất.');
dong.push('--');
dong.push('-- Ảnh lấy qua Openverse (phần lớn từ Flickr) và Wikimedia Commons, chỉ nhận giấy phép');
dong.push('-- CC0, phạm vi công cộng, CC BY và CC BY-SA. Cố ý loại nhóm giấy phép NC vì website');
dong.push('-- nhà hàng là mục đích thương mại. Bảng ghi nguồn từng ảnh: cong-cu/du-lieu-mau/mon-an/ghi-nguon-anh.md.');
dong.push('-- Món nào chưa có ảnh đúng chủ đề thì để trống, giao diện hiện khối giữ chỗ.');
dong.push('--');
dong.push('-- Dùng INSERT ... SELECT để lấy id danh mục theo mã thay vì ghi cứng số: số id phụ');
dong.push('-- thuộc thứ tự chạy migration, cơ sở dữ liệu dựng lại từ đầu có thể ra id khác.');
dong.push('');

// sort_order bắt đầu từ 100 để không chen vào giữa 26 món có sẵn
let thuTu = 100;

for (const m of MON) {
  const a = ANH[m.slug];
  dong.push(`-- ${m.ten}`);
  dong.push('INSERT INTO dishes (category_id, name, name_en, slug, description, description_en,');
  dong.push('                    price, image_url, best_seller, available, sort_order,');
  dong.push('                    ingredients, ingredients_en, preparation, preparation_en,');
  dong.push('                    order_note, order_note_en, portion_desc, portion_desc_en, prep_minutes,');
  dong.push('                    created_at, updated_at)');
  dong.push('SELECT c.id,');
  dong.push(`       ${q(m.ten)}, ${q(m.tenEn)}, ${q(m.slug)},`);
  dong.push(`       ${q(m.mota)}, ${q(m.motaEn)},`);
  dong.push(`       ${m.gia}, ${q(a ? a.url : null)}, 0, 1, ${thuTu},`);
  dong.push(`       ${q(m.nguyenLieu)}, ${q(m.nguyenLieuEn)},`);
  dong.push(`       ${q(m.cheBien)}, ${q(m.cheBienEn)},`);
  dong.push(`       ${q(m.luuY)}, ${q(m.luuYEn)},`);
  dong.push(`       ${q(m.phan)}, ${q(m.phanEn)}, ${m.phut},`);
  dong.push('       NOW(), NOW()');
  dong.push(`FROM dish_categories c WHERE c.code = ${q(m.dm)};`);
  dong.push('');
  thuTu++;
}

const tepRa = 'E:/vuonsen-fnb/backend/src/main/resources/db/migration/V22__mot_tram_mon_moi.sql';
fs.writeFileSync(tepRa, dong.join('\n'), 'utf8');

const coAnh = MON.filter((m) => ANH[m.slug]).length;
console.log(`Da sinh ${tepRa}`);
console.log(`${MON.length} mon, ${coAnh} mon co anh, ${dong.length} dong SQL`);

// ---- Bảng ghi nguồn ảnh, nối thêm vào doc/DANH-SACH-ANH.md ----
const bang = ['', '## Ảnh 100 món bổ sung (đợt 28/09/2026)', ''];
bang.push('Nguồn: Openverse (phần lớn dẫn về Flickr) và Wikimedia Commons.');
bang.push('');
bang.push('Chỉ dùng ảnh mang giấy phép CC0, phạm vi công cộng, CC BY hoặc CC BY-SA. Cố ý loại');
bang.push('nhóm giấy phép NC (phi thương mại) vì website nhà hàng là mục đích thương mại, dùng');
bang.push('ảnh NC vào đó là sai giấy phép. CC BY và CC BY-SA bắt buộc ghi tên tác giả nên bảng');
bang.push('dưới đây ghi đủ tên và đường dẫn trang gốc.');
bang.push('');
bang.push('Ảnh ở đây là ảnh minh họa đúng loại món, không phải ảnh chụp chính món của nhà hàng.');
bang.push('Khi nhà hàng có bộ ảnh riêng thì thay bằng cách dán đường dẫn mới ở trang quản trị.');
bang.push('');
bang.push('| Món | Giấy phép | Tác giả | Trang gốc |');
bang.push('|---|---|---|---|');
for (const m of MON) {
  const a = ANH[m.slug];
  if (!a) continue;
  bang.push(`| ${m.ten} | ${a.giayPhep} | ${a.tacGia} | ${a.trangGoc} |`);
}
fs.writeFileSync(path.join(__dirname, 'ghi-nguon-anh.md'), bang.join('\n') + '\n', 'utf8');
console.log('Da sinh bang ghi nguon anh: ghi-nguon-anh.md');
