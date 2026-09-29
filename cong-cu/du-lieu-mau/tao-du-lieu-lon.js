/*
 * Sinh dữ liệu quy mô lớn cho website Vườn Sen: khoảng 1.500 khách hàng cùng toàn bộ đơn
 * đặt tiệc, đơn đặt món, đánh giá và phiếu thu của họ.
 *
 * Vì sao không làm thành migration của Flyway: đây là dữ liệu để xem cho giống hệ thống
 * chạy thật, không phải một phần của cấu trúc cơ sở dữ liệu. Để thành migration thì mỗi lần
 * ai đó dựng lại cơ sở dữ liệu đều phải nạp mấy vạn dòng, mà sửa cũng không sửa được nữa vì
 * Flyway lưu mã kiểm tra. Nên xuất ra một tệp SQL riêng, ai cần thì nạp.
 *
 * Tiền của từng đơn vẫn tính đúng theo PricingService như bộ sinh dữ liệu mẫu trước đó, để
 * số trên trang thống kê cộng lại luôn khớp với công thức website đang dùng.
 *
 * Mã đơn đánh số từ 5001 để không đụng dải mã hệ thống tự cấp (bắt đầu từ 0001) lẫn dải của
 * dữ liệu mẫu nhỏ ở V15 (bắt đầu từ 9001).
 */
const fs = require('fs');

const RA = 'E:/vuonsen-fnb/cong-cu/du-lieu-mau/du-lieu-lon.sql';
const BAM_MAT_KHAU = '$2a$10$sOCQ2ZrHQUPZ97FJXgtrGuyJHk74vtrn5EtDTZ0ydimmRjUW78wz2'; // Khach@123

const SO_KHACH = 1500;
const HOM_NAY = new Date(2026, 8, 28);          // 28/09/2026
const SO_THANG_LICH_SU = 24;                    // dữ liệu trải 24 tháng

let hat = 28092026;
const ngauNhien = () => ((hat = (hat * 1103515245 + 12345) & 0x7fffffff) / 0x7fffffff);
const trong = (a, b) => a + Math.floor(ngauNhien() * (b - a + 1));
const chon = (ds) => ds[trong(0, ds.length - 1)];
const xacSuat = (p) => ngauNhien() < p;

// ---------------------------------------------------------------- dữ liệu gốc
const KHONG_GIAN = {
  'SANH-VEN-SONG': { min: 300, max: 800, thue: 15000000, donVi: 'SESSION' },
  'SANH-SEN-VANG': { min: 200, max: 500, thue: 12000000, donVi: 'SESSION' },
  'NHA-RUONG-GO': { min: 20, max: 60, thue: 3000000, donVi: 'SESSION' },
  'PHONG-HOI-NGHI': { min: 40, max: 150, thue: 6000000, donVi: 'SESSION' },
  'CUM-CHOI-SEN': { min: 8, max: 144, thue: 500000, donVi: 'HUT', sucChuaChoi: 12 },
  'VUON-CAU': { min: 60, max: 150, thue: 7000000, donVi: 'SESSION' },
};
const GOI = { 'DONG-QUE': 2900000, 'SEN-VANG': 4500000, 'THUONG-UYEN': 6800000 };

const KIEU = [
  { ma: 'WEDDING', kg: ['SANH-VEN-SONG', 'SANH-SEN-VANG', 'VUON-CAU'], goi: ['SEN-VANG', 'THUONG-UYEN'] },
  { ma: 'CORPORATE', kg: ['PHONG-HOI-NGHI', 'SANH-SEN-VANG'], goi: ['DONG-QUE', 'SEN-VANG'] },
  { ma: 'BIRTHDAY', kg: ['NHA-RUONG-GO', 'VUON-CAU'], goi: ['DONG-QUE', 'SEN-VANG'] },
  { ma: 'FAMILY', kg: ['NHA-RUONG-GO', 'CUM-CHOI-SEN'], goi: ['DONG-QUE'] },
  { ma: 'OTHER', kg: ['CUM-CHOI-SEN', 'VUON-CAU'], goi: ['DONG-QUE', 'SEN-VANG'] },
];

