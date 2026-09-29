/*
 * Sinh tài liệu mốc 9 (16/10 - 22/10): Nghiên cứu Git, GitHub và GitHub Actions.
 *
 * Mọi thông số kỹ thuật trong tài liệu đều tra lại từ tài liệu chính thức ngày 20/09/2026
 * (phiên bản các action, ảnh máy chủ chạy) chứ không viết theo trí nhớ. Phần quy trình chạy
 * bộ kiểm thử bám đúng cấu hình thật của dự án: Java 17, Maven, Vite 5, cổng 8080 và 5173,
 * tham số dòng lệnh của lớp CauHinh.
 *
 * Cách chạy: node tao-nghien-cuu-git-actions.js [đường dẫn ra]
 */
const {
  Document, Packer, Paragraph, TextRun, HeadingLevel, AlignmentType,
  Table, TableRow, TableCell, WidthType, ShadingType, LevelFormat, BorderStyle,
} = require('E:/vuonsen-fnb/cong-cu/nhat-ky/node_modules/docx');

// Khối mã không kẻ viền: các dòng liền nhau như một khối, không thành bảng
const KHONG_VIEN = ['top', 'bottom', 'left', 'right'].reduce(
  (v, canh) => ({ ...v, [canh]: { style: BorderStyle.NONE, size: 0, color: 'FFFFFF' } }), {},
);
const fs = require('fs');

const FONT = 'Times New Roman';
const DEN = '000000';
const CO = 26;
const W = 9071;
const HDR = 'D9D9D9';

const chu = (t, o = {}) => new TextRun({ text: t, font: FONT, size: o.size ?? CO, color: DEN, bold: !!o.dam, italics: !!o.nghieng });

const p = (t, o = {}) => new Paragraph({
  alignment: o.canh ?? AlignmentType.JUSTIFIED,
  spacing: { before: o.truoc ?? 0, after: o.sau ?? 120 },
  children: Array.isArray(t) ? t : [chu(t, o)],
});

const h1 = (t) => new Paragraph({
  heading: HeadingLevel.HEADING_1, spacing: { before: 360, after: 160 }, keepNext: true,
  children: [new TextRun({ text: t, font: FONT, size: 34, bold: true, color: DEN })],
});

const h2 = (t) => new Paragraph({
  heading: HeadingLevel.HEADING_2, spacing: { before: 260, after: 120 }, keepNext: true,
  children: [new TextRun({ text: t, font: FONT, size: 28, bold: true, color: DEN })],
});

const gach = (t) => new Paragraph({
  numbering: { reference: 'gach', level: 0 },
  alignment: AlignmentType.JUSTIFIED, spacing: { after: 60 },
  children: Array.isArray(t) ? t : [chu(t)],
});

const o = (t, rong, dau = false) => new TableCell({
  width: { size: rong, type: WidthType.DXA },
  shading: dau ? { type: ShadingType.CLEAR, fill: HDR, color: 'auto' } : undefined,
  margins: { top: 60, bottom: 60, left: 100, right: 100 },
  children: [new Paragraph({ children: [chu(t, { dam: dau, size: 24 })] })],
});

const bang = (cot, dauBang, dong) => new Table({
  width: { size: W, type: WidthType.DXA },
  columnWidths: cot,
  rows: [
    new TableRow({ tableHeader: true, children: dauBang.map((t, i) => o(t, cot[i], true)) }),
    ...dong.map((r) => new TableRow({ children: r.map((t, i) => o(t, cot[i])) })),
  ],
});

// Khối mã YAML: phông đều, nền xám nhạt, giữ nguyên thụt đầu dòng
const ma = (dong) => new Table({
  width: { size: W, type: WidthType.DXA },
  columnWidths: [W],
  rows: dong.map((d) => new TableRow({
    children: [new TableCell({
      width: { size: W, type: WidthType.DXA },
      shading: { type: ShadingType.CLEAR, fill: 'F2F2F2', color: 'auto' },
      borders: KHONG_VIEN,
      margins: { top: 10, bottom: 10, left: 120, right: 100 },
      children: [new Paragraph({
        spacing: { after: 0 },
        children: [new TextRun({ text: d === '' ? ' ' : d, font: 'Consolas', size: 19, color: DEN })],
      })],
    })],
  })),
});

