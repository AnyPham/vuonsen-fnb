/*
 * Sinh tai lieu moc 4 (08/09 - 15/09): Danh sach va dac ta test case.
 *
 * Moi ket qua mong doi deu doi chieu voi ma nguon that:
 *   - Tham so nghiep vu: backend/src/main/resources/application.yml
 *   - Cong thuc tinh tien tiec: modules/booking/PricingService.java
 *   - Cong thuc tinh tien mon: modules/dishorder/DishOrderPricing.java
 *   - Thong bao loi: BookingService, DishOrderService, ReviewController
 *   - Du lieu mau: db/migration/V2__seed_data.sql
 * Khong con so nao trong tai lieu nay la doan.
 *
 * Thong ke o muc 5 duoc tinh tu chinh du lieu ben duoi, khong go tay, de sua
 * test case thi bang tong hop tu dong dung theo.
 *
 * Cap nhat 17/09/2026: doi chieu voi ma kiem thu kiemthu/.../kichban, them Luong 14, 15,
 * sua ket qua mong doi cho dung cach giao dien chan loi. Chay:
 *   node tao-dac-ta.js [duong dan ra]
 */
const {
  Document, Packer, Paragraph, TextRun, HeadingLevel, AlignmentType,
  Table, TableRow, TableCell, WidthType, ShadingType,
} = require('E:/vuonsen-fnb/cong-cu/nhat-ky/node_modules/docx');
const fs = require('fs');

const W = 9026;
const HDR = 'D9D9D9';

const cell = (text, width, o = {}) =>
  new TableCell({
    width: { size: width, type: WidthType.DXA },
    shading: o.bg ? { type: ShadingType.CLEAR, fill: o.bg } : undefined,
    margins: { top: 50, bottom: 50, left: 80, right: 80 },
    children: (Array.isArray(text) ? text : [text]).map(
      (t) => new Paragraph({
        spacing: { after: 0 },
        alignment: o.center ? AlignmentType.CENTER : AlignmentType.LEFT,
        children: [new TextRun({ text: String(t), bold: !!o.bold, size: o.size ?? 19 })],
      }),
    ),
  });

const p = (t, o = {}) => new Paragraph({
  spacing: { after: o.after ?? 120 },
  alignment: o.center ? AlignmentType.CENTER : AlignmentType.LEFT,
  children: [new TextRun({ text: t, size: o.size ?? 23, bold: !!o.bold, italics: !!o.italic })],
});

const h1 = (t) => new Paragraph({
  heading: HeadingLevel.HEADING_1, spacing: { before: 320, after: 140 },
  children: [new TextRun({ text: t, bold: true, size: 27 })],
});

const h2 = (t) => new Paragraph({
  heading: HeadingLevel.HEADING_2, spacing: { before: 240, after: 100 },
  children: [new TextRun({ text: t, bold: true, size: 24 })],
});

const bullet = (t) => new Paragraph({
  bullet: { level: 0 }, spacing: { after: 60 },
  children: [new TextRun({ text: t, size: 23 })],
});

const bang = (widths, header, rows, o = {}) => new Table({
  columnWidths: widths,
  width: { size: W, type: WidthType.DXA },
  rows: [
    new TableRow({
      tableHeader: true,
      children: header.map((h, i) => cell(h, widths[i], { bold: true, bg: HDR, size: o.size })),
    }),
    ...rows.map((r) => new TableRow({
      children: r.map((c, i) => cell(c, widths[i], { size: o.size })),
    })),
  ],
});

// ---------- Bang dac ta test case ----------
const COT_TC = [820, 1750, 2530, 2500, 700, 726];

const bangTC = (ds) => new Table({
  columnWidths: COT_TC,
  width: { size: W, type: WidthType.DXA },
  rows: [
    new TableRow({
      tableHeader: true,
      children: ['Mã', 'Mục tiêu', 'Các bước', 'Kết quả mong đợi', 'Loại', 'Ưu tiên']
        .map((h, i) => cell(h, COT_TC[i], { bold: true, bg: HDR, center: i >= 4 })),
    }),
    ...ds.map((tc) => new TableRow({
      children: [
        cell(tc.ma, COT_TC[0]),
        cell(tc.mucTieu, COT_TC[1]),
        cell(tc.buoc, COT_TC[2]),
        cell(tc.ketQua, COT_TC[3]),
        cell(tc.loai, COT_TC[4], { center: true }),
        cell(tc.uuTien, COT_TC[5], { center: true }),
      ],
    })),
  ],
});

const T = 'Thuận';
const N = 'Nghịch';
const C = 'Cao';
const TB = 'TB';
const Th = 'Thấp';

// ============================================================
// DU LIEU TEST CASE THEO 13 LUONG
// ============================================================
const LUONG = [];
// lop: lớp kịch bản Selenium chứa các test case của luồng, để đối chiếu từ tài liệu sang mã
const LOP = {
  AUTH: 'KiemThuXacThuc', SPACE: 'KiemThuKhongGian', SPDT: 'KiemThuChiTietKhongGian', MENU: 'KiemThuThucDon',
  BOOK: 'KiemThuDatTiec', BOOKV: 'KiemThuRangBuocDatTiec', TRACK: 'KiemThuTraCuuTiec', DISH: 'KiemThuDatMon',
  DISHV: 'KiemThuRangBuocDatMon', REV: 'KiemThuDanhGia', BOT: 'KiemThuTroLy', PERM: 'KiemThuPhanQuyen',
  ADMIN: 'KiemThuQuanTri', HOL: 'KiemThuGiamGiaNgayLe', HADM: 'KiemThuQuanTriNgayLe',
};
const luong = (ma, ten, url, tienDieuKien, ds) => {
  LUONG.push({ ma, ten, url, tienDieuKien, ds, lop: LOP[ma] });
};

// ---------- 1 ----------
luong('AUTH', 'Đăng ký và đăng nhập', '/dang-ky, /dang-nhap',
  'Chưa đăng nhập. Mỗi kịch bản đăng ký dùng một email mới chưa có trong dữ liệu; kịch bản trùng email dùng email quản trị admin@vuonsen.vn vốn luôn có sẵn.', [
  { ma: 'TC-AUTH-01', mucTieu: 'Đăng ký tài khoản mới hợp lệ',
    buoc: ['1. Mở /dang-ky', '2. Nhập họ tên, email chưa dùng, số điện thoại 10 số bắt đầu bằng 0, mật khẩu 8 ký tự', '3. Bấm Đăng ký'],
    ketQua: 'Đăng ký thành công, chuyển sang trang đăng nhập hoặc tự đăng nhập, tên người dùng hiện trên thanh điều hướng', loai: T, uuTien: C },
  { ma: 'TC-AUTH-02', mucTieu: 'Từ chối email đã tồn tại',
    buoc: ['1. Mở /dang-ky', '2. Nhập email admin@vuonsen.vn, các ô khác hợp lệ', '3. Bấm Đăng ký'],
    ketQua: 'Không tạo tài khoản, hiện thông báo "Email này đã được đăng ký", không đăng nhập, vẫn ở lại trang đăng ký', loai: N, uuTien: C },
  { ma: 'TC-AUTH-03', mucTieu: "Từ chối mật khẩu dưới 6 ký tự",
    buoc: ["1. Mở /dang-ky", "2. Nhập mật khẩu 5 ký tự, các ô khác hợp lệ", "3. Bấm Đăng ký"],
    ketQua: "Trình duyệt chặn không gửi form vì ô mật khẩu yêu cầu tối thiểu 6 ký tự, câu nhắc hiện ngay tại ô mật khẩu. Không tạo tài khoản, vẫn ở trang đăng ký. Nếu gửi thẳng lên máy chủ thì bị từ chối với câu \"Mật khẩu tối thiểu 6 ký tự\".", loai: N, uuTien: TB },
  { ma: 'TC-AUTH-04', mucTieu: 'Từ chối số điện thoại sai định dạng',
    buoc: ['1. Mở /dang-ky', '2. Nhập số điện thoại 1234567', '3. Bấm Đăng ký'],
    ketQua: 'Hiện thông báo "Số điện thoại không hợp lệ". Quy tắc: bắt đầu bằng 0, tổng 9 đến 11 chữ số', loai: N, uuTien: TB },
  { ma: 'TC-AUTH-05', mucTieu: "Từ chối email sai định dạng",
    buoc: ["1. Mở /dang-ky", "2. Nhập email \"abc@\", các ô khác hợp lệ", "3. Bấm Đăng ký"],
    ketQua: "Trình duyệt chặn không gửi form vì ô email sai định dạng, câu nhắc hiện ngay tại ô email. Không tạo tài khoản, vẫn ở trang đăng ký. Nếu gửi thẳng lên máy chủ thì bị từ chối với câu \"Email không hợp lệ\".", loai: N, uuTien: Th },
  { ma: 'TC-AUTH-06', mucTieu: 'Đăng nhập đúng thông tin',
    buoc: ['1. Mở /dang-nhap', '2. Nhập email và mật khẩu đúng', '3. Bấm Đăng nhập'],
    ketQua: 'Vào được trang chủ hoặc trang hồ sơ, thanh điều hướng đổi sang trạng thái đã đăng nhập', loai: T, uuTien: C },
  { ma: 'TC-AUTH-07', mucTieu: 'Từ chối sai mật khẩu',
    buoc: ['1. Mở /dang-nhap', '2. Nhập email đúng, mật khẩu sai', '3. Bấm Đăng nhập'],
    ketQua: 'Không đăng nhập được, hiện thông báo sai thông tin, vẫn ở trang đăng nhập', loai: N, uuTien: C },
  { ma: 'TC-AUTH-08', mucTieu: "Từ chối email chưa đăng ký, không tiết lộ email có tồn tại hay không",
    buoc: ['1. Mở /dang-nhap', '2. Nhập email chưa tồn tại', '3. Bấm Đăng nhập'],
    ketQua: 'Hiện thông báo sai thông tin đăng nhập, không tiết lộ email có tồn tại hay không', loai: N, uuTien: TB },
  { ma: 'TC-AUTH-09', mucTieu: 'Đăng xuất',
    buoc: ['1. Đăng nhập', '2. Bấm Đăng xuất trên thanh điều hướng'],
    ketQua: 'Về trạng thái chưa đăng nhập, các trang cần đăng nhập không vào được nữa', loai: T, uuTien: C },
  { ma: 'TC-AUTH-10', mucTieu: 'Giữ phiên sau khi tải lại trang',
    buoc: ['1. Đăng nhập', '2. Nhấn F5 tải lại trang'],
    ketQua: 'Vẫn ở trạng thái đã đăng nhập, không bị đá về trang đăng nhập', loai: T, uuTien: TB },
]);

