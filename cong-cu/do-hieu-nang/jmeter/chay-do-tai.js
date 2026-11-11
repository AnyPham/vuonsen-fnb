/*
 * Chạy kịch bản JMeter ở nhiều mức tải rồi gộp kết quả thành bảng.
 *
 * VÌ SAO CÓ TỆP NÀY THAY VÌ MỞ GIAO DIỆN JMETER
 *
 * Giao diện JMeter chỉ dùng để soạn kịch bản, không dùng để đo: chính nó ngốn bộ nhớ và CPU
 * nên làm sai số liệu, tài liệu Apache cũng khuyến cáo như vậy. Đo thì chạy chế độ dòng lệnh.
 * Thêm nữa, câu hỏi cần trả lời không phải "ở một mức tải thì bao nhiêu" mà là "tăng người
 * dùng thì thời gian phản hồi đi theo đường nào", nên phải chạy nhiều mức rồi đặt cạnh nhau.
 *
 * NHỮNG CON SỐ ĐƯỢC TÍNH
 *
 *   Trung bình    dễ hiểu nhưng che mất đuôi chậm, không dùng để kết luận.
 *   p95, p99      95% và 99% số yêu cầu nhanh hơn mức này. Đây mới là con số nói đúng trải
 *                 nghiệm, vì người gặp lượt chậm nhất mới là người bỏ trang.
 *   Thông lượng   số yêu cầu hoàn thành mỗi giây. Chạm trần là dấu hiệu máy đã bão hoà.
 *   Tỉ lệ lỗi     phần trăm yêu cầu không trả về 200.
 *   Kiểm Little   đối chiếu số đo với định luật Little, xem phép đo có tự mâu thuẫn không.
 *
 * ĐỊNH LUẬT LITTLE
 *
 * Little, J. D. C. (1961), "A Proof for the Queuing Formula: L = λW", Operations Research
 * 9(3), trang 383-387.
 *
 *     L = λ × W
 *
 * L là số yêu cầu đang nằm trong hệ thống, λ là thông lượng, W là thời gian một yêu cầu ở
 * trong hệ thống. Định luật đúng với mọi hàng đợi ổn định, không cần giả định gì về phân bố.
 *
 * Áp dụng hai lần, cho hai "hệ thống" khác nhau:
 *
 *   Hệ thống là máy chủ. W là thời gian phản hồi, nên L = λ × W là số yêu cầu trung bình
 *   đang nằm trong máy chủ tại một thời điểm. Con số này trả lời câu "một trăm người cùng
 *   dùng thì máy chủ phải gánh cùng lúc bao nhiêu yêu cầu". Nó tự đúng trên chính số đo nên
 *   không dùng để kiểm, chỉ dùng để hiểu.
 *
 *   Hệ thống là cả vòng khách: gửi yêu cầu, chờ, đọc trang, gửi tiếp. Mỗi vòng dài W + Z, Z là
 *   thời gian nghĩ trung bình, 2 giây với bộ hẹn giờ 1 đến 3 giây. Số khách đang hoạt động là
 *   L, nên L = λ × (W + Z), suy ra thông lượng dự đoán λ = L / (W + Z). Đây mới là phép kiểm:
 *   so thông lượng đo được với thông lượng dự đoán. Khớp nghĩa là máy chủ phục vụ kịp mọi
 *   người, thông lượng chỉ bị giới hạn bởi số người. Đo được thấp hơn hẳn dự đoán nghĩa là có
 *   chỗ nghẽn, ở máy chủ hoặc ở chính máy đo.
 *
 * Số khách đang hoạt động không bằng số luồng N, vì số người tăng dần trong RAMP giây đầu:
 * JMeter cho luồng thứ k (đếm từ 0) bắt đầu ở giây k × RAMP / N. Trung bình theo thời gian trên
 * cả lượt đo dài T giây là N - RAMP × (N - 1) / (2T). Với một người thì đúng bằng 1, vì luồng
 * duy nhất chạy ngay từ đầu.
 *
 * HAI KỊCH BẢN
 *
 *   vuonsen-dat-tiec.jmx   luồng đọc: đăng nhập, tìm sảnh, xem sảnh, tính giá tiệc, xem thực
 *                          đơn, tính tiền gọi món. Mặc định đo 1, 10, 25, 50, 100 người.
 *   vuonsen-ghi-don.jmx    luồng ghi: đặt tiệc thật, tạo yêu cầu thanh toán cọc, xem tình hình
 *                          thanh toán. Mặc định đo 1, 10, 25 người, vì mỗi lượt sinh một đơn
 *                          thật; ở 100 người một phút là cả nghìn đơn.
 *
 * Luồng ghi tạo đơn thật mang tên khách "Do tai ghi don". Bộ chạy hủy hết các đơn này trước
 * khi đo, để buổi còn bị giữ chỗ từ lần đo trước không chặn lần đo này, và hủy lần nữa sau khi
 * đo, để không đọng lại đơn chờ xác nhận trong dữ liệu dùng để demo.
 *
 * CÁCH CHẠY
 *
 *   node cong-cu/do-hieu-nang/jmeter/chay-do-tai.js
 *   node cong-cu/do-hieu-nang/jmeter/chay-do-tai.js 1,10,25
 *   node cong-cu/do-hieu-nang/jmeter/chay-do-tai.js --ghi
 */