const noiDung = [
  p('NGHIÊN CỨU GIT, GITHUB VÀ GITHUB ACTIONS', { canh: AlignmentType.CENTER, dam: true, size: 34, sau: 160 }),
  p('Tiểu luận tốt nghiệp — Kiểm thử tự động ứng dụng web bằng Selenium WebDriver', { canh: AlignmentType.CENTER, nghieng: true, size: 24, sau: 140 }),
  p('Sinh viên: Phạm Trần Tuấn Anh — MSSV 21130004 — Lớp DH21DTA', { canh: AlignmentType.CENTER, sau: 40 }),
  p('Giảng viên hướng dẫn: TS. Nguyễn Thị Phương Trâm', { canh: AlignmentType.CENTER, sau: 40 }),
  p('Mốc kế hoạch: 16/10/2026 – 22/10/2026 — Thực hiện sớm ngày 20/09/2026', { canh: AlignmentType.CENTER, sau: 260 }),

  h1('1. Mục đích và phạm vi'),
  p('Tài liệu tổng hợp phần lý thuyết của mốc 9 trong kế hoạch: Git, GitHub và GitHub Actions, gồm workflow, trigger, job, step và runner. Phần cuối xác định quy trình chạy bộ kiểm thử Selenium của đề tài trên GitHub Actions, làm đầu vào trực tiếp cho mốc 10 là viết tệp workflow thật.'),
  p('Các thông số kỹ thuật như phiên bản action hay ảnh máy chủ chạy đều tra lại từ tài liệu chính thức ngày 20/09/2026, vì nhóm công cụ này thay đổi nhanh: chỉ trong năm 2026 đã có thêm actions/checkout v7, actions/setup-java v6 và actions/upload-artifact v7.'),

  h1('2. Git'),
  h2('2.1. Mô hình ba vùng'),
  p('Git quản lý mã nguồn theo ba vùng. Vùng làm việc là các tệp đang sửa trên máy. Vùng chuẩn bị giữ những thay đổi đã chọn để đưa vào lần ghi tới. Kho chứa giữ các commit đã ghi. Hiểu ba vùng này giải thích được vì sao sửa tệp xong mà chưa thấy trong lịch sử: thay đổi mới ở vùng làm việc.'),
  bang([2200, 6871], ['Lệnh', 'Tác dụng trong đề tài'], [
    ['git status', 'Xem thay đổi chưa ghi. Hiện dự án còn 87 thay đổi ở máy cá nhân.'],
    ['git add <tệp>', 'Đưa đúng nhóm tệp vào vùng chuẩn bị, tránh ghi lẫn tài liệu và tệp tạm.'],
    ['git commit -m "..."', 'Ghi một mốc thay đổi kèm mô tả.'],
    ['git log --oneline', 'Xem lịch sử. Commit gần nhất của dự án là 1382aa7 ngày 06/09/2026.'],
    ['git branch, git switch', 'Tạo và chuyển nhánh cho từng chức năng.'],
    ['git merge', 'Hợp nhất nhánh chức năng vào nhánh chính.'],
    ['.gitignore', 'Bỏ qua target, node_modules, tệp cấu hình chứa khóa API và thư mục doc.'],
  ]),
  h2('2.2. Nhánh và hợp nhất'),
  p('Mỗi chức năng làm trên một nhánh riêng rồi hợp nhất vào nhánh chính. Cách này giữ nhánh chính luôn chạy được, và là điều kiện để GitHub Actions kiểm tra từng thay đổi trước khi hợp nhất. Dự án hiện có hai nhánh đã đẩy lên nhưng chưa hợp nhất là tro-ly-ai/goi-mo-hinh-ngon-ngu và dat-mon/anh-chi-tiet-va-dat-mon-le.'),
  p('Hợp nhất bằng merge giữ nguyên lịch sử thật của hai nhánh, còn rebase viết lại lịch sử cho thẳng hàng. Đề tài dùng merge để lịch sử phản ánh đúng thứ tự công việc, phù hợp với việc đối chiếu tiến độ trong nhật ký.'),

  h1('3. GitHub'),
  p('GitHub là nơi lưu kho Git trên mạng, kèm các công cụ cộng tác. Ba thứ đề tài dùng tới:'),
  gach('Pull request: đề nghị hợp nhất một nhánh, kèm phần xem lại thay đổi. Kết quả chạy kiểm thử tự động hiện ngay trong pull request.'),
  gach('Bảo vệ nhánh: bắt buộc pull request phải chạy kiểm thử đạt mới cho hợp nhất vào nhánh chính. Đây là chỗ bộ kiểm thử tự động phát huy tác dụng rõ nhất.'),
  gach('Bí mật của kho: nơi cất khóa API để workflow dùng mà không lộ trong mã nguồn. Đề tài dùng cho khóa mô hình ngôn ngữ của trợ lý nếu sau này bật.'),

  h1('4. GitHub Actions'),
  h2('4.1. Các khái niệm'),
  bang([1800, 7271], ['Khái niệm', 'Ý nghĩa'], [
    ['Workflow', 'Một quy trình tự động, viết bằng YAML, đặt trong .github/workflows. Một kho có thể có nhiều workflow.'],
    ['Event, trigger', 'Sự kiện kích hoạt workflow: push, pull_request, schedule chạy theo giờ, workflow_dispatch bấm chạy tay.'],
    ['Job', 'Một nhóm bước chạy trên cùng một máy. Các job mặc định chạy song song, dùng needs để nối tiếp.'],
    ['Step', 'Một bước trong job: chạy lệnh bằng run, hoặc gọi một action có sẵn bằng uses.'],
    ['Action', 'Đơn vị dùng lại được, ví dụ actions/checkout lấy mã nguồn về máy chạy.'],
    ['Runner', 'Máy thực thi job. GitHub cung cấp sẵn máy Ubuntu, Windows, macOS; ngoài ra có thể tự dựng máy riêng.'],
    ['Artifact', 'Tệp kết quả giữ lại sau khi chạy, ví dụ báo cáo Surefire và ảnh chụp lúc kịch bản trượt.'],
    ['Cache', 'Bộ nhớ đệm cho thư viện, giúp lần chạy sau không tải lại toàn bộ phụ thuộc Maven và npm.'],
    ['Service container', 'Container phụ chạy kèm job, ví dụ MySQL, có kiểm tra sẵn sàng bằng health check.'],
  ]),
  h2('4.2. Máy chạy do GitHub cung cấp'),
  p('Kho công khai được dùng máy chạy miễn phí. Nhãn ubuntu-latest hiện trỏ tới Ubuntu 24.04; theo thông báo ngày 17/09/2026 của GitHub, nhãn này sẽ chuyển dần sang Ubuntu 26.04 trong khoảng 19/10 đến 19/11/2026. Vì đề tài chạy đúng giai đoạn chuyển đổi, workflow nên ghi rõ phiên bản, ví dụ ubuntu-24.04, để kết quả thực nghiệm lặp lại được, rồi thử ubuntu-26.04 như một phần đánh giá.'),
  p('Máy Ubuntu của GitHub cài sẵn Java nhiều phiên bản, Maven, Node.js, Google Chrome và ChromeDriver, nên bộ kiểm thử Selenium chạy được mà không phải cài thêm trình duyệt. Nếu ảnh máy chạy đổi và thiếu Chrome thì dùng action browser-actions/setup-chrome để cài đúng phiên bản.'),
  h2('4.3. Phiên bản action dùng trong đề tài'),
  p('Tra ngày 20/09/2026 tại kho chính thức của từng action:'),
  bang([2600, 1400, 5071], ['Action', 'Phiên bản', 'Dùng để làm gì'], [
    ['actions/checkout', 'v7.0.1', 'Lấy mã nguồn của kho về máy chạy.'],
    ['actions/setup-java', 'v6.0.1', 'Cài JDK 17 đúng bản dự án dùng, kèm cache maven có sẵn trong action.'],
    ['actions/setup-node', 'v5', 'Cài Node.js cho phần giao diện, kèm cache npm.'],
    ['actions/upload-artifact', 'v7.0.1', 'Giữ lại báo cáo Surefire và ảnh chụp khi kịch bản trượt.'],
  ]),

  h1('5. Quy trình chạy bộ kiểm thử của đề tài trên GitHub Actions'),
  h2('5.1. Ràng buộc từ chính dự án'),
  bang([3000, 6071], ['Ràng buộc', 'Cách xử lý trong workflow'], [
    ['Kiểm thử cần cả máy chủ và giao diện đang chạy', 'Job khởi động backend và Vite ở chế độ nền, chờ tới khi gọi được API rồi mới chạy kiểm thử.'],
    ['Máy chủ có hai profile: dev dùng H2 trong bộ nhớ, local dùng MySQL', 'Chạy profile dev cho nhanh và sạch. Muốn chạy trên MySQL thì thêm service container mysql kèm health check.'],
    ['Giao diện chạy cổng 5173, đặt strictPort', 'Không đổi cổng; Vite trên máy chạy phải mở bằng --host 127.0.0.1 vì mặc định chỉ lắng nghe IPv6.'],
    ['Dự án không có actuator', 'Chờ sẵn sàng bằng cách gọi lặp một API công khai, ví dụ /api/v1/spaces, thay cho endpoint health.'],
    ['Lớp CauHinh đọc tham số -D và biến môi trường', 'Truyền -Dheadless=true, hoặc đặt biến CI để lớp CauHinh tự bật chế độ ẩn.'],
    ['Kịch bản giảm giá dịp lễ phụ thuộc ngày', 'Đặt biến môi trường TZ=Asia/Ho_Chi_Minh để ngày trên máy chạy khớp với quy ước nghiệp vụ.'],
    ['Bộ kiểm thử chạy khoảng 10 phút', 'Đặt timeout-minutes cho job; cân nhắc chia theo lớp kịch bản bằng matrix ở mốc sau.'],
  ]),
  h2('5.2. Các bước của job kiểm thử'),
  gach('Lấy mã nguồn bằng actions/checkout.'),
  gach('Cài JDK 17 bằng actions/setup-java, bật cache maven.'),
  gach('Cài Node.js bằng actions/setup-node, bật cache npm, rồi npm ci trong thư mục frontend.'),
  gach('Khởi động máy chủ: mvn spring-boot:run với profile dev, chạy nền, ghi log ra tệp.'),
  gach('Khởi động giao diện: npm run dev -- --host 127.0.0.1, chạy nền.'),
  gach('Chờ sẵn sàng: gọi lặp http://127.0.0.1:8080/api/v1/spaces và http://127.0.0.1:5173 cho tới khi trả về, có giới hạn số lần thử.'),
  gach('Chạy kiểm thử: mvn -f kiemthu/pom.xml test -Dheadless=true.'),
  gach('Luôn tải lên artifact báo cáo Surefire và thư mục ảnh chụp khi trượt, kể cả khi bước kiểm thử thất bại, nhờ if: always().'),
  h2('5.3. Khung workflow dự kiến cho mốc 10'),
  p('Bản phác thảo dưới đây chưa đưa vào kho, sẽ hoàn thiện và chạy thử ở mốc 10:'),
  ma([
    'name: Kiem thu giao dien',
    'on:',
    '  push:',
    '    branches: [ main ]',
    '  pull_request:',
    '  workflow_dispatch:',
    '',
    'jobs:',
    '  kiem-thu:',
    '    runs-on: ubuntu-24.04',
    '    timeout-minutes: 30',
    '    env:',
    '      TZ: Asia/Ho_Chi_Minh',
    '    steps:',
    '      - uses: actions/checkout@v7',
    '      - uses: actions/setup-java@v6',
    '        with:',
    '          distribution: temurin',
    '          java-version: "17"',
    '          cache: maven',
    '      - uses: actions/setup-node@v5',
    '        with:',
    '          node-version: "20"',
    '          cache: npm',
    '          cache-dependency-path: frontend/package-lock.json',
    '      - name: Cai thu vien giao dien',
    '        run: npm ci',
    '        working-directory: frontend',
    '      - name: Khoi dong may chu',
    '        run: mvn -q spring-boot:run -Dspring-boot.run.profiles=dev > be.log 2>&1 &',
    '        working-directory: backend',
    '      - name: Khoi dong giao dien',
    '        run: npm run dev -- --host 127.0.0.1 > fe.log 2>&1 &',
    '        working-directory: frontend',
    '      - name: Cho ung dung san sang',
    '        run: |',
    '          for i in $(seq 1 60); do',
    '            curl -sf http://127.0.0.1:8080/api/v1/spaces > /dev/null && \\',
    '            curl -sf http://127.0.0.1:5173 > /dev/null && exit 0',
    '            sleep 5',
    '          done',
    '          echo "Ung dung khong san sang"; exit 1',
    '      - name: Chay kiem thu Selenium',
    '        run: mvn -f kiemthu/pom.xml test -Dheadless=true',
    '      - uses: actions/upload-artifact@v7',
    '        if: always()',
    '        with:',
    '          name: ket-qua-kiem-thu',
    '          path: |',
    '            kiemthu/target/surefire-reports',
    '            kiemthu/target/anh-loi',
  ]),
  p('Nếu muốn chạy trên MySQL thay cho H2 thì thêm khối services vào job và đổi profile:'),
  ma([
    '    services:',
    '      mysql:',
    '        image: mysql:8',
    '        env:',
    '          MYSQL_ROOT_PASSWORD: root',
    '          MYSQL_DATABASE: vuonsen_fnb',
    '        ports: [ "3306:3306" ]',
    '        options: >-',
    '          --health-cmd="mysqladmin ping -h 127.0.0.1"',
    '          --health-interval=10s --health-timeout=5s --health-retries=10',
  ]),

  h1('6. Rủi ro đã lường trước'),
  bang([3000, 6071], ['Rủi ro', 'Cách phòng'], [
    ['Kịch bản chập chờn trên máy chạy yếu hơn máy cá nhân', 'Đã dùng chờ tường minh thay cho dừng cứng; tăng -Dcho.giay khi chạy trên máy chạy; ghi nhận số lần chạy lại như một chỉ số thực nghiệm.'],
    ['Múi giờ máy chạy là UTC', 'Đặt TZ=Asia/Ho_Chi_Minh, nếu không các kịch bản giảm giá dịp lễ và đặt trước n ngày sẽ lệch một ngày.'],
    ['Chrome trên máy chạy khác phiên bản máy cá nhân', 'Selenium Manager tự tải đúng ChromeDriver; ghi phiên bản Chrome vào log để đối chiếu khi phân tích kết quả.'],
    ['Cổng 5173 hoặc 8080 bị chiếm', 'Máy chạy là máy mới mỗi lần nên hiếm gặp; vẫn kiểm tra ở bước chờ sẵn sàng.'],
    ['Thời gian chạy vượt hạn mức', 'Kho công khai không tốn phút chạy; vẫn đặt timeout-minutes để job treo không chiếm máy.'],
    ['Trợ lý AI không có khóa API trên máy chạy', 'Kịch bản trợ lý chỉ kiểm thử nhánh trả lời dự phòng nên không cần khóa; nếu cần thì lấy từ secrets.'],
  ]),

  h1('7. Chỉ số sẽ ghi nhận cho chương thực nghiệm'),
  gach('Số test case đạt trên tổng số, theo từng lần chạy và từng luồng nghiệp vụ.'),
  gach('Thời gian chạy trên máy chạy của GitHub so với máy cá nhân.'),
  gach('Số kịch bản chập chờn, tức đổi kết quả giữa các lần chạy dù mã không đổi.'),
  gach('Số lỗi thật của website do kiểm thử phát hiện.'),
  gach('Thời gian tiết kiệm nhờ cache Maven và npm giữa các lần chạy.'),

  h1('8. Việc tiếp theo'),
  gach('Mốc 10 (23/10 – 30/10): viết tệp .github/workflows, chạy thử, chỉnh cho tới khi xanh.'),
  gach('Mốc 11 (31/10 – 07/11): hoàn thiện tích hợp, thêm huy hiệu trạng thái vào README, cân nhắc chia nhỏ job bằng matrix.'),
  gach('Trước đó cần commit 87 thay đổi đang ở máy cá nhân và hợp nhất hai nhánh đã đẩy, vì workflow chỉ chạy với mã có trên GitHub.'),

  h1('9. Tài liệu tham khảo'),
  gach('GitHub Docs — Writing workflows, Events that trigger workflows, Using jobs, About runners: https://docs.github.com/actions'),
  gach('GitHub Changelog — Ubuntu 26 generally available and latest migration, 17/09/2026: https://github.blog/changelog/2026-09-17-ubuntu-26-generally-available-and-latest-migration/'),
  gach('actions/runner-images — danh sách phần mềm cài sẵn trên máy chạy: https://github.com/actions/runner-images'),
  gach('actions/checkout v7.0.1: https://github.com/actions/checkout/releases'),
  gach('actions/setup-java v6.0.1: https://github.com/actions/setup-java/releases'),
  gach('actions/upload-artifact v7.0.1: https://github.com/actions/upload-artifact/releases'),
  gach('browser-actions/setup-chrome: https://github.com/browser-actions/setup-chrome'),
];

