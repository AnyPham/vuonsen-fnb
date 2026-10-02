const {
  Document, Packer, Paragraph, TextRun, HeadingLevel, AlignmentType,
  Table, TableRow, TableCell, WidthType, ShadingType, ImageRun,
} = require('docx');
const fs = require('fs');
const { execSync } = require('child_process');

/*
 * Số liệu tự đếm từ mã nguồn mỗi lần chạy, không gõ tay.
 *
 * Nhờ vậy chạy lại script lúc nào thì mục "Tình trạng dự án" cũng đúng với thực tế lúc đó:
 * số lớp kiểm thử, kết quả lần chạy kiểm thử gần nhất, số bảng, số API, số thay đổi chưa commit.
 */
const DU_AN = 'E:/vuonsen-fnb';

const tepDeQuy = (thuMuc) => (fs.existsSync(thuMuc)
  ? fs.readdirSync(thuMuc, { withFileTypes: true }).flatMap((d) => (d.isDirectory()
    ? tepDeQuy(`${thuMuc}/${d.name}`) : [`${thuMuc}/${d.name}`]))
  : []);
const demMau = (cacTep, mau) => cacTep.reduce((tong, t) => tong + (fs.readFileSync(t, 'utf8').match(mau) || []).length, 0);
const hai = (n) => String(n).padStart(2, '0');
const ngayVn = (d) => `${hai(d.getDate())}/${hai(d.getMonth() + 1)}/${d.getFullYear()}`;

// Gộp các tệp XML của Surefire thành một con số tổng, kèm ngày chạy gần nhất
const ketQuaChay = (thuMuc, mauTen = /^TEST-.*\.xml$/) => {
  if (!fs.existsSync(thuMuc)) return null;
  const cacTep = fs.readdirSync(thuMuc).filter((t) => mauTen.test(t));
  if (!cacTep.length) return null;
  let bai = 0; let truot = 0; let loi = 0; let giay = 0; let luc = 0;
  for (const t of cacTep) {
    const x = fs.readFileSync(`${thuMuc}/${t}`, 'utf8');
    const lay = (ten) => Number((x.match(new RegExp(`\\s${ten}="([\\d.]+)"`)) || [0, 0])[1]);
    bai += lay('tests'); truot += lay('failures'); loi += lay('errors'); giay += lay('time');
    luc = Math.max(luc, fs.statSync(`${thuMuc}/${t}`).mtimeMs);
  }
  return { bai, truot, loi, dat: bai - truot - loi, giay: Math.round(giay), luc: new Date(luc) };
};

const git = (lenh) => {
  try {
    return execSync(lenh, { cwd: DU_AN, encoding: 'utf8', stdio: ['ignore', 'pipe', 'ignore'] }).trim();
  } catch (e) {
    return '';
  }
};

const javaMay = tepDeQuy(`${DU_AN}/backend/src/main/java`).filter((t) => t.endsWith('.java'));
const sql = tepDeQuy(`${DU_AN}/backend/src/main/resources/db/migration`).filter((t) => t.endsWith('.sql'));
const kichBan = tepDeQuy(`${DU_AN}/kiemthu/src/test/java/vn/vuonsen/kiemthu/kichban`).filter((t) => t.endsWith('.java'));

