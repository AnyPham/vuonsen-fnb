/*
 * Đọc báo cáo XML của Surefire rồi in ra một bảng Markdown tổng hợp.
 *
 * Dùng trong workflow GitHub Actions: kết quả được ghi thẳng vào trang tóm tắt của lần
 * chạy, nên mở lần chạy ra là thấy ngay bao nhiêu ca đạt, bao nhiêu ca trượt và mỗi nhóm
 * kịch bản mất bao lâu, không phải tải tệp báo cáo về rồi mở từng tệp một. Bảng này cũng
 * chính là số liệu cần cho mục thống kê kết quả kiểm thử của báo cáo.
 *
 * Chạy tay để xem lại lần chạy gần nhất trên máy:
 *   node cong-cu/kiem-tra/tom-tat-ket-qua.js kiemthu/target/surefire-reports
 *
 * Không dùng thư viện phân tích XML nào. Thứ duy nhất cần lấy là mấy thuộc tính nằm trên
 * thẻ <testsuite> ngoài cùng, mà Surefire luôn ghi thẻ đó ở ngay đầu tệp.
 */
const fs = require('fs');
const path = require('path');

const THU_MUC_MAC_DINH = path.join('kiemthu', 'target', 'surefire-reports');

/* Gỡ các thực thể XML trong tên nhóm kịch bản, vì @DisplayName hay có dấu & và dấu nháy. */
function goThucThe(chuoi) {
  return chuoi
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/&quot;/g, '"')
    .replace(/&apos;/g, "'")
    .replace(/&#(\d+);/g, (_, ma) => String.fromCharCode(Number(ma)))
    // Thay &amp; sau cùng, nếu không "&amp;lt;" sẽ bị gỡ hai lần thành "<"
    .replace(/&amp;/g, '&');
}

/* Ký tự sổ dọc trong tên sẽ cắt đôi ô của bảng Markdown nên phải rào lại. */
function raoOBang(chuoi) {
  return chuoi.replace(/\|/g, '\\|');
}

/*
 * Xếp bảng theo số luồng nghiệp vụ, không xếp theo tên tệp.
 *
 * Xếp theo tên tệp thì "Luồng 10" đứng trước "Luồng 2" vì so sánh từng ký tự một, đọc
 * bảng rất khó dò. Nhóm nào không mang số luồng, tức mấy lớp công cụ, đẩy xuống cuối.
 */
function thuTu(ten) {
  const khop = ten.match(/Luồng\s+(\d+)/i);
  return khop ? Number(khop[1]) : Number.MAX_SAFE_INTEGER;
}

function docMotTep(duongDan) {
  const noiDung = fs.readFileSync(duongDan, 'utf8');
  const the = noiDung.match(/<testsuite\s[^>]*>/);
  if (!the) {
    return null;
  }
  const doc = (ten) => {
    const khop = the[0].match(new RegExp(`\\b${ten}="([^"]*)"`));
    return khop ? khop[1] : '';
  };
  const so = (ten) => Number(doc(ten)) || 0;

  const tong = so('tests');
  const truot = so('failures');
  const loi = so('errors');
  const bo = so('skipped');
  return {
    ten: goThucThe(doc('name')) || path.basename(duongDan),
    tong,
    dat: tong - truot - loi - bo,
    hong: truot + loi,
    bo,
    giay: so('time'),
  };
}

function main() {
  const thuMuc = process.argv[2] || THU_MUC_MAC_DINH;

  let tep = [];
  if (fs.existsSync(thuMuc)) {
    tep = fs
      .readdirSync(thuMuc)
      .filter((t) => t.startsWith('TEST-') && t.endsWith('.xml'))
      .sort()
      .map((t) => path.join(thuMuc, t));
  }

  if (tep.length === 0) {
    console.log('### Kết quả kiểm thử giao diện\n');
    console.log(`Không tìm thấy báo cáo nào trong \`${thuMuc}\`. Bộ kiểm thử chưa chạy được.`);
    return;
  }

  const nhom = tep
    .map(docMotTep)
    .filter(Boolean)
    .sort((a, b) => thuTu(a.ten) - thuTu(b.ten) || a.ten.localeCompare(b.ten, 'vi'));
  const cong = (khoa) => nhom.reduce((tong, n) => tong + n[khoa], 0);
  const tong = cong('tong');
  const dat = cong('dat');
  const hong = cong('hong');
  const bo = cong('bo');
  const giay = cong('giay');

  const phuChu = [hong > 0 ? `${hong} trượt` : null, bo > 0 ? `${bo} bỏ qua` : null]
    .filter(Boolean)
    .join(' — ');

  console.log('### Kết quả kiểm thử giao diện\n');
  console.log(
    `**${dat}/${tong} đạt**` +
      (phuChu ? ` — ${phuChu}` : '') +
      ` — tổng thời gian ${giay.toFixed(1)} giây\n`,
  );
  console.log('| Nhóm kịch bản | Số ca | Đạt | Trượt | Bỏ qua | Thời gian (giây) |');
  console.log('| --- | ---: | ---: | ---: | ---: | ---: |');
  for (const n of nhom) {
    console.log(
      `| ${raoOBang(n.ten)} | ${n.tong} | ${n.dat} | ${n.hong} | ${n.bo} | ${n.giay.toFixed(1)} |`,
    );
  }
  console.log(
    `| **Tổng** | **${tong}** | **${dat}** | **${hong}** | **${bo}** | **${giay.toFixed(1)}** |`,
  );
}

main();