const HO = ['Nguyễn', 'Trần', 'Lê', 'Phạm', 'Hoàng', 'Huỳnh', 'Phan', 'Vũ', 'Võ', 'Đặng',
  'Bùi', 'Đỗ', 'Hồ', 'Ngô', 'Dương', 'Lý', 'Trương', 'Đinh', 'Cao', 'Tô'];
const DEM_NAM = ['Văn', 'Hữu', 'Đức', 'Minh', 'Quang', 'Xuân', 'Gia', 'Bảo', 'Tuấn', 'Thành', 'Công', 'Anh'];
const DEM_NU = ['Thị', 'Ngọc', 'Kim', 'Thanh', 'Hồng', 'Thu', 'Phương', 'Mai', 'Bích', 'Diệu', 'Lan', 'Tuyết'];
const TEN_NAM = ['An', 'Bình', 'Cường', 'Dũng', 'Đạt', 'Hải', 'Hùng', 'Khoa', 'Long', 'Nam',
  'Phong', 'Quân', 'Sơn', 'Thắng', 'Tú', 'Vinh', 'Kiên', 'Lâm', 'Nhật', 'Trung'];
const TEN_NU = ['Anh', 'Chi', 'Dung', 'Giang', 'Hà', 'Hương', 'Lan', 'Linh', 'My', 'Ngân',
  'Nhung', 'Oanh', 'Quyên', 'Thảo', 'Trang', 'Uyên', 'Vy', 'Yến', 'Hạnh', 'Nga'];

const DUONG = ['Nguyễn Huệ', 'Lê Lợi', 'Trần Hưng Đạo', 'Hai Bà Trưng', 'Điện Biên Phủ',
  'Xô Viết Nghệ Tĩnh', 'Phan Xích Long', 'Bình Quới', 'Nguyễn Thị Minh Khai', 'Cách Mạng Tháng Tám',
  'Nguyễn Văn Trỗi', 'Hoàng Văn Thụ', 'Lý Thường Kiệt', 'Nguyễn Trãi', 'Võ Văn Tần'];
const QUAN = ['Quận 1', 'Quận 3', 'Quận 4', 'Quận 5', 'Quận 7', 'Quận 10', 'Bình Thạnh',
  'Phú Nhuận', 'Tân Bình', 'Gò Vấp', 'Thủ Đức', 'Bình Tân'];

// ---------------------------------------------------------------- công thức giá
const mam = (khach) => Math.ceil(khach / 10);
const mamToiThieu = (kg) => Math.ceil(kg.min / 10);

function phiKhongGian(kg, khach, tienAn) {
  if (kg.donVi === 'HUT') return kg.thue * Math.ceil(khach / kg.sucChuaChoi);
  const mucToiThieu = kg.thue * 10;
  if (tienAn >= mucToiThieu) return 0;
  return Math.round((1 - Math.round((tienAn / mucToiThieu) * 10000) / 10000) * kg.thue);
}

function tinhTien(maKg, maGoi, khach, ngayTiec, ngayDat) {
  const kg = KHONG_GIAN[maKg];
  const donGia = GOI[maGoi];
  const soMam = Math.max(mam(khach), mamToiThieu(kg));
  const tienAn = donGia * soMam;
  const phiKg = phiKhongGian(kg, khach, tienAn);
  const tamTinh = tienAn + phiKg;
  const giam = (ngayTiec - ngayDat) / 86400000 >= 60 ? Math.round(tamTinh * 0.05) : 0;
  const chiuThue = tamTinh - giam;
  const vat = Math.round(chiuThue * 0.08);
  return { soMam, donGia, tienAn, phiKg, giam, vat, tong: chiuThue + vat };
}