// ---------- 2 ----------
luong('SPACE', 'Xem và lọc không gian', '/khong-gian',
  'Dữ liệu mẫu có đúng 6 không gian đang kinh doanh.', [
  { ma: 'TC-SPACE-01', mucTieu: 'Danh sách hiện đủ sáu không gian',
    buoc: ['1. Mở /khong-gian'],
    ketQua: 'Hiện đúng 6 thẻ: Sảnh Ven Sông, Sảnh Sen Vàng, Nhà Rường Gỗ, Phòng Hội Nghị Lúa, Cụm Chòi Sen, Vườn Cau', loai: T, uuTien: C },
  { ma: 'TC-SPACE-02', mucTieu: "Lọc theo số khách chỉ loại không gian vượt sức chứa tối đa",
    buoc: ["1. Mở /khong-gian", "2. Nhập 70 vào ô số khách"],
    ketQua: "Còn 5 không gian: Sảnh Ven Sông, Sảnh Sen Vàng, Phòng Hội Nghị Lúa, Cụm Chòi Sen, Vườn Cau. Chỉ Nhà Rường Gỗ (tối đa 60 khách) bị loại. Quy tắc: bộ lọc chỉ loại không gian có sức chứa tối đa nhỏ hơn số khách; không gian có sức chứa tối thiểu lớn hơn số khách vẫn hiện, vì nhà hàng nhận tiệc nhỏ và tính theo mức tối thiểu. Chọn 70 khách để kiểm chứng được cả hai vế của quy tắc.", loai: T, uuTien: C },
  { ma: 'TC-SPACE-03', mucTieu: 'Lọc theo loại không gian trong nhà',
    buoc: ['1. Mở /khong-gian', '2. Chọn loại INDOOR'],
    ketQua: 'Chỉ còn Sảnh Sen Vàng', loai: T, uuTien: TB },
  { ma: 'TC-SPACE-04', mucTieu: 'Lọc theo giá thuê tối đa 5.000.000đ',
    buoc: ['1. Mở /khong-gian', '2. Đặt giá tối đa 5.000.000'],
    ketQua: 'Chỉ còn Nhà Rường Gỗ (3.000.000) và Cụm Chòi Sen (500.000 mỗi chòi)', loai: T, uuTien: TB },
  { ma: 'TC-SPACE-05', mucTieu: 'Kết hợp hai tiêu chí lọc',
    buoc: ['1. Mở /khong-gian', '2. Nhập 500 khách', '3. Chọn loại OUTDOOR'],
    ketQua: 'Chỉ còn Sảnh Ven Sông', loai: T, uuTien: TB },
  { ma: 'TC-SPACE-06', mucTieu: "Lọc không ra kết quả thì hiện thông báo rỗng",
    buoc: ['1. Mở /khong-gian', '2. Nhập 900 khách'],
    ketQua: 'Danh sách rỗng, hiện thông báo không tìm thấy không gian phù hợp, không hiện lỗi kỹ thuật', loai: N, uuTien: C },
  { ma: 'TC-SPACE-07', mucTieu: "Bỏ bộ lọc thì danh sách quay lại đủ sáu không gian",
    buoc: ["1. Mở /khong-gian, chọn loại INDOOR để danh sách còn 1 kết quả", "2. Xóa lựa chọn ở các ô lọc"],
    ketQua: "Danh sách quay lại đủ 6 không gian. Trang không có nút xóa bộ lọc riêng, người dùng xóa trực tiếp từng ô lọc.", loai: T, uuTien: TB },
  { ma: 'TC-SPACE-08', mucTieu: 'Bấm thẻ mở trang chi tiết',
    buoc: ['1. Mở /khong-gian', '2. Bấm thẻ Sảnh Sen Vàng'],
    ketQua: 'Chuyển sang /khong-gian/sanh-sen-vang, tiêu đề trang là Sảnh Sen Vàng', loai: T, uuTien: C },
]);

// ---------- 3 ----------
luong('SPDT', 'Trang chi tiết không gian', '/khong-gian/:slug',
  'Không gian mẫu dùng để kiểm thử: Sảnh Sen Vàng, sức chứa 200-500 khách, phí thuê 12.000.000đ mỗi buổi.', [
  { ma: 'TC-SPDT-01', mucTieu: 'Hiện đúng sức chứa và giá thuê',
    buoc: ['1. Mở /khong-gian/sanh-sen-vang'],
    ketQua: 'Hiện sức chứa 200-500 khách và phí thuê 12.000.000đ, khớp với dữ liệu trong cơ sở dữ liệu', loai: T, uuTien: C },
  { ma: 'TC-SPDT-02', mucTieu: 'Danh sách tiện ích hiện đầy đủ',
    buoc: ['1. Mở trang chi tiết'],
    ketQua: 'Khối tiện ích hiện đủ các mục đã khai trong bảng space_amenities của không gian đó', loai: T, uuTien: TB },
  { ma: 'TC-SPDT-03', mucTieu: "Bấm ảnh nhỏ thì ảnh lớn đổi theo",
    buoc: ['1. Mở trang chi tiết', '2. Ghi lại đường dẫn ảnh lớn', '3. Bấm ảnh nhỏ thứ ba'],
    ketQua: 'Đường dẫn ảnh lớn đổi sang đúng ảnh vừa bấm', loai: T, uuTien: TB },
  { ma: 'TC-SPDT-04', mucTieu: 'Bản đồ hiển thị',
    buoc: ['1. Mở trang chi tiết', '2. Cuộn tới khối bản đồ'],
    ketQua: 'Khung bản đồ tải xong và hiển thị, không để khoảng trắng hoặc báo lỗi tải', loai: T, uuTien: Th },
  { ma: 'TC-SPDT-05', mucTieu: "Nút đặt tiệc dẫn sang trang đặt tiệc, chọn sẵn không gian vừa xem",
    buoc: ['1. Mở trang chi tiết', '2. Bấm nút đặt tiệc'],
    ketQua: 'Chuyển sang /dat-tiec, không gian vừa xem được chọn sẵn', loai: T, uuTien: TB },
  { ma: 'TC-SPDT-06', mucTieu: "Đường dẫn không tồn tại thì báo không tìm thấy",
    buoc: ['1. Mở /khong-gian/khong-co-that'],
    ketQua: 'Hiện trang báo không tìm thấy, không hiện màn hình trắng hay lỗi kỹ thuật', loai: N, uuTien: TB },
]);

// ---------- 4 ----------
luong('MENU', 'Thực đơn và chi tiết món', '/thuc-don, /thuc-don/:slug',
  'Thực đơn có 26 món chia 5 danh mục. Món Heo quay giòn bì không có giá cố định, ghi chú "Theo cân".', [
  { ma: 'TC-MENU-01', mucTieu: 'Trang thực đơn hiện đủ năm tab danh mục',
    buoc: ['1. Mở /thuc-don'],
    ketQua: 'Hiện 5 tab: khai vị, món chính, lẩu và nướng, tráng miệng, đồ uống', loai: T, uuTien: C },
  { ma: 'TC-MENU-02', mucTieu: "Chuyển tab thì danh sách món đổi theo",
    buoc: ['1. Mở /thuc-don', '2. Ghi lại danh sách món tab đang mở', '3. Bấm tab Tráng miệng'],
    ketQua: 'Danh sách món đổi hoàn toàn sang món tráng miệng, tab được bấm có trạng thái đang chọn', loai: T, uuTien: C },
  { ma: 'TC-MENU-03', mucTieu: 'Khối món bán chạy hiển thị',
    buoc: ['1. Mở /thuc-don'],
    ketQua: 'Khối món bán chạy hiện và có ít nhất một món', loai: T, uuTien: Th },
  { ma: 'TC-MENU-04', mucTieu: "Mở trang chi tiết món hiện đủ bốn khối thông tin",
    buoc: ['1. Mở /thuc-don', '2. Bấm một thẻ món'],
    ketQua: 'Chuyển sang /thuc-don/:slug đúng món, hiện đủ bốn khối: nguyên liệu, cách chế biến, khẩu phần, lưu ý khi đặt', loai: T, uuTien: C },
  { ma: 'TC-MENU-05', mucTieu: "Thư viện ảnh của món có 5 ảnh, bấm ảnh nhỏ đổi ảnh lớn",
    buoc: ['1. Mở trang chi tiết một món', '2. Bấm lần lượt các ảnh nhỏ'],
    ketQua: 'Món có 5 ảnh, bấm ảnh nhỏ nào thì ảnh lớn đổi sang đúng ảnh đó', loai: T, uuTien: Th },
  { ma: 'TC-MENU-06', mucTieu: 'Món tính giá theo cân hiển thị đúng',
    buoc: ['1. Mở trang chi tiết món Heo quay giòn bì'],
    ketQua: 'Chỗ hiển thị giá ghi "Theo cân" thay vì một con số, không hiện 0đ hay để trống', loai: T, uuTien: C },
  { ma: 'TC-MENU-07', mucTieu: "Thời gian chuẩn bị khớp dữ liệu",
    buoc: ['1. Mở trang chi tiết một món có khai thời gian chuẩn bị'],
    ketQua: 'Hiện số phút chuẩn bị khớp với trường prep_minutes trong cơ sở dữ liệu', loai: T, uuTien: Th },
  { ma: 'TC-MENU-08', mucTieu: "Đường dẫn món không tồn tại thì báo không tìm thấy",
    buoc: ['1. Mở /thuc-don/mon-khong-co'],
    ketQua: 'Hiện trang báo không tìm thấy', loai: N, uuTien: TB },
]);

// ---------- 5 ----------
luong('BOOK', 'Đặt tiệc ba bước', '/dat-tiec',
  'Đây là luồng phức tạp nhất. Tham số: 1 mâm 10 khách, VAT 8%, cọc 30%, giảm sớm 5% khi đặt trước từ 60 ngày, miễn phí thuê khi tiền ăn đạt 10 lần phí thuê. Ngày tổ chức trong luồng này chọn vào ngày không trùng dịp lễ đang bật; nếu trùng thì bảng tạm tính có thêm dòng giảm giá dịp lễ và các con số dưới đây không còn đúng (xem Luồng 14).', [
  { ma: 'TC-BOOK-01', mucTieu: "Đi trọn ba bước và gửi đơn, đơn ở trạng thái chờ xác nhận",
    buoc: ['1. Bước 1: tiệc cưới, 200 khách, ngày tổ chức sau 30 ngày', '2. Bước 2: Sảnh Sen Vàng, Gói Sen Vàng', '3. Bước 3: nhập họ tên, điện thoại, email', '4. Bấm gửi'],
    ketQua: 'Gửi thành công, hiện mã đơn dạng VS-yyyyMMdd-xxxx, trạng thái đơn là Chờ xác nhận', loai: T, uuTien: C },
  { ma: 'TC-BOOK-02', mucTieu: 'Số mâm tính đúng từ số khách',
    buoc: ['1. Bước 1 nhập 200 khách', '2. Đọc dòng số mâm ở bảng tạm tính'],
    ketQua: 'Hiện 20 mâm. Quy tắc: 1 mâm 10 khách, dư khách lẻ vẫn tính thêm một mâm', loai: T, uuTien: C },
  { ma: 'TC-BOOK-03', mucTieu: 'Số khách lẻ làm tròn lên một mâm',
    buoc: ['1. Bước 1 nhập 205 khách'],
    ketQua: 'Hiện 21 mâm chứ không phải 20,5 mâm', loai: T, uuTien: C },
  { ma: 'TC-BOOK-04', mucTieu: 'Bảng tạm tính đầy đủ, đúng công thức',
    buoc: ['1. 200 khách, Sảnh Sen Vàng, Gói Sen Vàng, ngày sau 30 ngày', '2. Đọc toàn bộ bảng tạm tính'],
    ketQua: 'Tiền ăn 90.000.000; phí thuê 3.000.000 (giảm theo tỉ lệ vì tiền ăn 90tr chưa đạt mức 120tr); VAT 7.440.000; tổng 100.440.000; cọc 30.132.000', loai: T, uuTien: C },
  { ma: 'TC-BOOK-05', mucTieu: "Bảng tạm tính cập nhật ngay khi đổi số khách",
    buoc: ['1. Đặt 200 khách, ghi lại tổng tiền', '2. Đổi sang 300 khách'],
    ketQua: 'Bảng tạm tính tính lại ngay, số mâm thành 30, tổng tiền tăng, không phải tải lại trang', loai: T, uuTien: C },
  { ma: 'TC-BOOK-06', mucTieu: 'Bảng tạm tính cập nhật khi đổi gói tiệc',
    buoc: ['1. Chọn Gói Sen Vàng, ghi lại tiền ăn', '2. Đổi sang Gói Thượng Uyển'],
    ketQua: 'Đơn giá đổi từ 4.500.000 sang 6.800.000 mỗi mâm, tiền ăn và tổng tiền tính lại theo', loai: T, uuTien: C },
  { ma: 'TC-BOOK-07', mucTieu: 'Miễn phí thuê không gian khi tiền ăn đạt mức',
    buoc: ['1. Chọn 270 khách, Sảnh Sen Vàng, Gói Sen Vàng'],
    ketQua: '27 mâm, tiền ăn 121.500.000 vượt mức 120.000.000 nên phí thuê về 0, bảng tạm tính ghi rõ lý do miễn phí', loai: T, uuTien: C },
  { ma: 'TC-BOOK-08', mucTieu: 'Giảm 5% khi đặt trước từ 60 ngày',
    buoc: ['1. Chọn 200 khách, Sảnh Sen Vàng, Gói Sen Vàng', '2. Chọn ngày tổ chức sau 70 ngày'],
    ketQua: 'Xuất hiện dòng giảm giá 4.650.000 (5% của 93.000.000), VAT tính trên phần còn lại là 7.068.000, tổng 95.418.000', loai: T, uuTien: C },
  { ma: 'TC-BOOK-09', mucTieu: 'Không giảm khi đặt trước dưới 60 ngày',
    buoc: ['1. Cùng lựa chọn như trên', '2. Chọn ngày tổ chức sau 59 ngày'],
    ketQua: 'Không có dòng giảm giá, tổng tiền bằng trường hợp TC-BOOK-04', loai: T, uuTien: TB },
  { ma: 'TC-BOOK-10', mucTieu: "Áp mức mâm tối thiểu của không gian",
    buoc: ['1. Chọn 200 khách nhưng chọn Sảnh Ven Sông (sức chứa tối thiểu 300)'],
    ketQua: 'Tính theo 30 mâm chứ không phải 20 mâm, bảng tạm tính ghi rõ lý do nhận tối thiểu 30 mâm', loai: T, uuTien: TB },
  { ma: 'TC-BOOK-11', mucTieu: "Không gian tính phí theo chòi",
    buoc: ['1. Chọn 50 khách, Cụm Chòi Sen, Gói Đồng Quê'],
    ketQua: 'Phí thuê 2.500.000 do thuê 5 chòi (mỗi chòi 12 khách, 500.000đ), không áp dụng miễn giảm theo tiền ăn', loai: T, uuTien: TB },
  { ma: 'TC-BOOK-12', mucTieu: "Quay lại bước trước vẫn giữ nguyên dữ liệu đã nhập",
    buoc: ['1. Điền xong bước 1 và 2', '2. Bấm quay lại bước 1'],
    ketQua: 'Các lựa chọn đã nhập vẫn còn nguyên, không bị xóa trắng', loai: T, uuTien: TB },
  { ma: 'TC-BOOK-13', mucTieu: "Chưa chọn không gian và gói thì không sang bước 3 được",
    buoc: ["1. Bước 1: 200 khách, ngày sau 30 ngày", "2. Sang bước 2, không chọn không gian, không chọn gói", "3. Bấm Tiếp tục"],
    ketQua: "Vẫn ở bước 2, hiện hai câu nhắc: \"Vui lòng chọn một không gian\" và \"Vui lòng chọn một gói tiệc, hoặc chọn chỉ thuê không gian\".", loai: N, uuTien: TB },
]);