const doc = new Document({
  creator: 'Phạm Trần Tuấn Anh',
  title: 'Nghiên cứu Git, GitHub và GitHub Actions',
  styles: {
    default: {
      document: { run: { font: FONT, size: CO, color: DEN }, paragraph: { spacing: { line: 312 } } },
      heading1: { run: { font: FONT, size: 34, bold: true, color: DEN } },
      heading2: { run: { font: FONT, size: 28, bold: true, color: DEN } },
    },
  },
  numbering: {
    config: [{
      reference: 'gach',
      levels: [{
        level: 0, format: LevelFormat.BULLET, text: '•', alignment: AlignmentType.LEFT,
        style: { paragraph: { indent: { left: 567, hanging: 283 } }, run: { font: FONT, color: DEN } },
      }],
    }],
  },
  sections: [{
    properties: { page: { size: { width: 11906, height: 16838 }, margin: { top: 1134, bottom: 1134, left: 1701, right: 1134 } } },
    children: noiDung,
  }],
});

const RA = process.argv[2] || 'E:/vuonsen-fnb/doc/NghienCuu_Git_GitHubActions.docx';
Packer.toBuffer(doc).then((buf) => {
  fs.writeFileSync(RA, buf);
  console.log(`da ghi ${RA} (${Math.round(buf.length / 1024)} KB)`);
});