// ---------------------------------------------------------------- tiện ích
const hai = (n) => String(n).padStart(2, '0');
const ngayISO = (d) => `${d.getFullYear()}-${hai(d.getMonth() + 1)}-${hai(d.getDate())}`;
const gioISO = (d) => `${ngayISO(d)} ${hai(d.getHours())}:${hai(d.getMinutes())}:00`;
const congNgay = (d, n) => new Date(d.getTime() + n * 86400000);
const nhay = (s) => "N'" + String(s).replace(/'/g, "''") + "'";

const khongDau = (s) => s.normalize('NFD').replace(/[\u0300-\u036f]/g, '')
  .replace(/đ/g, 'd').replace(/Đ/g, 'D').toLowerCase().replace(/[^a-z0-9]+/g, '.');

// ---------------------------------------------------------------- khách hàng
const emailDaDung = new Set();
const khach = [];

for (let i = 0; i < SO_KHACH; i++) {
  const nam = xacSuat(0.45);
  const ten = `${chon(HO)} ${nam ? chon(DEM_NAM) : chon(DEM_NU)} ${nam ? chon(TEN_NAM) : chon(TEN_NU)}`;

  let email = `${khongDau(ten)}${trong(1, 9999)}@example.com`;
  while (emailDaDung.has(email)) {
    email = `${khongDau(ten)}${trong(1, 99999)}@example.com`;
  }
  emailDaDung.add(email);

  khach.push({
    ten,
    email,
    dienThoai: `0${chon([3, 5, 7, 8, 9])}${String(trong(0, 99999999)).padStart(8, '0')}`,
    diaChi: `${trong(1, 350)} ${chon(DUONG)}, ${chon(QUAN)}, TP.HCM`,
    // Tài khoản tạo rải đều 24 tháng qua
    ngayTao: congNgay(HOM_NAY, -trong(1, SO_THANG_LICH_SU * 30)),
  });
}

// Bốn nhân viên, để màn hình quản trị tài khoản có đủ ba vai trò
const NHAN_VIEN = [
  ['Trần Thị Kim Loan', 'kim.loan@vuonsen.vn', '0903880011', 'STAFF'],
  ['Nguyễn Hoàng Long', 'hoang.long@vuonsen.vn', '0903880022', 'STAFF'],
  ['Lê Thị Bảo Trân', 'bao.tran@vuonsen.vn', '0903880033', 'STAFF'],
  ['Phạm Quốc Khánh', 'quoc.khanh@vuonsen.vn', '0903880044', 'ADMIN'],
];

// ---------------------------------------------------------------- đơn đặt tiệc
const BUOI = ['MORNING', 'NOON', 'EVENING'];
const BAT_DAU_SO = 5000;
const demTheoNgay = {};

function sinhMa(tienTo, ngay) {
  const khoa = ngayISO(ngay).replace(/-/g, '');
  demTheoNgay[khoa] = (demTheoNgay[khoa] || BAT_DAU_SO) + 1;
  return `${tienTo}-${khoa}-${String(demTheoNgay[khoa]).padStart(4, '0')}`;
}

const daDatCho = new Set();
const donTiec = [];

for (const k of khach) {
  // Phần lớn khách đặt một lần, một số ít là khách quen đặt nhiều lần
  const soDon = xacSuat(0.55) ? 1 : (xacSuat(0.75) ? 2 : trong(3, 5));

  for (let j = 0; j < soDon; j++) {
    const kieu = chon(KIEU);
    const maKg = chon(kieu.kg);
    const maGoi = chon(kieu.goi);
    const kg = KHONG_GIAN[maKg];
    const soKhach = trong(kg.min, Math.min(kg.max, Math.round(kg.min * 2.5)));

    /*
     * Tiệc luôn nằm trong quá khứ, không sinh đơn cho ngày tương lai.
     *
     * Lý do học được sau một lần chạy hỏng: bộ kiểm thử tự động dùng vài ngày cố định như
     * hôm nay cộng ba mươi để đặt thử. Chỉ cần một đơn của dữ liệu lớn đã xác nhận trùng
     * đúng không gian, đúng ngày, đúng buổi đó là kịch bản không đặt được và trượt oan, mà
     * nhìn vào thì tưởng website hỏng. Đơn sắp tới đã có sẵn ở dữ liệu mẫu V15, đủ để màn
     * hình quản trị có việc để làm.
     */
    const saiSo = Math.floor((HOM_NAY - k.ngayTao) / 86400000);
    if (saiSo < 10) continue;

    const ngayTiec = congNgay(k.ngayTao, trong(5, Math.max(6, saiSo - 1)));

    const buoi = chon(BUOI);
    const khoa = `${maKg}|${ngayISO(ngayTiec)}|${buoi}`;
    if (daDatCho.has(khoa)) continue;

    const ngayDat = congNgay(ngayTiec, -trong(15, 120));
    if (ngayDat < k.ngayTao || ngayDat >= HOM_NAY) continue;

    const trangThai = xacSuat(0.09) ? 'CANCELLED' : 'COMPLETED';
    if (trangThai !== 'CANCELLED') daDatCho.add(khoa);

    donTiec.push({
      ma: sinhMa('VS', ngayDat),
      email: k.email,
      maKg, maGoi, kieu: kieu.ma, soKhach, buoi, trangThai,
      tien: tinhTien(maKg, maGoi, soKhach, ngayTiec, ngayDat),
      ngayTiec, ngayDat,
      nguoi: k.ten,
      dienThoai: k.dienThoai,
    });
  }
}

// ---------------------------------------------------------------- đơn đặt món
function docMonAn() {
  const sql = fs.readFileSync('E:/vuonsen-fnb/backend/src/main/resources/db/migration/V2__seed_data.sql', 'utf8');
  const phan = sql.slice(sql.indexOf('INSERT INTO dishes ('));
  const than = phan.slice(0, phan.indexOf(';'));
  const mon = [];
  for (const dong of than.split('\n')) {
    const m = dong.match(/N'((?:[^']|'')+)',\s*N'(?:[^']|'')*',\s*(\d+)/);
    if (m) mon.push({ ten: m[1].replace(/''/g, "'"), gia: Number(m[2]) });
  }
  return mon;
}