// ---------- 6 ----------
luong('BOOKV', 'Ràng buộc khi đặt tiệc', '/dat-tiec',
  'Kiểm thử các trường hợp hệ thống phải từ chối. Tham số: số khách 10-800, báo trước 3 ngày, tiệc từ 20 mâm phải báo trước 7 ngày. Giao diện kiểm tra ngay ở bước 1 và bước 2 nên phần lớn trường hợp bị chặn trước khi gửi lên máy chủ; kết quả mong đợi ghi theo giao diện, kèm câu báo của máy chủ để đối chiếu khi gọi API trực tiếp.', [
  { ma: 'TC-BOOKV-01', mucTieu: "Số khách vượt sức chứa thì không chọn được không gian đó",
    buoc: ["1. Bước 1: 300 khách, ngày sau 30 ngày, buổi tối", "2. Bấm Tiếp tục", "3. Xem thẻ Nhà Rường Gỗ (tối đa 60 khách) và thẻ Sảnh Sen Vàng (tối đa 500 khách)"],
    ketQua: "Thẻ Nhà Rường Gỗ bị khóa, không chọn được; thẻ Sảnh Sen Vàng vẫn chọn được. Nếu gửi thẳng lên máy chủ thì bị từ chối với câu \"Nhà Rường Gỗ chỉ chứa tối đa 60 khách, không phục vụ được 300 khách\".", loai: N, uuTien: C },
  { ma: 'TC-BOOKV-02', mucTieu: "Số khách dưới mức tối thiểu",
    buoc: ["1. Bước 1: nhập 5 khách, ngày sau 30 ngày", "2. Bấm Tiếp tục"],
    ketQua: "Không sang được bước 2, ô số khách hiện câu nhắc \"Số khách từ 10 đến 800\". Nếu gửi thẳng lên máy chủ thì bị từ chối với câu \"Số khách phải trong khoảng 10 - 800\".", loai: N, uuTien: C },
  { ma: 'TC-BOOKV-03', mucTieu: "Số khách vượt mức tối đa",
    buoc: ["1. Bước 1: nhập 900 khách, ngày sau 30 ngày", "2. Bấm Tiếp tục"],
    ketQua: "Không sang được bước 2, hiện cùng câu nhắc \"Số khách từ 10 đến 800\".", loai: N, uuTien: TB },
  { ma: 'TC-BOOKV-04', mucTieu: "Tiệc nhỏ đặt sát ngày bị từ chối",
    buoc: ["1. Bước 1: 50 khách (5 mâm), ngày tổ chức sau 1 ngày", "2. Bấm Tiếp tục"],
    ketQua: "Không sang được bước 2, ô ngày tổ chức hiện câu nhắc \"Tiệc quy mô này cần đặt trước ít nhất 3 ngày\". Nếu gửi thẳng lên máy chủ thì bị từ chối với câu \"Tiệc 5 mâm cần đặt trước ít nhất 3 ngày, ngày bạn chọn chỉ còn 1 ngày\".", loai: N, uuTien: C },
  { ma: 'TC-BOOKV-05', mucTieu: "Tiệc từ 20 mâm đặt chưa đủ bảy ngày bị từ chối",
    buoc: ["1. Bước 1: 200 khách (20 mâm), ngày tổ chức sau 5 ngày", "2. Bấm Tiếp tục"],
    ketQua: "Không sang được bước 2, ô ngày tổ chức hiện câu nhắc \"Tiệc quy mô này cần đặt trước ít nhất 7 ngày\". Đây là ranh giới quan trọng: từ 20 mâm trở lên áp quy định tiệc lớn.", loai: N, uuTien: C },
  { ma: 'TC-BOOKV-06', mucTieu: "Ranh giới tiệc lớn: 19 mâm đặt trước 5 ngày vẫn được nhận",
    buoc: ['1. Chọn 190 khách (19 mâm)', '2. Chọn ngày tổ chức sau 5 ngày', '3. Gửi đơn'],
    ketQua: 'Chấp nhận, vì 19 mâm chưa tới ngưỡng 20 mâm nên chỉ cần báo trước 3 ngày', loai: T, uuTien: C },
  { ma: 'TC-BOOKV-07', mucTieu: "Gói tiệc dài hơn thời lượng buổi thì không chọn được",
    buoc: ["1. Bước 1: 200 khách, ngày sau 30 ngày, buổi sáng (4 tiếng)", "2. Bấm Tiếp tục", "3. Xem thẻ Gói Sen Vàng (5 tiếng) và thẻ Gói Đồng Quê (3 tiếng)"],
    ketQua: "Thẻ Gói Sen Vàng bị khóa vì dài hơn buổi sáng; thẻ Gói Đồng Quê vẫn chọn được. Nếu gửi thẳng lên máy chủ thì bị từ chối với câu \"Gói Sen Vàng cần 5 tiếng, Buổi sáng (7h00 - 11h00) chỉ có 4 tiếng. Vui lòng chọn buổi khác.\".", loai: N, uuTien: TB },
  { ma: 'TC-BOOKV-08', mucTieu: "Trùng lịch với đơn đã xác nhận thì bị từ chối",
    buoc: ['1. Tạo và xác nhận một đơn cho Sảnh Sen Vàng ngày X buổi tối', '2. Đặt đơn mới cùng không gian, cùng ngày, cùng buổi'],
    ketQua: 'Từ chối, thông báo không gian đã có tiệc vào buổi đó, gợi ý chọn buổi hoặc ngày khác', loai: N, uuTien: C },
  { ma: 'TC-BOOKV-09', mucTieu: 'Bỏ trống họ tên ở bước ba',
    buoc: ['1. Tới bước 3', '2. Để trống họ tên', '3. Bấm gửi'],
    ketQua: 'Không gửi được, hiện nhắc nhập họ tên ngay dưới ô đó', loai: N, uuTien: TB },
  { ma: 'TC-BOOKV-10', mucTieu: 'Bỏ trống số điện thoại ở bước ba',
    buoc: ['1. Tới bước 3', '2. Để trống số điện thoại', '3. Bấm gửi'],
    ketQua: 'Không gửi được, hiện nhắc nhập số điện thoại', loai: N, uuTien: TB },
  { ma: 'TC-BOOKV-11', mucTieu: "Ngày tổ chức trong quá khứ bị từ chối",
    buoc: ["1. Bước 1: 50 khách, ngày tổ chức là hôm qua", "2. Bấm Tiếp tục"],
    ketQua: "Không sang được bước 2, ô ngày tổ chức hiện câu nhắc lỗi vì ngày đã qua không đủ số ngày báo trước tối thiểu.", loai: N, uuTien: TB },
]);

// ---------- 7 ----------
luong('TRACK', 'Tra cứu đơn đặt tiệc', '/tra-cuu',
  'Cần một mã đơn có thật. Mỗi kịch bản tự tạo một đơn mới qua API để lấy mã, không dùng lại đơn của TC-BOOK-01, để các kịch bản chạy độc lập và đổi thứ tự chạy không ảnh hưởng kết quả.', [
  { ma: 'TC-TRACK-01', mucTieu: 'Tra cứu bằng mã đơn hợp lệ',
    buoc: ['1. Mở /tra-cuu', '2. Nhập mã đơn vừa tạo', '3. Bấm tra cứu'],
    ketQua: 'Hiện thông tin đơn: không gian, gói tiệc, số khách, ngày tổ chức, tổng tiền, trạng thái Chờ xác nhận', loai: T, uuTien: C },
  { ma: 'TC-TRACK-02', mucTieu: "Mã đơn không tồn tại thì báo không tìm thấy",
    buoc: ['1. Mở /tra-cuu', '2. Nhập VS-00000000-9999', '3. Bấm tra cứu'],
    ketQua: 'Hiện thông báo không tìm thấy đơn, không hiện màn hình trắng hay lỗi kỹ thuật', loai: N, uuTien: C },
  { ma: 'TC-TRACK-03', mucTieu: "Bỏ trống mã đơn thì hiện nhắc nhập mã",
    buoc: ['1. Mở /tra-cuu', '2. Bấm tra cứu khi ô còn trống'],
    ketQua: 'Không gọi máy chủ, hiện nhắc nhập mã đơn', loai: N, uuTien: Th },
  { ma: 'TC-TRACK-04', mucTieu: "Mã đơn có khoảng trắng thừa vẫn tìm ra đơn",
    buoc: ['1. Nhập mã đơn kèm dấu cách ở đầu và cuối', '2. Bấm tra cứu'],
    ketQua: 'Vẫn tìm ra đơn, vì hệ thống cắt khoảng trắng trước khi tra', loai: T, uuTien: Th },
]);