const fs = require('fs');
const path = require('path');
const { execFileSync } = require('child_process');

const THU_MUC = __dirname;
const JMETER = process.env.JMETER_HOME
  || 'E:/congcu-tai-ve/giai-nen/apache-jmeter-5.6.3';
const LENH = path.join(JMETER, 'bin', process.platform === 'win32' ? 'jmeter.bat' : 'jmeter');
const GHI = process.argv.includes('--ghi');
const DOC_LAI = process.argv.includes('--doc-lai');
const KICH_BAN = path.join(THU_MUC, GHI ? 'vuonsen-ghi-don.jmx' : 'vuonsen-dat-tiec.jmx');
const THU_MUC_KQ = path.join(THU_MUC, 'ket-qua');
const TIEN_TO = GHI ? 'ghi-' : '';

const CAC_MUC = (process.argv.slice(2).find((a) => /^\d+(,\d+)*$/.test(a))
  || (GHI ? '1,10,25' : '1,10,25,50,100')).split(',').map(Number);
const SO_GIAY = Number(process.env.SO_GIAY) || 60;

/*
 * JMeter mặc định giữ trước 1 GB bộ nhớ. Máy đo cũng là máy chạy máy chủ, MySQL và trình
 * duyệt, chỉ có 8 GB, nên xin 1 GB có lúc không được và JMeter tắt ngay. Một trăm luồng ở chế
 * độ dòng lệnh chỉ cần vài trăm MB; xin ít đi còn bớt tranh tài nguyên với máy chủ đang bị đo.
 *
 * Đặt thẳng vào môi trường của chính tiến trình này để JMeter thừa hưởng. Không truyền một bản
 * sao biến môi trường riêng: trên Windows bản sao đó làm lẫn Path với PATH và JMeter không
 * tìm thấy Java.
 */
if (!process.env.HEAP) process.env.HEAP = '-Xms256m -Xmx512m -XX:MaxMetaspaceSize=256m';

/* Bộ hẹn giờ trong cả hai kịch bản nghỉ ngẫu nhiên đều từ 1 đến 3 giây, trung bình 2 giây. */
const THOI_GIAN_NGHI = 2;

const GOC = process.env.API_URL || 'http://localhost:8080';
const TEN_KHACH_DO_TAI = 'Do tai ghi don';

/* Tăng dần số người trong khoảng bằng một phần tư thời gian đo, tối thiểu 5 giây: đủ chậm
   để không tạo đỉnh giả ở giây đầu, đủ nhanh để phần lớn thời gian đo là ở mức tải đầy. */
const rampCua = (n) => Math.max(5, Math.min(30, Math.ceil(SO_GIAY / 4)));

