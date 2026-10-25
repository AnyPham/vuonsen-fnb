/*
 * Sinh migration V24: thêm cột đánh dấu món chay rồi bật cờ cho đúng những món chay.
 *
 * Sinh bằng mã thay vì gõ tay 41 câu UPDATE, cùng lý do đã dùng khi sinh 100 món ở V22:
 * tiêu chí phân loại nằm ở một chỗ duy nhất, sửa tiêu chí thì chạy lại là ra tệp SQL mới.
 * Gõ tay thì tiêu chí nằm trong đầu người gõ, không ai rà lại được.
 */
const fs = require('fs');
const path = require('path');

const KQ = path.join(__dirname, 'ket-qua-chay.json');
const RA = 'E:/vuonsen-fnb/backend/src/main/resources/db/migration/V24__danh_dau_mon_chay.sql';

/* Đồ uống có cồn: không thịt thật, nhưng gắn nhãn chay cho bia rượu là sai với cách
   hiểu của người Việt, vì ăn chay theo nhà Phật kiêng cả đồ uống có cồn. */
const CO_CON = /(?<!\p{L})(bia|rượu|vang|cocktail|whisky|vodka)(?!\p{L})/iu;

const kq = JSON.parse(fs.readFileSync(KQ, 'utf8'));
const chay = kq.filter((m) => m.chay && !CO_CON.test(m.ten) && !CO_CON.test(m.nguyenLieu));
const bo = kq.filter((m) => m.chay && (CO_CON.test(m.ten) || CO_CON.test(m.nguyenLieu)));

const dong = [];
dong.push('-- Danh dau mon chay de khach loc duoc thuc don va tro ly tra loi dung.');
dong.push('--');
dong.push('-- Truoc do co so du lieu khong co cho nao ghi mon nao chay. Tro ly tu doan theo ten');
dong.push('-- mon va doan sai: no xep Nom hoa chuoi vao danh sach chay, trong khi nguyen lieu co');
dong.push('-- nuoc mam chua ngot, tuc nuoc mam thuong. Voi khach an chay truong thi do la sai');
dong.push('-- nghiem trong, nen phai co du lieu that chu khong de mo hinh doan.');
dong.push('--');
dong.push('-- Tieu chi: khong thit, hai san, nuoc mam thuong, dau hao, trung, mo hay nuoc dung');
dong.push('-- tu dong vat. Van cho phep sua va hanh toi, dung muc chay thong dung cua nha hang');
dong.push('-- Viet chu khong phai chay thuan hay chay nha Phat. Do uong co con khong tinh la');
dong.push('-- chay du khong co thit, vi an chay kieng ca do co con.');
dong.push('--');
dong.push('-- Sinh bang cong-cu, phan loai doc tu truong nguyen lieu cua tung mon.');
dong.push('');
dong.push('ALTER TABLE dishes ADD COLUMN vegetarian BOOLEAN NOT NULL DEFAULT FALSE;');
dong.push('');
dong.push('-- ' + chay.length + ' mon chay tren tong ' + kq.length + ' mon');

const theoDanhMuc = {};
for (const m of chay) {
  (theoDanhMuc[m.dm] = theoDanhMuc[m.dm] || []).push(m);
}
const ten = { khaivi: 'Khai vi', chinh: 'Mon chinh', lau: 'Lau va nuong', trangmieng: 'Trang mieng', douong: 'Do uong', '?': 'Mon co san tu dau' };

for (const dm of ['khaivi', 'chinh', 'lau', 'trangmieng', 'douong', '?']) {
  const ds = theoDanhMuc[dm];
  if (!ds) continue;
  dong.push('');
  dong.push('-- ' + (ten[dm] || dm) + ' (' + ds.length + ')');
  for (const m of ds) {
    dong.push("UPDATE dishes SET vegetarian = TRUE WHERE slug = '" + m.slug + "';");
  }
}
dong.push('');

fs.writeFileSync(RA, dong.join('\n'), 'utf8');

console.log('Da ghi ' + RA);
console.log('  danh dau chay : ' + chay.length + ' mon');
console.log('  loai vi co con: ' + bo.length + ' mon -> ' + bo.map((m) => m.ten).join(', '));
console.log('  khong chay    : ' + (kq.length - chay.length - bo.length) + ' mon');