// ---------- 8 ----------
luong('DISH', 'Đặt món lẻ', '/dat-mon',
  'Tham số: phí giao 30.000đ, miễn phí giao từ 500.000đ, đơn giao tối thiểu 150.000đ, VAT 8% chỉ tính trên tiền món. Món mẫu: Cơm cháy chà bông kho quẹt 135.000đ, Combo nướng than hoa 690.000đ. Giờ nhận chọn vào ngày không trùng dịp lễ đang bật.', [
  { ma: 'TC-DISH-01', mucTieu: "Thêm món vào giỏ từ trang chi tiết món",
    buoc: ['1. Mở /dat-mon', '2. Bấm thêm món Cơm cháy chà bông kho quẹt'],
    ketQua: 'Giỏ hiện 1 món, thành tiền 135.000đ', loai: T, uuTien: C },
  { ma: 'TC-DISH-02', mucTieu: "Tăng số lượng thì thành tiền cập nhật",
    buoc: ['1. Thêm một món vào giỏ', '2. Tăng số lượng lên 2'],
    ketQua: 'Thành tiền dòng đó thành 270.000đ, tiền món tổng cập nhật theo', loai: T, uuTien: C },
  { ma: 'TC-DISH-03', mucTieu: 'Xóa món khỏi giỏ',
    buoc: ['1. Thêm hai món', '2. Xóa một món'],
    ketQua: 'Giỏ còn một món, tổng tiền tính lại đúng', loai: T, uuTien: C },
  { ma: 'TC-DISH-04', mucTieu: 'Giỏ hàng còn nguyên sau khi tải lại trang',
    buoc: ['1. Thêm hai món vào giỏ', '2. Nhấn F5'],
    ketQua: 'Giỏ vẫn còn đủ hai món, vì giỏ được lưu ở trình duyệt', loai: T, uuTien: TB },
  { ma: 'TC-DISH-05', mucTieu: "Đơn giao dưới mức miễn phí thì tính phí giao, VAT không tính trên phí giao",
    buoc: ['1. Thêm 2 phần Cơm cháy (270.000đ)', '2. Chọn hình thức Giao tận nhà'],
    ketQua: 'Phí giao 30.000đ; VAT 21.600đ (8% của tiền món, không tính trên phí giao); tổng 321.600đ', loai: T, uuTien: C },
  { ma: 'TC-DISH-06', mucTieu: "Đơn đạt mức thì miễn phí giao",
    buoc: ['1. Thêm 1 Combo nướng than hoa (690.000đ)', '2. Chọn Giao tận nhà'],
    ketQua: 'Phí giao 0đ kèm câu giải thích đơn đã đạt mức miễn phí; VAT 55.200đ; tổng 745.200đ', loai: T, uuTien: C },
  { ma: 'TC-DISH-07', mucTieu: "Gợi ý đặt thêm bao nhiêu để được miễn phí giao",
    buoc: ['1. Thêm món dưới 500.000đ', '2. Chọn Giao tận nhà'],
    ketQua: 'Hiện câu gợi ý còn thiếu bao nhiêu tiền nữa là được miễn phí giao, số tiền khớp với phần chênh lệch', loai: T, uuTien: Th },
  { ma: 'TC-DISH-08', mucTieu: 'Chuyển sang ăn tại chỗ thì mất phí giao',
    buoc: ['1. Đang ở hình thức Giao tận nhà có phí giao 30.000đ', '2. Chuyển sang Ăn tại chỗ'],
    ketQua: 'Dòng phí giao biến mất hoặc về 0đ, ô địa chỉ được thay bằng ô số khách, tổng tiền giảm đúng 30.000đ', loai: T, uuTien: C },
  { ma: 'TC-DISH-09', mucTieu: 'Gửi đơn giao tận nhà thành công',
    buoc: ['1. Giỏ 690.000đ', '2. Chọn Giao tận nhà, nhập địa chỉ, chọn giờ nhận sau 3 tiếng', '3. Gửi đơn'],
    ketQua: 'Gửi thành công, nhận mã đơn dạng DM-yyyyMMdd-xxxx', loai: T, uuTien: C },
  { ma: 'TC-DISH-10', mucTieu: "Gửi đơn ăn tại chỗ thành công, không có phí giao",
    buoc: ['1. Giỏ 270.000đ', '2. Chọn Ăn tại chỗ, nhập 4 khách, chọn giờ sau 3 tiếng', '3. Gửi đơn'],
    ketQua: 'Gửi thành công, nhận mã đơn, tổng tiền 291.600đ (không có phí giao)', loai: T, uuTien: C },
  { ma: 'TC-DISH-11', mucTieu: "Tra cứu đơn đặt món bằng mã",
    buoc: ['1. Mở /tra-cuu-mon', '2. Nhập mã đơn vừa tạo'],
    ketQua: 'Hiện danh sách món đã đặt, hình thức nhận, giờ nhận và tổng tiền đúng như lúc đặt', loai: T, uuTien: C },
  { ma: 'TC-DISH-12', mucTieu: "Giá trong đơn cũ không đổi khi giá món thay đổi",
    buoc: ["1. Quản trị tạo một món riêng giá 200.000đ, không dùng món mẫu để khỏi làm đổi giá thực đơn", "2. Gửi đơn giao tận nhà có món đó", "3. Quản trị đổi giá món sang 250.000đ", "4. Tra cứu lại đơn cũ ở /tra-cuu-mon"],
    ketQua: "Đơn cũ vẫn hiện thành tiền 200.000đ, vì hệ thống chụp lại giá tại thời điểm đặt.", loai: T, uuTien: TB },
]);

// ---------- 9 ----------
luong('DISHV', 'Ràng buộc khi đặt món', '/dat-mon',
  'Kiểm thử các trường hợp hệ thống phải từ chối đơn đặt món. Các ô bắt buộc và ô có giới hạn số được trình duyệt chặn ngay tại chỗ, nên kết quả mong đợi ghi theo giao diện, kèm câu báo của máy chủ.', [
  { ma: 'TC-DISHV-01', mucTieu: "Đơn giao dưới mức tối thiểu bị từ chối",
    buoc: ['1. Thêm món tổng 120.000đ', '2. Chọn Giao tận nhà', '3. Gửi đơn'],
    ketQua: 'Từ chối, thông báo "Đơn giao tận nhà tối thiểu 150.000đ. Đơn hiện tại mới 120.000đ."', loai: N, uuTien: C },
  { ma: 'TC-DISHV-02', mucTieu: "Giao tận nhà bỏ trống địa chỉ",
    buoc: ["1. Giỏ 2 phần Cơm cháy, chọn Giao tận nhà", "2. Điền đủ thông tin nhưng để trống địa chỉ", "3. Bấm gửi đơn"],
    ketQua: "Trình duyệt chặn không gửi form vì ô địa chỉ bắt buộc, câu nhắc hiện ngay tại ô địa chỉ, vẫn ở trang đặt món. Nếu gửi thẳng lên máy chủ thì bị từ chối với câu \"Đơn giao tận nhà cần địa chỉ nhận hàng\".", loai: N, uuTien: C },
  { ma: 'TC-DISHV-03', mucTieu: "Ăn tại chỗ bỏ trống số khách",
    buoc: ["1. Giỏ 2 phần Cơm cháy, chọn Ăn tại chỗ", "2. Điền đủ thông tin nhưng để trống số khách", "3. Bấm gửi đơn"],
    ketQua: "Trình duyệt chặn không gửi form vì ô số khách bắt buộc, câu nhắc hiện ngay tại ô số khách, vẫn ở trang đặt món. Nếu gửi thẳng lên máy chủ thì bị từ chối với câu \"Đơn ăn tại chỗ cần cho biết số khách\".", loai: N, uuTien: C },
  { ma: 'TC-DISHV-04', mucTieu: "Ăn tại chỗ vượt 40 khách",
    buoc: ["1. Giỏ 2 phần Cơm cháy, chọn Ăn tại chỗ", "2. Nhập 50 khách", "3. Bấm gửi đơn"],
    ketQua: "Trình duyệt chặn không gửi form vì ô số khách chỉ nhận tối đa 40, câu nhắc hiện ngay tại ô số khách, vẫn ở trang đặt món. Nếu gửi thẳng lên máy chủ thì bị từ chối với câu \"Đơn ăn tại chỗ nhận tối đa 40 khách. Đông hơn thì bạn đặt tiệc theo gói giúp mình nhé.\".", loai: N, uuTien: C },
  { ma: 'TC-DISHV-05', mucTieu: "Món tính giá theo cân không thêm vào giỏ được",
    buoc: ['1. Mở trang chi tiết Heo quay giòn bì', '2. Thử thêm vào giỏ'],
    ketQua: 'Nút thêm vào giỏ bị vô hiệu, hoặc nếu gửi lên máy chủ thì nhận thông báo món tính giá theo thực tế nên chưa đặt lẻ được, kèm hướng dẫn liên hệ hoặc đặt qua gói tiệc', loai: N, uuTien: C },
  { ma: 'TC-DISHV-06', mucTieu: "Đặt sớm hơn thời gian chuẩn bị bị từ chối",
    buoc: ['1. Thêm một món có thời gian chuẩn bị dài', '2. Chọn giờ nhận sau 30 phút', '3. Gửi đơn'],
    ketQua: 'Từ chối, thông báo sớm nhất nhận được lúc mấy giờ, kèm tên món cần bao nhiêu phút chuẩn bị', loai: N, uuTien: C },
  { ma: 'TC-DISHV-07', mucTieu: "Đặt quá 30 ngày bị từ chối",
    buoc: ['1. Giỏ hợp lệ', '2. Chọn giờ nhận sau 40 ngày', '3. Gửi đơn'],
    ketQua: 'Từ chối, thông báo "Chỉ nhận đơn trong vòng 30 ngày tới"', loai: N, uuTien: TB },
  { ma: 'TC-DISHV-08', mucTieu: "Giỏ rỗng thì không gửi đơn được",
    buoc: ['1. Giỏ không có món nào', '2. Bấm gửi đơn'],
    ketQua: 'Không gửi được, nút bị vô hiệu hoặc hiện nhắc phải chọn ít nhất một món', loai: N, uuTien: TB },
  { ma: 'TC-DISHV-09', mucTieu: "Món đã ngừng phục vụ thì bị từ chối",
    buoc: ['1. Quản trị ngừng phục vụ một món', '2. Khách gửi đơn có món đó trong giỏ cũ'],
    ketQua: 'Từ chối, thông báo món đó hiện đã ngừng phục vụ', loai: N, uuTien: TB },
]);

// ---------- 10 ----------
luong('REV', 'Gửi đánh giá', '/danh-gia',
  'Đánh giá bắt buộc phải kèm mã đơn đã tổ chức. Điểm từ 1 đến 5. Đánh giá mới cần quản trị duyệt mới hiện công khai.', [
  { ma: 'TC-REV-01', mucTieu: 'Gửi đánh giá với mã đơn hợp lệ',
    buoc: ['1. Mở /danh-gia', '2. Nhập mã đơn có thật, tên, chọn 5 sao, nhập nội dung', '3. Bấm gửi'],
    ketQua: 'Gửi thành công, hiện thông báo đánh giá đang chờ duyệt', loai: T, uuTien: C },
  { ma: 'TC-REV-02', mucTieu: "Đánh giá mới chưa hiện công khai khi chưa được duyệt",
    buoc: ['1. Gửi đánh giá thành công', '2. Tải lại trang /danh-gia'],
    ketQua: 'Đánh giá vừa gửi không xuất hiện trong danh sách công khai, vì còn chờ duyệt', loai: T, uuTien: C },
  { ma: 'TC-REV-03', mucTieu: "Mã đơn không tồn tại thì báo không tìm thấy",
    buoc: ['1. Nhập mã đơn VS-00000000-9999', '2. Bấm gửi'],
    ketQua: 'Từ chối, thông báo không tìm thấy đơn đặt tiệc tương ứng', loai: N, uuTien: C },
  { ma: 'TC-REV-04', mucTieu: "Bỏ trống mã đơn",
    buoc: ["1. Mở /danh-gia", "2. Để trống mã đơn, điền các trường còn lại", "3. Bấm gửi"],
    ketQua: "Trình duyệt chặn không gửi form vì ô mã đơn bắt buộc, câu nhắc hiện ngay tại ô mã đơn. Nếu gửi thẳng lên máy chủ thì bị từ chối với câu \"Vui lòng nhập mã đơn đã tổ chức\".", loai: N, uuTien: TB },
  { ma: 'TC-REV-05', mucTieu: "Bỏ trống nội dung đánh giá",
    buoc: ["1. Mở /danh-gia", "2. Điền đủ các trường, để trống nội dung", "3. Bấm gửi"],
    ketQua: "Trình duyệt chặn không gửi form vì ô nội dung bắt buộc, câu nhắc hiện ngay tại ô nội dung. Nếu gửi thẳng lên máy chủ thì bị từ chối với câu \"Vui lòng nhập nội dung\".", loai: N, uuTien: TB },
  { ma: 'TC-REV-06', mucTieu: "Chọn số sao",
    buoc: ["1. Mở /danh-gia", "2. Ở ô chọn số sao, chọn 4"],
    ketQua: "Ô chọn giữ giá trị 4, giá trị gửi lên máy chủ là 4. Giao diện dùng ô chọn số sao chứ không dùng hàng ngôi sao bấm được.", loai: T, uuTien: TB },
  { ma: 'TC-REV-07', mucTieu: 'Danh sách đánh giá công khai hiển thị',
    buoc: ['1. Mở /danh-gia'],
    ketQua: 'Hiện các đánh giá đã duyệt kèm tên khách, số sao, nội dung và ảnh đính kèm nếu có', loai: T, uuTien: TB },
]);