function phanVi(ds, p) {
  if (!ds.length) return 0;
  const i = Math.min(ds.length - 1, Math.ceil((p / 100) * ds.length) - 1);
  return ds[i];
}

/*
 * Tách CSV đúng chuẩn: ô có dấu phẩy hay xuống dòng được JMeter bọc trong ngoặc kép, và ngoặc
 * kép bên trong ô viết đôi. Không tách kiểu cắt dòng rồi cắt dấu phẩy: thông báo lỗi của một
 * yêu cầu hỏng có cả dấu phẩy lẫn xuống dòng, cắt kiểu đó làm lệch cột và mọi con số thành NaN.
 */
function tachCsv(chu) {
  const dong = [];
  let o = [];
  let gt = '';
  let trongNgoac = false;
  for (let i = 0; i < chu.length; i++) {
    const c = chu[i];
    if (trongNgoac) {
      if (c === '"' && chu[i + 1] === '"') { gt += '"'; i++; } else if (c === '"') trongNgoac = false; else gt += c;
    } else if (c === '"') trongNgoac = true;
    else if (c === ',') { o.push(gt); gt = ''; }
    else if (c === '\n' || c === '\r') {
      if (c === '\r' && chu[i + 1] === '\n') i++;
      o.push(gt); dong.push(o); o = []; gt = '';
    } else gt += c;
  }
  if (gt || o.length) { o.push(gt); dong.push(o); }
  return dong;
}

/*
 * Đọc tệp .jtl do JMeter ghi. Định dạng CSV, mỗi dòng một yêu cầu.
 * Các cột cần dùng: timeStamp, elapsed, label, success.
 */
function docKetQua(tep) {
  const dong = tachCsv(fs.readFileSync(tep, 'utf8').trim());
  const cot = dong[0];
  const iMoc = cot.indexOf('timeStamp');
  const iMs = cot.indexOf('elapsed');
  const iTen = cot.indexOf('label');
  const iDat = cot.indexOf('success');

  const theoBuoc = new Map();
  let dauTien = Infinity;
  let cuoiCung = 0;

  for (let k = 1; k < dong.length; k++) {
    const o = dong[k];
    if (o.length <= iDat) continue;
    const moc = Number(o[iMoc]);
    const ms = Number(o[iMs]);
    const ten = o[iTen];
    const dat = o[iDat] === 'true';
    dauTien = Math.min(dauTien, moc);
    cuoiCung = Math.max(cuoiCung, moc + ms);

    if (!theoBuoc.has(ten)) theoBuoc.set(ten, { ms: [], loi: 0 });
    const b = theoBuoc.get(ten);
    b.ms.push(ms);
    if (!dat) b.loi++;
  }

  const giay = (cuoiCung - dauTien) / 1000;
  const buoc = [];
  let tongYeuCau = 0;
  let tongLoi = 0;
  let tongMs = 0;

  for (const [ten, b] of [...theoBuoc.entries()].sort()) {
    b.ms.sort((x, y) => x - y);
    const n = b.ms.length;
    tongYeuCau += n;
    tongLoi += b.loi;
    tongMs += b.ms.reduce((a, c) => a + c, 0);
    buoc.push({
      ten,
      soYeuCau: n,
      trungBinh: b.ms.reduce((a, c) => a + c, 0) / n,
      p95: phanVi(b.ms, 95),
      p99: phanVi(b.ms, 99),
      chamNhat: b.ms[n - 1],
      loi: b.loi,
    });
  }

  return {
    giay,
    buoc,
    tongYeuCau,
    tongLoi,
    thongLuong: tongYeuCau / giay,
    trungBinhChung: tongMs / tongYeuCau,
    tiLeLoi: (tongLoi / tongYeuCau) * 100,
  };
}

/*
 * Mỗi mức tải một dải ngày riêng cho luồng ghi. Một khách chiếm dải 50 ngày (xem kịch bản),
 * 100 khách là 5.000 ngày, nên cách nhau 6.000 ngày thì các mức không bao giờ đè lên nhau.
 */
