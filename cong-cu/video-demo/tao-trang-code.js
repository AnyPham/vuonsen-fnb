/*
 * Dựng các trang chiếu cho video demo gợi ý theo lượt xem: trang mở đầu, các trang code
 * đọc thẳng từ mã nguồn (giữ số dòng thật, tô vàng dòng quan trọng) và trang kết.
 * Ra kiemthu/target/video-demo/trang/*.html, khổ 1280x720.
 *   node cong-cu/video-demo/tao-trang-code.js          (bộ trang video gợi ý)
 *   node cong-cu/video-demo/tao-trang-code.js gmail    (bộ trang video Gmail)
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

/* Như khoiCode nhưng tìm dòng bắt đầu theo chuỗi mốc, để số dòng tự đúng khi code thay đổi. */
function doan(tep, moc, soDong, sang) {
  const dong = fs.readFileSync(path.join(DU_AN, tep), 'utf8').replace(/\r\n/g, '\n').split('\n');
  const i = dong.findIndex((d) => d.includes(moc));
  if (i < 0) throw new Error('Khong thay "' + moc + '" trong ' + tep);
  return khoiCode(tep, i + 1, i + soDong, sang);
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

const TRANG_GOI_Y = {
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

const B = 'backend/src/main/java/vn/vuonsen/fnb/';

const TRANG_GMAIL = {
  'g00-mo-dau': `<div class="giua"><h1>Đăng ký, đăng nhập và hóa đơn qua Gmail</h1>
<p>Mã OTP khi đăng ký · Đăng nhập bằng mật khẩu · Quên mật khẩu · Hóa đơn sau khi thanh toán</p>
<p style="color:#94a3b8;font-size:18px;margin-top:28px">Website Vườn Sen · Spring Boot + ReactJS + MySQL</p>
<p style="color:#fbbf24;font-size:17px;margin-top:22px">Máy quay video chưa bật Gmail, nên thư được hiện dưới dạng bản xem trước:<br>đúng lá thư hệ thống dựng và lưu trong bảng email_logs.</p></div>`,

  'g01-cac-buoc': `<h1>Các bước trong video</h1>
<p class="giai" style="font-size:22px;line-height:2">
1. Khách đăng ký bằng Gmail và mật khẩu<br>
2. Hệ thống gửi mã OTP 6 số về Gmail, hạn 15 phút<br>
3. Nhập sai mã thì bị đếm lượt, nhập đúng thì tài khoản được kích hoạt<br>
4. Đăng xuất, đăng nhập lại bằng Gmail và mật khẩu<br>
5. Quên mật khẩu: nhận thư có đường dẫn, đặt mật khẩu mới<br>
6. Đặt tiệc, đóng cọc, nhận hóa đơn qua Gmail</p>`,

  'g02-ma-otp': `<h1>Mã OTP được tạo và gửi đi thế nào</h1>
<p class="giai">Mã 6 số sinh bằng SecureRandom (không đoán trước được), lưu kèm hạn 15 phút rồi gửi thư HTML về Gmail của khách.</p>
${doan(B + 'modules/auth/XacThucEmailService.java', 'public void capMaVaGui(User user) {', 9, /sinhMa\(\)|plusMinutes|guiHtml/)}
${doan(B + 'modules/auth/XacThucEmailService.java', 'private String sinhMa() {', 6, /SecureRandom|nextInt|ngauNhien/)}`,

  'g03-kiem-ma': `<h1>Kiểm mã: sai quá 5 lần thì hủy mã</h1>
<p class="giai">Mỗi lần nhập sai được đếm. Đủ 5 lần, mã bị hủy và khách phải bấm gửi lại mã. Nhập đúng thì tài khoản được kích hoạt và đăng nhập luôn.</p>
${doan(B + 'modules/auth/XacThucEmailService.java', 'int lan = soLanSai.merge', 11, /SO_LAN_SAI_TOI_DA|huyMaXacThuc/)}`,

  'g04-gui-thu': `<h1>Gửi thư: một chỗ chung cho mọi loại thư</h1>
<p class="giai">Chưa khai báo Gmail, hoặc địa chỉ thuộc tên miền kiểm thử, thì chỉ ghi nhật ký. Ngược lại gửi thư UTF-8 có hai bản HTML và chữ trơn.</p>
${doan(B + 'modules/notification/EmailService.java', 'if (sender == null || properties.chiGhi(nguoiNhan)) {', 7, /chiGhi|CHI_GHI/)}
${doan(B + 'modules/notification/EmailService.java', 'MimeMessage thu = sender.createMimeMessage();', 11, /MimeMessageHelper|setText\(noiDung, html\)|send\(thu\)/)}`,

  'g05-hoa-don': `<h1>Hóa đơn gửi ngay khi tiền được ghi nhận</h1>
<p class="giai">Mọi đường thanh toán (VNPay, VietQR, tiền mặt) đều đi qua hàm xác nhận, nên chỉ cần gửi hóa đơn ở một chỗ. Gửi hỏng cũng không làm hỏng giao dịch.</p>
${doan(B + 'modules/payment/PaymentService.java', 'private void guiHoaDon(Payment phieu) {', 16, /HoaDonEmail|guiHtml/)}`,

  'g06-bat-gmail': `<h1>Bật gửi thư thật qua Gmail</h1>
<p class="giai">Tạo Mật khẩu ứng dụng 16 ký tự trong tài khoản Google (cần bật Xác minh 2 bước), bỏ dấu # và điền vào tệp tro-ly-ai.properties. Tệp này nằm trong .gitignore nên mật khẩu không lên GitHub.</p>
${doan('backend/tro-ly-ai.properties.example', '# --- Gui thu that qua Gmail', 18, /spring\.mail\.host|spring\.mail\.username|spring\.mail\.password|app\.mail\.from/)}`,

  'g99-ket': `<div class="giua"><h1>Tóm tắt</h1>
<p>Đăng ký → mã OTP qua Gmail → kích hoạt → đăng nhập bằng mật khẩu</p>
<p>Quên mật khẩu → thư đặt lại → mật khẩu mới</p>
<p>Đặt tiệc → đóng cọc → hóa đơn HTML qua Gmail</p>
<p style="font-size:19px;color:#94a3b8;margin-top:24px">Code: modules/auth, modules/notification, modules/payment</p></div>`,
};

const BO = process.argv[2] === 'gmail' ? TRANG_GMAIL : TRANG_GOI_Y;
for (const [ten, than] of Object.entries(BO)) fs.writeFileSync(path.join(RA, ten + '.html'), KHUNG(than));
console.log('da dung ' + Object.keys(BO).length + ' trang o ' + RA);