// ---------- 11 ----------
luong('BOT', 'Trợ lý tư vấn', 'Hộp thoại nổi trên mọi trang',
  'Chỉ kiểm thử nhánh trả lời dự phòng có sẵn trong hệ thống. Nhánh gọi mô hình ngôn ngữ ngoài nằm ngoài phạm vi vì kết quả không lặp lại được.', [
  { ma: 'TC-BOT-01', mucTieu: "Mở hộp thoại thấy lời chào và câu hỏi gợi ý",
    buoc: ['1. Mở trang chủ', '2. Bấm nút trợ lý'],
    ketQua: 'Hộp thoại mở ra, hiện lời chào và danh sách câu hỏi gợi ý', loai: T, uuTien: TB },
  { ma: 'TC-BOT-02', mucTieu: "Hỏi giờ mở cửa thì câu trả lời nhắc đúng giờ",
    buoc: ['1. Mở hộp thoại', '2. Nhập "Nhà hàng mở cửa mấy giờ"', '3. Gửi'],
    ketQua: 'Nhận được câu trả lời không rỗng trong vòng thời gian chờ, nội dung có nhắc giờ mở cửa 9:00 - 22:00', loai: T, uuTien: C },
  { ma: 'TC-BOT-03', mucTieu: "Bấm câu hỏi gợi ý thì tự gửi và nhận câu trả lời",
    buoc: ['1. Mở hộp thoại', '2. Bấm một câu hỏi gợi ý'],
    ketQua: 'Câu hỏi tự được gửi đi và nhận lại câu trả lời, không phải tự gõ', loai: T, uuTien: TB },
  { ma: 'TC-BOT-04', mucTieu: "Ô nhập trống thì nút gửi bị khóa",
    buoc: ['1. Bấm gửi khi ô nhập còn trống'],
    ketQua: 'Không gửi gì lên máy chủ, nút gửi bị vô hiệu', loai: N, uuTien: Th },
  { ma: 'TC-BOT-05', mucTieu: 'Đóng hộp thoại',
    buoc: ['1. Mở hộp thoại', '2. Bấm nút đóng'],
    ketQua: 'Hộp thoại đóng lại, nội dung trang phía sau không bị ảnh hưởng', loai: T, uuTien: Th },
]);

// ---------- 12 ----------
luong('PERM', 'Phân quyền', '/ho-so, /don-cua-toi, /quan-tri/*',
  'Ba vai trò: khách hàng, nhân viên, quản trị. Tài khoản quản trị mẫu: admin@vuonsen.vn. Đây là luồng kiểm thử trường hợp bị từ chối truy cập.', [
  { ma: 'TC-PERM-01', mucTieu: 'Khách chưa đăng nhập bị chặn khỏi trang hồ sơ',
    buoc: ['1. Đăng xuất', '2. Mở thẳng /ho-so'],
    ketQua: 'Bị chuyển về trang đăng nhập, không thấy nội dung hồ sơ', loai: N, uuTien: C },
  { ma: 'TC-PERM-02', mucTieu: 'Khách chưa đăng nhập bị chặn khỏi khu quản trị',
    buoc: ['1. Đăng xuất', '2. Mở thẳng /quan-tri/don-dat-tiec'],
    ketQua: 'Bị chuyển về trang đăng nhập, không thấy danh sách đơn', loai: N, uuTien: C },
  { ma: 'TC-PERM-03', mucTieu: 'Tài khoản khách hàng bị chặn khỏi khu quản trị',
    buoc: ['1. Đăng nhập bằng tài khoản khách hàng', '2. Mở /quan-tri/don-dat-tiec'],
    ketQua: 'Bị từ chối, không thấy dữ liệu đơn của người khác. Đây là kịch bản quan trọng nhất của luồng phân quyền', loai: N, uuTien: C },
  { ma: 'TC-PERM-04', mucTieu: 'Tài khoản quản trị vào được khu quản trị',
    buoc: ['1. Đăng nhập bằng admin@vuonsen.vn', '2. Mở /quan-tri/don-dat-tiec'],
    ketQua: 'Vào được, thấy danh sách đơn đặt tiệc', loai: T, uuTien: C },
  { ma: 'TC-PERM-05', mucTieu: "Khách hàng không thấy menu quản trị",
    buoc: ['1. Đăng nhập bằng tài khoản khách hàng', '2. Xem thanh điều hướng'],
    ketQua: 'Không có liên kết nào dẫn vào khu quản trị', loai: N, uuTien: TB },
  { ma: 'TC-PERM-06', mucTieu: 'Khách chỉ xem được đơn của chính mình',
    buoc: ['1. Đăng nhập tài khoản A', '2. Mở /don-cua-toi'],
    ketQua: 'Chỉ hiện đơn do tài khoản A tạo, không lẫn đơn của tài khoản khác', loai: T, uuTien: C },
  { ma: 'TC-PERM-07', mucTieu: "Mất phiên đăng nhập thì bị đưa về trang đăng nhập",
    buoc: ['1. Đăng nhập', '2. Xóa thẻ phiên ở trình duyệt', '3. Mở /ho-so'],
    ketQua: 'Bị chuyển về trang đăng nhập', loai: N, uuTien: TB },
]);

// ---------- 13 ----------
luong('ADMIN', 'Quản trị đơn', '/quan-tri/don-dat-tiec, /quan-tri/danh-gia',
  'Chuỗi trạng thái đơn: Chờ xác nhận sang Đã xác nhận hoặc Đã hủy; Đã xác nhận sang Đã hoàn thành hoặc Đã hủy; hai trạng thái cuối không chuyển đi đâu nữa.', [
  { ma: 'TC-ADMIN-01', mucTieu: 'Xem danh sách đơn đặt tiệc',
    buoc: ['1. Đăng nhập quản trị', '2. Mở /quan-tri/don-dat-tiec'],
    ketQua: 'Hiện bảng đơn kèm mã đơn, khách hàng, ngày tổ chức, tổng tiền và trạng thái', loai: T, uuTien: C },
  { ma: 'TC-ADMIN-02', mucTieu: "Lọc đơn theo trạng thái chờ xác nhận",
    buoc: ['1. Mở danh sách đơn', '2. Lọc theo trạng thái Chờ xác nhận'],
    ketQua: 'Chỉ còn các đơn đang ở trạng thái Chờ xác nhận', loai: T, uuTien: TB },
  { ma: 'TC-ADMIN-03', mucTieu: 'Xác nhận đơn',
    buoc: ['1. Mở một đơn Chờ xác nhận', '2. Chuyển sang Đã xác nhận'],
    ketQua: 'Trạng thái đổi thành Đã xác nhận, danh sách cập nhật theo, lịch sử trạng thái ghi thêm một dòng', loai: T, uuTien: C },
  { ma: 'TC-ADMIN-04', mucTieu: 'Hoàn thành đơn đã xác nhận',
    buoc: ['1. Mở một đơn Đã xác nhận', '2. Chuyển sang Đã hoàn thành'],
    ketQua: 'Trạng thái đổi thành Đã hoàn thành', loai: T, uuTien: TB },
  { ma: 'TC-ADMIN-05', mucTieu: "Đơn đã hủy không còn thao tác chuyển trạng thái nào",
    buoc: ['1. Mở một đơn Đã hủy', '2. Thử chuyển sang Đã xác nhận'],
    ketQua: 'Không cho chuyển. Giao diện ẩn lựa chọn đó, hoặc máy chủ trả thông báo không thể chuyển đơn từ trạng thái này sang trạng thái kia', loai: N, uuTien: C },
  { ma: 'TC-ADMIN-06', mucTieu: "Đơn đã xác nhận thì chiếm chỗ, đơn chờ xác nhận thì không",
    buoc: ["1. Tạo một đơn Sảnh Sen Vàng, Gói Đồng Quê, ngày Y buổi tối", "2. Khi đơn còn Chờ xác nhận, gửi thêm một đơn trùng không gian, ngày, buổi", "3. Quản trị xác nhận đơn đầu trên giao diện", "4. Gửi lại một đơn trùng"],
    ketQua: "Bước 2: đơn trùng vẫn được nhận vì đơn chờ xác nhận chưa chiếm chỗ. Bước 4: bị từ chối, câu báo có cụm \"đã có tiệc\". Việc gửi đơn ở bước 1, 2, 4 làm qua API để kịch bản tập trung vào thao tác xác nhận của quản trị.", loai: T, uuTien: C },
  { ma: 'TC-ADMIN-07', mucTieu: "Duyệt đánh giá thì đánh giá hiện công khai",
    buoc: ['1. Mở /quan-tri/danh-gia', '2. Duyệt một đánh giá đang chờ', '3. Mở trang /danh-gia công khai'],
    ketQua: 'Đánh giá vừa duyệt xuất hiện trong danh sách công khai', loai: T, uuTien: C },
  { ma: 'TC-ADMIN-08', mucTieu: "Quản trị thêm món mới thì món hiện ở thực đơn công khai",
    buoc: ['1. Mở /quan-tri/thuc-don', '2. Thêm một món mới', '3. Mở trang /thuc-don'],
    ketQua: 'Món mới xuất hiện đúng danh mục ở trang thực đơn công khai', loai: T, uuTien: TB },
]);