const gocNgayCua = (thuTu) => 400 + thuTu * 6000;

function chayMot(soNguoi, thuTu) {
  const jtl = path.join(THU_MUC_KQ, `${TIEN_TO}ketqua-${soNguoi}.jtl`);
  const bc = path.join(THU_MUC_KQ, `${TIEN_TO}bao-cao-${soNguoi}`);
  for (const p of [jtl, bc]) fs.rmSync(p, { recursive: true, force: true });

  console.log(`\n--- ${soNguoi} người, ${SO_GIAY} giây ---`);
  const daRa = execFileSync(LENH, [
    '-n', '-t', KICH_BAN,
    `-JSO_NGUOI=${soNguoi}`,
    `-JRAMP=${rampCua(soNguoi)}`,
    `-JSO_GIAY=${SO_GIAY}`,
    `-JGOC_NGAY=${gocNgayCua(thuTu)}`,
    `-JMAY_CHU=${new URL(GOC).hostname}`,
    `-JCONG=${new URL(GOC).port || 80}`,
    '-l', jtl,
    '-e', '-o', bc,
  ], { cwd: THU_MUC, stdio: ['ignore', 'pipe', 'pipe'] });

  /* jmeter.bat kết thúc bằng lệnh pause nên mã thoát vẫn là 0 dù JMeter lỗi, ví dụ kịch bản
     sai cú pháp. Không có tệp kết quả thì in đầu ra của JMeter ra để biết lỗi gì. */
  if (!fs.existsSync(jtl)) {
    throw new Error('JMeter không ghi ra kết quả. Đầu ra của JMeter:\n'
      + daRa.toString().split('\n').filter((d) => !d.includes('StatusConsoleListener')).slice(-15).join('\n'));
  }
  return docKetQua(jtl);
}

/*
 * Hủy mọi đơn đo tải còn chờ xác nhận, qua đúng API quản trị như nhân viên bấm hủy.
 * Hủy chứ không xóa: đơn hủy không còn chiếm buổi, và vẫn còn dấu vết nếu cần tra lại.
 */
async function huyDonDoTai() {
  /* Thử lại khi lỗi mạng: sau vài phút đo, kết nối cũ mà Node giữ lại để dùng tiếp có thể đã
     bị máy chủ đóng, lần gọi đầu tiên trên kết nối đó hỏng dù máy chủ vẫn chạy bình thường. */
  const goi = async (phuongThuc, duongDan, than, token) => {
    for (let lan = 1; ; lan++) {
      try {
        const r = await fetch(GOC + duongDan, {
          method: phuongThuc,
          headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: 'Bearer ' + token } : {}) },
          body: than ? JSON.stringify(than) : undefined,
        });
        if (!r.ok) throw new Error(`${phuongThuc} ${duongDan} -> HTTP ${r.status}`);
        return r.json();
      } catch (e) {
        if (lan >= 3 || e.message.includes('HTTP')) throw e;
        await new Promise((xong) => setTimeout(xong, 1000));
      }
    }
  };
  const token = (await goi('POST', '/api/auth/login', {
    email: process.env.ADMIN_EMAIL || 'admin@vuonsen.vn',
    password: process.env.ADMIN_PASSWORD || 'Admin@123',
  })).accessToken;

  let daHuy = 0;
  for (;;) {
    const trang = await goi('GET', '/api/v1/admin/bookings?status=PENDING&size=100&keyword='
      + encodeURIComponent(TEN_KHACH_DO_TAI), null, token);
    const ds = (trang.content || []).filter((d) => d.customerName === TEN_KHACH_DO_TAI);
    if (ds.length === 0) return daHuy;
    for (const d of ds) {
      await goi('PATCH', `/api/v1/admin/bookings/${d.id}/status`,
        { status: 'CANCELLED', note: 'Đơn sinh ra khi đo tải' }, token);
      daHuy++;
    }
  }
}

/* ------------------------------------------------------------------ chạy */

