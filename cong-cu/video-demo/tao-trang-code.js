/*
 * Dựng các trang chiếu cho video demo gợi ý theo lượt xem: trang mở đầu, các trang code
 * đọc thẳng từ mã nguồn (giữ số dòng thật, tô vàng dòng quan trọng) và trang kết.
 * Ra kiemthu/target/video-demo/trang/*.html, khổ 1280x720.
 *   node cong-cu/video-demo/tao-trang-code.js
 */
const fs = require('fs');
const path = require('path');

const DU_AN = path.resolve(__dirname, '..', '..');
const RA = path.join(DU_AN, 'kiemthu', 'target', 'video-demo', 'trang');
const GOI = 'backend/src/main/java/vn/vuonsen/fnb/modules/recommendation/canhan/';
fs.mkdirSync(RA, { recursive: true });

const thoat = (s) => s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');

function khoiCode(tep, tu, den, sang) {
  const dong = fs.readFileSync(path.join(DU_AN, tep), 'utf8').replace(/\r\n/g, '\n').split('\n');
  const cat = dong.slice(tu - 1, den);
  const le = Math.min(...cat.filter((d) => d.trim()).map((d) => d.match(/^ */)[0].length));
  const hang = cat.map((d, i) => {
    const so = tu + i;
    const noi = sang.test(d) ? ' class="sang"' : '';
    return `<div${noi}><span class="so">${so}</span>${thoat(d.slice(le)) || ' '}</div>`;
  }).join('');
  return `<div class="tep">${thoat(tep)} · dòng ${tu}–${den}</div><pre>${hang}</pre>`;
}

const KHUNG = (than) => `<!doctype html><html lang="vi"><head><meta charset="utf-8"><style>
html,body{margin:0;width:1280px;height:720px;overflow:hidden;background:#0f172a;color:#e2e8f0;font-family:"Segoe UI",Arial,sans-serif}
.trang{box-sizing:border-box;height:720px;padding:28px 40px;display:flex;flex-direction:column}
h1{margin:0 0 6px;font-size:28px;color:#fbbf24}
.giai{font-size:19px;line-height:1.45;margin:0 0 14px;color:#cbd5e1}
.tep{font:13px Consolas,monospace;color:#94a3b8;margin:8px 0 4px}
pre{margin:0;background:#020617;border:1px solid #334155;border-radius:8px;padding:10px 0;font:15px/1.38 Consolas,monospace;overflow:hidden}
pre div{padding:0 16px;white-space:pre}
pre .so{display:inline-block;width:44px;color:#475569;text-align:right;margin-right:16px}
pre .sang{background:#422006;color:#fde68a}
.giua{margin:auto;text-align:center}
.giua h1{font-size:40px;margin-bottom:18px}
.giua p{font-size:22px;line-height:1.6;color:#cbd5e1;margin:6px 0}
</style></head><body><div class="trang">${than}</div></body></html>`;

const TRANG = {
  '00-mo-dau': `<div class="giua"><h1>Gợi ý sảnh theo độ tương đồng</h1>
<p>Khách đã đăng nhập mở xem một sảnh rồi thoát ra.</p>
<p>Lần sau vào trang chủ, hệ thống gợi ý những sảnh giống sảnh đã xem.</p>
<p style="color:#94a3b8;font-size:18px;margin-top:28px">Website Vườn Sen · Spring Boot + ReactJS + MySQL</p></div>`,

  '01-ghi-luot-xem': `<h1>Bước 1. Ghi lượt xem</h1>
<p class="giai">Mỗi lần khách mở trang chi tiết một sảnh, máy chủ ghi một dòng vào bảng luot_xem, gắn với tài khoản đang đăng nhập.</p>
${khoiCode('backend/src/main/java/vn/vuonsen/fnb/modules/space/SpaceController.java', 56, 64, /ghiLuotXem|LoaiDoiTuong\.KHONG_GIAN/)}`,

  '02-dung-ho-so': `<h1>Bước 2. Đưa lượt xem vào hồ sơ khách</h1>
<p class="giai">Sảnh được xem từ 2 lần trở lên trong 60 ngày thì vector đặc trưng của sảnh được cộng vào hồ sơ với trọng số 0,2. Các ngưỡng nằm trong application.yml.</p>
${khoiCode(GOI + 'GoiYCaNhanService.java', 139, 148, /soLanXemToiThieu|themLuotXem/)}
${khoiCode('backend/src/main/resources/application.yml', 123, 125, /./)}`,

  '03-cosine': `<h1>Bước 3. Tính độ tương đồng cosine</h1>
<p class="giai">Hồ sơ khách và từng sảnh đều là vector 14 chiều (loại tiệc, số khách, trong nhà hay ngoài trời, mức giá). Cosine được tính bằng thư viện Apache Commons Math.</p>
${khoiCode(GOI + 'DoTuongDong.java', 43, 49, /cosine\(new/)}`,

  '04-xep-hang': `<h1>Bước 4. Xếp hạng sảnh</h1>
<p class="giai">Điểm = 0,6 × cosine với hồ sơ (lọc theo nội dung) + 0,4 × tỉ lệ khách có hồ sơ giống đã đặt sảnh đó (lọc cộng tác). Lấy 3 sảnh điểm cao nhất.</p>
${khoiCode(GOI + 'BoGoiYKhongGian.java', 74, 97, /DoTuongDong\.cosine|congTac \+=|cauHinh\.alpha\(\)/)}`,

  '99-ket': `<div class="giua"><h1>Tóm tắt</h1>
<p>Xem sảnh → ghi lượt xem → hồ sơ vector → cosine + lọc cộng tác → gợi ý trên trang chủ</p>
<p style="font-size:19px;color:#94a3b8;margin-top:24px">Code: gói modules/recommendation/canhan</p>
<p style="font-size:19px;color:#94a3b8">Tiểu luận: mục 2.2.1, 3.2.5.1, 3.3.3, 3.3.8.7</p></div>`,
};

for (const [ten, than] of Object.entries(TRANG)) fs.writeFileSync(path.join(RA, ten + '.html'), KHUNG(than));
console.log('da dung ' + Object.keys(TRANG).length + ' trang o ' + RA);