// ---------- 14 ----------
luong('HOL', 'Giảm giá ngày lễ', '/dat-tiec, /dat-mon, /tra-cuu-mon, hộp thoại trợ lý',
  "Bổ sung ngày 17/09/2026. Quy tắc: ngày tổ chức tiệc hoặc ngày nhận món rơi vào dịp lễ đang bật thì giảm theo mức của dịp đó; tiệc giảm trên tiền ăn cộng phí thuê, đơn món giảm trên tiền món; VAT tính sau khi giảm; không cộng dồn với giảm đặt sớm 5%, lấy mức cao hơn; mức miễn phí giao hàng xét trên tiền món trước khi giảm. Dịp lễ dùng trong kịch bản được tạo qua API quản trị vào ngày không trùng dịp lễ mẫu, tên bắt đầu bằng \"KT \" và bị xóa trước lẫn sau mỗi kịch bản. Tiệc chuẩn: 200 khách, buổi tối, Sảnh Sen Vàng, Gói Sen Vàng; chưa giảm là 93.000.000 trước thuế, tổng 100.440.000.", [
  { ma: 'TC-HOL-01', mucTieu: "Tiệc rơi vào dịp lễ khi chưa đủ ngày đặt sớm thì giảm theo mức dịp lễ",
    buoc: ["1. Tạo dịp lễ 15% kéo dài 2 ngày, bắt đầu khoảng 20 ngày tới", "2. Báo giá tiệc chuẩn vào ngày đầu của dịp lễ"],
    ketQua: "Giảm 13.950.000 (15% của 93.000.000); VAT 6.324.000 (8% của 79.050.000); tổng 85.374.000; cọc 25.612.200. Bảng tạm tính ghi \"Giảm 15% dịp <tên dịp>\", không có câu không cộng dồn vì chưa đủ ngày đặt sớm.", loai: T, uuTien: C },
  { ma: 'TC-HOL-02', mucTieu: "Tiệc vừa đặt sớm vừa trùng dịp lễ thì chỉ lấy mức cao hơn, không cộng dồn",
    buoc: ["1. Tạo dịp lễ 20% vào một ngày cách hôm nay hơn 70 ngày", "2. Báo giá tiệc chuẩn vào ngày đó"],
    ketQua: "Giảm 18.600.000, tức chỉ 20% chứ không phải 20% cộng 5%; VAT 5.952.000; tổng 80.352.000. Bảng tạm tính ghi \"Giảm 20% dịp <tên dịp>\" và câu ưu đãi đặt sớm và ưu đãi dịp lễ không cộng dồn, không có dòng giảm do đặt trước.", loai: T, uuTien: C },
  { ma: 'TC-HOL-03', mucTieu: "Ngày cuối của dịp lễ vẫn được giảm",
    buoc: ["1. Tạo dịp lễ 15% kéo dài 2 ngày", "2. Báo giá tiệc chuẩn vào ngày thứ hai của dịp"],
    ketQua: "Vẫn giảm 13.950.000, vì khoảng ngày của dịp lễ tính cả ngày kết thúc.", loai: T, uuTien: TB },
  { ma: 'TC-HOL-04', mucTieu: "Ngày ngay sau dịp lễ không được giảm",
    buoc: ["1. Tạo dịp lễ 15% kéo dài 2 ngày", "2. Báo giá tiệc chuẩn vào ngày ngay sau ngày kết thúc"],
    ketQua: "Không có dòng giảm giá, tổng 100.440.000.", loai: N, uuTien: TB },
  { ma: 'TC-HOL-05', mucTieu: "Dịp lễ đang tắt thì tiệc không được giảm",
    buoc: ["1. Tạo dịp lễ 15% ở trạng thái tắt", "2. Báo giá tiệc chuẩn vào ngày đó"],
    ketQua: "Không có dòng giảm giá, tổng 100.440.000.", loai: N, uuTien: C },
  { ma: 'TC-HOL-06', mucTieu: "Đặt món nhận vào dịp lễ thì bảng tạm tính có dòng giảm giá, VAT tính sau giảm",
    buoc: ["1. Tạo dịp lễ 10% trong vài ngày tới", "2. Giỏ 2 phần Cơm cháy (270.000đ), chọn Ăn tại chỗ, giờ nhận vào ngày thường", "3. Đổi giờ nhận sang 12:00 ngày lễ"],
    ketQua: "Tiền món 270.000; giảm 27.000; VAT 19.440 (8% của 243.000); tổng 262.440; ghi chú \"Giảm 10% dịp <tên dịp>.\"", loai: T, uuTien: C },
  { ma: 'TC-HOL-07', mucTieu: "Đơn giao đạt mức miễn phí giao trước khi giảm thì vẫn được miễn phí giao",
    buoc: ["1. Tạo dịp lễ 10% trong vài ngày tới", "2. Giỏ 4 phần Cơm cháy (540.000đ), chọn Giao tận nhà", "3. Chọn giờ nhận vào ngày lễ"],
    ketQua: "Giảm 54.000, tiền món còn 486.000 dưới mức 500.000 nhưng phí giao vẫn là Miễn phí, vì mức miễn phí xét trên tiền món trước khi giảm; VAT 38.880; tổng 524.880.", loai: T, uuTien: TB },
  { ma: 'TC-HOL-08', mucTieu: "Đổi giờ nhận từ dịp lễ sang ngày thường thì mất dòng giảm giá",
    buoc: ["1. Giỏ 2 phần Cơm cháy, Ăn tại chỗ, giờ nhận vào ngày lễ 10% (bảng có giảm 27.000)", "2. Đổi giờ nhận sang ngày hôm sau"],
    ketQua: "Dòng giảm giá và ghi chú giảm giá biến mất; tổng về 291.600 (270.000 cộng VAT 21.600).", loai: T, uuTien: TB },
  { ma: 'TC-HOL-09', mucTieu: "Gửi đơn món nhận vào dịp lễ thì trang tra cứu hiện dòng giảm giá và tổng đã giảm",
    buoc: ["1. Giỏ 2 phần Cơm cháy, Ăn tại chỗ, 4 khách, giờ nhận 12:00 vào ngày lễ 10%", "2. Điền thông tin và gửi đơn", "3. Xem trang tra cứu mở ra sau khi gửi"],
    ketQua: "Chuyển sang /tra-cuu-mon; đơn hiện giảm giá 27.000 và tổng cộng 262.440.", loai: T, uuTien: C },
  { ma: 'TC-HOL-10', mucTieu: "Trợ lý tư vấn báo được ưu đãi dịp lễ sắp tới lấy từ trang quản trị",
    buoc: ["1. Tạo dịp lễ 18% vào ngày mai", "2. Mở /thuc-don, mở trợ lý tư vấn", "3. Hỏi \"Dịp lễ có giảm giá không?\""],
    ketQua: "Câu trả lời có tên dịp lễ vừa tạo, mức 18% và nói rõ không cộng dồn với ưu đãi đặt sớm.", loai: T, uuTien: TB },
]);

// ---------- 15 ----------
luong('HADM', 'Quản trị ngày lễ', '/quan-tri/ngay-le',
  "Bổ sung ngày 17/09/2026. Trang chỉ dành cho quản trị: thêm, sửa mức giảm, bật tắt, xóa dịp lễ. Mức giảm chỉ nhận từ 10% đến 20%, ngày kết thúc không được trước ngày bắt đầu. Đơn đã đặt giữ nguyên số tiền đã chốt dù dịp lễ bị sửa hay xóa. Dịp lễ tạo trong kịch bản có tiền tố \"KT \" và bị xóa sau kịch bản.", [
  { ma: 'TC-HADM-01', mucTieu: "Quản trị xem danh sách dịp lễ, có đủ dịp lễ mẫu năm 2028",
    buoc: ["1. Đăng nhập quản trị", "2. Mở /quan-tri/ngay-le"],
    ketQua: "Bảng có dòng Tết Nguyên Đán 26/1/2028 – 28/1/2028 mức 20% và dòng Giỗ Tổ Hùng Vương 4/4/2028 mức 10%, lấy từ dữ liệu mẫu.", loai: T, uuTien: C },
  { ma: 'TC-HADM-02', mucTieu: "Thêm dịp lễ hợp lệ thì dịp hiện trong bảng với mức giảm và trạng thái Sắp tới",
    buoc: ["1. Bấm Thêm dịp lễ", "2. Nhập tên, ngày bắt đầu khoảng 40 ngày tới, ngày kết thúc sau đó 1 ngày, mức 12", "3. Bấm Lưu"],
    ketQua: "Dòng mới hiện đúng khoảng ngày, mức 12%, trạng thái Sắp tới.", loai: T, uuTien: C },
  { ma: 'TC-HADM-03', mucTieu: "Dịp lễ vừa thêm được áp dụng ngay khi khách báo giá tiệc",
    buoc: ["1. Quản trị thêm dịp lễ 15% vào một ngày khoảng 20 ngày tới", "2. Báo giá tiệc chuẩn vào ngày đó"],
    ketQua: "Giảm 13.950.000 (15% của 93.000.000), bảng tạm tính ghi \"Giảm 15% dịp <tên dịp>\". Không cần khởi động lại máy chủ.", loai: T, uuTien: C },
  { ma: 'TC-HADM-04', mucTieu: "Mức giảm trên 20% bị chặn, dịp lễ không được lưu",
    buoc: ["1. Bấm Thêm dịp lễ", "2. Nhập mức 25", "3. Bấm Lưu"],
    ketQua: "Trình duyệt chặn tại ô mức giảm, form vẫn mở, dịp lễ không được lưu. Nếu gửi thẳng lên máy chủ thì bị từ chối với câu \"Mức giảm tối đa là 20%\".", loai: N, uuTien: C },
  { ma: 'TC-HADM-05', mucTieu: "Mức giảm dưới 10% bị chặn, dịp lễ không được lưu",
    buoc: ["1. Bấm Thêm dịp lễ", "2. Nhập mức 5", "3. Bấm Lưu"],
    ketQua: "Trình duyệt chặn tại ô mức giảm, dịp lễ không được lưu. Nếu gửi thẳng lên máy chủ thì bị từ chối với câu \"Mức giảm tối thiểu là 10%\".", loai: N, uuTien: C },
  { ma: 'TC-HADM-06', mucTieu: "Ngày kết thúc trước ngày bắt đầu bị chặn, dịp lễ không được lưu",
    buoc: ["1. Bấm Thêm dịp lễ", "2. Chọn ngày kết thúc trước ngày bắt đầu 2 ngày, mức 15", "3. Bấm Lưu"],
    ketQua: "Trình duyệt chặn tại ô ngày kết thúc, dịp lễ không được lưu. Nếu gửi thẳng lên máy chủ thì bị từ chối với câu \"Ngày kết thúc không được trước ngày bắt đầu\".", loai: N, uuTien: TB },
  { ma: 'TC-HADM-07', mucTieu: "Sửa mức giảm thì bảng và dữ liệu cập nhật mức mới",
    buoc: ["1. Có sẵn một dịp lễ mức 10%", "2. Bấm Sửa, đổi mức sang 18, bấm Lưu"],
    ketQua: "Bảng hiện mức 18%, dữ liệu lưu tỉ lệ 0,18.", loai: T, uuTien: TB },
  { ma: 'TC-HADM-08', mucTieu: "Tắt Đang áp dụng thì dịp chuyển sang Đã tắt và khách không còn được giảm",
    buoc: ["1. Có sẵn dịp lễ 15% đang bật, khoảng 20 ngày tới", "2. Bấm Sửa, bỏ chọn Đang áp dụng, bấm Lưu", "3. Báo giá tiệc chuẩn vào ngày đó"],
    ketQua: "Trạng thái dòng thành Đã tắt; bảng tạm tính không có dòng giảm giá, tổng 100.440.000.", loai: T, uuTien: C },
  { ma: 'TC-HADM-09', mucTieu: "Xóa dịp lễ thì dịp biến mất khỏi bảng và dữ liệu",
    buoc: ["1. Có sẵn một dịp lễ", "2. Bấm Xóa, đồng ý ở hộp xác nhận"],
    ketQua: "Dòng biến mất khỏi bảng và không còn trong dữ liệu.", loai: T, uuTien: TB },
  { ma: 'TC-HADM-10', mucTieu: "Đơn đặt món đã gửi giữ nguyên tiền giảm sau khi xóa dịp lễ",
    buoc: ["1. Tạo dịp lễ 10%", "2. Gửi đơn giao tận nhà 1 Combo nướng than hoa (690.000đ) nhận vào ngày lễ", "3. Quản trị xóa dịp lễ", "4. Tra cứu lại đơn"],
    ketQua: "Đơn vẫn hiện giảm 69.000 và tổng 670.680 (còn 621.000, miễn phí giao, VAT 49.680), vì số tiền đã chốt lúc đặt.", loai: T, uuTien: C },
  { ma: 'TC-HADM-11', mucTieu: "Tài khoản khách hàng không vào được trang quản trị ngày lễ",
    buoc: ["1. Đăng nhập bằng tài khoản khách hàng", "2. Mở /quan-tri/ngay-le"],
    ketQua: "Hiện màn hình Không đủ quyền truy cập, không thấy danh sách dịp lễ.", loai: N, uuTien: C },
  { ma: 'TC-HADM-12', mucTieu: "Khách chưa đăng nhập bị chuyển sang trang đăng nhập",
    buoc: ["1. Chưa đăng nhập", "2. Mở /quan-tri/ngay-le"],
    ketQua: "Bị chuyển về trang đăng nhập.", loai: N, uuTien: C },
]);