const SL = {
  homNay: ngayVn(new Date()),
  bang: demMau(sql, /CREATE TABLE/g),
  migration: sql.length,
  module: fs.readdirSync(`${DU_AN}/backend/src/main/java/vn/vuonsen/fnb/modules`, { withFileTypes: true })
    .filter((d) => d.isDirectory()).length,
  controller: javaMay.filter((t) => t.endsWith('Controller.java')).length,
  api: demMau(javaMay, /@(Get|Post|Put|Patch|Delete)Mapping/g),
  trang: tepDeQuy(`${DU_AN}/frontend/src/pages`).filter((t) => t.endsWith('.jsx')).length,
  lopTestMay: tepDeQuy(`${DU_AN}/backend/src/test`).filter((t) => t.endsWith('.java')).length,
  chayMay: ketQuaChay(`${DU_AN}/backend/target/surefire-reports`),
  tcSelenium: demMau(kichBan, /@DisplayName\("TC-[A-Z]+-\d+/g),
  luongSelenium: kichBan.length,
  tepKiemThu: tepDeQuy(`${DU_AN}/kiemthu/src`).filter((t) => t.endsWith('.java')).length,
  // Chỉ đếm lớp kịch bản; công cụ chụp ảnh minh chứng cũng ghi báo cáo Surefire nhưng không phải kiểm thử
  chaySelenium: ketQuaChay(`${DU_AN}/kiemthu/target/surefire-reports`, /^TEST-.*\.kichban\..*\.xml$/),
  thayDoi: git('git status --porcelain').split('\n').filter(Boolean).length,
  commitCuoi: git('git log -1 --format=%h ngày %ad --date=format:%d/%m/%Y'),
};

const moTaChay = (kq) => (kq
  ? `đạt ${kq.dat} trên ${kq.bai} bài ở lần chạy ngày ${ngayVn(kq.luc)}, hết ${kq.giay} giây`
  : 'chưa có kết quả lần chạy nào lưu trên máy');
const hoaDau = (chuoi) => chuoi.charAt(0).toUpperCase() + chuoi.slice(1);

const W = 9026;
const HDR = 'D9D9D9';

const cell = (text, width, o = {}) =>
  new TableCell({
    width: { size: width, type: WidthType.DXA },
    columnSpan: o.span,
    shading: o.bg ? { type: ShadingType.CLEAR, fill: o.bg } : undefined,
    margins: { top: 60, bottom: 60, left: 100, right: 100 },
    children: (Array.isArray(text) ? text : [text]).map(
      (t) => new Paragraph({
        alignment: o.center ? AlignmentType.CENTER : AlignmentType.LEFT,
        children: [new TextRun({ text: String(t), bold: !!o.bold, size: 21 })],
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
  heading: HeadingLevel.HEADING_2, spacing: { before: 200, after: 100 },
  children: [new TextRun({ text: t, bold: true, size: 24 })],
});

const bullet = (t) => new Paragraph({
  bullet: { level: 0 }, spacing: { after: 60 },
  children: [new TextRun({ text: t, size: 23 })],
});

// Bảng chi tiết một việc: file nào, làm gì
const fileTable = (rows) => new Table({
  columnWidths: [3600, 5426],
  width: { size: W, type: WidthType.DXA },
  rows: [
    new TableRow({
      tableHeader: true,
      children: [
        cell('Tệp / vị trí', 3600, { bold: true, bg: HDR }),
        cell('Nội dung đã làm', 5426, { bold: true, bg: HDR }),
      ],
    }),
    ...rows.map((r) => new TableRow({ children: [cell(r[0], 3600), cell(r[1], 5426)] })),
  ],
});

// Ảnh minh chứng chụp bằng công cụ ChupAnhMinhChung, chép sang cạnh script để không mất khi chạy mvn clean
const THU_MUC_ANH = `${__dirname}/anh-minh-chung`;
const RONG_TOI_DA = 600; // bề ngang vùng in của trang A4 trừ lề, tính bằng điểm ảnh
const CAO_TOI_DA = 760;

const anh = (tenTep, chuThich) => {
  const duongDanPng = `${THU_MUC_ANH}/${tenTep}.png`;
  const duongDan = fs.existsSync(duongDanPng) ? duongDanPng : `${THU_MUC_ANH}/${tenTep}.jpg`;
  if (!fs.existsSync(duongDan)) {
    return [p(`(Chưa có ảnh ${tenTep}.png. Chạy công cụ ChupAnhMinhChung, chép ảnh vào cong-cu/nhat-ky/anh-minh-chung rồi chạy lại script.)`, { italic: true, size: 21 })];
  }
  const du = fs.readFileSync(duongDan);
  // Kích thước thật đọc từ phần đầu tệp (PNG: khối IHDR; JPEG: đoạn SOF), rồi thu nhỏ giữ đúng tỉ lệ cho vừa trang
  const laPng = duongDan.endsWith('.png');
  let rong = 0;
  let cao = 0;
  if (laPng) {
    rong = du.readUInt32BE(16);
    cao = du.readUInt32BE(20);
  } else {
    for (let i = 2; i + 9 < du.length;) {
      if (du[i] !== 0xFF) { i++; continue; }
      const loai = du[i + 1];
      if (loai >= 0xC0 && loai <= 0xC3) { cao = du.readUInt16BE(i + 5); rong = du.readUInt16BE(i + 7); break; }
      i += 2 + du.readUInt16BE(i + 2);
    }
  }
  const tiLe = Math.min(RONG_TOI_DA / rong, CAO_TOI_DA / cao, 1);
  return [
    new Paragraph({
      alignment: AlignmentType.CENTER,
      spacing: { before: 120, after: 60 },
      children: [new ImageRun({
        type: laPng ? 'png' : 'jpg', data: du,
        transformation: { width: Math.round(rong * tiLe), height: Math.round(cao * tiLe) },
      })],
    }),
    p(chuThich, { italic: true, center: true, size: 20, after: 160 }),
  ];
};

const khongAnh = (lyDo) => p(`Không kèm ảnh: ${lyDo}`, { italic: true, size: 21 });
const tieuDeAnh = () => p('Ảnh minh chứng, chụp ngày 17/09/2026 bằng Selenium trên dữ liệu MySQL:', { bold: true, size: 22, after: 60 });

const doc = new Document({
  sections: [{
    properties: { page: { margin: { top: 1134, bottom: 1134, left: 1134, right: 1134 } } },
    children: [
      p('NHẬT KÝ CÔNG VIỆC THEO NGÀY', { bold: true, center: true, size: 30, after: 100 }),
      p('Đề tài: Nghiên cứu và xây dựng nền tảng website dịch vụ nhà hàng và cho thuê không gian sự kiện F&B tích hợp hệ thống gợi ý và trợ lý AI thông minh', { center: true, italic: true, size: 21, after: 140 }),
      p('Sinh viên: Phạm Trần Tuấn Anh — MSSV 21130004 — Lớp DH21DTA', { center: true, size: 22, after: 50 }),
      p('Giảng viên hướng dẫn: TS. Nguyễn Thị Phương Trâm', { center: true, size: 22, after: 50 }),
      p('Kho mã nguồn: https://github.com/AnyPham/vuonsen-fnb', { center: true, size: 22, after: 260 }),

      p(`Ghi chú về trạng thái mã nguồn đến ngày ${SL.homNay}: nhánh chính trên GitHub đã hợp nhất tới Pull Request #13. Hai nhánh tro-ly-ai/goi-mo-hinh-ngon-ngu (1 commit) và dat-mon/anh-chi-tiet-va-dat-mon-le (4 commit) đã đẩy lên GitHub nhưng chưa hợp nhất. Mã nguồn từ Việc 25 trở đi mới nằm ở máy cá nhân, chưa commit, hiện là ${SL.thayDoi} thay đổi. Các tệp tài liệu báo cáo chỉ lưu ở máy cá nhân, không đưa lên kho mã nguồn.`, { italic: true, size: 21, after: 200 }),

      // ---------------- 06/08 ----------------
      h1('Ngày 06/08/2026'),
      h2('Buổi tối — Khởi tạo kho mã nguồn'),
      bullet('Tạo kho mã nguồn trên GitHub và đẩy thư mục tài liệu đề cương lên.'),
      p('Commit: 7ad2137 — "Tài liệu dự án, hướng dẫn và báo cáo".', { italic: true, size: 21 }),

      // ---------------- 15/08 ----------------
      h1('Ngày 15/08/2026'),
      h2('Buổi sáng và chiều — Dựng khung toàn bộ dự án'),
      fileTable([
        ['backend/ (60 tệp Java)', 'Dựng dự án Spring Boot 3.3 chia theo 8 module nghiệp vụ: auth, user, space, menu, partypackage, booking, review, gallery.'],
        ['backend/.../security/', 'Xác thực bằng JWT: JwtService tạo và kiểm token, JwtAuthenticationFilter đọc token mỗi request, refresh token lưu trong cơ sở dữ liệu.'],
        ['backend/.../config/SecurityConfig.java', 'Phân quyền ba nhóm: khách hàng, nhân viên, quản trị.'],
        ['backend/.../db/migration/V1, V2', 'Viết script tạo 13 bảng và nạp dữ liệu mẫu: 6 không gian, 26 món ăn, 3 gói tiệc.'],
        ['backend/.../booking/PricingService.java', 'Bộ tính giá tiệc: số mâm, tiền ăn, phí thuê, giảm giá, VAT.'],
        ['frontend/ (React + Vite)', 'Dựng 12 trang giao diện, quản lý trạng thái bằng Redux Toolkit, gọi API qua axios có tự làm mới token.'],
        ['frontend/.../BookingPage.jsx', 'Form đặt tiệc ba bước, hiển thị bảng kê chi phí tạm tính.'],
        ['docker-compose.yml, Dockerfile', 'Cấu hình triển khai ba dịch vụ: cơ sở dữ liệu, backend, frontend.'],
      ]),
      p('Kiểm chứng: backend 7/7 kiểm thử tự động đạt, frontend build không lỗi.', { italic: true, size: 21 }),

      h2('Buổi tối — Dọn dẹp mã nguồn và tài liệu'),
      bullet('Viết lại README theo bố cục thông thường của một dự án phần mềm.'),
      bullet('Rút gọn toàn bộ chú thích trong mã nguồn, chuyển sang tiếng Việt có dấu, thống nhất cách viết.'),
      bullet('Xóa reducer setActiveCategory không nơi nào dùng đến.'),
      p('Commit: e8f77cc, 4faae0b, d86f6a6.', { italic: true, size: 21 }),

      // ---------------- 16/08 ----------------
      h1('Ngày 16/08/2026'),
      h2('Buổi chiều — Rà soát và sửa logic nghiệp vụ theo thực tế kinh doanh'),
      p('Đối chiếu cách tính giá với cách trung tâm tiệc cưới vận hành thật, phát hiện và sửa các lỗi sau:', { size: 23 }),
      fileTable([
        ['PricingService.java', 'Bỏ quy tắc miễn phí thuê từ 30 mâm. Quy tắc cũ tạo vách giá: khách 300 người trả ít hơn khách 290 người 7,5 triệu đồng. Thay bằng mức doanh thu tối thiểu, phí thuê giảm dần theo tiền ăn.'],
        ['TimeSlot.java', 'Sửa giờ ba buổi không còn chồng nhau. Trước đó buổi sáng 8h-12h và buổi trưa 11h-15h trùng một tiếng nên một sảnh nhận được hai tiệc cùng lúc.'],
        ['BookingService.java', 'Thêm ràng buộc thời gian báo trước, từ chối gói tiệc dài hơn thời lượng buổi, tự động hủy đơn trùng lịch khi xác nhận một đơn, ghi mức hoàn cọc khi hủy.'],
        ['ReviewController.java', 'Bắt buộc nhập mã đơn đã hoàn thành mới gửi được đánh giá, mỗi đơn chỉ đánh giá một lần.'],
        ['application.yml', 'Đưa toàn bộ tham số tính giá ra tệp cấu hình: số khách mỗi mâm, VAT, tỉ lệ cọc, thời gian báo trước.'],
        ['PricingServiceTest.java', 'Bổ sung kiểm thử quét từ 200 đến 500 khách chứng minh tổng tiền không bao giờ giảm khi tăng số khách.'],
      ]),
      p('Commit: 5f112a3. Kiểm chứng: 10/10 kiểm thử đạt.', { italic: true, size: 21 }),

      // ---------------- 18/08 ----------------
      h1('Ngày 18/08/2026'),
      h2('Buổi tối — Sửa quy tắc nhận tiệc và đồng bộ ràng buộc'),
      fileTable([
        ['PricingService.java, BookingService.java', 'Bỏ việc từ chối khách ít hơn sức chứa. Thay bằng tính tiền theo số mâm tối thiểu của sảnh, giống cách trung tâm tiệc làm thật.'],
        ['SpaceRepository.java', 'Bộ lọc đổi từ khoảng sức chứa sang chỉ chặn khi vượt sức chứa tối đa.'],
        ['BookingController.java', 'API /bookings/options trả kèm các quy định để giao diện chặn sớm, tránh để khách điền hết ba bước rồi mới báo lỗi.'],
        ['BookingPage.jsx', 'Đọc quy định từ máy chủ thay vì tự đặt ra, khóa ngày và gói tiệc không hợp lệ ngay tại giao diện.'],
      ]),
      p('Commit: 5b92f1e, 0f83d02. Tạo và hợp nhất Pull Request #1 và #2 trên GitHub.', { italic: true, size: 21 }),

      // ---------------- 20/08 ----------------
      h1('Ngày 20/08/2026'),
      h2('Buổi sáng — Sửa lỗi form đặt tiệc không dùng được'),
      p('Khách mở trang đặt tiệc thấy hai ô "Loại hình sự kiện" và "Buổi" trống rỗng, không chọn được gì.', { size: 23 }),
      fileTable([
        ['SecurityConfig.java', 'Nguyên nhân: API /bookings/options chưa nằm trong danh sách cho phép truy cập ẩn danh nên trả về lỗi 403. Đã bổ sung.'],
        ['PublicEndpointsTest.java (mới)', 'Thêm 3 kiểm thử: khách chưa đăng nhập xem được 8 API công khai, bị chặn ở khu quản trị và trang hồ sơ.'],
        ['BookingPage.jsx', 'Chỉ báo lỗi sau khi bấm Tiếp tục, tránh vừa mở form đã hiện chữ đỏ.'],
        ['application.yml', 'Giảm thời gian báo trước với tiệc lớn từ 14 ngày xuống 7 ngày.'],
      ]),

      h2('Buổi chiều — Trang hồ sơ cá nhân'),
      fileTable([
        ['ProfilePage.jsx (mới)', 'Trang hồ sơ cho người đã đăng nhập: sửa họ tên, số điện thoại, địa chỉ. Email và quyền chỉ hiển thị, không cho sửa.'],
        ['authSlice.js', 'Thêm hành động cập nhật hồ sơ, lưu kết quả vào Redux để tên trên thanh menu đổi theo ngay.'],
        ['App.jsx, Header.jsx', 'Thêm đường dẫn /ho-so có bảo vệ đăng nhập và mục menu tương ứng.'],
      ]),

      h2('Buổi tối — Ổn định môi trường chạy'),
      bullet('Khóa Vite ở cổng 5173, trước đó mỗi lần chạy lại mà chưa tắt máy chủ cũ thì nó tự nhảy sang cổng khác, có lúc mở tới bốn cổng cùng lúc.'),
      bullet('Bỏ dòng khai báo dialect thừa mà Hibernate cảnh báo trong nhật ký.'),
      bullet('Chặn tệp tạm của Word không lọt vào kho mã nguồn.'),
      bullet('Sửa chữ "Khóa luận" thành "Tiểu luận" ở chân trang và trang thực đơn.'),
      p('Bốn nhánh của ngày 20/08 đã đẩy lên GitHub và hợp nhất vào nhánh chính qua các Pull Request #3, #5 và #6.', { italic: true, size: 21 }),

      // ---------------- 23/08 ----------------
      h1('Ngày 23/08/2026'),
      p('Mục tiêu: hoàn thiện các chức năng đã có sẵn phần máy chủ nhưng chưa có giao diện.', { size: 23 }),

      h2('Việc 1 — Trang chi tiết không gian'),
      p('Nhánh: khong-gian/trang-chi-tiet — commit d74a64d', { italic: true, size: 21 }),
      fileTable([
        ['SpaceDetailPage.jsx (mới)', 'Trang chi tiết một không gian: mô tả đầy đủ, bảng thông số sức chứa và giá thuê, danh sách tiện ích, bản đồ vị trí, nút đặt giữ chỗ.'],
        ['App.jsx', 'Thêm đường dẫn /khong-gian/:slug.'],
        ['SpacesPage.jsx', 'Tên không gian và nút ở mỗi thẻ nay dẫn sang trang chi tiết thay vì nhảy thẳng tới form đặt tiệc.'],
      ]),

      h2('Việc 2 — Giao diện đánh giá dịch vụ'),
      p('Nhánh: danh-gia/giao-dien-danh-gia — commit b9b7407', { italic: true, size: 21 }),
      fileTable([
        ['ReviewAdminController.java (mới)', 'API quản trị đánh giá: danh sách lọc theo chờ duyệt hoặc đã duyệt, duyệt đánh giá, xóa đánh giá không phù hợp.'],
        ['ReviewController.java', 'Chuyển endpoint duyệt sang controller quản trị cho thống nhất với các module khác.'],
        ['ReviewsPage.jsx (mới)', 'Trang đánh giá công khai: xem nhận xét đã duyệt kèm số sao và điểm trung bình, form gửi đánh giá bắt buộc nhập mã đơn.'],
        ['AdminReviewsPage.jsx (mới)', 'Màn hình duyệt đánh giá cho quản trị, có nút Duyệt và Xóa.'],
        ['endpoints.js, App.jsx, Header.jsx', 'Thêm ba hàm gọi API quản trị, hai đường dẫn /danh-gia và /quan-tri/danh-gia, các mục menu tương ứng.'],
      ]),

      h2('Việc 3 — Hiển thị món được gọi nhiều nhất'),
      p('Nhánh: thuc-don/mon-ban-chay — commit 2be565b', { italic: true, size: 21 }),
      fileTable([
        ['MenuPage.jsx', 'Thêm khối "Được gọi nhiều nhất" ở đầu trang thực đơn. API đã có sẵn nhưng chưa nơi nào gọi tới nên 4 món đánh dấu bán chạy trong dữ liệu mẫu không hiện ra.'],
      ]),

      h2('Việc 4 — Cập nhật tài liệu báo cáo'),
      p('Không thuộc mã nguồn, chỉ cập nhật hai tài liệu cho khớp trạng thái mới.', { size: 23 }),
      fileTable([
        ['doc/BaoCao_DoiChieu_TienDo.docx', 'Chuyển hai dòng "Xem thông tin chi tiết" và "Đánh giá dịch vụ" từ Một phần sang Xong. Mục 5.3 nay chỉ còn một hạng mục chưa làm là bộ lọc gói dịch vụ.'],
        ['doc/21130004_...KeHoachVaTienDo.docx', 'Cập nhật bảng tiến độ: năm dòng chuyển từ Chưa làm FE sang Hoàn thành, Frontend từ 0% lên 100%.'],
      ]),

      p('Kiểm chứng chung của ngày 23/08: backend 12/12 kiểm thử đạt, frontend build không lỗi.', { italic: true, size: 21, after: 200 }),

      // ---------------- Phần còn lại ----------------
      h2('Việc 5 — Hợp nhất mã nguồn và dọn kho'),
      fileTable([
        ['Pull Request #7, #8, #9', 'Hợp nhất ba nhánh của ngày 23/08 vào nhánh chính. Xử lý một xung đột ở MenuPage.jsx tại dòng khai báo import: giữ bản có useState vì mã nguồn cần biến này để lưu danh sách món bán chạy.'],
        ['Toàn bộ nhánh phụ', 'Bảy nhánh đã hợp nhất và được xóa khỏi kho. Hiện chỉ còn nhánh main.'],
        ['.gitignore', 'Gỡ tệp báo cáo nội bộ ra khỏi kho mã nguồn. Tệp này bị đưa lên do lệnh git add -A hôm 20/08. Bổ sung ba tệp tài liệu vào danh sách loại trừ để không lặp lại.'],
      ]),
      p('Kiểm chứng sau khi hợp nhất: backend 15/15 kiểm thử đạt, frontend build không lỗi, không còn dấu xung đột nào trong mã nguồn.', { italic: true, size: 21 }),
      p('Tổng kết đến 23/08/2026: nhánh chính có 26 commit, 9 pull request đã hợp nhất.', { italic: true, size: 21, after: 200 }),

      h2('Việc 6 — Xây dựng hệ thống gợi ý'),
      p('Đây là một trong hai nội dung trọng tâm nằm trong tên đề tài. Nhánh goi-y/he-thong-goi-y, commit c7d31dc.', { italic: true, size: 21 }),
      fileTable([
        ['V3__recommendation.sql (mới)', 'Thêm bảng space_event_types lưu mức phù hợp của từng không gian với từng loại sự kiện, chấm từ 1 đến 5. Thêm bảng recommendation_logs ghi lại mỗi lần gợi ý và kết quả khách có chọn hay không, dùng để đo độ chính xác trong chương thực nghiệm.'],
        ['RecommendationService.java (mới)', 'Thuật toán chấm điểm mỗi tổ hợp không gian và gói tiệc trên thang 100, chia đều cho bốn tiêu chí: mức phù hợp với loại sự kiện, mức vừa vặn của sức chứa, mức khớp ngân sách, mức phổ biến theo lịch sử đặt tiệc. Trả về ba tổ hợp cao điểm nhất kèm lý do đề xuất.'],
        ['RecommendationController.java (mới)', 'API POST /api/v1/recommendations, khách chưa đăng nhập cũng dùng được.'],
        ['SpaceEventType.java, RecommendationLog.java (mới)', 'Hai entity ánh xạ hai bảng mới.'],
        ['SuggestionBox.jsx (mới)', 'Khối gợi ý hiện ở bước 2 của form đặt tiệc. Bấm một nút là điền luôn không gian và gói tiệc vào form.'],
        ['RecommendationServiceTest.java (mới)', 'Sáu kiểm thử: trả đúng ba gợi ý xếp theo điểm, loại không gian không chứa nổi số khách, tiệc gia đình được gợi ý không gian ấm cúng, hội nghị được gợi ý phòng hội nghị, tôn trọng ngân sách, mọi gợi ý đều kèm lý do.'],
      ]),
      p('Phạm vi tuân thủ đề cương mục 6.2: dựa trên tiêu chí và dữ liệu người dùng, không dùng mô hình học sâu.', { size: 23 }),
      p('Kiểm chứng: 21/21 kiểm thử đạt, migration V3 chạy sạch, frontend build không lỗi.', { italic: true, size: 21 }),

      h2('Việc 7 — Chuyển sang cơ sở dữ liệu MySQL thật'),
      p('Đề cương yêu cầu chạy thử với MySQL thật chứ không chỉ H2. Trước đó hệ thống chạy bằng H2 trong bộ nhớ, tắt ứng dụng là mất sạch dữ liệu. Nhánh csdl/chay-tren-mysql-that, commit 70ae519.', { italic: true, size: 21 }),
      fileTable([
        ['application-local.yml (mới)', 'Cấu hình kết nối MySQL trên máy cá nhân, tách riêng khỏi profile prod vốn đọc từ biến môi trường. Chạy bằng lệnh mvn spring-boot:run -Dspring-boot.run.profiles=local.'],
        ['Khảo sát môi trường', 'Phát hiện máy có hai máy chủ MySQL cùng chạy: MySQL 8.0 được cấu hình ở cổng 3305, XAMPP dùng cổng 3306. Đây là nguyên nhân của lỗi kết nối trước đó chứ không phải sai mật khẩu.'],
        ['V1__init_schema.sql, V3__recommendation.sql (sửa)', 'Đổi toàn bộ kiểu TIMESTAMP thành DATETIME. Xem phần giải thích bên dưới.'],
      ]),
      p('Lần chạy đầu tiên Flyway dừng ngay ở câu lệnh tạo bảng đầu tiên với thông báo "Invalid default value for updated_at". Nguyên nhân là MySQL và MariaDB có hai quy tắc ngầm cho kiểu TIMESTAMP mà H2 không có:', { size: 23 }),
      bullet('Cột TIMESTAMP đầu tiên trong một bảng tự động được gán DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP.'),
      bullet('Các cột TIMESTAMP sau đó, nếu không khai báo gì, bị gán DEFAULT 0000-00-00 00:00:00, mà chế độ nghiêm ngặt cấm giá trị này.'),
      p('Quy tắc thứ nhất nguy hiểm hơn quy tắc làm treo migration. Ở bảng refresh_tokens, expires_at là cột TIMESTAMP đầu tiên nên sẽ bị gán ON UPDATE CURRENT_TIMESTAMP, tức là hạn sử dụng của refresh token bị đặt lại mỗi lần bản ghi được cập nhật. Token đáng lẽ hết hạn thì sống mãi, mà chạy trên H2 thì không đời nào phát hiện ra.', { size: 23 }),
      p('Cách sửa: đổi toàn bộ TIMESTAMP thành DATETIME. Kiểu DATETIME không có quy tắc ngầm nào, hành vi giống nhau trên cả H2 lẫn MySQL. Hai tệp migration này chưa từng chạy thật ở đâu ngoài H2 trong bộ nhớ nên sửa trực tiếp được, không cần tạo migration mới.', { size: 23 }),
      p('Kiểm chứng trên MySQL thật: ba migration chạy sạch tạo đủ 15 bảng; dữ liệu mẫu vào đủ gồm 6 không gian, 26 món ăn, 5 danh mục, 3 gói tiệc, 17 dịch vụ đi kèm, 18 tiện ích, 6 ảnh, 3 đánh giá và 30 dòng mức phù hợp của hệ thống gợi ý; tiếng Việt có dấu lưu và trả về đúng; đăng nhập quản trị rồi gọi cả ba API quản trị đều trả về 200. Kiểm chứng trên H2: 37/37 kiểm thử đạt.', { italic: true, size: 21 }),


      h2('Việc 8 — Xây dựng chức năng quản trị thực đơn, gói tiệc và không gian'),
      p('Việc này nằm trong kế hoạch giai đoạn 24/08 - 31/08. Hai nhánh: quan-tri/thuc-don-va-goi-tiec commit 95898ba hợp nhất qua Pull Request #11, và quan-tri/giao-dien-khong-gian commit cb20878 hợp nhất qua Pull Request #12.', { italic: true, size: 21 }),
      p('Trước đó muốn sửa một món ăn hay đổi giá một gói tiệc thì phải vào thẳng cơ sở dữ liệu, nay làm được trên web.', { size: 23 }),
      fileTable([
        ['MenuAdminService.java, DishAdminController.java (mới)', 'Thêm, sửa, ngừng bán món ăn. Ngừng bán là xóa mềm chứ không xóa hẳn, vì các đơn cũ vẫn cần tên món để tra cứu.'],
        ['PackageAdminService.java, PackageAdminController.java (mới)', 'Thêm, sửa, ngừng bán gói tiệc. Kiểm tra mã gói không được trùng, khi sửa thì bỏ qua chính nó.'],
        ['SpaceAdminController.java (sửa)', 'Bổ sung API lấy danh sách không gian cho quản trị. Backend đã có sẵn ba thao tác thêm, sửa, ngừng kinh doanh từ trước nhưng thiếu API này nên trang quản trị không dựng được.'],
        ['DishAdminResponse, PackageAdminResponse, SpaceAdminResponse (mới)', 'Trang quản trị cần thấy cả mục đã ngừng bán và thứ tự sắp xếp, khác với dữ liệu trả về cho khách.'],
        ['AdminMenuPage.jsx, AdminPackagesPage.jsx, AdminSpacesPage.jsx (mới)', 'Ba màn hình quản trị, mỗi màn hình gồm bảng danh sách và form thêm hoặc sửa. Chỉ tài khoản quản trị vào được, nhân viên thì không, vì sửa bảng giá là việc của quản trị.'],
        ['MenuAdminServiceTest, PackageAdminServiceTest, SpaceAdminServiceTest (mới)', 'Mười bốn kiểm thử cho các quy tắc nghiệp vụ: món không có giá phải ghi chú cách tính, mã gói và mã không gian không được trùng, ngừng bán thì khách không thấy nhưng quản trị vẫn thấy.'],
        ['PublicEndpointsTest (sửa)', 'Thêm hai kiểm thử phân quyền: tài khoản quản trị gọi được cả ba API danh sách, tài khoản nhân viên bị trả về 403. Trước đó chỉ kiểm tra khách chưa đăng nhập bị chặn, chưa có kiểm thử nào chứng minh quản trị thật sự vào được.'],
      ]),
      p('Hai quy tắc nghiệp vụ phát sinh khi làm phần này:', { size: 23 }),
      bullet('Món để trống giá thì bắt buộc phải ghi chú cách tính giá, ví dụ "Theo cân", nếu không khách sẽ không biết món đó bao nhiêu tiền.'),
      bullet('Không gian tính phí theo chòi mà quên khai số khách mỗi chòi thì trước đây PricingService lặng lẽ quay về tính theo buổi, giá báo cho khách sai mà không ai biết. Nay chặn ngay lúc lưu.'),
      p('Kiểm chứng: 31/31 kiểm thử đạt trên nhánh quản trị, 37/37 khi gộp cùng nhánh hệ thống gợi ý. Frontend build không lỗi.', { italic: true, size: 21 }),

      h2('Việc 9 — Sửa thao tác bấm vào thẻ không gian'),
      p('Nhánh khong-gian/bam-ca-the, commit 4d14c77.', { italic: true, size: 21 }),
      p('Ở trang danh sách không gian, trước đây chỉ bấm đúng tên không gian hoặc đúng nút "Xem chi tiết" mới mở được trang chi tiết. Bấm vào ảnh, vào đoạn mô tả hay vào vùng tiện ích thì không có gì xảy ra, người dùng tưởng website bị lỗi.', { size: 23 }),
      fileTable([
        ['global.css (sửa)', 'Thêm lớp card-clickable và full-link. Đường dẫn ở tiêu đề được kéo giãn bằng lớp giả ::after đặt tuyệt đối phủ kín cả thẻ. Nút bên trong được đẩy lên trên lớp phủ bằng z-index để vẫn bấm riêng được.'],
        ['SpacesPage.jsx (sửa)', 'Gắn hai lớp trên vào thẻ không gian và vào đường dẫn ở tiêu đề.'],
      ]),
      p('Cách khác là bọc cả thẻ vào trong một thẻ a, nhưng bên trong thẻ đã có sẵn hai đường dẫn rồi nên sẽ thành lồng thẻ a vào nhau, sai chuẩn HTML. Cách khác nữa là bắt sự kiện bấm trên thẻ div, nhưng như vậy mất ba thứ: bấm chuột phải mở tab mới, đi bằng phím Tab, và trình đọc màn hình nhận ra đó là đường dẫn. Cách kéo giãn lớp giả giữ được cả ba mà trang vẫn chỉ có một đường dẫn cho mỗi không gian.', { size: 23 }),
      p('Kiểm chứng trên trình duyệt: bấm thử bốn vùng khác nhau trong thẻ gồm ảnh minh họa, đoạn mô tả, vùng tiện ích và hàng giá tiền, cả bốn đều mở đúng trang chi tiết. Nút "Xem chi tiết" vẫn hoạt động riêng. Tải lại trang thì cả sáu thẻ đều bấm được. Frontend build không lỗi.', { italic: true, size: 21 }),

      h2('Việc 10 — Bổ sung gợi ý thực đơn cho khớp đề cương'),
      p('Nhánh goi-y/goi-y-thuc-don, commit ca579e2, ngày 01/09/2026.', { italic: true, size: 21 }),
      p('Rà soát chéo tiến độ với đề cương thì phát hiện một chỗ nói quá. Mục tiêu 6 ghi rõ là gợi ý "không gian, thực đơn, gói dịch vụ", nhưng hệ thống gợi ý làm ngày 23/08 mới ghép không gian với gói tiệc, không hề chấm điểm ở mức món ăn. Tìm cả module gợi ý không có một dòng nào nhắc đến món. Phần này bù lại chỗ còn thiếu đó.', { size: 23 }),
      fileTable([
        ['V4__dish_recommendation.sql (mới)', 'Thêm bảng dish_event_types lưu mức phù hợp của từng món với từng loại sự kiện, chấm từ 1 đến 5, giống cách làm của bảng space_event_types. Điểm đặt theo danh mục món rồi nạp bằng câu INSERT SELECT thay vì gõ tay 130 dòng.'],
        ['MenuSuggestionService.java (mới)', 'Chấm điểm mỗi món trên thang 100: mức phù hợp với loại sự kiện tối đa 50, món được gọi nhiều nhất tối đa 30, giá món sát với tiền ăn trung bình của gói tối đa 20. Sau đó chọn đúng số món mà gói tiệc ghi.'],
        ['DishEventType.java, DishEventTypeRepository.java (mới)', 'Entity và truy vấn cho bảng mới.'],
        ['DishBrief.java (mới), SuggestionResponse.java (sửa)', 'Trả thêm danh sách món trong kết quả gợi ý.'],
        ['SuggestionBox.jsx (sửa)', 'Thêm mục "Thực đơn gợi ý" gấp lại được, liệt kê từng món kèm danh mục và giá.'],
      ]),
      p('Cách chọn món gồm ba bước: bắt buộc có khai vị, món chính và tráng miệng vì một mâm tiệc Việt thiếu ba nhóm này thì không thành mâm; số món còn lại lấy theo điểm từ cao xuống, mỗi danh mục không quá một phần ba số món để không dồn hết vào một nhóm; nếu trần làm chưa đủ thì nới ra cho đủ.', { size: 23 }),
      p('Bản đầu tiên chia đều mỗi danh mục một món rồi vòng lại. Cách đó có vẻ cân đối nhưng sai nghiệp vụ: tiệc cưới vẫn bị nhét lẩu vào dù bảng đã chấm lẩu chỉ 2 điểm cho tiệc cưới, bởi vòng tròn luôn lấy đủ mỗi nhóm một món trước khi lấy món thứ hai của nhóm nào. Đổi sang lấy theo điểm thì nhóm ít hợp mới thật sự bị đẩy ra.', { size: 23 }),
      p('Kiểm chứng trên API thật với Gói Đồng Quê 7 món: tiệc cưới ra 3 khai vị, 3 món chính, 1 tráng miệng và không có lẩu; họp mặt gia đình có 2 món lẩu nướng; hội nghị có đồ uống mà không có lẩu. Gói Thượng Uyển 10 món cho thực đơn trị giá 2,625 triệu so với Gói Đồng Quê 7 món là 1,875 triệu. Kiểm thử: 45/45 đạt, trong đó 8 kiểm thử mới. Frontend build không lỗi.', { italic: true, size: 21 }),

      h2('Việc 11 — Gọn lại thanh menu khi đăng nhập quản trị'),
      p('Nhánh giao-dien/gon-thanh-menu, commit 1c3b995, ngày 01/09/2026.', { italic: true, size: 21 }),
      p('Sau khi thêm ba trang quản trị thực đơn, gói tiệc và không gian thì thanh menu có tới 15 mục. Chữ bị đẩy sang phải làm tên nhà hàng "Vườn Sen" bị xuống hai dòng, các mục như "Tra cứu đơn" cũng bị bẻ đôi. Nhìn như website bị lỗi.', { size: 23 }),
      fileTable([
        ['Header.jsx (sửa)', 'Gom năm mục quản trị vào một menu xổ xuống tên là "Quản trị". Trên thanh chỉ còn chiếm một chỗ thay vì năm. Menu đóng lại khi bấm ra ngoài, khi nhấn phím Esc, hoặc khi chọn một mục.'],
        ['global.css (sửa)', 'Thêm lớp dropdown và dropdown-panel. Trên điện thoại thì menu xổ thẳng trong dòng chứ không nổi lên trên. Thêm white-space nowrap cho tên nhà hàng và các mục để không bị bẻ dòng khi thanh chật.'],
      ]),
      p('Bỏ hai mục không cần thiết. Mục "Tra cứu đơn" chỉ hiện khi chưa đăng nhập, vì tra cứu bằng mã đơn là dành cho khách vãng lai không có tài khoản, ai đăng nhập rồi thì xem ở mục "Đơn của tôi". Mục "Đơn của tôi" không hiện với quản trị, vì quản trị xem toàn bộ đơn ở trang quản trị chứ không đặt tiệc cho chính mình. Nút đăng xuất cũng bỏ phần tên trong ngoặc, trước đây hiện "Đăng xuất (Quản trị hệ thống)" rất dài mà không thêm thông tin gì cần thiết.', { size: 23 }),
      p('Kết quả: quản trị thấy 9 mục thay vì 15, khách có tài khoản 9 mục, khách chưa đăng nhập 8 mục. Kiểm chứng trên trình duyệt: đăng nhập quản trị đếm đúng 9 mục, tên nhà hàng cao 37 pixel tức một dòng; menu xổ xuống đủ năm mục và nằm trọn trong khung nhìn; đóng đúng cả ba cách; trên màn hình điện thoại 375 pixel trang không bị tràn ngang.', { italic: true, size: 21 }),

      h2('Việc 12 — Bản đồ và ảnh thật'),
      p('Nhánh giao-dien/anh-va-ban-do, ba commit f66c1c7, 48e37f1 và 972bd6e, ngày 02/09/2026. Đây là hai hạng mục cuối của giai đoạn 24/08 - 31/08.', { italic: true, size: 21 }),

      p('Bản đồ', { bold: true, size: 24, after: 60 }),
      p('Trước đây không có khóa API thì chỗ bản đồ hiện dòng chữ "Đặt VITE_GOOGLE_MAPS_API_KEY trong file .env để hiện bản đồ". Đó là câu hướng dẫn cho người lập trình mà lại hiện ra cho khách xem, ở cả chân trang lẫn trang chi tiết không gian.', { size: 23 }),
      p('Google có hai đường nhúng bản đồ. Đường chính thức là Embed API, có tài liệu đầy đủ nhưng phải lấy khóa ở Google Cloud và bật thanh toán cho dự án, dù riêng Embed API không tính tiền. Đường thứ hai dùng maps.google.com với tham số output=embed, không nằm trong tài liệu chính thức nhưng chạy được và không cần tài khoản thanh toán. Nay hệ thống chọn theo việc có khóa hay không, đằng nào bản đồ cũng hiện. Thêm đường dẫn mở Google Maps ở tab mới để khách bấm vào là chỉ đường được.', { size: 23 }),

      p('Ảnh thật', { bold: true, size: 24, after: 60 }),
      p('Cơ sở dữ liệu vốn đã có ba chỗ lưu ảnh là spaces.thumbnail_url, dishes.image_url và gallery_images.url, nhưng không trang công khai nào đọc đến, chỗ nào cũng vẽ khối màu giữ chỗ. Nghĩa là có dán đường dẫn ảnh vào cơ sở dữ liệu thì trang web vẫn không hiện gì.', { size: 23 }),
      fileTable([
        ['Thumb.jsx (mới)', 'Ô ảnh dùng chung: có đường dẫn thì hiện ảnh thật, chưa có thì giữ khối màu như cũ. Đã thay cả sáu chỗ gồm trang chủ, danh sách không gian, chi tiết không gian, món bán chạy, lưới thư viện và ảnh phóng to.'],
        ['ImageUpload.jsx (mới)', 'Ô nhập ảnh cho trang quản trị, có hai cách điền: chọn tệp để tải thẳng lên Cloudinary bằng upload preset dạng unsigned, hoặc dán sẵn đường dẫn ảnh. Cách unsigned không cần API secret nên không lộ khóa ở phía trình duyệt.'],
        ['V5__cloudinary_images.sql (mới)', 'Gắn 23 đường dẫn ảnh vào 6 không gian, 11 món ăn và 6 ảnh thư viện. Đặt trong migration để ai tải mã nguồn về dựng lại cơ sở dữ liệu từ đầu cũng có ảnh, nhất là thư viện ảnh vì chưa có màn hình quản trị.'],
      ]),
      p('Sửa một lỗi có sẵn phát hiện khi làm: form quản trị không gian không gửi trường thumbnailUrl, form quản trị thực đơn không gửi imageUrl. Backend nhận thiếu trường thì hiểu là null rồi ghi đè, nghĩa là chỉ cần mở một không gian ra sửa bất kỳ thứ gì rồi lưu là ảnh của nó bị xóa mất.', { size: 23 }),

      p('Nguồn ảnh', { bold: true, size: 24, after: 60 }),
      p('Ảnh lấy từ Pexels, giấy phép cho dùng miễn phí kể cả mục đích thương mại và không bắt buộc ghi nguồn. Không dùng ảnh lấy từ trang web của nhà hàng, resort hay khu du lịch vì đó là tài sản có bản quyền của họ, tiểu luận nộp rồi lưu chiểu mà dùng ảnh đó là rủi ro. Dù giấy phép không bắt buộc, vẫn ghi đường dẫn trang gốc của từng ảnh vào tệp doc/DANH-SACH-ANH.md để khi bảo vệ có thể chỉ rõ ảnh ở đâu ra.', { size: 23 }),
      p('Một điểm suýt sai: tài khoản Cloudinary đang ở chế độ Dynamic folders nên tên thư mục không nằm trong đường dẫn, ảnh nằm thẳng ở gốc. Đã dò thử bốn kiểu đường dẫn trước khi ghi, chỉ kiểu không có thư mục trả về mã 200. Nếu đoán theo thư mục thì cả 23 ảnh đều hỏng.', { size: 23 }),
      p('Kiểm chứng: 23 đường dẫn đều trả về HTTP 200 trước khi ghi vào cơ sở dữ liệu. API công khai trả về đủ ảnh cho cả 6 không gian, 6 ảnh thư viện và 3 món bán chạy. Trên trình duyệt, 6 ảnh không gian tải xong ở kích thước thật 1600x1200. Kiểm thử 45/45 đạt, migration V5 chạy sạch.', { italic: true, size: 21 }),

      h2('Việc 13 — Tinh chỉnh trọng số thuật toán gợi ý'),
      p('Nhánh goi-y/tinh-chinh-trong-so, commit 356f4fb, ngày 02/09/2026. Đây là hạng mục của giai đoạn 16/09 - 23/09, làm sớm ba tuần.', { italic: true, size: 21 }),
      p('Đề cương ghi "kiểm thử và tinh chỉnh trọng số của thuật toán gợi ý dựa trên các tình huống thực tế". Đo thử ba tình huống thì phát hiện lỗi thiết kế chứ không phải chuyện chỉnh số cho khéo.', { size: 23 }),

      p('Kết quả đo trước khi chỉnh', { bold: true, size: 24, after: 60 }),
      fileTable([
        ['Tiệc cưới 300 khách', 'Ba phương án đều 62,5 điểm, cùng một sảnh'],
        ['Họp mặt gia đình 40 khách', 'Ba phương án đều 62,5 điểm, cùng một sảnh'],
        ['Hội nghị công ty 120 khách', '72,5 rồi 62,5 và 62,5'],
      ]),
      p('Ba phương án bằng điểm nhau nghĩa là thứ tự xếp hạng trở thành ngẫu nhiên. Truy ngược nguyên nhân: trong bốn tiêu chí cũ, hai tiêu chí đầu chỉ phụ thuộc không gian; tiêu chí ngân sách cho mọi tổ hợp 12,5 điểm khi khách không khai; tiêu chí lịch sử luôn bằng 0 khi hệ thống chưa có đơn nào. Kết quả là không tiêu chí nào phân biệt được gói tiệc.', { size: 23 }),

      p('Bốn thay đổi', { bold: true, size: 24, after: 60 }),
      fileTable([
        ['Thêm tiêu chí thứ năm', 'Tầm giá gói tiệc hợp với loại tiệc. Xếp gói theo giá một mâm rồi đối chiếu với tầm giá từng loại tiệc thường chọn: tiệc cưới và hội nghị chuộng gói cao cấp, họp mặt gia đình chuộng gói phổ thông, sinh nhật nằm giữa. Đây là tiêu chí duy nhất phân biệt được gói khi khách chưa khai ngân sách.'],
        ['Bỏ qua tiêu chí không dùng được', 'Thay vì cho mọi tổ hợp cùng một số điểm. Điểm cuối tính bằng tổng điểm chia tổng trọng số của những tiêu chí thực sự dùng được rồi quy về thang 100, nên thiếu tiêu chí không làm loãng thang điểm.'],
        ['Đa dạng không gian', 'Mỗi không gian chỉ xuất hiện một lần trong kết quả. Ít sảnh đủ sức chứa thì nới giới hạn để vẫn đủ ba phương án.'],
        ['RecommendationProperties.java (mới)', 'Đưa toàn bộ trọng số ra application.yml mục app.recommendation. Tinh chỉnh trọng số là việc phải làm đi làm lại, để ở tệp cấu hình thì đổi số rồi chạy lại, không phải dịch lại mã nguồn.'],
      ]),

      p('Kết quả đo sau khi chỉnh, cùng ba tình huống trên', { bold: true, size: 24, after: 60 }),
      fileTable([
        ['Tiệc cưới 300 khách', '100 rồi 87,5 và 86,67. Sảnh Sen Vàng với Gói Thượng Uyển đứng đầu'],
        ['Họp mặt gia đình 40 khách', '100 rồi 82,1 và 73,48. Nhà Rường Gỗ với Gói Đồng Quê đứng đầu'],
        ['Hội nghị công ty 120 khách', '88,24 rồi 82,35 và 74,12. Phòng Hội Nghị Lúa với Gói Thượng Uyển đứng đầu'],
      ]),
      p('Điểm đã phân biệt được, gói cao cấp lên đầu ở tiệc cưới và hội nghị, gói phổ thông lên đầu ở họp mặt gia đình, và mỗi tình huống trả về nhiều sảnh khác nhau.', { size: 23 }),
      p('Thêm năm kiểm thử khóa lại hành vi này. Một kiểm thử ban đầu viết sai kỳ vọng là "ba phương án luôn là ba sảnh khác nhau", chạy mới biết 300 khách thì chỉ có hai sảnh đủ sức chứa nên không thể ba sảnh. Mã nguồn đúng, kỳ vọng sai, đã tách thành hai kiểm thử riêng cho hai trường hợp.', { size: 23 }),
      p('Kiểm chứng: 50/50 kiểm thử đạt.', { italic: true, size: 21 }),

      h2('Việc 14 — Tích hợp gợi ý vào trang danh sách không gian'),
      p('Nhánh goi-y/tich-hop-trang-khong-gian, commit 102e9b8, ngày 02/09/2026. Hạng mục còn lại của giai đoạn 16/09 - 23/09.', { italic: true, size: 21 }),
      p('Đề cương ghi "tích hợp hệ thống gợi ý vào giao diện đặt tiệc và trang không gian". Trước đây khối gợi ý mới có ở bước 2 của form đặt tiệc, trang danh sách không gian chưa có.', { size: 23 }),
      fileTable([
        ['SuggestionBox.jsx (sửa)', 'Nới điều kiện hiện: trước bắt buộc có cả số khách lẫn loại sự kiện, nay chỉ bắt buộc số khách, đúng như ràng buộc của API. Ở trang không gian khách hay xem lướt trước khi biết mình tổ chức tiệc gì, bắt chọn loại sự kiện mới cho xem gợi ý thì mất tác dụng. Thêm hai thuộc tính để mỗi trang đặt tiêu đề và nhãn nút khác nhau.'],
        ['SpacesPage.jsx (sửa)', 'Thêm ô chọn loại sự kiện vào thanh lọc, ghi rõ là dùng để gợi ý chứ không lọc danh sách. Bấm chọn một phương án thì điền sẵn không gian, gói tiệc, số khách và loại sự kiện vào form đặt tiệc rồi chuyển thẳng sang bước 2, dùng chung kho Redux của form nên không phải truyền qua đường dẫn.'],
      ]),
      p('Kiểm chứng trên trình duyệt thật với 40 khách: chưa chọn loại sự kiện thì gợi ý Nhà Rường Gỗ, Cụm Chòi Sen và Phòng Hội Nghị, đều Gói Sen Vàng là gói tầm giữa. Chọn Họp mặt gia đình thì đổi sang Gói Đồng Quê là gói phổ thông, và Phòng Hội Nghị bị thay bằng Vườn Cau. Bấm chọn phương án thì chuyển sang trang đặt tiệc đúng bước 2, Nhà Rường Gỗ và Gói Đồng Quê đã được chọn sẵn, quay về bước 1 thấy số khách 40 và loại sự kiện còn nguyên.', { size: 23 }),
      p('Thêm một kiểm thử cho đường đi mới là gọi API không kèm loại sự kiện, phải trả về ba phương án và thực đơn rỗng chứ không được lỗi. Kiểm chứng: 51/51 kiểm thử đạt, frontend build không lỗi.', { italic: true, size: 21 }),

      h2('Việc 15 — Viết cơ sở lý thuyết về hệ thống gợi ý'),
      p('Tệp doc/CoSoLyThuyet_HeThongGoiY.docx, ngày 02/09/2026. Đây là hạng mục của giai đoạn 01/09 - 07/09, cũng là phần lý thuyết cho chương tương ứng của tiểu luận.', { italic: true, size: 21 }),
      p('Kế hoạch giai đoạn này gồm hai ý: nghiên cứu cơ sở lý thuyết về hệ thống gợi ý, và phân tích dữ liệu sẵn có của hệ thống làm đầu vào cho thuật toán. Cả hai nằm trong cùng một tài liệu, gồm tám mục và sáu bảng.', { size: 23 }),
      fileTable([
        ['Mục 1 và 2', 'Đặt vấn đề và ba nhóm phương pháp gợi ý: theo nội dung, cộng tác dựa trên lịch sử người dùng, và theo tiêu chí kèm tri thức nghiệp vụ. Có bảng so sánh năm chiều giữa ba nhóm, và phần về phương pháp kết hợp có trọng số.'],
        ['Mục 3', 'Vấn đề khởi động nguội với ba dạng: người dùng mới, đối tượng mới, hệ thống mới. Vườn Sen rơi vào hai dạng cùng lúc vì website mới triển khai và khách đặt tiệc không cần tài khoản.'],
        ['Mục 4', 'Lý do chọn phương pháp cho hệ thống này. Bảng đối chiếu sáu đặc điểm của bài toán với hệ quả tương ứng, ví dụ tần suất đặt tiệc rất thấp nên không xây được hồ sơ sở thích, do đó phương pháp cộng tác thuần khó áp dụng.'],
        ['Mục 5', 'Phân tích dữ liệu sẵn có, tách thành ba nhóm: dữ liệu mô tả đối tượng, dữ liệu tri thức nghiệp vụ, và dữ liệu lịch sử. Liệt kê từng trường cụ thể trong cơ sở dữ liệu kèm vai trò của nó trong thuật toán.'],
        ['Mục 6', 'Mô hình hóa bài toán: ràng buộc cứng, công thức chấm điểm, bảng năm tiêu chí kèm trọng số và điều kiện dùng được, cách bảo đảm đa dạng kết quả, và cách dựng thực đơn gợi ý.'],
        ['Mục 7 và 8', 'Phương pháp đánh giá dựa trên bảng recommendation_logs, và ghi chú về tài liệu tham khảo.'],
      ]),
      p('Tài liệu viết bám theo mã nguồn thật chứ không nói chung chung. Ba chỗ nối lý thuyết với thực tế của dự án: mục 5.3 ghi rõ bảng đơn mới có một bản ghi nên thành phần lịch sử gần như chưa có tác dụng, đúng là biểu hiện của khởi động nguội đã nêu ở mục 3; mục 6.2 giải thích vì sao phải chia cho tổng trọng số của riêng tập tiêu chí dùng được, chính là cách sửa lỗi ba phương án bằng điểm nhau ở Việc 13; mục 7 nêu cách đánh giá bằng tập tình huống mẫu, chính là cách đã dùng trong đợt tinh chỉnh trọng số.', { size: 23 }),
      p('Phần tài liệu tham khảo chỉ ghi tên tác giả và tên sách, cố ý không ghi năm xuất bản và số trang vì chưa kiểm chứng được, và có ghi dòng nhắc phải tự kiểm tra rồi trình bày theo mẫu trích dẫn của khoa trước khi nộp.', { size: 23 }),

      h2('Việc 16 — Viết phân tích yêu cầu và thiết kế hệ thống'),
      p('Tệp doc/PhanTichVaThietKe_HeThong.docx, ngày 03/09/2026. Gồm sáu mục và mười bảng.', { italic: true, size: 21 }),
      p('Đây là chương tài liệu mà đề cương yêu cầu: phân tích yêu cầu, thiết kế kiến trúc, thiết kế cơ sở dữ liệu và các chức năng chính. Trước đây phần kiến trúc mới mô tả sơ trong tệp README của dự án.', { size: 23 }),
      fileTable([
        ['Mục 1 Phân tích yêu cầu', 'Bốn tác nhân kèm quyền hạn, chín nhóm yêu cầu chức năng, năm yêu cầu phi chức năng kèm cách đáp ứng, và phần quy tắc nghiệp vụ.'],
        ['Mục 1.4 Quy tắc nghiệp vụ', 'Sáu quy tắc nhận tiệc gồm sức chứa, thời gian báo trước, gói vừa buổi, trùng lịch, tự hủy đơn trùng, chuyển trạng thái. Bảy bước tính giá kèm giải thích quy tắc miễn phí thuê không gian giảm dần.'],
        ['Mục 2 Thiết kế kiến trúc', 'Ba thành phần chạy độc lập, bốn tầng phía máy chủ, cách chia mã nguồn theo module nghiệp vụ kèm số tệp từng module, và cơ chế xác thực phân quyền hai lớp.'],
        ['Mục 3 Thiết kế cơ sở dữ liệu', '16 bảng chia bảy nhóm, bốn quan hệ chính, và năm quyết định thiết kế kèm lý do.'],
        ['Mục 4 và 5', 'Thiết kế API chia ba nhóm theo mức quyền, và 19 trang giao diện chia năm nhóm kèm luồng chính của khách.'],
        ['Mục 6 Hạn chế', 'Bảy hạn chế đã biết của thiết kế hiện tại, ghi trung thực để làm cơ sở cho hướng phát triển tiếp.'],
      ]),
      p('Tài liệu viết bám theo mã nguồn thật, mọi con số đều đếm từ dự án chứ không ước lượng: 9 module với số tệp cụ thể, 16 bảng, 14 controller, 19 trang, 51 kiểm thử. Sau Việc 18 và 19 các con số này đã được cập nhật lại thành 10 module, 15 controller và 72 kiểm thử. Phần quyết định thiết kế ghi lại cả những chỗ từng gặp sự cố, ví dụ lý do đổi kiểu TIMESTAMP sang DATETIME là vì MySQL có quy tắc ngầm làm hạn dùng của refresh token bị đặt lại mỗi lần cập nhật bản ghi.', { size: 23 }),

      h2('Việc 17 — Viết tổng quan công nghệ sử dụng'),
      p('Tệp doc/TongQuanCongNghe.docx, ngày 03/09/2026. Gồm chín mục và bảy bảng.', { italic: true, size: 21 }),
      p('Đề cương liệt kê cụ thể bảy công nghệ cần trình bày: Spring Boot, ReactJS, SQL, Spring Security, JWT, Google Maps API và ứng dụng trí tuệ nhân tạo. Tài liệu bao đủ cả bảy, kèm phần dịch vụ hỗ trợ và phần lý do lựa chọn.', { size: 23 }),
      fileTable([
        ['Mục 1', 'Bảng tổng hợp 15 công nghệ kèm phiên bản chính xác, lấy từ tệp pom.xml và package.json thật của dự án.'],
        ['Mục 2 và 3', 'Java 17 và Spring Boot, các thành phần đang dùng. Cơ sở dữ liệu MySQL khi chạy thật và H2 khi kiểm thử, cách truy cập dữ liệu bằng JPA và khi nào phải viết SQL tay, quản lý phiên bản cơ sở dữ liệu bằng Flyway.'],
        ['Mục 4', 'Bảo mật: lý do chọn JWT thay cho phiên trên máy chủ, cơ chế hai thẻ với thời hạn và cách xoay vòng, băm mật khẩu bằng BCrypt, phân quyền hai lớp.'],
        ['Mục 5', 'ReactJS và Vite, tiêu chí quyết định trạng thái nào đưa vào kho chung Redux và trạng thái nào để trong trang, điều hướng và cơ chế tự làm mới thẻ của Axios.'],
        ['Mục 6', 'Google Maps: so sánh hai đường nhúng và lý do hệ thống hỗ trợ cả hai.'],
        ['Mục 7', 'Ứng dụng trí tuệ nhân tạo, viết thẳng thắn về phạm vi thực tế.'],
        ['Mục 8 và 9', 'Dịch vụ và công cụ hỗ trợ gồm Cloudinary, Maven, Git, Docker. Bảng lý do lựa chọn từng công nghệ.'],
      ]),
      p('Mục 7 là phần cần cẩn thận nhất khi trình bày. Tên đề tài có cụm trí tuệ nhân tạo nhưng hệ thống gợi ý đã cài đặt không dùng mô hình học máy, nó thuộc nhóm gợi ý dựa trên tri thức và tiêu chí. Tài liệu ghi rõ điều này kèm ba lý do vì sao đó là lựa chọn có chủ đích chứ không phải làm thiếu: đề cương mục 6.2 đã giới hạn phạm vi, hệ thống mới nên chưa có dữ liệu để huấn luyện, và khách cần biết lý do được đề xuất nên phương pháp phải giải thích được. Phần trợ lý AI lúc viết còn ghi là chưa thực hiện kèm bảng so sánh hai phương án đang cân nhắc; sau Việc 18 và 19 thì cả hai phương án đều đã làm nên mục này đã được viết lại.', { size: 23 }),
      p('Mục 3.1 cũng ghi lại bài học từ sự cố thật: dùng H2 khi kiểm thử và MySQL khi chạy thật có rủi ro mã chạy đúng trên H2 nhưng sai trên MySQL, dự án đã gặp đúng tình huống đó với kiểu dữ liệu TIMESTAMP. Bài học là phải chạy thử trên hệ quản trị thật từ sớm.', { size: 23 }),

      h2('Việc 18 — Xây dựng trợ lý tư vấn trả lời theo dữ liệu hệ thống'),
      p('Ngày 03/09/2026, nhánh tro-ly-ai/hoi-dap-theo-du-lieu, hợp nhất qua Pull Request #13.', { italic: true, size: 21 }),
      p('Đây là hạng mục trọng tâm còn lại của đề tài. Kế hoạch xếp trợ lý AI vào giai đoạn 24/09 đến 15/10, và mốc 01/10 ghi rõ phải xây dựng cơ chế dự phòng khi không kết nối được dịch vụ AI bên ngoài. Bản này chính là cơ chế dự phòng đó nên được làm trước: nó chạy độc lập, không cần khóa API, không tốn phí và không phụ thuộc kết nối mạng.', { size: 23 }),
      p('Cách hoạt động: nhận diện ý định của câu hỏi bằng so khớp từ khóa, sau đó dựng câu trả lời từ dữ liệu thật trong cơ sở dữ liệu và tham số trong tệp cấu hình, không viết cứng nội dung. Thêm một không gian hay đổi tỉ lệ đặt cọc thì câu trả lời tự đổi theo. Đây đúng giới hạn đề cương đặt ra là trợ lý hoạt động trong phạm vi dữ liệu của hệ thống và không bịa thông tin không có.', { size: 23 }),
      fileTable([
        ['Intent', 'Mười một loại câu hỏi nhận ra được: chào hỏi, không gian, gói tiệc, thực đơn, chi phí, đặt cọc, khuyến mãi, quy trình đặt, tra cứu đơn, liên hệ và không hiểu.'],
        ['IntentDetector', 'Chuẩn hóa câu hỏi bằng cách bỏ dấu tiếng Việt rồi so khớp từ khóa, nên khách gõ nhanh không dấu vẫn hiểu được. Có lấy cả số khách trong câu để lọc đúng nhu cầu.'],
        ['AssistantService', 'Dựng câu trả lời cho từng loại, kèm câu hỏi gợi ý tiếp theo và đường dẫn trang liên quan.'],
        ['AssistantController', 'API POST /api/v1/assistant/ask, khách chưa đăng nhập vẫn hỏi được.'],
        ['ContactProperties', 'Đọc địa chỉ, điện thoại, email và giờ mở cửa từ tệp cấu hình, để trợ lý trả lời câu hỏi liên hệ mà không viết cứng trong mã nguồn.'],
        ['AssistantWidget.jsx', 'Hộp thoại ở góc dưới bên phải mọi trang. Mỗi câu trả lời kèm vài câu hỏi gợi ý để khách bấm tiếp, và có thể kèm nút chuyển sang trang liên quan. Đóng bằng phím Esc.'],
      ]),
      p('Thử với câu hỏi thật đã lộ ra hai lỗi mà chạy kiểm thử suông không thấy. Thứ nhất, câu Chi phí một tiệc bao nhiêu bị nhận thành lời chào, vì từ khóa hi khớp vào giữa chữ chi phí khi so theo kiểu chứa chuỗi; đã sửa bằng cách so theo từ trọn vẹn. Thứ hai, câu Chỗ bạn có chứa nổi 2000 khách không thì không nhận ra vì thiếu từ khóa; nay trả lời thẳng là không gian lớn nhất chứa được 800 khách nên chưa nhận được, kèm số điện thoại, thay vì gợi ý bừa một sảnh không vừa. Cả hai lỗi đều được viết kiểm thử hồi quy riêng.', { size: 23 }),
      p('Kiểm chứng: thử 13 câu hỏi qua API thật, gồm cả câu gõ không dấu và câu ngoài phạm vi. Trên trình duyệt đã mở hộp thoại, bấm câu gợi ý và nhận đúng hai sảnh chứa nổi 300 khách kèm nút chuyển trang. Kiểm thử tự động tăng từ 51 lên 62, trong đó 11 kiểm thử mới.', { size: 23 }),

      h2('Việc 19 — Thêm nhánh gọi mô hình ngôn ngữ ngoài'),
      p('Ngày 03/09/2026, nhánh tro-ly-ai/goi-mo-hinh-ngon-ngu, chưa hợp nhất.', { italic: true, size: 21 }),
      p('Hoàn tất hạng mục trợ lý AI. Bản ở Việc 18 chạy bằng so khớp từ khóa, lần này thêm nhánh gọi dịch vụ AI bên ngoài. Điểm cần nhấn mạnh là nhánh mới không thay thế bản cũ mà đặt bản cũ làm lưới đỡ bên dưới, đúng như đề cương yêu cầu.', { size: 23 }),
      p('Thứ tự trong AssistantFacade là có chủ đích: dựng câu trả lời dự phòng trước, rồi mới hỏi mô hình. Nghe thì ngược vì tốn thêm mấy câu truy vấn ngay cả khi mô hình sẽ trả lời. Đổi lại, đến lúc mô hình hỏng thì câu trả lời đã nằm sẵn trong tay, không phải chạy đi dựng lại giữa lúc đang lỗi. Cơ chế dự phòng chỉ đáng tin khi nó không cần thêm điều kiện gì mới chạy được.', { size: 23 }),
      fileTable([
        ['AssistantProperties', 'Đọc cấu hình. Khóa API bắt buộc truyền qua biến môi trường, không viết vào tệp cấu hình vì tệp đó nằm trong git.'],
        ['LlmClient', 'Giao diện cổng gọi ra ngoài. Đổi nhà cung cấp chỉ cần viết một lớp cài đặt mới, không đụng tới cách dựng ngữ cảnh hay cơ chế dự phòng.'],
        ['AnthropicLlmClient', 'Gọi Messages API. Đặt thời gian chờ ngắn vì khách đang ngồi đợi trong hộp thoại.'],
        ['SystemContextBuilder', 'Dựng chỉ dẫn hệ thống từ dữ liệu thật. Đây là chỗ quyết định trợ lý có bịa hay không.'],
        ['LlmAssistant', 'Chính sách gọi và bắt lỗi. Gọi hỏng kiểu gì cũng không ném lỗi ra ngoài.'],
        ['AssistantFacade', 'Cửa vào duy nhất, nối hai nhánh trả lời lại với nhau.'],
        ['AnswerSource', 'Cho biết câu trả lời đến từ mô hình ngôn ngữ hay từ cơ chế dự phòng.'],
      ]),
      p('Chống bịa: mô hình không được nối vào cơ sở dữ liệu, nó chỉ biết đúng những gì SystemContextBuilder chép vào chỉ dẫn. Toàn bộ không gian, gói tiệc, cả thực đơn kèm nhóm món, luật tính giá và thông tin liên hệ đều đọc thẳng từ cơ sở dữ liệu và tệp cấu hình mỗi lần hỏi, nên sửa giá trong trang quản trị là trợ lý biết ngay. Kèm theo năm luật cấm: không bịa, hỏi ngoài dữ liệu thì chỉ số điện thoại, không hứa giảm giá, không nhận giữ chỗ, và lờ đi yêu cầu bỏ qua chỉ dẫn. Mô hình cũng chỉ được thay phần lời văn, còn đường dẫn trang và câu hỏi gợi ý vẫn của hệ thống nên nó không đẩy khách sang trang không có thật được.', { size: 23 }),
      p('Kiểm chứng trên máy chủ thật với MySQL: khi không có khóa API thì trả lời bằng cơ chế dự phòng trong 0,0 giây; khi đặt khóa sai thì hệ thống gọi thật ra dịch vụ ngoài, nhận lỗi 401 rồi tự quay về cơ chế dự phòng, khách không thấy lỗi. Độ trễ nhảy từ 0,0 lên 0,5 đến 1,2 giây chứng tỏ có gọi ra mạng thật chứ không bỏ qua. Khóa API không xuất hiện lần nào trong nhật ký. Kiểm thử tự động tăng từ 62 lên 72, trong đó 10 kiểm thử mới dùng bản giả thay cho dịch vụ thật.', { size: 23 }),
      p('Còn một hạn chế phải ghi rõ: đường thành công thật sự chưa chạy lần nào vì chưa có khóa API. Phần đó hiện mới được phủ bằng kiểm thử với bản giả, tức là logic đúng nhưng câu trả lời thật từ mô hình thì cần chạy thử khi có khóa.', { size: 23 }),

      h2('Việc 20 — Bổ sung 285 ảnh và mở thư viện ảnh cho không gian, đánh giá'),
      p('Ngày 03/09/2026, migration V7 và V8, chưa hợp nhất.', { italic: true, size: 21 }),
      p('Trước đợt này mỗi không gian chỉ hiện được một ảnh đại diện, còn đánh giá của khách thì không có chỗ nào lưu ảnh. Chỗ chứa ảnh của toàn hệ thống tối đa chỉ 32 tấm.', { size: 23 }),
      p('Hai việc phải làm trước khi có chỗ đổ ảnh vào. Thứ nhất, bảng space_images đã tạo từ migration V1 nhưng suốt từ đầu dự án nằm không vì chưa có lớp nào ánh xạ tới; nay thêm lớp SpaceImage nên mỗi không gian chứa được nhiều ảnh. Thứ hai, bảng reviews không có cột ảnh nào, phải thêm bảng review_images ở V7.', { size: 23 }),
      fileTable([
        ['Nguồn ảnh', 'Lấy từ Pexels qua API chính thức, giấy phép cho dùng miễn phí kể cả mục đích thương mại. Tác giả từng ảnh ghi trong doc/NGUON-ANH-BO-SUNG.md.'],
        ['Phân bổ', '72 ảnh cho 6 không gian, 27 ảnh kèm 9 đánh giá, 54 ảnh cho thư viện theo 5 danh mục, 2 ảnh cho hai món còn thiếu.'],
        ['Chú thích', 'Mỗi ảnh không gian có chú thích tiếng Việt riêng, ví dụ "Bàn tiệc nhìn ra mặt nước", "Lối cầu gỗ nối các chòi", chứ không để trống.'],
        ['Giao diện', 'Trang chi tiết không gian có dải ảnh nhỏ, bấm vào thì đổi ảnh ở khung lớn kèm chú thích. Thẻ đánh giá hiện ảnh khách chụp, bấm mở ảnh gốc.'],
      ]),
      p('Một chi tiết kỹ thuật suýt gây lỗi: dự án đặt open-in-view bằng false, nên đọc danh sách ảnh ở tầng controller sẽ ném lỗi nạp trễ. Phải mở giao dịch cho phương thức đọc đánh giá, nếu không trang đánh giá vỡ ngay khi có ảnh đầu tiên. Trang danh sách không gian thì cố ý không trả về thư viện ảnh, vì lấy thêm ảnh chi tiết tốn một câu truy vấn cho mỗi không gian mà trang đó chỉ hiện một ảnh mỗi thẻ.', { size: 23 }),
      p('Kiểm chứng: 155/155 ảnh tải lên không lỗi tấm nào, migration chạy sạch trên cả H2 và MySQL thật. Trên trình duyệt đã bấm ảnh thứ ba, ảnh lớn đổi đúng và chú thích hiện theo. Hiện chỉ 9 trong 27 ảnh đánh giá hiển thị, vì dữ liệu mẫu có 9 đánh giá nhưng mới 3 cái được duyệt; số còn lại sẽ hiện khi quản trị duyệt, đúng quy trình kiểm duyệt của hệ thống.', { size: 23 }),

      h2('Việc 21 — Trang chi tiết món ăn và 130 ảnh món'),
      p('Ngày 03/09/2026, migration V9 và V10, chưa hợp nhất.', { italic: true, size: 21 }),
      p('Trước đây khách chỉ thấy tên món, một dòng mô tả và giá. Muốn biết món có nguyên liệu gì, làm mất bao lâu, có cay không thì phải gọi điện hỏi. Món cũng chưa có đường dẫn riêng nên không có cách nào trỏ tới một món cụ thể.', { size: 23 }),
      fileTable([
        ['Cột thêm vào bảng dishes', 'slug làm đường dẫn, ingredients, preparation, order_note, portion_desc và prep_minutes.'],
        ['Nội dung', 'Viết cho đủ cả 26 món theo món ăn miền Tây thật, không phải chữ lấp chỗ trống.'],
        ['Mục lưu ý khi đặt', 'Cố ý viết thẳng điểm trừ: heo quay phải đặt trước một ngày và tính tiền theo cân thực tế; cá kèo có vị đắng ở phần ruột; bò lá lốt nướng than có khói nên không đặt được cho phòng máy lạnh kín; rượu nếp than không phục vụ người dưới 18 tuổi.'],
        ['Bảng dish_images', 'Thư viện ảnh riêng cho từng món, 26 món mỗi món 5 ảnh.'],
        ['Trang /thuc-don/<slug>', 'Dải ảnh bấm đổi, nguyên liệu, cách chế biến, bảng thông số gồm giá, khẩu phần và thời gian bếp cần, cùng khối lưu ý viền vàng.'],
      ]),
      p('Lý do viết thẳng những điểm trừ trong mục lưu ý: khách đọc trước thì bớt gọi điện hỏi, mà cũng bớt chuyện đặt xong mới phát hiện không ăn được. Đây là thông tin nhà hàng thật nào cũng phải nói với khách qua điện thoại, đưa lên web chỉ là chuyển chỗ nói.', { size: 23 }),
      p('Món thêm mới từ trang quản trị sẽ tự sinh đường dẫn từ tên món, trùng tên thì nối thêm mã món phía sau. Không dùng số ngẫu nhiên vì đường dẫn cần ổn định, sửa tên món xong mở lại vẫn phải ra đúng trang đó.', { size: 23 }),
      p('Kiểm chứng: 130/130 ảnh tải lên đủ cho cả 26 món, kiểm thử 72/72 đạt, migration chạy sạch trên MySQL thật. Trên trình duyệt trang món Cá lóc nướng trui hiện đúng giá 395.000đ, khẩu phần 4-6 người, bếp cần khoảng 40 phút, kèm cảnh báo cá còn xương dăm.', { size: 23 }),
      p('Tổng cộng hai đợt: 285 ảnh trên Cloudinary. Cơ sở dữ liệu tăng từ 16 lên 18 bảng, giao diện từ 19 lên 20 trang.', { size: 23 }),

      h2('Việc 22 — Đặt món lẻ: giao tận nhà hoặc tới ăn tại chỗ'),
      p('Ngày 06/09/2026, migration V11, nhánh dat-mon/anh-chi-tiet-va-dat-mon-le, chưa hợp nhất.', { italic: true, size: 21 }),
      p('Mục đích kinh doanh: nhận nhóm khách nhỏ chưa cần đặt trọn sảnh tiệc lớn, và nhận khách chỉ muốn mua món mang về. Trước đây khách muốn ăn ở Vườn Sen thì hoặc tới gọi tại bàn, hoặc phải đặt trọn một gói tiệc với thực đơn cố định không tùy biến được.', { size: 23 }),
      p('Đây là luồng đặt thứ hai, tách hẳn khỏi bảng bookings. Lý do không dùng chung: đơn đặt tiệc gắn với một không gian và một gói trọn gói, tính tiền theo mâm; còn đơn đặt món tính theo từng phần khách chọn, có thể không cần không gian nào. Nhét cả hai vào một bảng thì quá nửa số cột luôn để trống.', { size: 23 }),
      fileTable([
        ['Bảng dish_orders', 'Một đơn đặt món: hình thức nhận, thông tin khách, thời điểm nhận, tiền món, phí giao, thuế, tổng cộng và trạng thái.'],
        ['Bảng dish_order_items', 'Từng dòng món trong đơn, có chép lại tên món và đơn giá.'],
        ['DishOrderPricing', 'Tính phí giao và thuế. Tách riêng, không đụng cơ sở dữ liệu nên kiểm thử được mà không cần dựng cả ứng dụng.'],
        ['DishOrderService', 'Quy tắc nhận đơn: món có bán lẻ được không, bếp có kịp làm không, đơn giao đã đạt mức tối thiểu chưa.'],
        ['Giỏ món phía giao diện', 'Giữ trong localStorage để khách xem trang khác rồi quay lại vẫn còn. Chỉ lưu mã món và giá để hiện tạm, số tiền thật luôn hỏi lại máy chủ.'],
      ]),
      p('Ba quyết định thiết kế đáng nói. Thứ nhất, chép lại tên món và đơn giá vào từng dòng đơn chứ không chỉ giữ khóa ngoại: bảng giá thay đổi theo thời gian mà hóa đơn cũ phải giữ đúng con số đã chốt với khách, nếu đọc giá qua khóa ngoại thì hôm sau tăng giá là đơn cũ tự đổi theo. Thứ hai, thời gian báo trước lấy theo món lâu nhất trong đơn chứ không phải một mức cứng, dùng lại cột prep_minutes đã nhập ở Việc 21. Thứ ba, thuế tính trên tiền món chứ không tính trên phí giao, vì phí giao là khoản thu hộ phần vận chuyển, gộp vào rồi đánh thuế lên nó làm con số khó giải thích khi khách đối chiếu hóa đơn.', { size: 23 }),
      p('Món tính giá linh hoạt không đặt lẻ được. Heo quay tính theo cân, phải quay xong cân lên mới biết tiền, mà đơn đặt món chốt tiền ngay lúc gửi. Hệ thống nói thẳng và chỉ đường sang đặt tiệc, hơn là để khách đặt rồi mới báo lại.', { size: 23 }),
      p('Kiểm chứng 10 tình huống trên API thật: tạm tính đơn nhỏ ra 224.400đ; đơn từ 500.000đ được miễn phí giao; heo quay bị từ chối; đặt sát giờ bị từ chối kèm giờ sớm nhất nhận được; đơn giao 45.000đ bị từ chối vì dưới mức tối thiểu; tạo đơn thật và tra cứu lại bằng mã; đơn ăn tại chỗ 60 khách bị chỉ sang đặt tiệc theo gói; khu quản trị chưa đăng nhập trả về 403. Kiểm thử tự động tăng từ 72 lên 85. Trên trình duyệt đã đặt trọn một đơn từ giao diện, giỏ tự xóa và nhảy sang trang tra cứu.', { size: 23 }),
      p('Hai lỗi tự phát hiện khi xem lại giao diện: form bị chật vì dùng thẻ label bọc input trong khi dự án dùng lớp fgroup và form-row, và mất giờ ở dòng thời điểm nhận vì gọi nhầm hàm định dạng chỉ có ngày. Cả hai đã sửa.', { size: 23 }),
      p('Phần này nằm ngoài phạm vi đề cương, cần hỏi ý kiến giảng viên hướng dẫn xem có đưa vào tiểu luận hay để riêng làm hướng phát triển.', { size: 23 }),

      tieuDeAnh(),
      ...anh('viec-22-dat-mon-le', 'Hình 22.1. Giỏ hai món, chọn giao tận nhà, bảng tạm tính tính tiền món, phí giao và VAT.'),

      h2('Việc 23 — Tách kế hoạch thành hai chặng và xác định phạm vi kiểm thử'),
      p('Ngày 07/09/2026, chỉ sửa tài liệu, không đụng mã nguồn.', { italic: true, size: 21 }),
      p('Đối chiếu lại kế hoạch giảng viên giao thì đề tài có hai chặng nối tiếp. Chặng một xây website. Chặng hai lấy chính website đó làm đối tượng thực nghiệm để xây hệ thống kiểm thử tự động bằng Selenium WebDriver theo mô hình Page Object Model, chạy tự động trên GitHub Actions. Website vì vậy không phải sản phẩm cuối mà là vật thí nghiệm.', { size: 23 }),
      fileTable([
        ['21130004_..._KeHoachVaTienDo.docx', 'Tách phần kế hoạch thành hai mục. Mục I, kế hoạch thực hiện web, giữ nguyên các mốc cũ. Mục II, kế hoạch thực hiện kiểm thử, chép nguyên văn 18 mốc giảng viên giao từ 16/08 đến 16/12. Thêm cột Tiến độ cho cả hai bảng.'],
        ['PhamVi_DoiTuong_YeuCau_KiemThu.docx (mới)', 'Mục thứ ba của mốc 16/08 – 23/08: chọn website Vườn Sen làm đối tượng, khoanh 13 luồng nghiệp vụ cần kiểm thử, ghi rõ phần ngoài phạm vi, bảy yêu cầu đối với bộ kiểm thử và ba vấn đề kỹ thuật phải giải quyết.'],
      ]),
      p('Cần phân biệt để tránh nhầm khi báo cáo: các kiểm thử hiện có của website là kiểm thử đơn vị viết bằng JUnit, chạy phía máy chủ và không mở trình duyệt. Bộ kiểm thử của chặng hai là kiểm thử giao diện bằng Selenium, điều khiển trình duyệt thật, phải xây mới hoàn toàn.', { size: 23 }),
      p('Ghi nhận thật: ba mốc đầu của chặng hai, từ 16/08 đến 07/09, bị trễ vì khoảng thời gian đó dùng để xây website.', { size: 23 }),

      khongAnh('việc lập kế hoạch, không có màn hình.'),

      h2('Việc 24 — Nghiên cứu lý thuyết kiểm thử và đặc tả 108 test case'),
      p('Ngày 12/09/2026, chỉ viết tài liệu, ứng với bốn mốc đầu của chặng hai.', { italic: true, size: 21 }),
      fileTable([
        ['TongQuan_KiemThuPhanMem.docx (mới)', 'Mốc 16/08 – 23/08: bốn cấp độ kiểm thử, so sánh kiểm thử thủ công và tự động, đặc thù của kiểm thử ứng dụng web, vai trò của kiểm thử tự động trong giai đoạn bảo trì.'],
        ['NghienCuu_SeleniumWebDriver.docx (mới)', 'Mốc 24/08 – 31/08: kiến trúc ba tầng, khởi tạo và tùy chọn chạy ẩn, điều hướng trình duyệt, tám chiến lược tìm phần tử, cơ chế chờ, các lỗi thường gặp.'],
        ['NghienCuu_PageObjectModel.docx (mới)', 'Mốc 01/09 – 07/09: bảy nguyên tắc tổ chức Page Object, ranh giới giữa thành phần giao diện và logic kiểm thử, ưu điểm về bảo trì, tái sử dụng và mở rộng, cấu trúc thư mục dự kiến.'],
        ['DacTa_TestCase.docx (mới)', 'Mốc 08/09 – 15/09: 108 test case cho 13 luồng nghiệp vụ, gồm 67 trường hợp thuận và 41 trường hợp nghịch, trong đó 59 trường hợp ưu tiên cao.'],
      ]),
      p('Mọi kết quả mong đợi trong bản đặc tả đều đối chiếu với mã nguồn thật: tham số lấy từ application.yml, công thức lấy từ PricingService và DishOrderPricing, thông báo lỗi chép nguyên văn từ các lớp dịch vụ. Việc đối chiếu bắt được một lỗi của chính tài liệu: ví dụ tiệc 200 khách ban đầu ghi VAT 7.200.000đ, tức 8% của riêng tiền ăn. Đọc mã mới thấy VAT tính trên tiền ăn cộng phí thuê sau giảm giá, đúng phải là 7.440.000đ. Đã sửa trước khi xuất tài liệu.', { size: 23 }),
      p('Tiến độ chặng hai sau việc này: xong 4/18 mốc. Ba mốc đầu hoàn thành trễ lần lượt 20, 12 và 5 ngày; mốc thứ tư xong sớm 3 ngày.', { size: 23 }),

      khongAnh('việc nghiên cứu và viết đặc tả, kết quả nằm trong tài liệu DacTa_TestCase.docx.'),

      h2('Việc 25 — Gắn thuộc tính data-test mẫu cho giao diện'),
      p('Ngày 12/09/2026, chưa commit, đang chờ duyệt quy ước đặt tên.', { italic: true, size: 21 }),
      p('Chuẩn bị cho chặng hai. Nếu locator của Selenium bám vào lớp CSS thì chỉ cần đổi giao diện là hỏng hàng loạt kịch bản. Thuộc tính data-test không phục vụ trình bày nên không bị sửa khi làm đẹp giao diện. Phải làm trước khi viết Page Object, làm sau thì phải sửa lại toàn bộ locator.', { size: 23 }),
      fileTable([
        ['StateBlock.jsx', 'Gắn data-test cho ba khối dùng chung ở mọi màn hình: loading, error và empty. Sửa một chỗ nhưng phủ được trạng thái tải, thông báo lỗi và trạng thái rỗng của toàn bộ ứng dụng.'],
        ['LoginPage.jsx', 'Làm mẫu cho một trang: login-form, email, password, submit-login.'],
      ]),
      p('Quy ước: đặt tên theo vai trò chứ không theo hình thức, viết kebab-case, phần tử trong danh sách thêm hậu tố slug. Còn 14 tệp giao diện chưa gắn, chờ duyệt quy ước rồi mới làm tiếp.', { size: 23 }),

      khongAnh('thuộc tính data-test không hiện trên giao diện.'),

      h2('Việc 26 — Trang thống kê kinh doanh bằng biểu đồ'),
      p('Ngày 12/09/2026, chưa commit.', { italic: true, size: 21 }),
      p('Hoàn thành hạng mục báo cáo doanh thu và tỉ lệ lấp đầy không gian. Trang /quan-tri/thong-ke gồm bốn ô số liệu tổng quan và bảy biểu đồ: doanh thu theo tháng, đơn theo trạng thái, tỉ lệ chọn gói tiệc, doanh thu theo không gian, đơn theo loại sự kiện, tỉ lệ lấp đầy không gian, và đơn đặt món lẻ theo tháng.', { size: 23 }),
      fileTable([
        ['modules/statistic (mới)', 'Module thứ 12 của máy chủ. Một đường dẫn GET /api/v1/admin/statistics trả đủ số liệu cho cả trang trong một lần gọi. Bỏ trống khoảng ngày thì lấy 12 tháng gần nhất; chặn khoảng dài quá 3 năm.'],
        ['ThongKeAggregator.java (mới)', 'Toàn bộ cách gộp số liệu. Không đụng cơ sở dữ liệu nên kiểm thử được mà không cần dựng ứng dụng, cùng cách làm với DishOrderPricing.'],
        ['BookingRepository, DishOrderRepository', 'Thêm truy vấn nạp đơn theo khoảng ngày. Đơn đặt tiệc nạp sẵn không gian và gói để mỗi đơn không sinh thêm một truy vấn.'],
        ['AdminStatsPage.jsx (mới)', 'Vẽ biểu đồ bằng thư viện recharts 3.10.1, thêm mục Thống kê vào menu quản trị.'],
        ['ThongKeAggregatorTest.java (mới)', '10 kiểm thử chốt các quy ước đếm.'],
      ]),
      p('Ba quy ước nghiệp vụ được chốt bằng kiểm thử, vì sai quy ước đếm thì biểu đồ vẫn vẽ ra đẹp, chỉ có con số là sai. Thứ nhất, doanh thu chỉ tính đơn đã xác nhận và đã hoàn thành; đơn chờ chưa chắc thành tiền, đơn hủy thì không. Thứ hai, tỉ lệ lấp đầy đếm số buổi khác nhau chứ không đếm số đơn, nếu không thì dữ liệu lỗi có hai đơn trùng buổi sẽ đẩy tỉ lệ vượt 100%. Thứ ba, doanh thu đặt món ghi theo tháng phục vụ chứ không theo tháng đặt, cho khớp với đơn đặt tiệc vốn ghi theo ngày tổ chức.', { size: 23 }),
      p('Một vấn đề tự phát hiện: thư viện biểu đồ đẩy tệp JavaScript chính lên 749 kB, bắt mọi khách vào xem thực đơn cũng phải tải một thư viện chỉ dành cho quản trị. Đã tách trang thống kê ra tải riêng bằng React.lazy: tệp chính còn 352 kB, phần biểu đồ 398 kB chỉ tải khi mở trang thống kê.', { size: 23 }),
      p('Kiểm chứng: kiểm thử tự động tăng từ 85 lên 95, build giao diện đạt. Hạn chế: chưa thử trên trình duyệt, và cơ sở dữ liệu chưa có đơn mẫu nào nên mở trang hiện chỉ thấy thông báo chưa có đơn.', { size: 23 }),

      tieuDeAnh(),
      ...anh('viec-26-thong-ke-1', 'Hình 26.1. Nửa trên trang thống kê năm 2026: bốn ô số liệu, doanh thu theo tháng, đơn theo trạng thái, tỉ lệ chọn gói tiệc.'),
      ...anh('viec-26-thong-ke-2', 'Hình 26.2. Nửa dưới: doanh thu theo không gian, đơn theo loại sự kiện, tỉ lệ lấp đầy không gian, đơn đặt món lẻ theo tháng.'),
      p('Ảnh chụp sau khi sửa lỗi trang không hiện số liệu ở Việc 36. Số liệu là đơn nhập tay và đơn do kiểm thử tạo, không phải dữ liệu kinh doanh thật; trạng thái 6 đơn món và 1 đơn tiệc được chuyển qua API quản trị để biểu đồ có số liệu minh họa.', { italic: true, size: 21 }),

      h2('Việc 27 — Ẩn đường dẫn ảnh trên giao diện'),
      p('Ngày 13/09/2026, chưa commit.', { italic: true, size: 21 }),
      fileTable([
        ['ImageUpload.jsx', 'Bỏ ô chữ hiện nguyên đường dẫn Cloudinary dài hơn trăm ký tự ở form món ăn và form không gian. Thay bằng ảnh xem trước lớn hơn cùng hai nút Đổi ảnh và Bỏ ảnh. Đường dẫn vẫn nằm trong dữ liệu form và gửi lên máy chủ như cũ.'],
        ['Lightbox.jsx (mới)', 'Khung xem ảnh phóng to dùng chung, giao diện giống trang thư viện: Esc để đóng, phím mũi tên để chuyển ảnh.'],
        ['ReviewsPage.jsx', 'Bấm ảnh khách gửi thì phóng to ngay trong trang, thay vì mở đường dẫn gốc ra thẻ mới làm lộ đường dẫn trên thanh địa chỉ.'],
      ]),
      p('Một lỗi tự phát hiện: CSS giới hạn kích thước ảnh đánh giá qua bộ chọn .anh-danh-gia a, mà phần tử bọc ảnh đã đổi từ thẻ a sang button, nên ảnh sẽ bung ra kích thước gốc và vỡ khung. Đã đổi bộ chọn.', { size: 23 }),
      p('Giới hạn cần nói rõ: không giấu tuyệt đối được. Trình duyệt phải biết đường dẫn thì mới tải được ảnh, người rành kỹ thuật vẫn xem được qua công cụ dành cho nhà phát triển. Muốn giấu hẳn thì ảnh phải đi vòng qua máy chủ, chậm hơn và tốn băng thông. Kiểm chứng: build giao diện đạt, chưa thử trên trình duyệt.', { size: 23 }),

      tieuDeAnh(),
      ...anh('viec-27-form-sua-mon', 'Hình 27.1. Form sửa món chỉ còn ảnh xem trước cùng hai nút Đổi ảnh, Bỏ ảnh; không còn ô chữ hiện đường dẫn Cloudinary.'),
      ...anh('viec-27-anh-danh-gia-phong-to', 'Hình 27.2. Bấm ảnh khách gửi kèm đánh giá thì phóng to ngay trong trang, có nút chuyển ảnh, không mở đường dẫn gốc ra thẻ mới.'),

      h2('Việc 28 — Cho thuê không gian không kèm gói tiệc'),
      p('Ngày 13/09/2026, migration V12, chưa commit.', { italic: true, size: 21 }),
      p('Trước đây gói tiệc là bắt buộc ở mọi tầng, khách chỉ cần mặt bằng và tự lo ăn uống thì không đặt được. Bước 2 của form đặt tiệc nay có thêm lựa chọn Không cần gói tiệc. Khách vẫn phải chọn rõ một trong hai, để không lỡ bỏ trống rồi tưởng giá đã gồm tiệc.', { size: 23 }),
      p('Cách tính tiền dùng lại công thức có sẵn, không thêm luật mới: không có mâm và không có tiền ăn; phí thuê tính đủ vì mức giảm phí thuê vốn dựa trên tiền ăn; không áp mức mâm tối thiểu của sảnh; vẫn giảm 5% khi đặt sớm, VAT 8% và cọc 30%. Ví dụ thuê riêng Sảnh Sen Vàng: 12.000.000đ cộng VAT 960.000đ, tổng 12.960.000đ, cọc 3.888.000đ.', { size: 23 }),
      fileTable([
        ['V12__thue_rieng_khong_gian.sql (mới)', 'Cho phép cột package_id để trống. Ba cột số mâm, đơn giá và tiền ăn vẫn giữ bắt buộc và ghi 0, để các phép cộng tiền ở báo cáo không phải xử lý giá trị rỗng.'],
        ['PricingService.java', 'Tính tiền cho đơn không kèm gói.'],
        ['BookingService.java', 'Ba chỗ kiểm tra lịch là gói vừa buổi, thuê trọn ngày và trùng lịch đều xử lý được đơn không gói.'],
        ['Booking.java, BookingResponse.java', 'Chỗ nào cần tên gói thì trả về nhãn Chỉ thuê không gian, nên trang quản trị không phải sửa.'],
        ['BookingPage, TrackBookingPage, MyBookingsPage', 'Thêm lựa chọn ở bước 2, ẩn các dòng số mâm và tiền ăn khi đơn không kèm gói.'],
      ]),
      p('Hai lỗi ngầm chặn được trong lúc làm. Thứ nhất, truy vấn thống kê ở Việc 26 nối bảng gói bằng JOIN thường, nên đơn không có gói sẽ bị loại khỏi thống kê mà không báo lỗi gì; đã đổi sang LEFT JOIN. Thứ hai, luật thuê trọn ngày đọc số giờ của gói, gặp đơn không gói sẽ văng lỗi đúng lúc quản trị xác nhận đơn; nay đơn không gói được tính là một buổi.', { size: 23 }),
      p('Kiểm chứng: kiểm thử tự động tăng lên 98, migration V12 chạy thành công trên H2, build giao diện đạt. Chưa chạy V12 trên MariaDB thật, chưa thử trên trình duyệt.', { size: 23 }),

      tieuDeAnh(),
      ...anh('viec-28-buoc-2-khong-can-goi', 'Hình 28.1. Bước 2 chọn Sảnh Sen Vàng và Không cần gói tiệc: tạm tính chỉ gồm phí thuê 12.000.000 đ cộng VAT, tổng 12.960.000 đ.'),
      ...anh('viec-28-tra-cuu-chi-thue-khong-gian', 'Hình 28.2. Tra cứu đơn thuê riêng: cột gói tiệc ghi Chỉ thuê không gian. Đơn cách hơn 60 ngày nên được giảm 5% đặt sớm trên phí thuê.'),

      h2('Việc 29 — Xử lý nhiều người thao tác cùng lúc'),
      p('Ngày 13/09/2026, chưa commit.', { italic: true, size: 21 }),
      p('Rà mã nguồn tìm các chỗ hai người cùng thao tác có thể gây lỗi. Các chỗ tìm được đều chung một gốc: kiểm tra xong rồi mới ghi, mà giữa hai bước không khóa gì. Trước việc này, cả dự án không có chỗ nào dùng khóa.', { size: 23 }),
      fileTable([
        ['Xác nhận đơn trùng lịch', 'Hai quản trị cùng xác nhận một đơn thuê trọn ngày và một đơn buổi khác cùng ngày; cả hai cùng đọc thấy ngày còn trống nên cùng được xác nhận. Sửa bằng khóa bi quan: khóa dòng không gian bằng SELECT ... FOR UPDATE trước khi kiểm tra lịch, kèm mức cô lập READ_COMMITTED.'],
        ['Trùng mã đơn', 'Mã đơn tính bằng số đơn trong ngày cộng 1, nên hai khách gửi cùng giây ra trùng mã; khách thứ hai nhận lỗi 500 và mất đơn. Sửa bằng khóa lạc quan: database báo trùng thì thử lại trong giao dịch mới, tối đa 5 lần, chờ ngẫu nhiên giữa các lần. Áp dụng cho cả đơn đặt tiệc và đơn đặt món.'],
        ['Trùng email khi đăng ký', 'Database đã chặn đúng nhưng người chậm hơn nhận lỗi 500. Nay báo Email này đã được đăng ký.'],
        ['GlobalExceptionHandler.java', 'Lưới an toàn cuối: lỗi trùng dữ liệu hoặc lỗi khóa nào còn lọt thì trả mã 409 kèm câu dễ hiểu thay vì lỗi 500.'],
      ]),
      p('Lý do phải có READ_COMMITTED: MariaDB mặc định dùng REPEATABLE_READ, mọi câu đọc trong một giao dịch nhìn theo ảnh chụp dữ liệu từ lần đọc đầu tiên. Nếu chỉ khóa, người đến sau dù đã chờ lấy được khóa vẫn đọc ảnh chụp cũ, không thấy đơn vừa được xác nhận và vẫn xác nhận trùng.', { size: 23 }),
      p('Kiểm chứng bằng cách chạy ngược: có khóa thì ba kịch bản đồng thời đều đạt, mỗi kịch bản xác nhận chạy 20 vòng. Gỡ khóa tạm thời thì kịch bản thuê trọn ngày trượt ngay vòng đầu, hai đơn cùng được xác nhận. Sau đó khôi phục và so từng byte với bản có khóa. Bước chạy ngược này chứng minh lỗi có thật và kiểm thử đủ nhạy để bắt được nó. Log của kiểm thử gửi đơn đồng thời cũng cho thấy 4 trên 5 luồng thật sự bị trùng mã ở lần ghi đầu, cuối cùng cả 5 đơn đều thành công với mã khác nhau.', { size: 23 }),
      p('Một sai sót tự phát hiện: kịch bản kiểm thử viết lần đầu dùng hai đơn cùng buổi, và vẫn đạt khi đã gỡ khóa. Nguyên nhân là khi xác nhận, hai đơn cùng buổi tự hủy chéo đơn của nhau, hai giao dịch ghi chung hai dòng nên database tự chặn một bên. Kịch bản đó không bao giờ ra trùng lịch mà chỉ làm một quản trị nhận lỗi 500. Đã viết lại theo kịch bản thuê trọn ngày, giữ kịch bản cùng buổi làm bài riêng.', { size: 23 }),
      p('Kiểm chứng: kiểm thử tự động tăng lên 107, đạt 107/107. Hạn chế: kiểm thử chạy trên H2 vốn mặc định đã là READ_COMMITTED, nên phần mức cô lập mới dựa trên lập luận, chỉ kiểm chứng được khi chạy trên MariaDB thật.', { size: 23 }),

      tieuDeAnh(),
      ...anh('viec-29-hai-don-cung-sanh-cung-buoi', 'Hình 29.1. Hai khách gửi đơn cùng lúc cho Sảnh Sen Vàng, cùng ngày 27/12/2026, cùng buổi tối: cả hai đơn đều được nhận, trạng thái Chờ xác nhận, mã đơn không trùng.'),
      ...anh('viec-29-xac-nhan-mot-don-don-kia-tu-huy', 'Hình 29.2. Quản trị xác nhận đơn của khách A thì đơn của khách B tự chuyển sang Đã hủy.'),
      p('Hai đơn trong ảnh do công cụ chụp ảnh gửi đồng thời bằng hai luồng qua API, rồi bấm Xác nhận trên giao diện quản trị. Trường hợp hai quản trị bấm xác nhận đúng cùng một thời điểm không tái hiện được bằng tay, phần đó được kiểm chứng bằng kiểm thử XuLyDongThoiDatTiecTest chứ không bằng ảnh.', { italic: true, size: 21 }),

      // ---------------- 14/09 ----------------
      h1('Ngày 14/09/2026'),
      p('Mục tiêu: dựng xong hệ thống kiểm thử tự động bằng Selenium, là sản phẩm chính của chặng hai.', { size: 23 }),

      h2('Việc 30 — Gắn thuộc tính data-test cho toàn bộ giao diện'),
      p('Ngày 14/09/2026, chưa commit.', { italic: true, size: 21 }),
      p('Việc 25 mới gắn một tệp làm mẫu. Lần này gắn cho toàn bộ giao diện: 149 thuộc tính data-test trên các trang không gian, thực đơn, đặt tiệc, đặt món, đánh giá, trợ lý và bốn màn hình quản trị. Kịch bản kiểm thử tìm phần tử theo thuộc tính này thay vì theo lớp CSS hay chữ hiển thị, nên sửa giao diện không làm hỏng kiểm thử.', { size: 23 }),
      fileTable([
        ['4 script dt-1 đến dt-4', 'Chia theo nhóm màn hình, chạy trên cùng một bộ máy dt-ap-dung.js. Mỗi script sửa xong thì build lại để biết ngay chỗ nào hỏng.'],
        ['Quy ước tên', 'Đặt theo vai trò chứ không theo hình thức: space-card, submit-booking, order-total, admin-booking-row. Các khối dùng chung có tên thống nhất: loading, error, empty.'],
        ['Thumb.jsx, GoogleMap.jsx', 'Thêm tham số để nơi dùng tự đặt tên data-test cho ảnh nhỏ và khung bản đồ.'],
      ]),
      p('Kiểm chứng: build giao diện đạt.', { italic: true, size: 21 }),

      khongAnh('thuộc tính data-test không hiện trên giao diện.'),

      h2('Việc 31 — Dựng hệ thống kiểm thử tự động bằng Selenium'),
      p('Ngày 14/09/2026, thư mục kiemthu/, chưa commit.', { italic: true, size: 21 }),
      p('Dự án kiểm thử tách riêng khỏi mã nguồn website, dùng Selenium WebDriver 4.40, JUnit 5, AssertJ và Maven Surefire, tổ chức theo bốn tầng của mô hình Page Object Model: coso, thanhphan, trang, luong, và kichban là nơi duy nhất chứa câu khẳng định. Viết 108 test case phủ 13 luồng nghiệp vụ, đúng theo bản đặc tả đã lập ở Việc 24.', { size: 23 }),
      fileTable([
        ['coso/', 'Cấu hình đọc từ tham số dòng lệnh, lớp nền cho trang và kịch bản, tự chụp màn hình khi trượt, gọi API để chuẩn bị dữ liệu thử.'],
        ['trang/ và thanhphan/', 'Page Object cho từng trang, Component Object cho thanh điều hướng, dải ảnh, bảng tạm tính và hộp thoại trợ lý.'],
        ['kichban/', '13 lớp kịch bản, mỗi lớp một luồng nghiệp vụ.'],
        ['Chờ phần tử', 'Chỉ dùng chờ tường minh, không có Thread.sleep nào trong toàn bộ dự án.'],
      ]),
      p('Chạy năm lần, mỗi lần lộ ra lỗi của chính công cụ rồi sửa: Surefire bỏ qua lớp tên KiemThu nên báo thành công mà không chạy bài nào; máy chủ Vite chỉ lắng nghe địa chỉ IPv6 còn Java gọi vào IPv4 nên hỏng toàn bộ bước chuẩn bị dữ liệu; trang đặt cuộn mượt làm lệnh bấm rơi ra ngoài màn hình; bảng vẽ lại giữa lúc đọc chữ; chạy lại trên backend chưa khởi động lại thì trùng ngày đã chiếm lịch; khối gợi ý tải sau đẩy nút đi chỗ khác.', { size: 23 }),
      p('Kiểm chứng: lần chạy thứ năm đạt 99/108. Cả 9 test case không đạt đều là lỗi thật của website, gom thành 5 lỗi, nặng nhất là khách chưa đăng nhập không gửi được đánh giá. Kết quả và ảnh chụp màn hình ghi trong BaoCao_KiemThuTuDong.docx. Các lỗi này chưa sửa.', { size: 23 }),

      khongAnh('hệ thống kiểm thử không có màn hình riêng; kết quả chạy và ảnh chụp khi test case trượt nằm trong BaoCao_KiemThuTuDong.docx.'),

      // ---------------- 15/09 ----------------
      h1('Ngày 15/09/2026'),

      h2('Việc 32 — Giảm giá vào dịp lễ'),
      p('Ngày 15/09/2026, migration V13, chưa commit.', { italic: true, size: 21 }),
      p('Ngày tổ chức tiệc hoặc ngày nhận món rơi vào dịp lễ thì được giảm từ 10% đến 20% theo mức của dịp đó. Ưu đãi này không cộng dồn với giảm giá đặt sớm 5%, hệ thống lấy mức cao hơn, tránh giảm quá sâu.', { size: 23 }),
      fileTable([
        ['V13__giam_gia_ngay_le.sql (mới)', 'Bảng holiday_discounts và 15 dòng dữ liệu mẫu cho các ngày nghỉ chính thức ba năm 2026, 2027, 2028. Ngày dương của Tết Nguyên Đán và Giỗ Tổ đổi từ âm lịch, có đối chiếu với bốn ngày đã biết chắc.'],
        ['modules/holiday/ (mới)', 'Entity, repository, service tra mức giảm theo ngày và API quản trị. Phần tra cứu tách thành interface để kiểm thử công thức tính giá không cần cơ sở dữ liệu.'],
        ['PricingService.java', 'Chọn mức cao hơn giữa giảm lễ và giảm đặt sớm, ghi rõ lý do vào bảng tạm tính.'],
        ['DishOrderPricing, DishOrderService', 'Giảm trên tiền món, VAT tính sau khi giảm. Mức miễn phí giao vẫn xét trên tiền món trước giảm, để khách không mất quyền miễn phí giao vì được giảm giá.'],
        ['AdminHolidaysPage.jsx (mới)', 'Trang quản trị ngày lễ: thêm, sửa, bật tắt, xóa. Mức giảm chỉ nhận từ 10% đến 20%, ngày kết thúc không được trước ngày bắt đầu.'],
      ]),
      p('Kiểm chứng: thêm 37 kiểm thử đơn vị cho công thức, dữ liệu mẫu, quản trị và phân quyền. Gọi thử API trên máy chủ đang chạy: 19 trường hợp đều đúng, trong đó tiệc mùng 2 Tết 2027 chỉ giảm 20% chứ không cộng dồn thành 25%.', { size: 23 }),

      tieuDeAnh(),
      ...anh('viec-32-tiec-dip-tet', 'Hình 32.1. Tiệc cưới ngày 06/02/2027 rơi vào Tết Nguyên Đán: giảm 20%, bảng tạm tính ghi rõ ưu đãi đặt sớm và ưu đãi dịp lễ không cộng dồn, áp dụng mức cao hơn.'),
      ...anh('viec-32-dat-mon-dip-le', 'Hình 32.2. Đặt món nhận vào dịp lễ: dòng Giảm giá dịp lễ 15%, VAT tính sau khi giảm.'),
      p('Dịp lễ trong Hình 32.2 là dịp minh họa có tiền tố KT, tạo tạm rồi xóa ngay sau khi chụp, vì đơn món chỉ nhận trước tối đa 30 ngày mà dịp lễ thật gần nhất còn xa hơn.', { italic: true, size: 21 }),
      ...anh('viec-32-quan-tri-ngay-le', 'Hình 32.3. Trang quản trị ngày lễ: danh sách dịp lễ, mức giảm, trạng thái và nút Sửa, Xóa.'),

      h2('Việc 33 — Nâng cấp trợ lý AI'),
      p('Ngày 15/09/2026, chưa commit.', { italic: true, size: 21 }),
      p('Trợ lý trước đây gọi dịch vụ AI bằng HTTP thô, mỗi câu hỏi gửi đi riêng lẻ nên không hiểu câu hỏi nối tiếp, và phần dữ liệu gửi kèm còn thiếu nhiều mảng của website.', { size: 23 }),
      fileTable([
        ['AnthropicLlmClient.java', 'Viết lại bằng SDK Java chính thức của Anthropic. Chỉ dẫn hệ thống được đánh dấu lưu đệm, mức công sức suy luận để thấp cho câu tư vấn ngắn, không tự thử lại khi lỗi để khách không phải chờ.'],
        ['LlmClient, LuotHoiThoai, AssistantFacade', 'Gửi kèm tối đa 10 lượt trò chuyện gần nhất, xếp lại cho đúng thứ tự khách và trợ lý, cắt bớt lượt quá dài.'],
        ['SystemContextBuilder.java', 'Bổ sung dữ liệu: quy định đặt món lẻ, khung giờ ba buổi và loại tiệc, điểm đánh giá của khách, các dịp lễ sắp tới, danh sách trang có thật của website và ngày hôm nay.'],
        ['application.yml, tro-ly-ai.properties', 'Khóa API đọc tự động từ tệp riêng nằm ngoài git, nên mở web là AI đã bật, không phải đặt biến môi trường bằng tay. Lúc khởi động có dòng nhật ký báo đang chạy AI hay đang dùng cơ chế dự phòng.'],
      ]),
      p('Kiểm chứng: thêm 6 kiểm thử cho phần lịch sử trò chuyện và hình dạng yêu cầu gửi Claude. Chạy thử với một khóa giả: backend gọi Anthropic thật, bị từ chối đúng như dự kiến rồi quay về cơ chế dự phòng, khách vẫn nhận được câu trả lời. Đường gọi thành công chưa chạy được vì chưa có khóa thật.', { size: 23 }),

      tieuDeAnh(),
      ...anh('viec-33-tro-ly-uu-dai-dip-le', 'Hình 33.1. Hỏi trợ lý về giảm giá dịp lễ: trợ lý liệt kê ba dịp lễ sắp tới cùng mức giảm, lấy từ dữ liệu trang quản trị.'),
      p('Máy chưa có khóa API nên câu trả lời trong ảnh là của cơ chế dự phòng, chưa phải của mô hình ngôn ngữ.', { italic: true, size: 21 }),

      h2('Việc 34 — Kiểm thử cho giảm giá ngày lễ'),
      p('Ngày 15/09/2026, chưa commit.', { italic: true, size: 21 }),
      fileTable([
        ['KiemThuGiamGiaNgayLe (Luồng 14)', '10 test case phía khách: tiệc trùng dịp lễ, không cộng dồn với đặt sớm, biên ngày đầu và ngày cuối, dịp đã tắt, đặt món vào dịp lễ, miễn phí giao xét trước khi giảm, trợ lý báo dịp lễ sắp tới.'],
        ['KiemThuQuanTriNgayLe (Luồng 15)', '12 test case cho trang quản trị: thêm, sửa, tắt, xóa, chặn mức ngoài khoảng 10% đến 20%, chặn ngày kết thúc trước ngày bắt đầu, phân quyền, và đơn đã đặt giữ nguyên tiền giảm sau khi xóa dịp lễ.'],
        ['GoiApi.java', 'Thêm hàm tạo và dọn dịp lễ thử, và hàm tìm ngày không trùng dịp lễ. Các test case so số tiền cố định nay tự né ngày lễ nên kết quả không phụ thuộc ngày chạy.'],
      ]),
      p('Kiểm chứng: hai luồng mới chạy riêng đạt 22/22. Chạy lại toàn bộ 130 test case đạt 120/130 trong 573 giây. Lần này lộ thêm một lỗi thật của website: đăng nhập lần thứ hai trong cùng một giây bị báo xung đột dữ liệu, vì refresh token sinh ra trùng chuỗi với token trước. Tổng số lỗi website đã phát hiện là 6, chưa sửa lỗi nào.', { size: 23 }),

      khongAnh('việc viết kịch bản kiểm thử; kết quả chạy ghi ở BaoCao_KiemThuTuDong.docx.'),

      h2('Việc 35 — Cập nhật hai tài liệu báo cáo'),
      p('Ngày 15/09/2026.', { italic: true, size: 21 }),
      fileTable([
        ['21130004_..._KeHoachVaTienDo.docx', 'Cập nhật tiến độ đến 15/09: ba mốc chặng kiểm thử hoàn thành sớm, thêm 5 dòng chức năng mới vào bảng báo cáo, thêm bốn đoạn nhận xét và phần hạn chế.'],
        ['BaoCao_KiemThuTuDong.docx', 'Thêm cột Lệnh -Dtest cạnh cột mã để chạy lại từng test case, thêm mục Cách chạy lại kiểm thử, cập nhật sang 130 test case của 15 luồng và lần chạy thứ sáu.'],
      ]),

      khongAnh('việc cập nhật tài liệu.'),

      // ---------------- 17/09 ----------------
      h1('Ngày 17/09/2026'),
      p('Mục tiêu: rà lại bốn yêu cầu hoàn thiện web đã nhận ngày 12 và 13/09, chụp ảnh minh chứng để báo cáo tuần, chuẩn bị video demo kiểm thử tự động.', { size: 23 }),

      h2('Việc 36 — Rà soát bốn yêu cầu hoàn thiện web, sửa lỗi trang thống kê'),
      p('Ngày 17/09/2026, chưa commit.', { italic: true, size: 21 }),
      p('Rà lại bốn yêu cầu: thống kê bằng biểu đồ (Việc 26), ẩn đường dẫn ảnh (Việc 27), đặt sảnh không cần gói tiệc (Việc 28), hai người cùng đăng ký một sảnh (Việc 29). Cả bốn đều có trong mã nguồn. Nhưng khi mở thật trên trình duyệt với MySQL thì trang thống kê không hiện gì dưới ô chọn ngày.', { size: 23 }),
      fileTable([
        ['AdminStatsPage.jsx', 'Lỗi: axiosClient đã bóc sẵn response.data, trang lại đọc thêm .data nên số liệu luôn rỗng. API trả đúng, còn trang không hiện ô số liệu, biểu đồ hay câu báo lỗi nào. Sửa một dòng. Kiểm thử phía máy chủ không bắt được lỗi này vì chỉ kiểm tra phần gộp số liệu, và Việc 26 đã ghi là chưa thử trên trình duyệt.'],
        ['Cơ sở dữ liệu MySQL (XAMPP)', 'Lần đầu chạy profile local từ sau Việc 32: Flyway áp dụng V13 thành công trên MariaDB 10.4. Xác nhận một đơn tiệc chạy qua khóa bi quan và mức cô lập READ_COMMITTED không lỗi.'],
        ['Dữ liệu minh họa', 'Chuyển 6 đơn đặt món đã qua giờ nhận sang Đã hoàn thành và xác nhận 1 đơn tiệc qua API quản trị, để biểu đồ doanh thu có số liệu.'],
      ]),
      p('Ba lỗi mới phát hiện trong lúc rà, chưa sửa. Thứ nhất, trang đặt món gửi một yêu cầu tạm tính mỗi lần đổi hình thức nhận hay giờ nhận nhưng không bỏ phản hồi cũ, nên phản hồi về sau có thể đè kết quả mới: đã gặp một lần bảng tạm tính vẫn hiện giá giao tận nhà, không giảm dịp lễ, dù đã chọn ăn tại chỗ vào ngày lễ. Đơn gửi đi vẫn đúng giá vì máy chủ tính lại. Thứ hai, trang thống kê đọc sai trường thông báo lỗi nên khi có lỗi luôn hiện câu chung chung. Thứ ba, gửi dữ liệu JSON sai mã hóa thì backend trả lỗi 500 thay vì 400.', { size: 23 }),
      p('Kiểm chứng: mở lại trang thống kê bằng Selenium thấy đủ bốn ô số liệu và bảy biểu đồ, ảnh ở Việc 26.', { size: 23 }),

      h2('Việc 37 — Ảnh minh chứng và chế độ trình diễn để quay video'),
      p('Ngày 17/09/2026, chưa commit.', { italic: true, size: 21 }),
      fileTable([
        ['congcu/ChupAnhMinhChung.java', 'Công cụ Selenium chụp 13 ảnh cho Việc 22, 26 đến 29, 32 và 33. Không có câu khẳng định và không chạy chung với bộ kiểm thử. Trang dài chụp bằng lệnh Page.captureScreenshot của Chrome DevTools để lấy trọn form và bảng tạm tính trong một ảnh.'],
        ['CauHinh, TrangCoSo, KiemThuCoSo', 'Chế độ trình diễn bật bằng -Ddemo=true: tô viền đỏ phần tử trước mỗi lần bấm, gõ, chọn, dừng 0,8 giây (đổi bằng -Ddemo.dung), giữ màn hình kết quả thêm 2,4 giây trước khi đóng trình duyệt. Mặc định tắt, chạy kiểm thử thật không bị chậm.'],
        ['cong-cu/nhat-ky', 'Nhúng ảnh vào nhật ký. Ảnh chép sang thư mục anh-minh-chung cạnh script để không mất khi chạy mvn clean. Phần tổng hợp kết quả Selenium nay chỉ đếm các lớp kịch bản, không đếm công cụ chụp ảnh.'],
      ]),
      p('Kiểm chứng: TC-HOL-02 chạy ở chế độ trình diễn vẫn đạt; đặt dừng 5 giây thì thời gian chạy tăng khoảng 43 giây, chứng tỏ chế độ có tác dụng. Chạy lại toàn bộ 10 test case của Luồng 14 trên MySQL: đạt 9/10. TC-HOL-01 báo lỗi vì lựa chọn Sảnh Sen Vàng không được ghi nhận, ảnh lỗi cho thấy gói đã chọn mà sảnh chưa; ngày 15/09 test case này vẫn đạt, chưa rõ nguyên nhân, nghi cùng nhóm với lỗi chập chờn đã ghi nhận trước đó. Công cụ chụp ảnh để lại trong database hai đơn thuê riêng tên KT Demo khách A và B.', { size: 23 }),

      h2('Việc 38 — Sửa hai lỗi: bảng tạm tính đặt món hiện kết quả cũ, trang thống kê báo lỗi sai'),
      p('Ngày 17/09/2026, chưa commit.', { italic: true, size: 21 }),
      fileTable([
        ['DishOrderPage.jsx', 'Mỗi lần khách đổi hình thức nhận hay giờ nhận, trang gửi một yêu cầu tạm tính; trước đây phản hồi nào về sau cùng thì được ghi, kể cả phản hồi của lựa chọn cũ. Nay mỗi lượt tính có cờ conHieuLuc: lựa chọn đổi thì React dọn effect cũ, cờ tắt, phản hồi cũ về muộn bị bỏ qua. Giỏ bị xóa hết thì tắt luôn trạng thái đang tính, để không kẹt ở trạng thái đó.'],
        ['AdminStatsPage.jsx', 'axiosClient đưa lỗi về dạng { status, message }, trang lại đọc e.response.data.message nên luôn hiện câu chung chung. Đổi sang đọc e.message như các trang quản trị khác.'],
        ['congcu/KiemChungSuaLoi.java (mới)', 'Hai kịch bản kiểm chứng. Lỗi thứ nhất là tranh chấp mạng, bấm tay chỉ thỉnh thoảng gặp, nên kịch bản chèn JavaScript giữ yêu cầu tạm tính cũ lại 2 giây để phản hồi cũ chắc chắn về sau phản hồi mới.'],
      ]),
      p('Kiểm chứng theo cách chạy ngược. Trước khi sửa, cả hai kịch bản đều trượt: dòng giảm giá dịp lễ biến mất khi phản hồi cũ về muộn, và trang thống kê hiện câu Không tải được số liệu thống kê. Sau khi sửa, cả hai đều đạt: bảng giữ đúng mức giảm 15%, trang hiện đúng câu của máy chủ. Build giao diện đạt. Chạy hồi quy các luồng đặt món: Luồng 8 đạt 12/12, bốn test case đặt món của Luồng 14 đạt 4/4. Chạy lại cả 10 test case Luồng 14 sau khi sửa: đạt 9/10, lần này TC-HOL-05 báo lỗi với đúng triệu chứng của TC-HOL-01 buổi sáng: ở bước 2 trang đặt tiệc, gói đã chọn mà sảnh chưa được ghi nhận. Lỗi chập chờn này nằm ở trang đặt tiệc, đã gặp trước khi sửa và không liên quan hai chỗ vừa sửa, chưa tìm ra nguyên nhân. Các lần chạy kiểm thử sau khi sửa tạo thêm 6 đơn đặt món thử trong MySQL.', { size: 23 }),
      tieuDeAnh(),
      ...anh('viec-38-tam-tinh-sau-khi-phan-hoi-cu-ve-muon', 'Hình 38.1. Sau khi sửa: phản hồi tạm tính cũ bị giữ 2 giây rồi mới về, bảng vẫn hiện đúng lựa chọn mới nhất là ăn tại chỗ vào dịp lễ, giảm 15%.'),
      ...anh('viec-38-thong-ke-bao-loi-khoang-ngay', 'Hình 38.2. Sau khi sửa: chọn khoảng thống kê gần 7 năm thì trang hiện đúng câu báo của máy chủ.'),

      h2('Việc 39 — Cập nhật đặc tả test case cho khớp mã kiểm thử'),
      p('Ngày 17/09/2026. Tài liệu lưu ở máy cá nhân, không đưa lên kho mã nguồn.', { italic: true, size: 21 }),
      p('Bản DacTa_TestCase.docx lập ngày 12/09 chưa cập nhật từ đó, trong khi mã kiểm thử đã có 130 test case và giao diện đã thay đổi nhiều chỗ. Đối chiếu tự động từng test case giữa tài liệu và mã, rồi cập nhật lại tài liệu.', { size: 23 }),
      fileTable([
        ['cong-cu/dac-ta/tao-dac-ta.js (mới)', 'Chép bộ sinh tài liệu gốc từ lúc viết đặc tả vào dự án. Trước khi sửa đã chạy lại bản gốc và so từng ký tự với tệp hiện có để chắc chắn không mất chỉnh sửa tay nào.'],
        ['Bổ sung', 'Luồng 14 giảm giá ngày lễ (10 test case) và Luồng 15 quản trị ngày lễ (12 test case), kèm dữ liệu mẫu 15 dịp lễ và tham số mức giảm, thời lượng buổi.'],
        ['Sửa', 'Kết quả mong đợi của 21 test case cho đúng cách giao diện chặn lỗi: trình duyệt chặn ở ô nhập, thẻ không gian và thẻ gói bị khóa, câu nhắc ở bước 1 khác câu của máy chủ. TC-SPACE-02 là chỗ bản đầu viết sai quy tắc lọc. Sửa tiền điều kiện Luồng 1 (tài khoản mẫu không có thật) và Luồng 7.'],
        ['Thống nhất', 'Tên của 130 test case trùng từng chữ với tên trong mã; mỗi luồng ghi tên lớp kịch bản; mục môi trường ghi đúng cách giữ dữ liệu độc lập; thêm mục Lịch sử cập nhật.'],
      ]),
      p('Kiểm chứng: đối chiếu lại tài liệu mới với mã được 130/130 mã test case và 130/130 tên trùng khớp. 30 câu thông báo trích trong tài liệu đều có thật trong mã nguồn website. Xem bản dựng qua Microsoft Word, 18 trang, bảng không vỡ.', { size: 23 }),
      khongAnh('việc cập nhật tài liệu, không có màn hình.'),

      h2('Việc 40 — Video demo kiểm thử tự động quay trực tiếp khung trình duyệt'),
      p('Ngày 17/09/2026, chưa commit.', { italic: true, size: 21 }),
      p('Báo cáo tiến độ cần một video demo Selenium. Máy không có công cụ quay màn hình điều khiển được từ dòng lệnh và không có ffmpeg, nên dựng công cụ quay và ghép video ngay trong dự án kiểm thử, không cài thêm phần mềm.', { size: 23 }),
      fileTable([
        ['coso/QuayManHinh.java (mới)', 'Bật bằng -Dquay=true. Mở thêm một kết nối Chrome DevTools tới đúng tab Selenium đang điều khiển, dùng Page.startScreencast nhận từng khung hình mỗi khi trang vẽ lại, lưu JPEG kèm thời điểm. Kết nối tách khỏi WebDriver nên không làm đổi kết quả kiểm thử.'],
        ['congcu/GhepVideo.java (mới)', 'Chrome chạy ẩn phát lại các khung đúng nhịp lên canvas, thêm màn mở đầu, thẻ tên test case, thanh phụ đề, nhãn Đạt hoặc Không đạt và màn tổng kết, rồi dùng MediaRecorder ghi ra MP4 H.264. Nhãn Đạt hay Không đạt đọc từ báo cáo Surefire của chính lần chạy; khoảng chờ trên 2 giây rút còn 2 giây và ghi rõ trong video.'],
        ['TrangCoSo, BangTamTinh, TrangDatMon, TrangQuanTriNgayLe', 'Chế độ trình diễn thêm viền xanh: cuộn tới kết quả mà kịch bản vừa đọc để kiểm tra và tô viền, người xem thấy đúng con số đang được so. Mặc định tắt.'],
      ]),
      p('Kiểm chứng: quay thử TC-HOL-02, kịch bản vẫn đạt. Video mẫu 1280x720, dài khoảng 48 giây; trích khung ở nhiều thời điểm để xem lại màn mở đầu, khung quay, viền xanh ở bảng tạm tính, nhãn Đạt và màn tổng kết. Theo góp ý, bỏ ngày giờ chạy khỏi màn mở đầu và sửa chữ khóa luận thành tiểu luận.', { size: 23 }),
      p('Chưa làm: chưa quay bản đầy đủ 5 test case TC-BOOK-01, TC-HOL-02, TC-HOL-06, TC-HADM-02, TC-PERM-03; lệnh chạy đã soạn sẵn. Quay một phần các lớp kịch bản sẽ ghi đè báo cáo Surefire của lớp đó, nên phải sao lưu và trả lại báo cáo trước và sau khi quay. Video chỉ có khung trình duyệt, không có terminal và lời thuyết minh.', { size: 23 }),
      tieuDeAnh(),
      ...anh('viec-40-khung-video-mau', 'Hình 40.1. Một khung trích từ video mẫu TC-HOL-02: trang tự cuộn tới bảng tạm tính và tô viền xanh phần đang kiểm tra, thanh phụ đề ghi mã và tên test case.'),

      // ---------------- 20/09 ----------------
      h1('Ngày 20/09/2026'),
      p('Mục tiêu: làm hai mốc kế hoạch là hoàn thiện bộ kiểm thử (mốc 08/10 – 15/10) và nghiên cứu Git, GitHub Actions (mốc 16/10 – 22/10).', { size: 23 }),

      h2('Việc 41 — Hoàn thiện bộ kiểm thử và sửa sáu lỗi website'),
      p('Ngày 20/09/2026, migration V14, chưa commit.', { italic: true, size: 21 }),
      p('Chạy lại toàn bộ 130 test case để lấy danh sách lỗi mới nhất: đạt 119/130. Trong 11 test case không đạt có 9 do lỗi thật của website và 2 do kịch bản chập chờn. Sửa cả hai nhóm rồi chạy lại.', { size: 23 }),
      fileTable([
        ['TrangDatTiec.java', 'Bấm thẻ không gian hoặc gói ở bước 2 xong phải thấy thẻ mang trạng thái đang chọn, chưa thấy thì bấm lại, tối đa ba lần. Khối gợi ý tải xong đẩy bố cục xuống làm cú bấm rơi trượt, trước đây kịch bản trượt mãi ở bước đọc bảng tạm tính nên báo sai chỗ.'],
        ['TrangDatMon.java', 'Mở trang mà giỏ rỗng thì ghi lại localStorage và tải lại đúng một lần, rồi mới báo lỗi.'],
        ['TrangCoSo.java', 'Ô thả xuống chờ tới khi có đúng lựa chọn cần chọn, thay cho lỗi "Cannot locate option with value" khi danh sách từ máy chủ chưa kịp về.'],
        ['SecurityConfig.java (L01)', 'Mở quyền gửi đánh giá cho khách chưa đăng nhập. Vẫn phải kèm mã đơn có thật và vẫn chờ quản trị duyệt.'],
        ['GlobalExceptionHandler.java (L02)', 'Câu báo lỗi kiểm tra dữ liệu ghi rõ ô nào sai, thay vì chỉ "Dữ liệu gửi lên không hợp lệ".'],
        ['SpaceDetailPage.jsx, BookingPage.jsx (L03)', 'Nút đặt tiệc mang theo slug không gian trên địa chỉ, trang đặt tiệc chọn sẵn không gian đó khi khách chưa tự chọn.'],
        ['ResourceNotFoundException.java (L04)', 'Câu báo không tìm thấy có dấu tiếng Việt.'],
        ['TrackBookingPage.jsx (L05)', 'Bỏ trống mã đơn rồi bấm tra cứu thì hiện câu nhắc, thay vì im lặng.'],
        ['JwtService.java, V14 (L07)', 'Refresh token thêm mã định danh ngẫu nhiên nên hai lần đăng nhập trong cùng một giây không còn trùng chuỗi. Migration V14 nới cột token từ 255 lên 512 ký tự.'],
      ]),
      p('Hai sai sót của chính tôi trong lúc sửa, đều tự phát hiện và sửa ngay. Thứ nhất, script vá tệp đọc lại bản gốc cho mỗi lần thay nên lần ghi cuối đè mất hai lần trước, trang đặt tiệc trắng vì thiếu lệnh import. Thứ hai, tôi đọc danh sách không gian sai cấu trúc, danh sách nằm ở spaces.items chứ không phải chính spaces. Cả hai lần đều tìm ra bằng cách đọc log lỗi ngay trong trình duyệt.', { size: 23 }),
      p('Sửa L07 còn làm lộ một lỗi ngầm khác: thêm mã định danh làm chuỗi JWT dài 274 ký tự, vượt cột token 255 ký tự, nên mọi lần đăng nhập đều hỏng. Cột này vốn đã chật so với JWT, chỉ là chưa ai chạm tới; đã nới bằng migration V14.', { size: 23 }),
      p("Kiểm chứng: sau khi sửa, chạy lại toàn bộ 130 test case đạt 130/130 trong khoảng 362 giây, lần đầu bộ kiểm thử xanh hết kể từ khi dựng. Kiểm thử phía máy chủ vẫn đạt 150/150. Riêng migration V14 mới chạy trên H2, lần tới mở profile local sẽ chạy trên MariaDB.", { size: 23 }),

      h2('Việc 42 — Nghiên cứu Git, GitHub và GitHub Actions'),
      p('Ngày 20/09/2026. Tài liệu lưu ở máy cá nhân, không đưa lên kho mã nguồn.', { italic: true, size: 21 }),
      p('Làm sớm mốc 16/10 – 22/10. Tài liệu NghienCuu_Git_GitHubActions.docx gồm: Git ba vùng, nhánh và hợp nhất; GitHub với pull request, bảo vệ nhánh và bí mật của kho; các khái niệm của GitHub Actions là workflow, trigger, job, step, action, runner, artifact, cache và service container; quy trình chạy bộ kiểm thử của đề tài kèm khung workflow dự kiến; bảng rủi ro; các chỉ số sẽ ghi nhận cho chương thực nghiệm.', { size: 23 }),
      p('Phiên bản các action đều tra lại từ kho chính thức ngày 20/09 chứ không viết theo trí nhớ: actions/checkout v7.0.1, actions/setup-java v6.0.1, actions/upload-artifact v7.0.1. Ghi nhận thêm một điểm quan trọng cho phần thực nghiệm: nhãn ubuntu-latest sẽ chuyển dần sang Ubuntu 26.04 trong khoảng 19/10 đến 19/11/2026, nên workflow phải ghi rõ phiên bản máy chạy để kết quả lặp lại được.', { size: 23 }),
      khongAnh('việc nghiên cứu và viết tài liệu.'),

      h2('Việc 43 — Đồng bộ số liệu test case giữa các tài liệu'),
      p('Ngày 20/09/2026.', { italic: true, size: 21 }),
      p('Các tài liệu đang ghi ba con số khác nhau nên dễ hiểu nhầm: chỗ ghi 108, chỗ ghi 130, chỗ ghi 150. Rà lại và thống nhất cách nói: 150 là số bài kiểm thử phía máy chủ viết bằng JUnit, thuộc chặng một xây website; 130 là số test case giao diện chạy bằng Selenium, thuộc chặng hai và là sản phẩm chính của tiểu luận. Hai con số đo hai mức khác nhau nên không cộng vào nhau.', { size: 23 }),
      fileTable([
        ['21130004_..._KeHoachVaTienDo.docx', 'Sửa các chỗ còn ghi 108 test case và kết quả 99/108. Ghi rõ chỗ phân biệt 150 kiểm thử máy chủ với 130 test case giao diện. Cập nhật kết quả lần chạy gần nhất.'],
        ['BaoCao_KiemThuTuDong.docx', 'Bộ sinh báo cáo chỉ đếm các lớp kịch bản. Trước đó nó đếm lẫn cả lớp công cụ chụp ảnh và ghép video nên ra 135 test case. Thêm lần chạy thứ 7 và thứ 8, đánh dấu sáu lỗi đã sửa.'],
        ['DacTa_TestCase.docx', 'Đã khớp từ ngày 17/09: 130 test case, tên từng test case trùng từng chữ với tên trong mã.'],
      ]),
      p('Kiểm chứng bằng cách đếm lại từ mã nguồn: 130 phương thức kiểm thử trong thư mục kichban, 150 phương thức kiểm thử trong backend/src/test.', { size: 23 }),
      khongAnh('việc rà soát tài liệu.'),

      // ---------------- 27/09 ----------------
      h1('Ngày 27/09/2026'),
      p('Mục tiêu: rà lại website xem còn thiếu gì so với một trang thương mại hoàn chỉnh, rồi bù các chỗ thiếu. Sáu việc dưới đây đều sinh ra từ đợt rà soát đó.', { size: 23 }),

      h2('Việc 44 — Dựng dữ liệu giao dịch mẫu cho trang thống kê'),
      p('Ngày 27/09/2026, migration V15, chưa commit.', { italic: true, size: 21 }),
      p('Cơ sở dữ liệu mới cài chỉ có dữ liệu gốc là không gian, món ăn và gói tiệc, không có đơn nào. Gọi thử API thống kê thì doanh thu bằng 0, cả bảy biểu đồ đều rỗng, nghĩa là trang thống kê làm xong nhưng không có gì để xem. Đánh giá cũng chỉ có ba cái, đều 5 sao và đều đề cùng một ngày, nhìn vào biết ngay là dữ liệu dựng.', { size: 23 }),
      p('Viết một bộ sinh bằng Node tạo ra migration V15. Điểm quan trọng là tiền của từng đơn không gõ tay mà tính lại đúng theo PricingService: mâm tối thiểu của từng không gian, phí thuê giảm dần theo tiền ăn, giảm 5% khi đặt trước 60 ngày, VAT 8% tính sau giảm. Nhờ vậy số trong cơ sở dữ liệu không mâu thuẫn với công thức mà website đang dùng, ai mở một đơn ra cộng lại cũng khớp.', { size: 23 }),
      fileTable([
        ['V15__du_lieu_mau.sql (mới)', '5 khách hàng, 52 đơn đặt tiệc rải đều 11 tháng gần nhất, 35 đơn đặt món, 16 đánh giá nhiều mức sao. Hai đánh giá để chờ duyệt nên trang duyệt đánh giá có việc để làm.'],
        ['Ràng buộc giữ đúng', 'Không có hai đơn đã xác nhận trùng không gian, trùng ngày và trùng buổi. Đơn đã qua thì hoàn thành hoặc hủy, đơn sắp tới thì chờ xác nhận hoặc đã xác nhận.'],
        ['Bộ sinh', 'Dùng bộ số giả ngẫu nhiên hạt giống cố định, chạy lại cho ra đúng tệp cũ, nên sửa dữ liệu mẫu là sửa bộ sinh chứ không sửa tay vào SQL.'],
      ]),
      p('Sau khi nạp: doanh thu ghi nhận 2,4 tỉ đồng trên 41 đơn, 4.924 lượt khách, tỉ lệ hủy 6,8%, biểu đồ doanh thu có đủ 12 cột tháng, biểu đồ theo không gian có đủ sáu mục. Điểm đánh giá trung bình còn 4,6 trên 5 thay vì 5,0 tuyệt đối.', { size: 23 }),

      h2('Việc 45 — Trang quản trị đơn đặt món'),
      p('Ngày 27/09/2026, chưa commit.', { italic: true, size: 21 }),
      p('Máy chủ đã có sẵn ba đường dẫn quản trị đơn đặt món từ đợt làm chặng một, nhưng giao diện chưa có trang nào gọi tới, nên khách đặt món xong thì không ai xử lý được đơn. Dựng trang theo đúng khuôn của trang quản trị đơn đặt tiệc: ba ô đếm theo trạng thái, lọc theo trạng thái và hình thức nhận, tìm theo mã, tên hoặc số điện thoại, và nút chuyển trạng thái theo đúng luồng bốn bước.', { size: 23 }),
      fileTable([
        ['AdminDishOrdersPage.jsx (mới)', 'Bảng đơn hiện luôn danh sách món của từng đơn, để bếp không phải mở từng đơn ra xem.'],
        ['endpoints.js', 'Thêm ba lời gọi. Riêng lệnh đổi trạng thái đi bằng tham số truy vấn chứ không phải thân yêu cầu, khác với đơn đặt tiệc, vì máy chủ nhận như vậy.'],
        ['App.jsx, Header.jsx', 'Thêm đường dẫn /quan-tri/don-dat-mon và mục trong menu quản trị.'],
      ]),

      h2('Việc 46 — Ghi nhận tiền đặt cọc'),
      p('Ngày 27/09/2026, migration V16, chưa commit.', { italic: true, size: 21 }),
      p('Website chưa nối cổng thanh toán và cũng chưa nên nối, vì cổng thật cần hợp đồng và phí. Nhưng tiền cọc thì vẫn thu bằng chuyển khoản hoặc tiền mặt tại quầy, trước đây ghi ra sổ giấy nên mở đơn lên không biết đơn nào đã có tiền. Bổ sung bốn cột vào bảng bookings và một đường dẫn cho quản trị ghi nhận từng lần thu.', { size: 23 }),
      fileTable([
        ['V16__ghi_nhan_dat_coc.sql (mới)', 'Bốn cột: khoản phải cọc, khoản đã thu, thời điểm thu, hình thức. Đơn cũ suy ra khoản phải cọc từ tổng tiền, đơn đã xác nhận đánh dấu đã thu đủ.'],
        ['BookingService.ghiNhanCoc', 'Cộng dồn từng lần thu nên khách đóng cọc làm hai lần vẫn ghi được. Chặn ghi cọc cho đơn đã hủy và chặn thu vượt quá tổng tiền đơn.'],
        ['AdminBookingsPage.jsx', 'Thêm cột Cọc với ba trạng thái chưa cọc, cọc thiếu, đã cọc; ô ghi nhận điền sẵn phần còn thiếu.'],
      ]),
      p('Khoản phải cọc chốt ngay lúc đặt chứ không tính lại theo tỉ lệ hiện hành, để sau này đổi tỉ lệ cọc thì đơn cũ không bị đổi theo.', { size: 23 }),

      h2('Việc 47 — Bổ sung nội dung trang chủ'),
      p('Ngày 27/09/2026, chưa commit.', { italic: true, size: 21 }),
      p('Trang chủ trước đây chỉ có ba khối: ảnh bìa, danh sách không gian và bảng giá gói tiệc. Thiếu hẳn phần thuyết phục khách. Thêm ba khối lấy dữ liệu thật và một khối kêu gọi liên hệ: món khách gọi nhiều nhất, ảnh các buổi tiệc đã diễn ra, đánh giá sau tiệc kèm điểm trung bình. Ảnh bìa cũng đổi từ nền màu trơn sang ảnh thật lấy từ thư viện, phủ lớp màu xanh mờ lên để chữ vẫn đọc được.', { size: 23 }),
      p('Ảnh bìa lấy theo dữ liệu chứ không ghi cứng đường dẫn, nên sau này đổi ảnh chỉ cần đổi trong trang quản trị thư viện. Khối nào gọi dữ liệu hỏng thì tự ẩn, trang chủ vẫn chạy.', { size: 23 }),

      h2('Việc 48 — Ba lỗi nhỏ nhìn thấy được và một lỗi cũ còn tồn'),
      p('Ngày 27/09/2026, chưa commit.', { italic: true, size: 21 }),
      fileTable([
        ['application.yml', 'Thông tin liên hệ viết không dấu nên trợ lý tư vấn trả lời khách "1147 Binh Quoi, Phuong 28", trong khi chân trang ghi có dấu. Viết lại có dấu.'],
        ['GlobalExceptionHandler.java', 'Đường dẫn không tồn tại ngoài phạm vi API trả 500 "hệ thống đang bận", nghe như website hỏng. Nay trả 404 đúng bản chất.'],
        ['GlobalExceptionHandler.java', 'Gửi JSON sai định dạng cũng trả 500. Lỗi này do bên gửi nên phải là 400. Đây là lỗi đã ghi nhận từ ngày 17/09 mà chưa sửa.'],
        ['catalogSlice.js (L06)', 'Đổi bộ lọc nhanh tay thì kết quả của lần gọi cũ về muộn ghi đè lên kết quả mới. So requestId để bỏ kết quả về muộn. Sửa ở hàm dùng chung nên cả bốn danh sách không gian, món, danh mục, gói tiệc đều hết lỗi.'],
      ]),
      p('Riêng đường dẫn lạ trong phạm vi /api vẫn trả 403 chứ không phải 404, và giữ nguyên có chủ ý: trả 404 thì người ngoài dò được đường dẫn nào có thật trong hệ thống.', { size: 23 }),

      h2('Việc 49 — Chuẩn bị cho công cụ tìm kiếm'),
      p('Ngày 27/09/2026, chưa commit.', { italic: true, size: 21 }),
      p('Dự án chưa có thư mục public nên không có tệp nào cho công cụ tìm kiếm đọc, biểu tượng trang thì đang là một ký tự emoji nhúng thẳng vào HTML. Bổ sung robots.txt chặn khu quản trị và các trang tra cứu theo mã đơn, sitemap.xml liệt kê tám trang công khai, biểu tượng vẽ bằng SVG theo màu thương hiệu, và bộ thẻ Open Graph để dán liên kết lên Zalo hay Facebook thì hiện tên, mô tả và ảnh xem trước.', { size: 23 }),
      p("Kiểm chứng sau đợt bổ sung: kiểm thử phía máy chủ 150/150, bộ Selenium chạy lại đầy đủ đạt 130/130 trong khoảng 420 giây. Hai migration V15 và V16 mới chỉ chạy trên H2, lần tới mở profile local sẽ chạy trên MariaDB.", { size: 23 }),
      khongAnh('đợt bổ sung này.'),

      h2('Việc 50 — Chạy thử trên MySQL thật và sửa năm lỗi lộ ra từ đó'),
      p('Ngày 27/09/2026, chưa commit.', { italic: true, size: 21 }),
      p('Sáu việc ban đầu đều làm và kiểm chứng trên H2, là cơ sở dữ liệu trong bộ nhớ, mỗi lần khởi động dựng lại từ trắng. Bật XAMPP chạy lại trên MySQL thật thì lộ ra một loạt vấn đề mà H2 không bao giờ cho thấy, vì cơ sở dữ liệu thật đã có dữ liệu của những lần chạy trước.', { size: 23 }),
      fileTable([
        ['V15__du_lieu_mau.sql', 'Migration hỏng giữa chừng vì mã đơn mẫu trùng mã đơn có thật trong máy. Hệ thống cấp mã theo thứ tự trong ngày bắt đầu từ 0001, mà máy đã có sẵn 7 đơn tiệc và 14 đơn món từ những lần thử trước. Cho dữ liệu mẫu đánh số từ 9001 trở đi: hai dải không bao giờ gặp nhau, mà dạng mã vẫn đúng bốn chữ số như mã thật.'],
        ['Phân trang (mới)', 'Không một danh sách nào trong dự án có phân trang. Khi dữ liệu mới có vài dòng thì không ai thấy, nhưng với 60 đơn thật thì trang quản trị chỉ hiện 30 dòng đầu và không có cách nào xem phần còn lại, cũng không có gì báo là còn dữ liệu phía sau. Thêm thành phần dùng chung Pagination, nối vào năm danh sách.'],
        ['AdminBookingsPage.jsx', 'Ô nhập tiền cọc đặt bước nhảy 1000, mà tiền cọc bằng 30% tổng đơn nên hiếm khi tròn nghìn. Trình duyệt coi số lẻ là không hợp lệ và chặn gửi mà không báo gì: bấm nút như không có chuyện gì xảy ra. Đây là lỗi của chính tôi viết ra sáng nay, đổi bước nhảy về 1.'],
        ['AdminStatsPage.jsx', 'Khoảng ngày mặc định dùng toISOString nên quy về giờ UTC. Việt Nam lệch +7 nên ô từ ngày luôn hiện sớm hơn một ngày, còn ô đến ngày sẽ lùi một ngày nếu mở trang trước 7 giờ sáng. Đổi sang lấy năm, tháng, ngày theo giờ địa phương.'],
        ['BookingPage.jsx', 'Màn hình báo đặt tiệc thành công hiện ngày dạng 2026-11-11 trong khi mọi chỗ khác hiện 11/11/2026. Dùng chung hàm định dạng ngày.'],
        ['TrackBookingPage.jsx', 'Thêm dòng tiền cọc cho khách tự xem đã đóng tới đâu, thay vì phải gọi điện hỏi nhà hàng.'],
      ]),
      p('Cách thử: dựng lại website bằng profile local trỏ vào MySQL của XAMPP, rồi tự đi hết luồng đặt tiệc ba bước như một khách thật. Đơn VS-20260927-0001 tạo được, tra cứu ra đúng, ghi cọc 32.756.400 đồng vào đơn đó rồi trang tra cứu hiện "Đã nhận đủ". Cũng kiểm lại bộ lọc không gian, trang thực đơn 28 món, thư viện 60 ảnh đều tải chậm đúng cách, trang 404, trang giỏ món rỗng và ba trang phân trang.', { size: 23 }),
      p('Một điều rút ra cho phần viết tiểu luận: kiểm thử trên cơ sở dữ liệu dựng lại từ trắng mỗi lần chạy thì không bao giờ thấy loại lỗi do dữ liệu cũ gây ra. Bốn trong năm lỗi trên chỉ xuất hiện khi trong máy đã có dữ liệu thật.', { size: 23 }),
      p("Kiểm chứng sau khi sửa: chạy lại toàn bộ bộ kiểm thử trên MySQL thật đạt 130/130 trong khoảng 415 giây, kiểm thử phía máy chủ 150/150. Hai migration V15 và V16 nay đã chạy thật trên MariaDB chứ không chỉ trên H2.", { size: 23 }),
      khongAnh('đợt rà soát này.'),

      h2('Việc 51 — Đối chiếu với ba website cùng ngành và bổ sung ba tính năng'),
      p('Ngày 27/09/2026, chưa commit.', { italic: true, size: 21 }),
      p('Mở thử ba website thật để xem mình đang thiếu gì so với mặt bằng chung: Pizza 4P\u2019s là chuỗi nhà hàng có hệ thống đặt bàn trực tuyến riêng, Riverside Palace là trung tâm hội nghị tiệc cưới cùng loại hình cho thuê không gian, và PasGo là nền tảng đặt chỗ nhà hàng. Chấm theo mười tiêu chí, mỗi tiêu chí thang 5 điểm.', { size: 23 }),
      new Table({
        columnWidths: [1900, 3600, 3526],
        width: { size: W, type: WidthType.DXA },
        rows: [
          new TableRow({
            tableHeader: true,
            children: [cell("Tiêu chí", 1900, { bold: true, bg: HDR }), cell("Điểm trên thang 5", 3600, { bold: true, bg: HDR }), cell("Nhận xét", 3526, { bold: true, bg: HDR })],
          }),
          new TableRow({ children: [cell("Tìm kiếm và lọc", 1900), cell("Vườn Sen 4 · Pizza 4P’s 3 · Riverside Palace 1 · PasGo 5", 3600), cell("PasGo mạnh nhất: lọc theo khu vực, kiểu món và năm khoảng giá. Riverside Palace không có ô tìm nào.", 3526)] }),
          new TableRow({ children: [cell("Minh bạch giá", 1900), cell("Vườn Sen 5 · Pizza 4P’s 4 · Riverside Palace 1 · PasGo 3", 3600), cell("Điểm mạnh nhất của Vườn Sen. Riverside Palace không công bố giá, khách phải để lại số điện thoại chờ gọi lại.", 3526)] }),
          new TableRow({ children: [cell("Đặt trực tuyến trọn vẹn", 1900), cell("Vườn Sen 5 · Pizza 4P’s 5 · Riverside Palace 1 · PasGo 4", 3600), cell("Vườn Sen và Pizza 4P’s đều ra mã đơn ngay. Riverside Palace chỉ có biểu mẫu tư vấn.", 3526)] }),
          new TableRow({ children: [cell("Xem tình trạng còn trống", 1900), cell("Vườn Sen 4 · Pizza 4P’s 5 · Riverside Palace 1 · PasGo 3", 3600), cell("Pizza 4P’s cho tìm bàn theo khung giờ trước khi đặt. Đây là tính năng đã học và bổ sung.", 3526)] }),
          new TableRow({ children: [cell("Ưu đãi", 1900), cell("Vườn Sen 4 · Pizza 4P’s 3 · Riverside Palace 4 · PasGo 5", 3600), cell("Hai trang kia đặt ưu đãi ngay trang chủ. Trước đợt này Vườn Sen có dữ liệu giảm giá nhưng khách không xem được.", 3526)] }),
          new TableRow({ children: [cell("Đánh giá của khách", 1900), cell("Vườn Sen 4 · Pizza 4P’s 2 · Riverside Palace 2 · PasGo 5", 3600), cell("Vườn Sen có duyệt đánh giá, đánh giá kèm ảnh và điểm trung bình; hai trang thương hiệu không hiện đánh giá.", 3526)] }),
          new TableRow({ children: [cell("Tài khoản và tra cứu đơn", 1900), cell("Vườn Sen 5 · Pizza 4P’s 3 · Riverside Palace 1 · PasGo 4", 3600), cell("Vườn Sen tra cứu được bằng mã đơn mà không cần tài khoản, lại có trang đơn của tôi.", 3526)] }),
          new TableRow({ children: [cell("Thanh toán trực tuyến", 1900), cell("Vườn Sen 2 · Pizza 4P’s 4 · Riverside Palace 1 · PasGo 3", 3600), cell("Chỗ yếu của Vườn Sen: mới ghi nhận cọc thủ công, chưa nối cổng thanh toán.", 3526)] }),
          new TableRow({ children: [cell("Nội dung và SEO", 1900), cell("Vườn Sen 3 · Pizza 4P’s 5 · Riverside Palace 4 · PasGo 5", 3600), cell("Ba trang kia đều có mục tin tức hoặc blog nuôi từ khóa. Vườn Sen mới có sitemap và thẻ chia sẻ.", 3526)] }),
          new TableRow({ children: [cell("Hỗ trợ tức thì", 1900), cell("Vườn Sen 4 · Pizza 4P’s 2 · Riverside Palace 3 · PasGo 4", 3600), cell("Trợ lý tư vấn trả lời ngay trong trang là điểm Vườn Sen hơn hai trang thương hiệu.", 3526)] }),
        ],
      }),
      p(''),
      p('Tổng điểm: Vườn Sen 40, PasGo 41, Pizza 4P\u2019s 36, Riverside Palace 19. Điều bất ngờ là Vườn Sen hơn hẳn trung tâm tiệc cưới ở đúng hai chỗ quan trọng nhất với khách: công bố giá và cho đặt trực tuyến ra mã đơn ngay. Riverside Palace là thương hiệu lớn nhưng trang web chỉ dừng ở giới thiệu và thu số điện thoại. Chỗ thua rõ nhất của Vườn Sen là thanh toán trực tuyến và nội dung nuôi từ khóa tìm kiếm.', { size: 23 }),
      p('Ba tính năng học được và đã làm:', { size: 23 }),
      fileTable([
        ['Trang Ưu đãi (mới)', 'Học từ Riverside Palace và PasGo. Dữ liệu dịp lễ đã có sẵn trong máy nhưng chỉ quản trị xem được, khách chỉ biết mình được giảm khi bấm tới bước báo giá. Thêm đường dẫn công khai /api/v1/holidays và trang /uu-dai liệt kê các dịp đang áp dụng hoặc sắp tới kèm mức giảm, thêm mục Ưu đãi trên thanh menu.'],
        ['Tìm món trên trang thực đơn', 'Học từ PasGo. Máy chủ đã nhận tham số keyword từ lâu mà giao diện chưa có chỗ nhập, khách phải bấm qua năm danh mục để tìm một món. Thêm ô tìm, chờ 350ms sau lần gõ cuối mới gọi máy chủ để không gọi thừa.'],
        ['Xem buổi nào đã kín', 'Học từ Pizza 4P\u2019s, nơi khách tìm bàn theo khung giờ trước khi đặt. Thêm đường dẫn /api/v1/bookings/availability trả về buổi đã kín của từng không gian trong một ngày; bước chọn không gian hiện ngay nhãn "Buổi này đã có tiệc".'],
      ]),
      p('Một quyết định đáng ghi lại: ban đầu tôi khóa luôn nút chọn với không gian đã kín, nhưng làm vậy sai về mặt kỹ thuật lẫn kiểm thử. Sai kỹ thuật vì đó chỉ là ảnh chụp tình trạng lúc mở trang, trong lúc khách điền form có thể có đơn vừa bị hủy làm buổi đó trống trở lại, khóa theo ảnh chụp cũ là chặn oan khách. Sai về kiểm thử vì test case TC-BOOKV-08 cần chọn đúng sảnh đã kín rồi gửi đơn để kiểm tra máy chủ từ chối; khóa nút thì kịch bản không chọn được. Đổi thành chỉ cảnh báo, máy chủ vẫn là nơi quyết định.', { size: 23 }),
      p('Một lỗi tự gây ra rồi tự tìm thấy: khối chọn không gian nằm trong một component con, tôi khai báo biến ở component cha nên lúc chạy báo "tinhTrangTrong is not defined" và trang đặt tiệc trắng xóa. Lệnh build không bắt được vì đây là lỗi lúc chạy chứ không phải lỗi cú pháp; phải mở nhật ký lỗi của trình duyệt mới thấy. Sửa bằng cách truyền xuống dạng thuộc tính.', { size: 23 }),
      p("Kiểm chứng: chạy lại toàn bộ bộ kiểm thử trên MySQL sau khi thêm ba tính năng, đạt 130/130 trong khoảng 446 giây, không test case nào phải sửa.", { size: 23 }),
      khongAnh('việc khảo sát và bổ sung này.'),

      h2('Việc 52 — Hoàn chỉnh luồng thanh toán bằng VietQR'),
      p('Ngày 27/09/2026, migration V17, chưa commit.', { italic: true, size: 21 }),
      p('Phần thanh toán trước đó mới dừng ở hai cột trên đơn đặt tiệc: phải cọc bao nhiêu, đã thu bao nhiêu. Cách đó không trả lời được những câu hỏi bình thường của một nhà hàng: khoản này thu ngày nào, ai thu, thu bằng tiền mặt hay chuyển khoản, và khách đã chuyển rồi nhưng kế toán chưa đối soát thì nằm ở đâu.', { size: 23 }),
      p('Chọn hướng VietQR thay vì cổng thanh toán. Cổng thật như VNPay hay MoMo đều cần hợp đồng, mã merchant và phí giao dịch, không hợp với một đề tài tốt nghiệp. VietQR là chuẩn mã chuyển khoản của Napas, mọi ngân hàng trong nước đều quét được, không phải đăng ký gì và không mất phí. Khách quét mã là ứng dụng ngân hàng điền sẵn số tài khoản, số tiền và nội dung nên không gõ nhầm; nhân viên mở sao kê thấy tiền thì bấm xác nhận.', { size: 23 }),
      fileTable([
        ['V17__thanh_toan.sql (mới)', 'Bảng payments làm sổ ghi từng lần thu, thêm hai cột tiền cho đơn đặt món. Dựng lại 85 phiếu thu từ các khoản cọc và đơn món đã hoàn thành trước đây để sổ không bắt đầu từ con số không.'],
        ['VietQrBuilder.java (mới)', 'Tự dựng chuỗi mã theo chuẩn EMVCo của Napas, gồm cả ô kiểm tra CRC-16. Tự làm chứ không gọi dịch vụ sinh QR bên ngoài: không phụ thuộc dịch vụ có thể chết, không gửi số tài khoản nhà hàng ra ngoài, và quan trọng nhất là kiểm thử được.'],
        ['VietQrBuilderTest.java (mới)', 'Tám bài kiểm thử: cấu trúc khối, khối thụ hưởng, số tiền, làm tròn về đồng, bỏ dấu nội dung, và ba bài riêng cho ô kiểm tra CRC. CRC tính sai một ly là ứng dụng ngân hàng báo mã hỏng, mà không thể quét tay mỗi lần sửa mã nguồn nên phải có bài kiểm thử đối chứng.'],
        ['PaymentService.java (mới)', 'Tạo yêu cầu thanh toán, ghi khoản thu tại quầy, xác nhận đối soát, hủy phiếu. Tiền chỉ cộng vào đơn khi phiếu được xác nhận, không cộng lúc tạo phiếu. Chặn thu vượt tổng tiền đơn.'],
        ['PaymentPage.jsx (mới)', 'Trang khách nhập mã đơn, xem còn nợ bao nhiêu, bấm một nút là có mã QR. Mã QR vẽ ngay trong trình duyệt bằng thư viện qrcode chứ không gọi dịch vụ ảnh bên ngoài.'],
        ['AdminPaymentsPage.jsx (mới)', 'Sổ thanh toán và màn hình đối soát: lọc theo trạng thái, tìm theo mã phiếu hoặc mã đơn, xác nhận kèm mã giao dịch trên sao kê, và ô ghi khoản thu tiền mặt tại quầy.'],
      ]),
      p('Điểm phải cân nhắc kỹ là số tài khoản nhận tiền. Không thể ghi sẵn một số bịa ra, vì số bịa vẫn có thể là tài khoản thật của người khác, quét nhầm là tiền đi mất. Cách giải quyết: cấu hình để trống, kèm một chế độ chạy thử. Bật chế độ thử thì hệ thống dùng số tài khoản toàn số 0, chắc chắn không phải của ai, ứng dụng ngân hàng quét sẽ báo tài khoản không tồn tại. Giao diện hiện cảnh báo đỏ ngay trên mã QR, và mở thêm một nút giả lập ngân hàng báo có để đi hết luồng mà không phải chuyển tiền thật. Chạy thật chỉ cần điền số tài khoản và tắt cờ sandbox.', { size: 23 }),
      p('Kiểm chứng bằng cách tự đi hết luồng trên MySQL thật: đơn VS-20260927-0035 tạo phiếu cọc 19.760.883 đồng, nhận mã QR, giả lập báo có thì đơn chuyển sang đã đóng cọc, trả nốt phần còn lại thì thành đã thanh toán đủ, trả thêm lần nữa thì bị từ chối đúng câu "Đơn này đã thanh toán đủ". Phía quản trị: phiếu chờ đối soát hiện đúng một dòng, bấm xác nhận kèm mã giao dịch thì ô đếm chuyển từ 1 chờ sang 0 chờ và 89 đã nhận tiền, đồng thời cột tiền trên đơn cập nhật theo.', { size: 23 }),
      p("Kiểm chứng: kiểm thử phía máy chủ tăng từ 150 lên 158 bài nhờ tám bài mới cho bộ dựng mã VietQR, đạt 158/158. Bộ Selenium chạy lại trên MySQL đạt 130/130 trong khoảng 460 giây, không test case nào phải sửa.", { size: 23 }),
      khongAnh('phần thanh toán này.'),

      // ---------------- 28/09 ----------------
      h1('Ngày 28/09/2026'),
      p('Mục tiêu: làm nốt bốn hạng mục còn thiếu so với một website nhà hàng chạy thật, rồi đổ dữ liệu quy mô lớn để nhìn hệ thống ở mức tải gần với thực tế.', { size: 23 }),

      h2('Việc 53 — Bốn hạng mục còn thiếu của một website chạy thật'),
      p('Ngày 28/09/2026, migration V18 và V19, chưa commit.', { italic: true, size: 21 }),
      p('Đợt rà soát ngày 27/09 kết luận website còn thiếu bốn thứ, tất cả đều vướng ở chỗ phải có tài khoản hoặc dịch vụ bên ngoài. Cách xử lý chung cho cả bốn: viết trọn phần mã, để trống chỗ điền khóa, và làm sao cho hệ thống vẫn chạy đầy đủ khi chưa có khóa. Tải mã nguồn về là chạy được ngay, không bị chặn ở bước đăng ký dịch vụ.', { size: 23 }),
      fileTable([
        ['Email xác nhận đơn', 'Thêm EmailService. Chưa khai báo máy chủ thư thì không gửi đi đâu cả, chỉ ghi toàn văn nội dung vào bảng email_logs và in ra màn hình; luồng nghiệp vụ vẫn chạy trọn vẹn. Dấu hiệu nhận biết lấy ngay từ việc Spring có dựng được bộ gửi thư hay không, khỏi phải khai thêm một cờ bật tắt rồi hai chỗ khai lệch nhau. Gửi thư hỏng chỉ ghi nhật ký chứ không ném lỗi ra ngoài: đơn đã lưu xong rồi thì không có lý do gì báo lỗi cho khách vì máy chủ thư trục trặc.'],
        ['Quên mật khẩu', 'Khách nhập email, hệ thống gửi liên kết chứa mã dùng một lần, sống 30 phút. Mã chỉ lưu dưới dạng băm SHA-256 chứ không lưu nguyên văn, ai đọc được bảng cũng không dựng lại được liên kết để chiếm tài khoản. Nhập email chưa đăng ký vẫn báo thành công y hệt email có thật, nếu báo khác nhau thì người ngoài dò được email nào đã có tài khoản.'],
        ['Cổng thanh toán VNPay', 'Viết đủ luồng: dựng địa chỉ chuyển sang cổng, ký HMAC-SHA512, nhận kết quả trả về và kiểm chữ ký. Điểm quan trọng nhất là kiểm chữ ký trước khi đọc bất kỳ tham số nào khác; tin vào số tiền trong tham số rồi mới kiểm là bỏ luôn tác dụng bảo vệ vì tham số trên thanh địa chỉ ai cũng sửa được. Chưa khai mã đơn vị thì giao diện ẩn hẳn nút VNPay, khách vẫn trả bằng mã VietQR như cũ.'],
        ['Tài khoản nhân viên', 'Trước đây muốn có tài khoản nhân viên phải sửa thẳng cột role trong cơ sở dữ liệu vì trang đăng ký chỉ tạo ra khách hàng. Nay có màn hình quản trị tài khoản: tạo tài khoản nhân viên hoặc quản trị, đổi vai trò, khóa mở, đặt lại mật khẩu hộ. Hai quy tắc tự bảo vệ: không tự đổi vai trò của chính mình và không tự khóa chính mình, tránh cảnh quản trị viên duy nhất tự khóa rồi không ai vào được nữa.'],
      ]),
      p('Một lỗi tự gây rồi tự sửa: hai bảng mới đều kế thừa lớp BaseEntity, mà lớp đó có sẵn hai cột thời gian, nhưng migration V18 chỉ tạo một cột nên Hibernate kiểm tra cấu trúc lúc khởi động và từ chối chạy. Loại lỗi này trình biên dịch không bắt được vì viết SQL và viết entity là hai nơi tách rời. Vì V18 đã chạy rồi nên không sửa được nữa, phải thêm V19 bù cột.', { size: 23 }),
      p('Kiểm chứng từng luồng trên MySQL thật: xin liên kết đặt lại mật khẩu cho email có thật và email không tồn tại đều trả về cùng một kết quả; đặt lại mật khẩu bằng mã trong thư thành công, dùng lại đúng mã đó lần hai bị từ chối; đăng nhập bằng mật khẩu mới được. Tạo một tài khoản nhân viên, đăng nhập được, vào được màn hình đơn đặt tiệc nhưng bị chặn khỏi màn hình quản trị tài khoản, đúng như mong đợi.', { size: 23 }),

      h2('Việc 54 — Đổ dữ liệu quy mô lớn'),
      p('Ngày 28/09/2026, tệp cong-cu/du-lieu-mau/du-lieu-lon.sql, chưa commit.', { italic: true, size: 21 }),
      p('Dữ liệu mẫu ở V15 chỉ có 52 đơn, đủ để nhìn thấy biểu đồ nhưng chưa phải mức tải thật. Sinh thêm một bộ dữ liệu lớn trải 24 tháng để xem hệ thống ở quy mô gần với một nhà hàng đang kinh doanh.', { size: 23 }),
      fileTable([
        ['1.500 khách hàng', 'Tên ghép từ vốn họ, tên đệm và tên riêng của người Việt; email, số điện thoại, địa chỉ và ngày mở tài khoản đều rải đều 24 tháng.'],
        ['1.603 đơn đặt tiệc', 'Tiền tính đúng theo PricingService nên số trên trang thống kê cộng lại luôn khớp công thức. Không có hai đơn đã xác nhận trùng không gian, ngày và buổi.'],
        ['1.480 đơn đặt món', '5.274 dòng món, xen kẽ giao tận nhà và ăn tại chỗ, phí giao và VAT tính theo đúng quy định.'],
        ['460 đánh giá', 'Phân bố sao lệch về phía tốt nhưng không tuyệt đối: 271 đánh giá 5 sao, 121 bốn sao, 47 ba sao, 21 một và hai sao. Nội dung viết khác nhau theo mức sao.'],
        ['4 nhân viên', 'Ba nhân viên và một quản trị, để màn hình quản trị tài khoản có đủ ba vai trò mà xem.'],
      ]),
      p('Cố ý không làm thành migration của Flyway. Đây là dữ liệu để xem cho giống hệ thống chạy thật chứ không phải một phần cấu trúc cơ sở dữ liệu; để thành migration thì ai dựng lại cơ sở dữ liệu cũng phải nạp mấy vạn dòng, mà sửa cũng không sửa được nữa vì Flyway lưu mã kiểm tra. Xuất ra tệp SQL riêng, nạp bằng một lệnh khi cần, nạp hết 1,3 giây.', { size: 23 }),
      p('Một chi tiết phải tính trước: mã đơn của bộ dữ liệu lớn đánh số từ 5001, tách khỏi dải hệ thống tự cấp bắt đầu từ 0001 và dải dữ liệu mẫu nhỏ bắt đầu từ 9001. Bài học rút từ lần V15 hỏng giữa chừng vì trùng mã. Cũng chỉ để khoảng 4% đơn rơi vào tương lai, vì đơn tương lai chiếm chỗ ngày mà bộ kiểm thử tự động lại cần tìm ngày còn trống.', { size: 23 }),
      p('Sau khi nạp: 1.532 tài khoản, 1.714 đơn đặt tiệc, 1.547 đơn đặt món với 5.414 dòng món, 488 đánh giá, 2.888 phiếu thu, 3.175 dòng lịch sử trạng thái. Trang thống kê 12 tháng gần nhất ghi nhận doanh thu 90,5 tỉ đồng trên 1.234 đơn, 174.087 lượt khách, trung bình 73,3 triệu một đơn, tỉ lệ hủy 9,4%, cả 12 tháng đều có doanh thu. Điểm đánh giá trung bình 4,38 trên 5 từ 449 đánh giá đã duyệt.', { size: 23 }),
      p("Kiểm chứng: kiểm thử phía máy chủ 158/158. Bộ Selenium chạy lần đầu trên dữ liệu lớn trượt hai test case, cả hai đều do dữ liệu tôi sinh ra chứ không phải lỗi website: một đơn đã xác nhận của bộ dữ liệu lớn trùng đúng không gian, ngày và buổi mà kịch bản TC-BOOK-01 dùng cố định; và mười lăm đánh giá bị đề ngày ở tương lai nên đứng trên đánh giá mà TC-ADMIN-07 vừa duyệt, đẩy nó khỏi trang đầu. Sửa cả dữ liệu đang có lẫn bộ sinh, chạy lại đạt 130/130 trong khoảng 451 giây.", { size: 23 }),
      khongAnh('hai việc này.'),

      h2('Việc 55 — Dọn dữ liệu do bộ kiểm thử để lại trong dữ liệu thật'),
      p('Ngày 28/09/2026, chưa commit.', { italic: true, size: 21 }),
      p('Mở thực đơn trên trình duyệt thì thấy lẫn vào giữa các món thật những dòng tên kiểu "Món thử giá 317610324460300". Truy ra là do bộ kiểm thử tự động: ba kịch bản tạo món và một kịch bản gửi đánh giá đều tạo dữ liệu qua giao diện hoặc API nhưng không dọn sau khi chạy. Mỗi lần chạy lại đẻ thêm một lớp, tích lại thành 17 món và 15 đánh giá giả nằm trong cơ sở dữ liệu thật, trong đó 5 đánh giá đã duyệt nên hiện công khai và kéo lệch điểm trung bình của nhà hàng.', { size: 23 }),
      p('Đây là lỗi của bộ kiểm thử chứ không phải của website, và là loại lỗi tự nó lớn dần theo số lần chạy. Sửa ở ba lớp thay vì chỉ xóa dữ liệu, vì chỉ xóa thì lần chạy sau lại có.', { size: 23 }),
      fileTable([
        ['Dữ liệu đang có', 'Xóa 17 món, 15 đánh giá và 7 đơn đặt món rỗng do kiểm thử để lại. Nhận diện bằng dãy từ mười chữ số liền nhau trong tên hoặc nội dung, đó là dấu vết của System.nanoTime mà kịch bản nối vào để mỗi lần chạy ra một giá trị khác nhau; dữ liệu thật không bao giờ có dãy số dài như vậy.'],
        ['Backend', 'Thêm đường dẫn DELETE /api/v1/admin/dishes/{id}/vinh-vien để xóa hẳn một món. Chặn lại nếu món đã từng nằm trong đơn, vì hóa đơn cũ còn phải tra ra được tên món; trường hợp đó vẫn dùng chức năng ngừng bán như trước. Chức năng này cũng có ích cho quản trị viên lỡ tạo nhầm một món.'],
        ['Bộ kiểm thử', 'Thêm bước dọn chạy một lần sau mỗi lớp kịch bản, đặt ở lớp cơ sở nên mọi lớp viết sau này cũng được dọn. Dọn theo tên chứ không theo mã món đã ghi lại lúc tạo, vì TC-ADMIN-08 tạo món qua giao diện quản trị nên không có mã để ghi; dọn theo tên còn quét luôn phần sót của các lần chạy trước.'],
      ]),
      p('Hai lỗi tự gây rồi tự sửa trong lúc làm phần này. Thứ nhất, hàm dọn đánh giá gọi đường dẫn danh sách mà không truyền tham số, mà đường dẫn đó mặc định chỉ trả về đánh giá đang chờ duyệt; TC-ADMIN-07 lại duyệt đánh giá trước khi kết thúc nên nó đã nằm ở danh sách đã duyệt, dọn hụt đúng cái cần dọn. Sửa thành quét cả hai danh sách. Thứ hai, món do TC-DATMON-09 tạo ra bị chính kịch bản đó đặt vào một đơn, nên backend từ chối xóa hẳn đúng như thiết kế; thêm bước lùi về ngừng bán để món biến khỏi thực đơn công khai mà dòng dữ liệu vẫn còn cho đơn cũ tra cứu.', { size: 23 }),
      p('Kiểm chứng: chạy lại toàn bộ bộ kiểm thử rồi đếm lại trong cơ sở dữ liệu, không còn món kiểm thử nào hiện trên thực đơn công khai và không còn đánh giá kiểm thử nào.', { italic: true, size: 21 }),

      h2('Việc 56 — Đa ngôn ngữ Việt và Anh'),
      p('Ngày 28/09/2026, migration V20 đến V22, chưa commit.', { italic: true, size: 21 }),
      p('Khách nước ngoài vào website thì không đọc được gì. Làm song ngữ Việt và Anh, mặc định tiếng Việt, đổi bằng một nút ở góc phải thanh đầu trang.', { size: 23 }),
      p('Cố ý không dùng thư viện đa ngôn ngữ có sẵn mà tự viết một lớp nhỏ 115 dòng theo cùng mô hình khóa và giá trị. Lý do: nhu cầu của dự án chỉ là tra khóa và thay biến trong câu, trong khi thư viện chuẩn thêm khoảng 15KB sau khi nén vào gói tải về, mà đo Lighthouse trước đó đã chỉ ra website đang thừa JavaScript. Kết quả đo lại: gói chính trước 443,15KB, sau 443,13KB.', { size: 23 }),
      fileTable([
        ['Chọn ngôn ngữ', 'Đặt ngoài thẻ nav chứ không nằm trong danh sách mục menu. Trên điện thoại cả menu bị thu vào nút ba gạch, để bên trong thì khách nước ngoài phải mở menu ra mới thấy, mà họ chưa đọc được tiếng Việt thì không biết bấm nút nào.'],
        ['Không tự đoán ngôn ngữ', 'Nhiều máy của người Việt cài Windows bản tiếng Anh. Đoán theo ngôn ngữ trình duyệt thì khách người Việt mở web ra lại thấy tiếng Anh, hỏng hơn là không đoán. Lựa chọn của khách được nhớ lại nên chỉ cần đổi một lần.'],
        ['Nội dung cơ sở dữ liệu', 'Thêm cột tiếng Anh cho món ăn, danh mục, không gian, gói tiệc và chú thích ảnh. Backend trả cả hai bản trong một lần gọi, giao diện tự chọn; cách này tránh phải luồn tham số ngôn ngữ qua từng lớp service. Cột để trống thì tự lùi về bản tiếng Việt nên thêm món mới chưa kịp dịch cũng không vỡ trang.'],
        ['Cụm từ chuẩn hóa', 'Tiện ích không gian, hạng mục gói tiệc và tên ngày lễ tra bằng từ điển trong mã nguồn thay vì thêm cột. Cả hệ thống chỉ có 18 tiện ích và 5 tên ngày lễ dùng chung cho nhiều dòng; thêm cột thì phải lưu lại cùng một chữ ở nhiều chỗ rồi sửa một chỗ lại quên chỗ kia.'],
        ['Nhãn trạng thái', 'Cả mười enum của backend trả kèm nhãn tiếng Anh, gồm trạng thái đơn, loại sự kiện, buổi tổ chức, hình thức nhận món và các nhãn thanh toán.'],
        ['Tiền, ngày và thẻ html', 'Tiếng Việt 15.000.000 ₫, tiếng Anh 15,000,000 VND. Không quy đổi sang đô la vì tỉ giá đổi hằng ngày, hiện số cũ thành báo giá sai. Thuộc tính lang của thẻ html đổi theo, nếu để nguyên thì trình đọc màn hình đọc tiếng Anh bằng giọng tiếng Việt. Tiêu đề tab và thẻ mô tả cũng đổi vì đó là dòng Google lấy làm tiêu đề kết quả tìm kiếm.'],
      ]),
      p('Phạm vi: 22 trang dành cho khách đã dịch hết, 426 khóa mỗi thứ tiếng. Mười trang quản trị cố ý để nguyên tiếng Việt vì nhân viên là người Việt, dịch phần đó là công vô ích.', { size: 23 }),
      p('Một lỗi đáng kể bắt được nhờ tự viết công cụ rà: hàm kiểm tra dữ liệu của trang đặt tiệc nằm ngoài component nên không lấy được hàm dịch. Vite build vẫn qua vì đây là lỗi lúc chạy chứ không phải lỗi cú pháp, và trang chỉ vỡ khi khách bỏ trống một ô bắt buộc — thử bằng tay rất dễ sót. Viết thêm hai script kiểm tra chạy được như một bước kiểm thử: một script đối chiếu hai tệp ngôn ngữ xem có khóa nào lệch hoặc bỏ trống, một script rà toàn dự án xem có chỗ nào gọi hàm dịch mà không lấy được nó.', { size: 23 }),
      p('Cũng sửa một migration hỏng do tôi viết: V20 gộp ba lệnh thêm cột vào một câu ALTER TABLE kèm mệnh đề AFTER, là cú pháp riêng của MySQL. Chạy trên MySQL thật thì trót lọt, nhưng hồ sơ kiểm thử dùng H2 nên 106 trên 158 test phía máy chủ hỏng cùng lúc. Sửa thành ba câu ALTER riêng, đúng như các migration V16 đến V19 vẫn làm.', { size: 23 }),

      h2('Việc 57 — Bổ sung một trăm món vào thực đơn'),
      p('Ngày 28/09/2026, migration V22, chưa commit.', { italic: true, size: 21 }),
      p('Thực đơn 26 món là quá mỏng so với một nhà hàng tiệc thật. Bổ sung 100 món, đưa tổng lên 126: khai vị 26, món chính 36, lẩu và nướng 26, tráng miệng 19, đồ uống 19. Mỗi món ghi đủ bảy trường như các món cũ và có cả hai thứ tiếng: tên, mô tả ngắn, nguyên liệu, cách chế biến, khẩu phần, lưu ý khi đặt và thời gian bếp cần.', { size: 23 }),
      p('Sinh migration bằng script thay vì gõ tay 100 câu INSERT. Dữ liệu nằm ở một chỗ duy nhất, sửa mô tả một món thì chạy lại là ra tệp SQL mới; gõ tay thì hai nơi lệch nhau lúc nào không biết vì SQL vẫn chạy trót lọt.', { size: 23 }),
      p('Phần ảnh minh họa là chỗ mất công nhất và cũng là chỗ chưa đạt yêu cầu ban đầu. Tra ảnh tự động theo từ khóa cho ra rất nhiều ảnh sai chủ đề.', { size: 23 }),
      fileTable([
        ['Vòng 1 — Wikimedia Commons', 'Tra theo tên món, lấy kết quả đầu tiên hợp giấy phép. Cho ra một bức tranh của Bruegel cho món cá tai tượng chiên xù, đĩa sushi cho lẩu nấm chay, con tem bưu chính cho soda chanh bạc hà vì tên tệp có chữ mint. Bỏ cách này.'],
        ['Vòng 2 — thêm điều kiện lọc', 'Bắt buộc tên tệp phải chứa từ khóa của món và ảnh phải nằm trong thể loại đồ ăn thức uống. Điều kiện thể loại chặn được tem và tranh, nhưng chỉ còn 55 trên 100 món có ảnh vì Commons là kho tư liệu bách khoa, ảnh món ăn thường ngày rất mỏng.'],
        ['Vòng 3 — đổi sang Openverse', 'Openverse gom ảnh giấy phép mở từ nhiều nguồn, trong đó có Flickr, nơi có rất nhiều ảnh món ăn thật. Tra "banh khot" ra 41 kết quả đúng món, trong khi Commons không có kết quả nào dùng được. API không cần khóa nên vẫn không tốn phí.'],
        ['Vòng 4 — duyệt bằng mắt', 'Đọc tiêu đề từng ảnh rồi loại tay 30 ảnh còn lệch, ví dụ Toffee Bananas cho sườn ram mặn, Frogs for sale là ảnh ếch sống ngoài chợ cho món ếch xào lăn. Bước này không tự động hóa được: máy không phân biệt được con tem với đĩa đồ ăn, người đọc tiêu đề thì thấy ngay.'],
      ]),
      p('Kết quả: 104 trên 126 món có ảnh, tức 78 trên 100 món mới. 22 món để trống và hiện khối giữ chỗ có sẵn kèm biểu tượng. Dừng ở đó là lựa chọn có chủ đích: thực đơn dán ảnh sai món tệ hơn thực đơn thiếu ảnh, khách mở ra thấy món lẩu hiện ảnh sushi thì mất lòng tin vào cả trang. Cách đúng về lâu dài là nhà hàng tự chụp rồi tải lên qua màn hình quản trị, ô tải ảnh đã có sẵn từ Việc 11.', { size: 23 }),
      p('Giấy phép chỉ nhận CC0, phạm vi công cộng, CC BY và CC BY-SA. Cố ý loại nhóm NC là phi thương mại, vì website nhà hàng là mục đích thương mại, dùng ảnh NC vào đó là sai giấy phép. Bảng ghi nguồn 78 ảnh kèm tên tác giả và đường dẫn trang gốc lưu ở cong-cu/du-lieu-mau/mon-an/ghi-nguon-anh.md.', { size: 23 }),
      p('Sửa thêm một test viết chưa tốt lộ ra khi thực đơn nở ra: ApplicationSmokeTest đòi đúng sáu món trong danh mục lẩu, thêm 100 món vào là trượt ngay dù bộ lọc không hỏng chỗ nào. Đòi một con số cố định gắn với dữ liệu mẫu thì cứ mở rộng dữ liệu là phải sửa test, mà sửa mãi thì người ta quen tay sửa cho qua chứ không đọc xem nó báo gì. Đổi thành kiểm tính chất: có trả về món, và không lọt món của danh mục khác.', { size: 23 }),
      p('Kiểm chứng ba việc 55, 56, 57: kiểm thử phía máy chủ 158/158, Selenium 130/130 trên MySQL thật với 126 món. Hai script kiểm tra bản dịch đều đạt, 426 khóa khớp nhau ở cả hai tệp ngôn ngữ và không chỗ nào gọi hàm dịch mà thiếu. Kiểm trên trình duyệt: đổi sang tiếng Anh thì cả 104 đường dẫn ảnh đều tải được, nhãn menu, thực đơn, trang đặt tiệc và trang ưu đãi đều sang tiếng Anh, tiền hiện đúng dạng 135,000 VND, tiêu đề tab đổi theo; đổi ngược về tiếng Việt thì không sót chữ tiếng Anh nào.', { italic: true, size: 21 }),
      khongAnh('ba việc này.'),

      h2('Việc 58 — Workflow GitHub Actions chạy hai bộ kiểm thử'),
      p('Ngày 01/10/2026, commit d71c495.', { italic: true, size: 21 }),
      p('Mốc kế hoạch 23/09 – 28/09 là dựng workflow tự thiết lập môi trường, cài dependency rồi chạy bộ kiểm thử. Trước việc này kho mã nguồn chưa hề có thư mục .github nào, nên mốc đó trễ ba ngày.', { size: 23 }),
      p('Tách thành hai công việc chạy song song và cố ý không cho cái sau phụ thuộc cái trước. Đây là hai số liệu thực nghiệm riêng của đề tài: kiểm thử đơn vị JUnit chạy trên H2 trong bộ nhớ, và kiểm thử giao diện Selenium chạy trên MySQL. Kiểm thử đơn vị trượt mà chặn luôn Selenium thì mất số liệu của lần chạy đó, trong khi hai bộ không kiểm cùng một thứ.', { size: 23 }),
      fileTable([
        ['Dùng profile local, không dùng prod', 'Prod đặt include-message never nên lỗi trả về bị giấu, mà một số kịch bản lại kiểm đúng câu thông báo lỗi hiện trên giao diện. Dùng local kèm MySQL bỏ trống mật khẩu root còn làm cho máy chủ CI giống hệt môi trường XAMPP vẫn chạy ở máy cá nhân, nên hai nơi mới ra cùng kết quả để so sánh được.'],
        ['Đặt múi giờ ở cấp công việc', 'Nhiều kịch bản tự tính "ngày mai" hoặc "sau 3 ngày" rồi đối chiếu với quy tắc báo trước mà backend kiểm. Máy chủ GitHub chạy giờ UTC, lệch 7 tiếng, nên từ 17 giờ UTC trở đi hai bên hiểu "hôm nay" là hai ngày khác nhau và kịch bản trượt vì lý do không liên quan đến lỗi thật.'],
        ['Giao diện phục vụ bằng bản dịch production', 'Không còn proxy của Vite nên mọi lệnh gọi API thành khác nguồn. Phải ghi địa chỉ backend vào lúc dịch và khai cổng 4173 vào danh sách CORS của backend.'],
        ['Thăm dò /api/v1/spaces', 'SecurityConfig có cho phép /actuator/health nhưng dự án không khai Spring Boot Actuator nên đường dẫn đó trả 404, vòng chờ sẽ đợi mãi không thấy backend sẵn sàng.'],
      ]),
      p('Thêm script tom-tat-ket-qua.js đọc báo cáo XML của Surefire rồi in bảng Pass/Fail kèm thời gian từng luồng thẳng lên trang tóm tắt của lần chạy. Đây chính là số liệu cần cho mục bảng thống kê kết quả kiểm thử, khỏi phải tải tệp về mở từng tệp một.', { size: 23 }),
      p('Kiểm chứng trước khi đẩy lên, bằng cách dựng lại đúng môi trường đó ngay trên máy: bản dịch production phục vụ qua vite preview ở cổng 4173, gọi API khác nguồn sang 8080, Chrome chạy ẩn. Preflight từ nguồn 4173 trả 200 đủ header, nguồn lạ 9999 bị chặn 403, đường dẫn sâu /thuc-don trả 200 nên SPA fallback đúng. Chạy thật hai nhóm kịch bản trên cấu hình đó: 18/18 đạt.', { italic: true, size: 21 }),
      p('Workflow chưa chạy lần nào trên GitHub: nó chỉ kích hoạt khi mã nằm trên nhánh main, mà nhánh hiện tại chưa hợp nhất. Ghi rõ ở đây để không nhầm "đã viết xong" thành "đã chạy được".', { size: 23 }),
      khongAnh('việc này; kết quả chạy sẽ nằm ở trang tóm tắt của GitHub Actions sau khi hợp nhất.'),

      h2('Việc 59 — Bộ lọc gói tiệc, mở rộng danh sách gói và Luồng 16 kiểm thử'),
      p('Ngày 01/10 – 02/10/2026, commit 2974198 và 440b750.', { italic: true, size: 21 }),
      p('Mốc 15/09 – 21/09 còn sót yêu cầu tab/filter của đề cương: lọc không gian và tab thực đơn đã có từ trước, riêng gói tiệc thì trang bày ra cả ba gói và hết, khách không thu hẹp được theo ngân sách hay theo số món. Bổ sung ba điều kiện lọc: giá tối đa mỗi mâm, số món tối thiểu, và thời gian dùng không gian. Mốc trọn ngày để 8 giờ, lấy theo app.booking.full-day-package-hours chứ không tự đặt ra, vì để hai nơi lệch nhau thì khách lọc ra một gói trọn ngày mà lúc tính tiền lại không được tính trọn ngày.', { size: 23 }),
      p('Lọc trên ba gói gần như không có tác dụng, nên migration V23 mở danh sách lên tám gói. Ba gói cũ đều là tiệc lớn kiểu cưới hỏi và gala, khoảng giá 2,9 đến 6,8 triệu. Năm gói thêm vừa phủ kín các loại tiệc nhà hàng F&B thật sự nhận, vừa giải rộng cả ba trục mà bộ lọc dùng đến: Họp Mặt 1,8 triệu 5 món 2 giờ; Sen Trắng 3,6 triệu 7 món 4 giờ; Tất Niên 3,9 triệu 9 món 4 giờ; Hội Nghị 5,2 triệu 8 món 8 giờ; Cưới Trọn Gói 8,5 triệu 12 món 10 giờ. Lọc theo các điều kiện khác nhau giờ ra từ 0 đến 8 gói.', { size: 23 }),
      p('Thêm Luồng 16 với 10 test case và lớp trang TrangGoiTiec. Trước đó cả bộ kiểm thử không có kịch bản nào đi qua trang bảng giá, dù đây là một trong năm trang khách xem nhiều nhất trước khi quyết định đặt tiệc. TC-PKG-10 cũng là kịch bản đầu tiên canh chức năng đa ngôn ngữ: trang này có cả chữ cố định lấy từ tệp ngôn ngữ lẫn chữ lấy từ cơ sở dữ liệu nên một kịch bản kiểm được cả hai đường dịch.', { size: 23 }),
      p('Hai chỗ sai lộ ra khi dữ liệu nở ra, cả hai đều nằm ở kiểm thử chứ không phải ở chức năng:', { size: 23 }),
      fileTable([
        ['ApplicationSmokeTest đòi đúng ba gói tiệc', 'Đúng cái bẫy đã vấp một lần ở Việc 57 với 100 món ăn. Sửa theo cùng cách: kiểm tính chất, tức có gói để bán và mỗi gói đủ ba con số mà trang bảng giá lẫn bộ lọc đều dùng đến.'],
        ['returnsTopThreeSortedByScore đòi cả ba gợi ý xếp theo điểm giảm dần', 'Điều đó chưa bao giờ là hợp đồng của hàm chọn đa dạng không gian: khi không đủ sảnh khác nhau, nhóm nới giới hạn được nối vào cuối và có thể hơn điểm phương án của sảnh thứ hai. Ba gói thì số tổ hợp ít nên nó tình cờ vẫn đúng, tám gói là trượt ngay. Ban đầu sửa nhầm hướng, xếp lại toàn danh sách theo điểm cho kiểm thử đạt; làm vậy thì hai phương án đầu có thể rơi vào cùng một sảnh, tức mất đúng cái mà hàm đó sinh ra để bảo đảm, mà giao diện lại không hề in điểm ra cho khách. Nên quay lại giữ nguyên hành vi, ghi rõ hợp đồng vào comment để lần sau không ai sửa hỏng nữa, và sửa kiểm thử cho đúng.'],
      ]),
      p('Kiểm chứng: 189/189 kiểm thử đơn vị phía máy chủ; 140/140 kiểm thử giao diện trên 16 luồng, chạy hết 7 phút 44 giây; 437/437 khóa hai bản dịch khớp nhau. Mười tổ hợp lọc thử thẳng trên API đều ra đúng kết quả mong đợi; thử trên trình duyệt cả tiếng Việt lẫn tiếng Anh thì lọc, đếm kết quả, bỏ lọc và khối rỗng đều đúng. Số ca chạy thật bằng đúng số ca trong đặc tả, tài liệu và mã nguồn không còn lệch nhau.', { italic: true, size: 21 }),
      khongAnh('hai việc này.'),

      h1(`Tình trạng dự án đến ngày ${SL.homNay}`),
      p(`Số liệu do script tự đếm từ mã nguồn ngày ${SL.homNay}, không gõ tay và không ước lượng.`, { italic: true, size: 21 }),
      bullet(`Kiểm thử phía máy chủ: ${SL.lopTestMay} lớp viết bằng JUnit, ${moTaChay(SL.chayMay)}.`),
      bullet(`Kiểm thử giao diện bằng Selenium: ${SL.tcSelenium} test case trong ${SL.luongSelenium} lớp kịch bản, tổng ${SL.tepKiemThu} tệp chia bốn tầng của mô hình Page Object Model. ${hoaDau(moTaChay(SL.chaySelenium))}; các test case không đạt đều do lỗi thật của website chưa sửa.`),
      bullet(`Cơ sở dữ liệu: ${SL.bang} bảng, ${SL.migration} migration.`),
      bullet(`Máy chủ: ${SL.module} module nghiệp vụ, ${SL.controller} controller, ${SL.api} đường dẫn API.`),
      bullet(`Giao diện: ${SL.trang} trang.`),
      bullet('Chặng hai, hệ thống kiểm thử tự động: xong 7/18 mốc, gồm bốn mốc lý thuyết và ba mốc cài Selenium, viết Page Object, viết test case. Ba mốc sau làm sớm hơn kế hoạch từ 9 đến 22 ngày. Mốc tiếp theo là cấu hình GitHub Actions, từ 16/10.'),
      bullet(`Mã nguồn: commit gần nhất ${SL.commitCuoi}, nhánh chính trên GitHub hợp nhất tới Pull Request #13. Hai nhánh đã đẩy nhưng chưa hợp nhất là tro-ly-ai/goi-mo-hinh-ngon-ngu (1 commit) và dat-mon/anh-chi-tiet-va-dat-mon-le (4 commit). Ở máy cá nhân còn ${SL.thayDoi} thay đổi chưa commit, gồm toàn bộ các việc từ Việc 25 trở đi.`),

      h1('Đối chiếu với kế hoạch 18 mốc'),
      p('Bảng dưới đây đối chiếu từng mốc trong tệp kế hoạch của khoa với tình trạng thật của dự án, để nhìn một lần là thấy đang đứng ở đâu.', { size: 23 }),
      new Table({
        columnWidths: [520, 1500, 3200, 3806],
        width: { size: W, type: WidthType.DXA },
        rows: [
          new TableRow({
            tableHeader: true,
            children: [cell("Mốc", 520, { bold: true, bg: HDR }), cell("Thời gian", 1500, { bold: true, bg: HDR }), cell("Nội dung theo kế hoạch", 3200, { bold: true, bg: HDR }), cell("Tình trạng thực tế", 3806, { bold: true, bg: HDR })],
          }),
          new TableRow({ children: [cell("1", 520), cell("16/08 – 23/08", 1500), cell("Tổng quan kiểm thử phần mềm, xác định phạm vi và đối tượng", 3200), cell("Xong. TongQuan_KiemThuPhanMem.docx và PhamVi_DoiTuong_YeuCau_KiemThu.docx", 3806)] }),
          new TableRow({ children: [cell("2", 520), cell("24/08 – 31/08", 1500), cell("Nghiên cứu Selenium WebDriver", 3200), cell("Xong. NghienCuu_SeleniumWebDriver.docx", 3806)] }),
          new TableRow({ children: [cell("3", 520), cell("01/09 – 07/09", 1500), cell("Nghiên cứu mô hình Page Object Model", 3200), cell("Xong. NghienCuu_PageObjectModel.docx", 3806)] }),
          new TableRow({ children: [cell("4", 520), cell("08/09 – 15/09", 1500), cell("Phân tích ứng dụng, đặc tả danh sách test case", 3200), cell("Xong. DacTa_TestCase.docx, bản cập nhật 01/10 có 140 test case trên 16 luồng", 3806)] }),
          new TableRow({ children: [cell("5", 520), cell("16/09 – 23/09", 1500), cell("Cài Selenium, khởi tạo dự án theo Page Object Model", 3200), cell("Xong sớm từ 14/09. Dự án kiemthu chia năm tầng: coso, thanhphan, trang, luong, kichban", 3806)] }),
          new TableRow({ children: [cell("6", 520), cell("24/09 – 30/09", 1500), cell("Xây Page Object và locator", 3200), cell("Xong sớm. 17 lớp trang và thành phần, định vị bằng thuộc tính data-test", 3806)] }),
          new TableRow({ children: [cell("7", 520), cell("01/10 – 07/10", 1500), cell("Viết test case tự động bằng Selenium", 3200), cell("Xong sớm. 140 test case trên 16 luồng nghiệp vụ", 3806)] }),
          new TableRow({ children: [cell("8", 520), cell("08/10 – 15/10", 1500), cell("Hoàn thiện bộ test, assertion, xử lý chờ và trường hợp không đạt", 3200), cell("Xong sớm ngày 20/09. Sửa ba chỗ chập chờn trong kịch bản và sáu lỗi website, kết quả 130/130; sau khi thêm Luồng 16 ngày 01/10 thì đạt 140/140", 3806)] }),
          new TableRow({ children: [cell("9", 520), cell("16/10 – 22/10", 1500), cell("Nghiên cứu Git, GitHub và GitHub Actions", 3200), cell("Xong sớm ngày 20/09. NghienCuu_Git_GitHubActions.docx, kèm khung workflow dự kiến", 3806)] }),
          new TableRow({ children: [cell("10", 520), cell("23/10 – 30/10", 1500), cell("Viết và cấu hình workflow GitHub Actions", 3200), cell("Đã viết xong ngày 01/10 và đẩy lên GitHub, sớm 22 ngày. Chưa chạy lần nào vì workflow chỉ kích hoạt khi mã nằm trên nhánh main", 3806)] }),
          new TableRow({ children: [cell("11", 520), cell("31/10 – 07/11", 1500), cell("Hoàn thiện tích hợp Selenium, POM và Actions", 3200), cell("Chưa làm", 3806)] }),
          new TableRow({ children: [cell("12", 520), cell("08/11 – 15/11", 1500), cell("Thực nghiệm: chạy toàn bộ test case, ghi Pass/Fail, thời gian, lỗi", 3200), cell("Làm trước một phần. Đã có 8 lần chạy ghi trong BaoCao_KiemThuTuDong.docx; đợt thực nghiệm chính thức sẽ chạy lại sau khi có Actions", 3806)] }),
          new TableRow({ children: [cell("13", 520), cell("16/11 – 23/11", 1500), cell("Phân tích kết quả thực nghiệm", 3200), cell("Một phần. Đã phân tích 10 lỗi phát hiện được; phần đánh giá GitHub Actions chưa có", 3806)] }),
          new TableRow({ children: [cell("14", 520), cell("24/11 – 30/11", 1500), cell("Hoàn thiện, bổ sung test case, sửa lỗi mã kiểm thử", 3200), cell("Một phần. Đã bổ sung 22 test case cho hai luồng ngày lễ và sửa các chỗ chập chờn", 3806)] }),
          new TableRow({ children: [cell("15", 520), cell("01/12 – 05/12", 1500), cell("Bảng thống kê, hình ảnh minh họa quá trình thực thi", 3200), cell("Một phần. Đã có 13 ảnh minh chứng và video demo Selenium; phần ảnh Actions chưa có", 3806)] }),
          new TableRow({ children: [cell("16", 520), cell("06/12 – 10/12", 1500), cell("Viết nội dung tiểu luận", 3200), cell("Chưa làm. Mới có đề cương", 3806)] }),
          new TableRow({ children: [cell("17", 520), cell("11/12 – 13/12", 1500), cell("Kiểm tra toàn bộ hệ thống, mã nguồn, workflow và tài liệu", 3200), cell("Chưa làm", 3806)] }),
          new TableRow({ children: [cell("18", 520), cell("14/12 – 16/12", 1500), cell("Hoàn thiện, rà soát để nộp", 3200), cell("Chưa làm", 3806)] }),
        ],
      }),
      p(''),
      p('Theo lịch thì hiện đang ở mốc 5. Thực tế đã làm xong tới mốc 9, tức sớm hơn kế hoạch khoảng năm tuần ở phần xây dựng và hoàn thiện bộ kiểm thử. Phần còn lại nặng nhất là nhóm GitHub Actions, gồm mốc 10 và 11, sau đó là viết tiểu luận từ mốc 16.', { size: 23 }),

      h1('Các hạng mục chưa thực hiện'),
      p('Những phần dưới đây chưa làm, hoặc đã làm nhưng chưa kiểm chứng đủ. Tách riêng nhóm chưa kiểm chứng để nhật ký không ghi quá lên tiến độ thật.', { size: 23 }),
      h2('Chặng một — website'),
      
      bullet('Đặt bàn cho sảnh nhỏ từ 5 đến 40 khách với thực đơn tự chọn, thay vì phải đặt trọn gói tiệc lớn.'),
      bullet('Dán khóa API thật để bật trợ lý AI. Phần mã đã xong: mở web là khóa được đọc tự động, chỉ còn thiếu đúng khóa.'),
      
      bullet('Màn hình lịch đặt chỗ theo ngày và tháng.'),
      bullet('Đánh giá hiệu năng hệ thống.'),
      bullet('Hai lỗi website chưa sửa: trang không gian không bỏ qua kết quả lọc cũ về muộn (L06), cùng dạng với lỗi bảng tạm tính trang đặt món đã sửa ngày 17/09; và gửi dữ liệu sai mã hóa thì backend trả lỗi 500 thay vì 400. Sáu lỗi còn lại đã sửa ở Việc 41: khách chưa đăng nhập không gửi được đánh giá; báo lỗi đăng ký không nói rõ ô nào sai; nút đặt giữ chỗ không chọn sẵn không gian; câu báo không tìm thấy bị thiếu dấu; bỏ trống mã đơn khi tra cứu thì không có câu nhắc; đăng nhập hai lần trong cùng một giây bị báo xung đột.'),
      h2('Đã làm nhưng chưa kiểm chứng đủ'),
      bullet('Trang thống kê đã hiện đủ biểu đồ, nhưng số liệu hiện là đơn nhập tay và đơn do kiểm thử tạo, chưa có dữ liệu kinh doanh đủ nhiều để đưa vào tiểu luận.'),
      bullet('Việc 29 trên MariaDB mới chạy từng lượt xác nhận đơn một; kịch bản hai quản trị xác nhận cùng lúc vẫn chỉ chạy trên H2, nên phần mức cô lập READ_COMMITTED chưa kiểm chứng dưới tải đồng thời thật.'),
      bullet('Nhánh gọi mô hình ngôn ngữ mới kiểm chứng bằng bản giả và bằng một khóa sai; đường gọi thành công chưa chạy lần nào.'),
      h2('Chặng hai — hệ thống kiểm thử tự động'),
      bullet('Quay bản video demo đầy đủ 5 test case bằng công cụ QuayManHinh và GhepVideo, lệnh đã soạn sẵn.'),
      bullet('Hợp nhất nhánh vào main để workflow GitHub Actions chạy lần đầu. Phần viết và cấu hình đã xong ở Việc 58.'),
      bullet('Mười một mốc còn lại từ 08/10 đến 16/12: hoàn thiện bộ test, GitHub Actions, thực nghiệm chính thức, phân tích kết quả và viết tiểu luận.'),
      bullet('Rút ngắn thời gian chạy, hiện khoảng 8 phút cho 140 test case; thử chạy song song các lớp kịch bản và chạy thêm trên Edge.'),
      h2('Mã nguồn'),
      bullet(`Commit ${SL.thayDoi} thay đổi đang ở máy cá nhân, và hợp nhất hai nhánh đã đẩy theo đúng thứ tự: tro-ly-ai/goi-mo-hinh-ngon-ngu trước, dat-mon/anh-chi-tiet-va-dat-mon-le sau.`),
    ],
  }],
});

Packer.toBuffer(doc).then((buf) => {
  const out = process.argv[2] || 'E:/vuonsen-fnb/doc/NhatKyCongViec_PhamTranTuanAnh.docx';
  fs.writeFileSync(out, buf);
  console.log('Da tao: ' + out + ' (' + Math.round(buf.length / 1024) + ' KB)');
  console.log(`Ngay cap nhat: ${SL.homNay} | thay doi chua commit: ${SL.thayDoi}`);
  console.log(`Kiem thu may chu: ${SL.lopTestMay} lop, ${moTaChay(SL.chayMay)}`);
  console.log(`Kiem thu Selenium: ${SL.tcSelenium} test case / ${SL.luongSelenium} luong, ${moTaChay(SL.chaySelenium)}`);
});