const MON = docMonAn();
if (MON.length < 10) {
  throw new Error(`Chỉ đọc được ${MON.length} món, không đủ để sinh đơn`);
}

const donMon = [];
for (const k of khach) {
  if (!xacSuat(0.55)) continue;          // hơn nửa số khách có đặt món lẻ
  const soDon = xacSuat(0.6) ? 1 : trong(2, 4);

  for (let j = 0; j < soDon; j++) {
    const giao = xacSuat(0.62);
    const dong = [];
    const daChon = new Set();
    const soDong = trong(2, 5);
    while (dong.length < soDong) {
      const m = chon(MON);
      if (daChon.has(m.ten)) continue;
      daChon.add(m.ten);
      const sl = trong(1, 3);
      dong.push({ ten: m.ten, gia: m.gia, sl, thanhTien: m.gia * sl });
    }

    const tamTinh = dong.reduce((t, d) => t + d.thanhTien, 0);
    if (giao && tamTinh < 150000) continue;

    const saiSo = Math.floor((HOM_NAY - k.ngayTao) / 86400000);
    if (saiSo < 3) continue;

    const ngayNhan = new Date(congNgay(k.ngayTao, trong(1, saiSo)).setHours(
      chon([11, 12, 17, 18, 19]), chon([0, 30]), 0, 0));
    if (ngayNhan >= HOM_NAY) continue;

    const phiGiao = giao && tamTinh < 500000 ? 30000 : 0;
    const vat = Math.round(tamTinh * 0.08);
    const trangThai = xacSuat(0.07) ? 'CANCELLED' : 'COMPLETED';

    donMon.push({
      ma: sinhMa('DM', congNgay(ngayNhan, -1)),
      hinhThuc: giao ? 'DELIVERY' : 'DINE_IN',
      nguoi: k.ten,
      dienThoai: k.dienThoai,
      email: k.email,
      diaChi: giao ? k.diaChi : null,
      soKhach: giao ? null : trong(2, 12),
      ngayNhan,
      ngayDat: new Date(ngayNhan.getTime() - trong(3, 40) * 3600000),
      dong, tamTinh, phiGiao, vat,
      tong: tamTinh + phiGiao + vat,
      trangThai,
      daThu: trangThai === 'COMPLETED' ? tamTinh + phiGiao + vat : 0,
    });
  }
}