// ============================================================
// THONG KE TU DONG
// ============================================================
const tongTC = LUONG.reduce((s, l) => s + l.ds.length, 0);
const dem = (loc) => LUONG.reduce((s, l) => s + l.ds.filter(loc).length, 0);
const soThuan = dem((t) => t.loai === T);
const soNghich = dem((t) => t.loai === N);
const soCao = dem((t) => t.uuTien === C);
const soTB = dem((t) => t.uuTien === TB);
const soThap = dem((t) => t.uuTien === Th);

const bangTongHop = LUONG.map((l, i) => [
  String(i + 1),
  l.ten,
  l.ma,
  l.lop,
  String(l.ds.length),
  String(l.ds.filter((t) => t.loai === T).length),
  String(l.ds.filter((t) => t.loai === N).length),
  String(l.ds.filter((t) => t.uuTien === C).length),
]);
bangTongHop.push(['', 'Tổng cộng', '', '', String(tongTC), String(soThuan), String(soNghich), String(soCao)]);

// ============================================================
// DUNG TAI LIEU
// ============================================================
const noiDung = [
  p('DANH SÁCH VÀ ĐẶC TẢ TEST CASE', { center: true, bold: true, size: 30, after: 80 }),
  p('Đề tài kiểm thử tự động ứng dụng web bằng Selenium WebDriver', { center: true, italic: true, size: 24, after: 160 }),
  p('Sinh viên thực hiện: Phạm Trần Tuấn Anh — MSSV: 21130004 — Lớp: DH21DTA', { center: true, size: 22, after: 40 }),
  p('Giảng viên hướng dẫn: TS. Nguyễn Thị Phương Trâm', { center: true, size: 22, after: 40 }),
  p('Mốc kế hoạch: 08/09/2026 – 15/09/2026 — Ngày viết: 12/09/2026 — Cập nhật: 17/09/2026', { center: true, size: 22, after: 260 }),

  // ---- 1 ----
  h1('1. Mục đích tài liệu'),
  p('Tài liệu này đặc tả ' + tongTC + ' trường hợp kiểm thử cho ' + LUONG.length + ' luồng nghiệp vụ đã khoanh ở mốc trước, làm đầu vào trực tiếp cho việc viết mã kiểm thử Selenium ở các mốc sau.'),
  p('Mỗi trường hợp được viết đủ chi tiết để người khác đọc vào là thực hiện lại được bằng tay, đồng thời đủ rõ ràng để chuyển thẳng thành một phương thức kiểm thử. Toàn bộ con số trong cột kết quả mong đợi đều đối chiếu với mã nguồn thật của website chứ không phỏng đoán: tham số nghiệp vụ lấy từ tệp cấu hình application.yml, công thức tính tiền lấy từ PricingService và DishOrderPricing, thông báo lỗi lấy nguyên văn từ các lớp dịch vụ và từ giao diện, dữ liệu mẫu lấy từ các tệp migration.'),
  p('Việc đối chiếu này quan trọng vì một kết quả mong đợi viết sai sẽ làm kịch bản trượt trong khi website vẫn đúng, dẫn tới kết luận sai trong báo cáo thực nghiệm.'),
  p('Bản cập nhật ngày 17/09/2026 đối chiếu lại toàn bộ tài liệu với mã kiểm thử đã viết và giao diện thật: bổ sung 22 trường hợp của hai luồng giảm giá ngày lễ, sửa kết quả mong đợi của 21 trường hợp cho đúng cách giao diện chặn lỗi, thống nhất tên trường hợp với tên trong mã. Chi tiết ở mục 8.'),

  // ---- 2 ----
  h1('2. Quy ước'),
  h2('2.1. Mã trường hợp kiểm thử'),
  p('Mã có dạng TC-<mã luồng>-<số thứ tự hai chữ số>. Mã luồng viết tắt tên luồng nghiệp vụ, ví dụ TC-BOOK-04 là trường hợp thứ tư của luồng đặt tiệc. Trong mã kiểm thử, mã được viết lại thành tiền tố tên phương thức, ví dụ TC-BOOK-04 ứng với phương thức tcBook04_... trong lớp KiemThuDatTiec, chạy riêng được bằng lệnh mvn test "-Dtest=KiemThuDatTiec#tcBook04*". Tên lớp kịch bản của từng luồng ghi ở đầu mỗi mục 4.x và ở bảng tổng hợp mục 5.'),

  h2('2.2. Phân loại'),
  bang([1500, 7526], ['Loại', 'Ý nghĩa'], [
    ['Thuận', 'Kiểm thử đường đi bình thường: người dùng thao tác đúng và hệ thống phải cho kết quả đúng.'],
    ['Nghịch', 'Kiểm thử trường hợp phải bị từ chối: dữ liệu sai, vi phạm ràng buộc, hoặc truy cập không được phép. Đây là phần dễ bị bỏ sót nhưng lại hay có lỗi nhất.'],
  ]),

  h2('2.3. Mức ưu tiên'),
  bang([1500, 7526], ['Mức', 'Ý nghĩa'], [
    ['Cao', 'Luồng chính hoặc ràng buộc mà hỏng thì thiệt hại lớn, ví dụ tính sai tiền hoặc lọt phân quyền. Phải tự động hóa trước.'],
    ['TB', 'Chức năng quan trọng nhưng hỏng thì còn xoay xở được. Tự động hóa ở đợt hai.'],
    ['Thấp', 'Chi tiết giao diện hoặc trường hợp hiếm. Tự động hóa nếu còn thời gian.'],
  ]),

  // ---- 3 ----
  h1('3. Môi trường và dữ liệu kiểm thử'),
  h2('3.1. Môi trường'),
  bang([2400, 6626], ['Thành phần', 'Cấu hình khi kiểm thử'], [
    ['Trình duyệt', 'Chrome bản mới nhất, đổi sang Edge bằng tham số -Dbrowser=edge. Chạy có giao diện khi phát triển kịch bản; chạy ẩn bằng -Dheadless=true hoặc khi thực thi trên GitHub Actions. Tham số -Ddemo=true làm chậm từng thao tác và tô viền phần tử để quay video trình diễn.'],
    ['Địa chỉ ứng dụng', 'http://localhost:5173 cho giao diện, http://localhost:8080 cho máy chủ.'],
    ['Cơ sở dữ liệu', 'Chạy trên backend đang bật: profile dev dùng H2 trong bộ nhớ, profile local dùng MySQL. Không dựng lại cơ sở dữ liệu mỗi lượt chạy; để kết quả vẫn lặp lại được, ngày đặt tiệc được dò qua API để không trùng đơn cũ, dịp lễ thử mang tiền tố KT và bị xóa trước, sau mỗi kịch bản, các kịch bản so số tiền tự chọn ngày không trùng dịp lễ, và không kịch bản nào đếm tổng số bản ghi.'],
    ['Kích thước cửa sổ', 'Cố định 1920x1080 để locator không phụ thuộc cách sắp xếp đáp ứng theo bề rộng.'],
  ]),

  h2('3.2. Tài khoản dùng trong kiểm thử'),
  bang([2600, 2600, 3826], ['Vai trò', 'Tài khoản', 'Dùng cho luồng nào'], [
    ['Khách vãng lai', 'Không đăng nhập', 'Xem không gian, thực đơn, đặt tiệc, đặt món, tra cứu, đánh giá'],
    ['Khách hàng', 'Tạo mới qua API cho từng kịch bản cần đăng nhập', 'Hồ sơ cá nhân, đơn của tôi, kiểm thử bị chặn khỏi khu quản trị và trang quản trị ngày lễ'],
    ['Quản trị', 'admin@vuonsen.vn', 'Duyệt đơn, duyệt đánh giá, quản lý thực đơn, không gian và dịp lễ'],
    ['Nhân viên', 'Chưa có tài khoản trong dữ liệu mẫu', 'Cần tạo trước khi kiểm thử phân quyền mức nhân viên'],
  ]),

  h2('3.3. Dữ liệu mẫu về không gian'),
  bang([2100, 1500, 1700, 3726], ['Không gian', 'Sức chứa', 'Phí thuê', 'Ghi chú dùng trong kiểm thử'], [
    ['Sảnh Ven Sông', '300 - 800', '15.000.000đ', 'Dùng kiểm thử mức mâm tối thiểu 30 mâm'],
    ['Sảnh Sen Vàng', '200 - 500', '12.000.000đ', 'Không gian chuẩn dùng cho hầu hết kịch bản tính tiền'],
    ['Nhà Rường Gỗ', '20 - 60', '3.000.000đ', 'Dùng kiểm thử vượt sức chứa'],
    ['Phòng Hội Nghị Lúa', '40 - 150', '6.000.000đ', 'Dùng kiểm thử bộ lọc'],
    ['Cụm Chòi Sen', '8 - 144', '500.000đ mỗi chòi', 'Dùng kiểm thử cách tính phí theo đơn vị chòi'],
    ['Vườn Cau', '60 - 150', '7.000.000đ', 'Dùng kiểm thử bộ lọc'],
  ]),

  h2('3.4. Dữ liệu mẫu về gói tiệc'),
  bang([2400, 2000, 1400, 3226], ['Gói tiệc', 'Giá mỗi mâm', 'Số món', 'Thời lượng'], [
    ['Gói Đồng Quê', '2.900.000đ', '7', '3 tiếng'],
    ['Gói Sen Vàng', '4.500.000đ', '8', '5 tiếng'],
    ['Gói Thượng Uyển', '6.800.000đ', '10', '12 tiếng'],
  ]),

  h2('3.5. Tham số nghiệp vụ'),
  p('Đây là các con số quyết định kết quả mong đợi. Nếu sau này đổi cấu hình thì phải rà lại các trường hợp kiểm thử có dùng tới.'),
  bang([4000, 1600, 3426], ['Tham số', 'Giá trị', 'Ảnh hưởng tới trường hợp nào'], [
    ['Số khách mỗi mâm', '10', 'TC-BOOK-02, TC-BOOK-03'],
    ['Thuế giá trị gia tăng', '8%', 'Mọi trường hợp tính tiền'],
    ['Tỉ lệ đặt cọc', '30%', 'TC-BOOK-04'],
    ['Số khách tối thiểu và tối đa', '10 - 800', 'TC-BOOKV-02, TC-BOOKV-03'],
    ['Số ngày báo trước, tiệc thường', '3 ngày', 'TC-BOOKV-04, TC-BOOKV-06'],
    ['Ngưỡng tiệc lớn', 'Từ 20 mâm', 'TC-BOOKV-05, TC-BOOKV-06'],
    ['Số ngày báo trước, tiệc lớn', '7 ngày', 'TC-BOOKV-05'],
    ['Đặt sớm được giảm', 'Từ 60 ngày, giảm 5%', 'TC-BOOK-08, TC-BOOK-09'],
    ['Hệ số doanh thu tối thiểu', '10 lần phí thuê', 'TC-BOOK-04, TC-BOOK-07'],
    ['Phí giao món', '30.000đ', 'TC-DISH-05'],
    ['Miễn phí giao từ', '500.000đ', 'TC-DISH-06'],
    ['Đơn giao tối thiểu', '150.000đ', 'TC-DISHV-01'],
    ['Số khách tối đa ăn tại chỗ', '40', 'TC-DISHV-04'],
    ['Đặt món trước tối thiểu', '2 tiếng', 'TC-DISHV-06'],
    ['Đặt món xa nhất', '30 ngày', 'TC-DISHV-07'],
    ['Thời lượng buổi', 'Sáng 4 tiếng, trưa 5 tiếng, tối 5 tiếng', 'TC-BOOKV-07'],
    ['Mức giảm dịp lễ', 'Từ 10% đến 20%', 'Luồng 14, TC-HADM-04, TC-HADM-05'],
    ['Giảm dịp lễ và giảm đặt sớm', 'Không cộng dồn, lấy mức cao hơn', 'TC-HOL-02'],
  ]),

  h2('3.6. Dữ liệu mẫu về dịp lễ'),
  p('Migration V13 chèn sẵn 15 dịp nghỉ lễ chính thức. Ngày Tết Nguyên Đán và Giỗ Tổ Hùng Vương đã đổi từ âm lịch sang dương lịch.'),
  bang([3600, 2800, 2626], ['Dịp lễ', 'Ngày 2026 / 2027 / 2028', 'Mức giảm'], [
    ['Tết Dương lịch', '1/1 mỗi năm', '10%'],
    ['Tết Nguyên Đán', '17-19/2/2026; 6-8/2/2027; 26-28/1/2028', '20%'],
    ['Giỗ Tổ Hùng Vương', '26/4/2026; 16/4/2027; 4/4/2028', '10%'],
    ['Giải phóng miền Nam và Quốc tế Lao động', '30/4 - 1/5 mỗi năm', '15%'],
    ['Quốc khánh', '2/9 mỗi năm', '15%'],
  ]),

  // ---- 4 ----
  h1('4. Đặc tả trường hợp kiểm thử'),
];