if (!fs.existsSync(LENH)) {
  console.error('Không tìm thấy JMeter ở ' + LENH);
  console.error('Đặt biến môi trường JMETER_HOME trỏ tới thư mục đã giải nén.');
  process.exit(1);
}
if (!fs.existsSync(path.join(THU_MUC, 'taikhoan.csv'))) {
  console.error('Chưa có taikhoan.csv. Chạy trước:');
  console.error('  node cong-cu/do-hieu-nang/jmeter/chuan-bi-tai-khoan.js');
  process.exit(1);
}
fs.mkdirSync(THU_MUC_KQ, { recursive: true });

function inKetQua(tatCa) {
  console.log('\n\n' + '='.repeat(78));
  console.log('KẾT QUẢ ĐO TẢI — ' + (GHI ? 'LUỒNG GHI: ĐẶT TIỆC VÀ THANH TOÁN' : 'LUỒNG ĐỌC: TÌM SẢNH, TÍNH GIÁ, GỌI MÓN'));
  console.log('='.repeat(78));

  console.log('\n■ TOÀN LUỒNG');
  console.log('  ' + 'Người'.padEnd(8) + 'Yêu cầu'.padStart(9) + 'Thông lượng'.padStart(13)
    + 'Trung bình'.padStart(12) + 'Tỉ lệ lỗi'.padStart(11));
  for (const r of tatCa) {
    console.log('  ' + String(r.soNguoi).padEnd(8)
      + String(r.tongYeuCau).padStart(9)
      + (r.thongLuong.toFixed(1) + '/s').padStart(13)
      + (Math.round(r.trungBinhChung) + ' ms').padStart(12)
      + (r.tiLeLoi.toFixed(2) + '%').padStart(11));
  }

  console.log('\n■ TỪNG BƯỚC — p95 theo mức tải (mili giây)');
  const tenBuoc = tatCa[0].buoc.map((b) => b.ten);
  console.log('  ' + 'Bước'.padEnd(26) + tatCa.map((r) => (r.soNguoi + ' ng').padStart(10)).join(''));
  for (const ten of tenBuoc) {
    const hang = tatCa.map((r) => {
      const b = r.buoc.find((x) => x.ten === ten);
      return (b ? Math.round(b.p95) : '-').toString().padStart(10);
    }).join('');
    console.log('  ' + (ten.length > 25 ? ten.slice(0, 25) : ten).padEnd(26) + hang);
  }

  console.log('\n■ TỪNG BƯỚC — số lỗi');
  for (const ten of tenBuoc) {
    const hang = tatCa.map((r) => {
      const b = r.buoc.find((x) => x.ten === ten);
      return (b ? b.loi : '-').toString().padStart(10);
    }).join('');
    console.log('  ' + (ten.length > 25 ? ten.slice(0, 25) : ten).padEnd(26) + hang);
  }

  console.log('\n■ ĐỐI CHIẾU ĐỊNH LUẬT LITTLE   L = λ × W');
  console.log('  Little, J.D.C. (1961), Operations Research 9(3), tr. 383-387');
  console.log('  Z = ' + THOI_GIAN_NGHI + ' giây nghĩ trung bình mỗi bước, khách hoạt động trung bình = N - RAMP(N-1)/(2T)');
  console.log('');
  console.log('  ' + 'Người'.padEnd(7) + 'Đang h.động'.padStart(12) + 'λ đo'.padStart(8)
    + 'W (s)'.padStart(8) + 'λ dự đoán'.padStart(11) + 'Lệch'.padStart(8) + 'Yêu cầu ở máy chủ'.padStart(19));
  for (const r of tatCa) {
    const W = r.trungBinhChung / 1000;
    const dangHoatDong = r.soNguoi - (rampCua(r.soNguoi) * (r.soNguoi - 1)) / (2 * SO_GIAY);
    const duDoan = dangHoatDong / (W + THOI_GIAN_NGHI);
    const lech = ((r.thongLuong - duDoan) / duDoan) * 100;
    /* λ × W: số yêu cầu trung bình đang nằm trong máy chủ cùng lúc. Nhỏ hơn số người rất nhiều
       vì phần lớn thời gian khách đang đọc trang, không bắt máy chủ làm gì. */
    const trongMayChu = r.thongLuong * W;
    r.little = { dangHoatDong, duDoan, lech, trongMayChu };
    console.log('  ' + String(r.soNguoi).padEnd(7)
      + dangHoatDong.toFixed(1).padStart(12)
      + r.thongLuong.toFixed(1).padStart(8)
      + W.toFixed(3).padStart(8)
      + duDoan.toFixed(1).padStart(11)
      + ((lech >= 0 ? '+' : '') + lech.toFixed(1) + '%').padStart(8)
      + trongMayChu.toFixed(2).padStart(19));
  }

  /*
   * Bão hoà khi thông lượng đo được tụt hẳn dưới mức định luật Little dự đoán cho số người đó:
   * khách vẫn gửi đều mà máy chủ không phục vụ kịp, yêu cầu phải xếp hàng. Lấy ngưỡng lệch 10%
   * vì dưới mức đó là sai số đo thông thường, nhất là ở mức ít người có ít mẫu.
   */
  console.log('\n■ ĐIỂM BÃO HOÀ');
  const bao = tatCa.find((r) => r.little.lech < -10);
  if (bao) {
    console.log('  Từ ' + bao.soNguoi + ' người, thông lượng đo được thấp hơn dự đoán '
      + Math.abs(bao.little.lech).toFixed(1) + '%: máy chủ không phục vụ kịp, yêu cầu bắt đầu xếp hàng.');
    console.log('  Thêm người quá mức này chỉ làm hàng đợi dài ra, thời gian chờ tăng theo số người.');
  } else {
    const cuoi = tatCa[tatCa.length - 1];
    console.log('  Ở mọi mức đã đo, thông lượng khớp dự đoán trong vòng 10%: chưa bão hoà.');
    console.log('  Ở mức cao nhất, trung bình chỉ ' + cuoi.little.trongMayChu.toFixed(2)
      + ' yêu cầu nằm trong máy chủ cùng lúc. Muốn tìm điểm bão hoà thì đo thêm ở mức cao hơn.');
  }

  const tep = path.join(THU_MUC_KQ, `tong-hop${GHI ? '-ghi' : ''}.json`);
  fs.writeFileSync(tep, JSON.stringify(tatCa, null, 2), 'utf8');
  console.log('\nSố liệu đầy đủ: ' + tep);
  console.log('Báo cáo HTML của JMeter: ' + THU_MUC_KQ + `\\${TIEN_TO}bao-cao-<số người>\\index.html`);
}

/*
 * In kết quả trước rồi mới hủy đơn: hủy đơn lỗi thì vẫn còn kết quả, chạy lại kèm --doc-lai
 * là hủy tiếp được mà không phải đo lại. --doc-lai đọc các tệp .jtl đã có thay vì gọi JMeter.
 */
async function chay() {
  const tatCa = [];
  if (GHI && !DOC_LAI) console.log(`Đã hủy ${await huyDonDoTai()} đơn đo tải còn sót từ lần trước.`);
  CAC_MUC.forEach((n, thuTu) => tatCa.push({
    soNguoi: n,
    ...(DOC_LAI ? docKetQua(path.join(THU_MUC_KQ, `${TIEN_TO}ketqua-${n}.jtl`)) : chayMot(n, thuTu)),
  }));
  inKetQua(tatCa);
  if (GHI) {
    try {
      console.log(`\nĐã hủy ${await huyDonDoTai()} đơn sinh ra khi đo tải.`);
    } catch (e) {
      console.log(`\nChưa hủy được đơn đo tải (${e.message}). Chạy lại kèm --ghi --doc-lai để hủy.`);
    }
  }
}

chay().catch((e) => {
  console.error('\nLỗi: ' + e.message);
  process.exit(1);
});