// ---------------------------------------------------------------- đánh giá
const NOI_DUNG_TOT = [
  'Tiệc diễn ra suôn sẻ, món ra đúng giờ, nhân viên phục vụ nhanh nhẹn.',
  'Không gian đẹp hơn trong ảnh, khách của tôi ai cũng khen.',
  'Báo giá rõ ràng từng khoản, thanh toán xong không phát sinh gì thêm.',
  'Đặt online tiện, có mã đơn tra cứu lúc nào cũng được.',
  'Món miền Tây đúng vị, người lớn tuổi trong nhà ăn được hết.',
  'Nhân viên tư vấn kỹ, giúp chọn gói hợp với số khách của gia đình.',
  'Lần thứ hai đặt ở đây, vẫn hài lòng như lần đầu.',
  'Khu vực gửi xe rộng, khách tới đông vẫn không kẹt.',
  'Âm thanh ánh sáng chuẩn bị chu đáo, MC dẫn chương trình tự nhiên.',
  'Bên nhà hàng chủ động gọi xác nhận trước một ngày, rất yên tâm.',
];
const NOI_DUNG_KHA = [
  'Nhìn chung ổn, chỉ có một món ra hơi chậm so với dự kiến.',
  'Đồ ăn ngon, không gian đẹp, trừ điểm chỗ bãi xe hơi xa.',
  'Phục vụ tốt nhưng hôm đó đông nên phải chờ thêm một chút.',
  'Giá hợp lý so với mặt bằng chung, sẽ quay lại.',
  'Mọi thứ đúng như cam kết, chỉ mong thực đơn có thêm lựa chọn chay.',
];
const NOI_DUNG_TRUNG_BINH = [
  'Tiệc ổn nhưng hôm đó trùng ngày có tiệc lớn bên cạnh nên hơi ồn.',
  'Món ăn được, phục vụ hơi chậm vào giờ cao điểm.',
  'Không gian đẹp nhưng điều hòa khu ngoài trời chưa đủ mát.',
];

/*
 * Kho câu riêng cho đánh giá một và hai sao.
 *
 * Trước đây ba mức một, hai và ba sao dùng chung một kho câu, nên sinh ra những đánh giá
 * chấm một sao mà nội dung lại là "tiệc ổn nhưng hơi ồn". Đọc vào là biết ngay dữ liệu dựng
 * chứ không phải khách viết. Người chấm một sao thì phải có chuyện thật sự hỏng.
 */
const NOI_DUNG_KEM = [
  'Đặt bàn mười hai người mà tới nơi chỉ xếp được bàn tám, phải chờ gần bốn mươi phút mới kê thêm.',
  'Gọi xác nhận ba lần không ai bắt máy, tới nơi mới biết đơn chưa được duyệt.',
  'Món chính ra sau món tráng miệng, nhắc nhân viên hai lần mới thấy chuyển.',
  'Hóa đơn tính thừa một mâm so với số khách thực tế, phải ngồi đối chiếu lại từ đầu.',
  'Phòng đặt trước bị đổi sang khu khác vào phút chót mà không ai báo trước.',
  'Điều hòa hỏng suốt buổi tiệc, khách lớn tuổi phải ra ngoài ngồi.',
];

const donHoanThanh = donTiec.filter((d) => d.trangThai === 'COMPLETED');
const danhGia = [];
for (const don of donHoanThanh) {
  if (!xacSuat(0.32)) continue;         // khoảng một phần ba khách có để lại đánh giá

  const r = ngauNhien();
  const sao = r < 0.58 ? 5 : (r < 0.85 ? 4 : (r < 0.96 ? 3 : trong(1, 2)));
  // Nội dung phải khớp số sao, không dùng chung một kho câu cho mọi mức thấp
  const noiDung = sao >= 5 ? chon(NOI_DUNG_TOT)
    : (sao === 4 ? chon(NOI_DUNG_KHA)
      : (sao === 3 ? chon(NOI_DUNG_TRUNG_BINH) : chon(NOI_DUNG_KEM)));

  danhGia.push({
    maDon: don.ma,
    ten: don.nguoi,
    sao,
    noiDung,
    kieu: don.kieu,
    duyet: xacSuat(0.93),              // vài đánh giá để chờ duyệt cho giống thật
    /*
     * Ngày đánh giá không được vượt quá hôm nay.
     *
     * Danh sách đánh giá công khai sắp mới trước, một đánh giá đề ngày tương lai sẽ đứng
     * trên cả đánh giá vừa mới duyệt xong, đẩy nó ra khỏi trang đầu.
     */
    ngay: new Date(Math.min(congNgay(don.ngayTiec, trong(1, 10)).getTime(), HOM_NAY.getTime() - 86400000)),
  });
}