LUONG.forEach((l, i) => {
  noiDung.push(h2('4.' + (i + 1) + '. Luồng ' + (i + 1) + ': ' + l.ten));
  noiDung.push(p('Đường dẫn: ' + l.url + '. Lớp kịch bản: ' + l.lop, { size: 21, italic: true, after: 60 }));
  noiDung.push(p('Tiền điều kiện: ' + l.tienDieuKien, { size: 21, after: 100 }));
  noiDung.push(bangTC(l.ds));
  noiDung.push(p('', { after: 80 }));
});

// ---- 5 ----
noiDung.push(h1('5. Tổng hợp'));
noiDung.push(p('Bảng dưới đây được tính tự động từ chính phần đặc tả ở mục 4, nên luôn khớp khi bổ sung hoặc bớt trường hợp kiểm thử.'));
noiDung.push(bang(
  [450, 2150, 800, 2226, 800, 800, 900, 900],
  ['STT', 'Luồng nghiệp vụ', 'Mã', 'Lớp kịch bản', 'Số test case', 'Thuận', 'Nghịch', 'Ưu tiên cao'],
  bangTongHop,
));

noiDung.push(p(''));
noiDung.push(p('Nhận xét về cơ cấu:', { bold: true, after: 80 }));
noiDung.push(bullet('Tổng cộng ' + tongTC + ' trường hợp kiểm thử trên ' + LUONG.length + ' luồng nghiệp vụ.'));
noiDung.push(bullet('Trường hợp thuận ' + soThuan + ', trường hợp nghịch ' + soNghich + '. Tỉ lệ nghịch chiếm khoảng ' + Math.round(soNghich * 100 / tongTC) + '%, đây là chủ ý: phần lớn lỗi thực tế nằm ở nhánh xử lý sai và nhánh bị từ chối, không nằm ở đường đi thuận lợi.'));
noiDung.push(bullet('Ưu tiên cao ' + soCao + ', trung bình ' + soTB + ', thấp ' + soThap + '. Kế hoạch tự động hóa sẽ làm hết nhóm ưu tiên cao trước, vì đây là nhóm phải có để bảo vệ được đề tài.'));
noiDung.push(bullet('Hai luồng nặng nhất là đặt tiệc và đặt món lẻ, cộng cả phần ràng buộc thì chiếm khoảng một nửa số trường hợp. Đúng với thực tế: đây là hai luồng có công thức tính tiền và nhiều điều kiện từ chối nhất.'));

// ---- 6 ----
noiDung.push(h1('6. Việc phải chuẩn bị trước khi viết mã kiểm thử'));
noiDung.push(p('Bốn việc dưới đây phát sinh trong lúc đặc tả ngày 12/09. Cột cuối ghi tình trạng tại lần cập nhật 17/09.'));

noiDung.push(bang([500, 2000, 4126, 2400], ['STT', 'Việc', 'Lý do và hướng xử lý', 'Tình trạng 17/09'], [
  ['1', 'Bổ sung thuộc tính data-test vào giao diện',
    'Hiện các phần tử chưa có thuộc tính đánh dấu riêng cho kiểm thử. Nếu bám vào lớp CSS thì đổi giao diện là hỏng hàng loạt kịch bản. Cần thêm data-test cho các ô nhập, nút bấm, thẻ và các dòng của bảng tạm tính.', 'Đã làm: gắn data-test cho toàn bộ giao diện.'],
  ['2', 'Tạo tài khoản nhân viên',
    'Dữ liệu mẫu hiện chỉ có tài khoản quản trị, chưa có tài khoản nhân viên, nên chưa kiểm thử được phân quyền ở mức giữa. Cần thêm một tài khoản nhân viên vào dữ liệu mẫu.', 'Chưa làm: vẫn chưa kiểm thử phân quyền mức nhân viên.'],
  ['3', 'Cơ sở dữ liệu riêng cho kiểm thử',
    'Các kịch bản đặt tiệc và đặt món tạo bản ghi mới mỗi lượt chạy. Chạy nhiều lần thì dữ liệu phình ra và kịch bản đếm số lượng sẽ sai. Cần một cơ sở dữ liệu riêng, dựng lại bằng Flyway trước mỗi lượt.', 'Xử lý theo cách khác: dò ngày trống qua API, dọn dịp lễ thử, không đếm tổng số bản ghi (xem mục 3.1).'],
  ['4', 'Xử lý dữ liệu phụ thuộc ngày giờ',
    'Nhiều trường hợp dùng ngày tương đối như sau 30 ngày hoặc sau 70 ngày. Kịch bản phải tính ngày lúc chạy chứ không được ghi cứng một ngày cụ thể, nếu không thì vài tháng sau chạy lại sẽ trượt.', 'Đã làm: ngày tính lúc chạy, tự né ngày lễ.'],
]));

noiDung.push(h1('7. Kết luận của mốc này'));
noiDung.push(bullet('Đã đặc tả ' + tongTC + ' trường hợp kiểm thử phủ đủ ' + LUONG.length + ' luồng nghiệp vụ trong phạm vi đề tài.'));
noiDung.push(bullet('Toàn bộ kết quả mong đợi đã đối chiếu với mã nguồn và tệp cấu hình thật, gồm cả các con số tính tiền và các câu thông báo lỗi nguyên văn.'));
noiDung.push(bullet('Đã xác định được bốn việc phải chuẩn bị trước khi viết mã, trong đó việc bổ sung thuộc tính data-test cần làm sớm nhất vì nó đụng tới mã nguồn frontend.'));
noiDung.push(p('Ghi chú cập nhật 17/09: các mốc cài đặt Selenium, dựng Page Object Model và viết mã kiểm thử đã hoàn thành sớm. Bộ kiểm thử hiện có ' + tongTC + ' test case trên ' + LUONG.length + ' luồng; kết quả chạy ghi trong tài liệu BaoCao_KiemThuTuDong.docx.'));

noiDung.push(h1('8. Lịch sử cập nhật'));
noiDung.push(bang([1500, 7526], ['Ngày', 'Nội dung'], [
  ['12/09/2026', 'Bản đầu: 108 trường hợp kiểm thử trên 13 luồng nghiệp vụ.'],
  ['17/09/2026', 'Đối chiếu lại với mã kiểm thử và giao diện thật. Bổ sung Luồng 14 và Luồng 15 (22 trường hợp). Thống nhất tên trường hợp với tên trong mã. Sửa tiền điều kiện Luồng 1 và Luồng 7. Sửa kết quả mong đợi của 21 trường hợp ở bảng dưới. Ghi chú né ngày lễ cho các luồng tính tiền, cập nhật môi trường, tài khoản, tham số và dữ liệu mẫu dịp lễ, thêm tên lớp kịch bản cho từng luồng.'],
]));
noiDung.push(p(''));
noiDung.push(p('Các trường hợp sửa kết quả mong đợi ngày 17/09:', { bold: true, after: 80 }));
noiDung.push(bang([1900, 7126], ['Mã', 'Lý do sửa'], [
  ['TC-AUTH-02', 'Dữ liệu mẫu không có tài khoản khach@vuonsen.vn như bản đầu ghi; kịch bản dùng email quản trị, câu báo thật là Email này đã được đăng ký.'],
  ['TC-SPACE-02', 'Bản đầu viết sai quy tắc lọc: tưởng lọc theo cả sức chứa tối thiểu. Quy tắc thật chỉ loại không gian vượt sức chứa tối đa; đổi sang 70 khách để kiểm được cả hai vế.'],
  ['TC-SPACE-07', 'Trang không có nút xóa bộ lọc; người dùng xóa từng ô lọc.'],
  ['TC-AUTH-03, TC-AUTH-05', 'Trình duyệt chặn ngay tại ô mật khẩu, ô email trước khi gửi, câu báo của máy chủ không hiện ra.'],
  ['TC-BOOK-13', 'Sau khi có lựa chọn Không cần gói tiệc, câu nhắc ở bước 2 đổi thành chọn gói hoặc chọn chỉ thuê không gian.'],
  ['TC-BOOKV-01, TC-BOOKV-07', 'Giao diện khóa luôn thẻ không gian vượt sức chứa và thẻ gói dài hơn buổi, chặn trước khi gửi. TC-BOOKV-07 dùng buổi sáng 4 tiếng và Gói Sen Vàng 5 tiếng.'],
  ['TC-BOOKV-02 đến TC-BOOKV-05, TC-BOOKV-11', 'Giao diện chặn ở bước 1 với câu nhắc riêng, khác câu báo của máy chủ.'],
  ['TC-DISH-12', 'Dùng món tạo riêng giá 200.000đ thay cho món mẫu, để kiểm thử không làm đổi giá thực đơn thật.'],
  ['TC-DISHV-02 đến TC-DISHV-04', 'Ô địa chỉ, ô số khách có ràng buộc bắt buộc và tối đa 40, trình duyệt chặn trước khi gửi.'],
  ['TC-REV-04, TC-REV-05', 'Ô mã đơn và ô nội dung bắt buộc, trình duyệt chặn trước khi gửi.'],
  ['TC-REV-06', 'Giao diện dùng ô chọn số sao thay cho hàng ngôi sao bấm được.'],
  ['TC-ADMIN-06', 'Ghi rõ đơn chờ xác nhận chưa chiếm chỗ, và việc gửi đơn làm qua API.'],
]));

const doc = new Document({
  sections: [{
    properties: { page: { margin: { top: 1000, bottom: 1000, left: 1000, right: 1000 } } },
    children: noiDung,
  }],
});

const RA = process.argv[2] || 'E:/vuonsen-fnb/doc/DacTa_TestCase.docx';
Packer.toBuffer(doc).then((buf) => {
  fs.writeFileSync(RA, buf);
  console.log('da ghi ' + RA + ' (' + Math.round(buf.length / 1024) + ' KB)');
  console.log('tong ' + tongTC + ' test case / ' + LUONG.length + ' luong');
  console.log('  thuan ' + soThuan + ', nghich ' + soNghich);
  console.log('  cao ' + soCao + ', TB ' + soTB + ', thap ' + soThap);
  LUONG.forEach((l, i) => console.log('  ' + (i + 1) + '. ' + l.ma + ' ' + l.ten + ': ' + l.ds.length));
});
