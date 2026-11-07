/*
 * Tạo sẵn tài khoản khách để JMeter dùng khi đo tải, rồi ghi ra tệp CSV.
 *
 * VÌ SAO CẦN NHIỀU TÀI KHOẢN
 *
 * Cho cả trăm luồng cùng đăng nhập một tài khoản thì phép đo bị sai theo hai hướng. Hướng
 * thứ nhất là quá lạc quan: cùng một dòng người dùng được đọc đi đọc lại nên nằm sẵn trong
 * bộ đệm của cơ sở dữ liệu, nhanh hơn thực tế. Hướng thứ hai là quá bi quan: nhiều luồng
 * cùng ghi thời điểm đăng nhập cuối lên một dòng sẽ tranh khoá nhau, chậm hơn thực tế.
 * Người dùng thật mỗi người một tài khoản, nên bộ đo cũng phải vậy.
 *
 * VÌ SAO KHÔNG ĐỂ JMETER TỰ TẠO
 *
 * Tạo tài khoản là việc chuẩn bị, không phải việc cần đo. Gộp vào kịch bản đo thì thời gian
 * tạo tài khoản lẫn vào số liệu, và lần chạy thứ hai sẽ lỗi vì email đã tồn tại.
 *
 * CÁCH TẠO
 *
 * Đi đúng luồng thật: đăng ký bình thường rồi nhờ quản trị mở khoá, thay cho bước nhập mã
 * gửi về email mà script không đọc được. Giống hệt cách bộ kiểm thử Selenium làm.
 *
 * Cách chạy, cần backend đang chạy ở cổng 8080:
 *   node cong-cu/do-hieu-nang/jmeter/chuan-bi-tai-khoan.js [so tai khoan]
 */
const fs = require('fs');
const path = require('path');

const GOC = process.env.API_URL || 'http://localhost:8080';
const SO_TAI_KHOAN = Number(process.argv[2]) || 100;
const MAT_KHAU = 'Dotai123';
const TEP_CSV = path.join(__dirname, 'taikhoan.csv');

/* Tài khoản quản trị của dữ liệu mẫu, dùng để mở khoá các tài khoản vừa đăng ký.
   Giống giá trị mặc định mà bộ kiểm thử Selenium dùng trong CauHinh. */
const QUAN_TRI = {
  email: process.env.ADMIN_EMAIL || 'admin@vuonsen.vn',
  matKhau: process.env.ADMIN_PASSWORD || 'Admin@123',
};

async function goi(phuongThuc, duongDan, than, token) {
  const r = await fetch(GOC + duongDan, {
    method: phuongThuc,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: 'Bearer ' + token } : {}),
    },
    body: than ? JSON.stringify(than) : undefined,
  });
  const chu = await r.text();
  if (!r.ok) throw new Error(`${phuongThuc} ${duongDan} -> HTTP ${r.status} ${chu}`);
  return chu ? JSON.parse(chu) : null;
}

async function dangNhap(email, matKhau) {
  return (await goi('POST', '/api/auth/login', { email, password: matKhau })).accessToken;
}

/* Đăng ký xong tài khoản ở trạng thái chờ xác thực email; quản trị mở khoá thay cho việc
   nhập mã, vì script không đọc được hộp thư. */
async function taoMot(tokenQuanTri, email, hoTen) {
  await goi('POST', '/api/auth/register', {
    fullName: hoTen, email, phone: '0900000000', password: MAT_KHAU,
  });
  const tim = await goi('GET',
    '/api/v1/admin/users?keyword=' + encodeURIComponent(email), null, tokenQuanTri);
  const ds = Array.isArray(tim) ? tim : tim.content;
  const tk = ds.find((u) => u.email.toLowerCase() === email.toLowerCase());
  if (!tk) throw new Error('Đăng ký xong nhưng không tìm thấy tài khoản ' + email);
  await goi('PATCH', `/api/v1/admin/users/${tk.id}/enabled?enabled=true`, null, tokenQuanTri);
}

(async () => {
  console.log(`Máy chủ: ${GOC}`);
  const tokenQuanTri = await dangNhap(QUAN_TRI.email, QUAN_TRI.matKhau);
  console.log('Đã đăng nhập quản trị.');

  /* Dùng lại tệp cũ nếu còn, để chạy đo nhiều lần không đẻ thêm tài khoản rác. Kiểm tra
     bằng cách đăng nhập thử dòng đầu, vì tệp có thể còn mà cơ sở dữ liệu đã bị dựng lại. */
  if (fs.existsSync(TEP_CSV)) {
    const dong = fs.readFileSync(TEP_CSV, 'utf8').trim().split('\n');
    if (dong.length >= SO_TAI_KHOAN) {
      const [email, matKhau] = dong[0].split(',');
      try {
        await dangNhap(email, matKhau);
        console.log(`Tệp ${path.basename(TEP_CSV)} đã có ${dong.length} tài khoản dùng được, giữ nguyên.`);
        return;
      } catch {
        console.log('Tệp cũ không đăng nhập được, tạo lại từ đầu.');
      }
    }
  }

  const moc = Date.now();
  const dong = [];
  for (let i = 1; i <= SO_TAI_KHOAN; i++) {
    const email = `dotai.${moc}.${i}@vuonsen.vn`;
    await taoMot(tokenQuanTri, email, `Khách Đo Tải ${i}`);
    dong.push(`${email},${MAT_KHAU}`);
    if (i % 20 === 0 || i === SO_TAI_KHOAN) process.stdout.write(`\r  đã tạo ${i}/${SO_TAI_KHOAN}`);
  }
  console.log('');

  /* Không ghi dòng tiêu đề: CSV Data Set Config của JMeter đọc dòng đầu là dữ liệu. */
  fs.writeFileSync(TEP_CSV, dong.join('\n') + '\n', 'utf8');
  console.log(`Đã ghi ${dong.length} tài khoản vào ${TEP_CSV}`);
  console.log(`Mật khẩu dùng chung: ${MAT_KHAU}`);
})().catch((e) => {
  console.error('\nLỗi: ' + e.message);
  process.exit(1);
});