// ---------------------------------------------------------------- kết xuất SQL
const d = [];
const khoi = (ten) => d.push('', `-- ================= ${ten} =================`);

d.push('/*');
d.push(' * Dữ liệu quy mô lớn cho website Vườn Sen.');
d.push(' *');
d.push(` * Sinh ngày ${ngayISO(HOM_NAY)} bằng cong-cu/du-lieu-mau/tao-du-lieu-lon.js.`);
d.push(' * KHÔNG phải migration của Flyway: đây là dữ liệu để xem cho giống hệ thống chạy thật,');
d.push(' * không phải một phần cấu trúc cơ sở dữ liệu. Nạp bằng tay khi cần.');
d.push(' *');
d.push(' * Cách nạp:');
d.push(' *   mysql -u root vuonsen_fnb < cong-cu/du-lieu-mau/du-lieu-lon.sql');
d.push(' *');
d.push(' * Chỉ nạp MỘT LẦN. Nạp lần hai sẽ báo trùng mã đơn và trùng email.');
d.push(` * Mật khẩu của toàn bộ ${SO_KHACH} tài khoản khách bên dưới đều là Khach@123.`);
d.push(' */');
d.push('');
d.push('SET autocommit = 0;');
d.push('START TRANSACTION;');

khoi(`${khach.length} khách hàng và ${NHAN_VIEN.length} nhân viên`);
const dongKhach = khach.map((k) =>
  `(${nhay(k.ten)}, '${k.email}', '${k.dienThoai}', '${BAM_MAT_KHAU}', ${nhay(k.diaChi)}, 'CUSTOMER', TRUE, '${gioISO(k.ngayTao)}')`);
const dongNhanVien = NHAN_VIEN.map(([ten, email, dt, vaiTro]) =>
  `(${nhay(ten)}, '${email}', '${dt}', '${BAM_MAT_KHAU}', NULL, '${vaiTro}', TRUE, '${gioISO(congNgay(HOM_NAY, -trong(200, 700)))}')`);

// Chia thành từng lô để mỗi câu lệnh không quá dài, MySQL có giới hạn kích thước gói tin
const LO = 200;
for (let i = 0; i < dongKhach.length; i += LO) {
  d.push('INSERT INTO users (full_name, email, phone, password_hash, address, role, enabled, created_at) VALUES');
  d.push(dongKhach.slice(i, i + LO).join(',\n') + ';');
}
d.push('INSERT INTO users (full_name, email, phone, password_hash, address, role, enabled, created_at) VALUES');
d.push(dongNhanVien.join(',\n') + ';');

khoi(`${donTiec.length} đơn đặt tiệc`);
const dongTiec = donTiec.map((b) => {
  const coc = Math.round(b.tien.tong * 0.3);
  const daThu = b.trangThai === 'COMPLETED' ? b.tien.tong
    : (b.trangThai === 'CONFIRMED' ? coc : 0);
  const luc = b.trangThai === 'CANCELLED' || daThu === 0
    ? 'NULL' : `'${gioISO(congNgay(b.ngayDat, 1))}'`;
  return `('${b.ma}', (SELECT id FROM users WHERE email = '${b.email}'),`
    + ` (SELECT id FROM spaces WHERE code = '${b.maKg}'), (SELECT id FROM party_packages WHERE code = '${b.maGoi}'),`
    + ` '${b.kieu}', '${ngayISO(b.ngayTiec)}', '${b.buoi}', ${b.soKhach}, ${b.tien.soMam},`
    + ` ${b.tien.donGia}, ${b.tien.tienAn}, ${b.tien.phiKg}, ${b.tien.giam}, 0.08, ${b.tien.vat}, ${b.tien.tong},`
    + ` ${coc}, ${daThu}, ${luc}, ${daThu > 0 ? "'TRANSFER'" : 'NULL'},`
    + ` ${nhay(b.nguoi)}, '${b.dienThoai}', '${b.email}', '${b.trangThai}',`
    + ` '${gioISO(new Date(b.ngayDat.getTime() + 10 * 3600000))}')`;
});
for (let i = 0; i < dongTiec.length; i += LO) {
  d.push('INSERT INTO bookings (code, user_id, space_id, package_id, event_type, event_date, time_slot,');
  d.push('                      guest_count, table_count, unit_price, food_amount, space_fee, discount_amount,');
  d.push('                      vat_rate, vat_amount, total_amount, deposit_amount, deposit_paid, deposit_paid_at,');
  d.push('                      deposit_method, customer_name, customer_phone, customer_email, status, created_at) VALUES');
  d.push(dongTiec.slice(i, i + LO).join(',\n') + ';');
}

khoi('Lịch sử chuyển trạng thái đơn tiệc');
d.push("INSERT INTO booking_status_history (booking_id, from_status, to_status, changed_by, note, created_at)");
d.push("SELECT id, 'PENDING', 'CONFIRMED', 'admin@vuonsen.vn', N'Đã liên hệ khách và nhận cọc', created_at");
d.push("FROM bookings WHERE status IN ('CONFIRMED', 'COMPLETED') AND code LIKE 'VS-%-5%';");
d.push("INSERT INTO booking_status_history (booking_id, from_status, to_status, changed_by, note, created_at)");
d.push("SELECT id, 'CONFIRMED', 'COMPLETED', 'admin@vuonsen.vn', N'Tiệc đã diễn ra xong', created_at");
d.push("FROM bookings WHERE status = 'COMPLETED' AND code LIKE 'VS-%-5%';");
d.push("INSERT INTO booking_status_history (booking_id, from_status, to_status, changed_by, note, created_at)");
d.push("SELECT id, 'PENDING', 'CANCELLED', 'admin@vuonsen.vn', N'Khách báo hoãn', created_at");
d.push("FROM bookings WHERE status = 'CANCELLED' AND code LIKE 'VS-%-5%';");

khoi(`${donMon.length} đơn đặt món`);
const dongMonAn = donMon.map((o) =>
  `('${o.ma}', '${o.hinhThuc}', ${nhay(o.nguoi)}, '${o.dienThoai}', '${o.email}',`
  + ` ${o.diaChi ? nhay(o.diaChi) : 'NULL'}, ${o.soKhach ?? 'NULL'}, '${gioISO(o.ngayNhan)}',`
  + ` ${o.tamTinh}, 0, ${o.phiGiao}, ${o.vat}, ${o.tong}, ${o.daThu},`
  + ` ${o.daThu > 0 ? (o.hinhThuc === 'DELIVERY' ? "'COD'" : "'CASH'") : 'NULL'},`
  + ` '${o.trangThai}', (SELECT id FROM users WHERE email = '${o.email}'), '${gioISO(o.ngayDat)}')`);
for (let i = 0; i < dongMonAn.length; i += LO) {
  d.push('INSERT INTO dish_orders (code, fulfillment_type, customer_name, customer_phone, customer_email,');
  d.push('                         delivery_address, guest_count, serve_at, subtotal, discount_amount,');
  d.push('                         delivery_fee, vat_amount, total, paid_amount, payment_method,');
  d.push('                         status, user_id, created_at) VALUES');
  d.push(dongMonAn.slice(i, i + LO).join(',\n') + ';');
}

khoi('Các dòng món của từng đơn');
const dongChiTiet = [];
for (const o of donMon) {
  for (const l of o.dong) {
    dongChiTiet.push(`((SELECT id FROM dish_orders WHERE code = '${o.ma}'),`
      + ` (SELECT id FROM dishes WHERE name = ${nhay(l.ten)}), ${nhay(l.ten)}, ${l.gia}, ${l.sl}, ${l.thanhTien})`);
  }
}
for (let i = 0; i < dongChiTiet.length; i += LO) {
  d.push('INSERT INTO dish_order_items (order_id, dish_id, dish_name, unit_price, quantity, line_total) VALUES');
  d.push(dongChiTiet.slice(i, i + LO).join(',\n') + ';');
}

khoi(`${danhGia.length} đánh giá`);
const dongDanhGia = danhGia.map((r) =>
  `((SELECT id FROM bookings WHERE code = '${r.maDon}'), NULL, ${nhay(r.ten)}, ${r.sao}, ${nhay(r.noiDung)},`
  + ` '${r.kieu}', ${r.duyet ? 'TRUE' : 'FALSE'}, '${gioISO(new Date(r.ngay.getTime() + 15 * 3600000))}')`);
for (let i = 0; i < dongDanhGia.length; i += LO) {
  d.push('INSERT INTO reviews (booking_id, user_id, customer_name, rating, content, event_type, approved, created_at) VALUES');
  d.push(dongDanhGia.slice(i, i + LO).join(',\n') + ';');
}

khoi('Phiếu thu tương ứng, dựng từ số tiền đã ghi trên đơn');
d.push('INSERT INTO payments (code, order_type, booking_id, purpose, amount, method, status,');
d.push('                      reference, note, confirmed_by, confirmed_at, created_at)');
d.push("SELECT CONCAT('TT-B-', SUBSTRING(b.code, 4)), 'BOOKING', b.id,");
d.push("       CASE WHEN b.deposit_paid >= b.total_amount THEN 'FULL' ELSE 'DEPOSIT' END,");
d.push("       b.deposit_paid, 'TRANSFER', 'CONFIRMED', b.code,");
d.push("       N'Dựng từ dữ liệu quy mô lớn', 'admin@vuonsen.vn',");
d.push('       COALESCE(b.deposit_paid_at, b.created_at), COALESCE(b.deposit_paid_at, b.created_at)');
d.push("FROM bookings b WHERE b.deposit_paid > 0 AND b.code LIKE 'VS-%-5%';");
d.push('');
d.push('INSERT INTO payments (code, order_type, dish_order_id, purpose, amount, method, status,');
d.push('                      reference, note, confirmed_by, confirmed_at, created_at)');
d.push("SELECT CONCAT('TT-M-', SUBSTRING(o.code, 4)), 'DISH_ORDER', o.id, 'FULL', o.paid_amount,");
d.push("       COALESCE(o.payment_method, 'CASH'), 'CONFIRMED', o.code,");
d.push("       N'Dựng từ dữ liệu quy mô lớn', 'admin@vuonsen.vn', o.serve_at, o.serve_at");
d.push("FROM dish_orders o WHERE o.paid_amount > 0 AND o.code LIKE 'DM-%-5%';");

d.push('');
d.push('COMMIT;');
d.push('SET autocommit = 1;');
d.push('');

fs.mkdirSync('E:/vuonsen-fnb/cong-cu/du-lieu-mau', { recursive: true });
fs.writeFileSync(RA, d.join('\n'), 'utf8');

// ---------------------------------------------------------------- tóm tắt
const doanhThuTiec = donTiec.filter((b) => ['CONFIRMED', 'COMPLETED'].includes(b.trangThai))
  .reduce((t, b) => t + b.tien.tong, 0);
const doanhThuMon = donMon.filter((o) => o.trangThai === 'COMPLETED')
  .reduce((t, o) => t + o.tong, 0);
const demSao = {};
danhGia.forEach((r) => { demSao[r.sao] = (demSao[r.sao] || 0) + 1; });

console.log(`Da ghi ${RA} (${Math.round(fs.statSync(RA).size / 1024)} KB)`);
console.log(`  khach hang       : ${khach.length}`);
console.log(`  nhan vien/admin  : ${NHAN_VIEN.length}`);
console.log(`  don dat tiec     : ${donTiec.length}`);
console.log(`  don dat mon      : ${donMon.length} (${dongChiTiet.length} dong mon)`);
console.log(`  danh gia         : ${danhGia.length}`);
console.log(`  doanh thu tiec   : ${doanhThuTiec.toLocaleString('vi-VN')} d`);
console.log(`  doanh thu mon    : ${doanhThuMon.toLocaleString('vi-VN')} d`);
console.log('  trang thai don tiec:', ['PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED']
  .map((s) => `${s} ${donTiec.filter((b) => b.trangThai === s).length}`).join(', '));
console.log('  phan bo sao      :', [5, 4, 3, 2, 1].map((s) => `${s}* ${demSao[s] || 0}`).join(', '));
