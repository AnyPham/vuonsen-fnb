/*
 * So sánh Vườn Sen với các website cùng lĩnh vực.
 *
 * VÌ SAO PHẢI TỰ CHẤM, KHÔNG CÓ CÔNG CỤ TỰ ĐỘNG
 *
 * Không có phần mềm nào đo được "hai website giống nhau bao nhiêu" theo nghĩa tính năng.
 * Công cụ so mã nguồn chỉ cho biết hai trang viết code giống nhau tới đâu, mà điều đó
 * không nói lên gì về việc khách đặt tiệc được hay không. Nên phải khảo sát bằng tay,
 * theo tiêu chí rõ ràng.
 *
 * CÁCH DÙNG
 *
 *   1. Mở từng website, làm đúng chuỗi việc ghi ở CHUOI_VIEC bên dưới.
 *   2. Chấm từng tiêu chí theo MO_TA_MUC_DIEM, sửa số trong bảng DIEM.
 *   3. Chạy: node "E:\vuonsen-fnb\cong-cu\kiem-tra\do-tuong-dong.js"
 *   4. Chụp màn hình làm bằng chứng cho mỗi điểm số, lưu vào doc/anh-khao-sat/
 *
 * THÊM MỘT WEBSITE MỚI (khi giáo viên yêu cầu so với trang khác)
 *
 * Thêm một dòng vào bảng DIEM, tên trang và mười số theo đúng thứ tự TIEU_CHI, rồi
 * chạy lại. Không phải sửa gì khác, mọi bảng tự tính lại theo số trang có trong DIEM.
 *
 *   'Tên trang mới':  [  3,   2,   4,    1,    3,    5,   2,   4,   3,   1 ],
 *
 * Điểm của các trang cũ không đổi khi thêm trang mới — xem phần CÔNG THỨC để biết
 * vì sao điều đó quan trọng.
 *
 * ============================================================================
 * CÔNG THỨC TOÁN HỌC
 * ============================================================================
 *
 * [1] CÔNG THỨC NÀY DÙNG ĐỂ LÀM GÌ
 *
 * Bài toán đặt ra: có hai website, mỗi trang được chấm điểm trên cùng mười tiêu chí.
 * Cần quy toàn bộ mười cặp điểm đó về một con số duy nhất, cho biết hai trang gần nhau
 * tới đâu về mặt chức năng.
 *
 * Không cộng tổng điểm rồi trừ nhau được, vì hai trang cùng tổng 40 điểm vẫn có thể
 * mạnh yếu ở những tiêu chí hoàn toàn khác nhau. Phải so từng tiêu chí một rồi mới gộp.
 * Đó đúng là việc mà hệ số Gower sinh ra để làm.
 *
 * Trong tiểu luận, con số này trả lời câu: "thị trường đã có sẵn trang nào làm gần giống
 * những gì đề tài định làm chưa, và gần tới mức nào".
 *
 * [2] NGUỒN
 *
 * Gower, J. C. (1971). "A General Coefficient of Similarity and Some of Its Properties".
 * Biometrics, tập 27, số 4 (tháng 12/1971), trang 857-871.
 * DOI: 10.2307/2528823   —   JSTOR: https://www.jstor.org/stable/2528823
 *
 * Bài báo này là nguồn gốc của hệ số Gower, được trích dẫn rộng rãi trong thống kê và
 * phân tích cụm. Công thức dưới đây lấy từ phần bài báo bàn về biến định lượng
 * (quantitative characters). Đây là công thức có nguồn, không phải tự nghĩ ra, nên khi
 * hội đồng hỏi "căn cứ nào tính ra con số này" thì chỉ thẳng vào trang 857-871.
 *
 * Chép vào mục Tài liệu tham khảo của tiểu luận theo đúng dòng in nghiêng ở trên.
 *
 * [3] CÔNG THỨC
 *
 * Dạng tổng quát như Gower trình bày:
 *
 *                       1    n   | aᵢ − bᵢ |
 *       S(a, b)  =  1 − ─   ∑   ───────────
 *                       n   i=1       Rᵢ
 *
 *   S(a, b)  độ tương đồng giữa hai trang a và b, ra số từ 0 đến 1 (nhân 100 thành %)
 *   n        số tiêu chí, ở đây n = 10
 *   aᵢ, bᵢ   điểm của trang a và trang b tại tiêu chí thứ i
 *   Rᵢ       khoảng biến thiên của tiêu chí thứ i
 *
 * Tổng ∑ |aᵢ − bᵢ| ở tử số chính là khoảng cách Manhattan giữa hai trang, còn gọi là
 * khoảng cách L1 hoặc khoảng cách taxi: cộng dồn trị tuyệt đối của chênh lệch từng chiều.
 *
 * Tệp này lấy Rᵢ = 4 cho cả mười tiêu chí, vì thang chấm từ 1 đến 5 nên chênh lệch lớn
 * nhất có thể là 5 − 1 = 4. Khi mọi Rᵢ bằng nhau, công thức rút gọn lại còn:
 *
 *                   ∑ | aᵢ − bᵢ |              tổng chênh lệch
 *       S  =  1 − ───────────────   =   1 − ────────────────────
 *                      n × 4                       10 × 4
 *
 * [4] CÁCH TÍNH TAY
 *
 * Ba bước, máy tính bỏ túi làm được, không cần chạy script.
 *
 *   Bước 1. Viết hai dòng điểm chồng lên nhau, trừ từng cột, lấy trị tuyệt đối.
 *   Bước 2. Cộng dòng chênh lệch lại.
 *   Bước 3. Lấy 1 trừ đi (tổng chênh chia cho 40), rồi nhân 100.
 *
 * Ví dụ với Pizza 4P's:
 *
 *                 Tìm  Giá  Đặt  Trống Ưuđãi Đánh TàiK  Trả  SEO  HỗTrợ
 *   Vườn Sen        4    5    5     4     4     4    5    2    3     4
 *   Pizza 4P's      3    4    5     5     3     2    3    4    5     2
 *   Chênh lệch      1    1    0     1     1     2    2    2    2     2
 *
 *   Tổng chênh lệch = 1+1+0+1+1+2+2+2+2+2 = 14
 *   S = 1 − 14/40 = 1 − 0,35 = 0,65 → 65%
 *
 * [5] VÌ SAO LẤY Rᵢ = 4 MÀ KHÔNG LẤY KHOẢNG QUAN SÁT
 *
 * Gower trong bài gốc lấy Rᵢ là khoảng biến thiên quan sát được ở tiêu chí đó, tức điểm
 * cao nhất trừ điểm thấp nhất trong chính tập dữ liệu. Nếu làm vậy ở đây thì kết quả
 * phân biệt tốt hơn hẳn: PasGo 63,3% thay vì 72,5%, Pizza 4P's 49,2% thay vì 65%, và
 * mốc ngẫu nhiên hạ từ 61% xuống 48,7%.
 *
 * Nhưng tệp này vẫn chọn Rᵢ = 4, vì khoảng quan sát phụ thuộc vào chính tập trang đang
 * có. Khảo sát thêm một website thứ tư là Rᵢ đổi, kéo theo điểm của cả ba trang cũ đổi
 * theo. Số liệu đã in vào tiểu luận mà tự đổi khi thêm một trang khảo sát thì không bảo
 * vệ được trước hội đồng.
 *
 * Lấy Rᵢ = 4 thì mỗi cặp trang tính độc lập với nhau, thêm bao nhiêu trang cũng không
 * làm xê dịch số cũ. Đánh đổi độ nhạy để lấy tính ổn định.
 *
 * Nếu cô hỏi vì sao không dùng khoảng quan sát theo đúng bài gốc, trả lời đúng đoạn này.
 *
 * [6] PHẦN KHÔNG DÙNG CÔNG THỨC NÀO
 *
 * Bảng phân tích khoảng trống chỉ là phép đếm, không có công thức: đếm xem bao nhiêu
 * tiêu chí hai trang chênh nhau không quá NGUONG_CUNG_MUC điểm, bao nhiêu tiêu chí mình
 * cao hơn, bao nhiêu tiêu chí mình thấp hơn.
 *
 * Đếm thì không có giả định ngầm nào để bắt bẻ, nên đây mới là phần đáng tin nhất của
 * cả tệp, và là phần nên đưa vào tiểu luận làm kết luận.
 * ============================================================================
 *
 * VÌ SAO KHÔNG LẤY PHẦN TRĂM TƯƠNG ĐỒNG LÀM KẾT QUẢ CHÍNH
 *
 * Bản đầu của tệp này báo một con số duy nhất: độ tương đồng, tính bằng
 * 1 − (tổng chênh lệch ÷ chênh lệch tối đa). Phép tính đúng, nhưng đọc ra thì sai, vì
 * mẫu số là mức chênh tối đa về lý thuyết — đòi một trang toàn điểm 1 và trang kia toàn
 * điểm 5. Thực tế không trang nào như vậy, nên mọi kết quả đều bị đẩy lên cao.
 *
 * Quét hết 9.765.625 tổ hợp điểm có thể có cho thấy ba chỗ hỏng:
 *   - Một trang rỗng, không có chức năng nào, vẫn được 25%. Thang không bắt đầu từ 0.
 *   - Chấm ngẫu nhiên trung bình đã 61%. Nên kết quả 65% không chứng minh được gì.
 *   - Một trang hơn Vườn Sen ở cả mười tiêu chí được 75%, cao hơn cả Pizza 4P's.
 *     Vì |a−b| vứt mất dấu, phép đo không phân biệt nổi "giống nhau" với "hơn hẳn".
 *
 * Nên tệp này báo phân tích khoảng trống làm chính, còn phần trăm tương đồng chỉ để
 * tham khảo và luôn in kèm mốc ngẫu nhiên 61% để không ai đọc nhầm.
 *
 * Phân tích khoảng trống mới là thứ tiểu luận cần: không phải chứng minh web giống ai,
 * mà chỉ ra thị trường thiếu gì và đề tài lấp chỗ nào.
 */

/* Chuỗi việc phải làm giống hệt nhau trên mọi website, để điểm số so sánh được với nhau */
const CHUOI_VIEC = [
  'Tìm không gian hoặc bàn phù hợp với số khách của mình',
  'Xem cho ra giá, không phải để lại số điện thoại',
  'Đặt một đơn tới bước cuối cùng',
  'Tra lại đơn vừa đặt',
];

/*
 * Mô tả từng mức điểm.
 *
 * Thiếu phần này thì điểm số chỉ là cảm tính, người khác chấm lại ra kết quả khác, và
 * không bảo vệ được trước hội đồng. Tiểu luận hiện mới ghi "thang 5 điểm" mà chưa nói
 * 1 với 5 nghĩa là gì, nên ba dòng dưới đây cần được chép vào đoạn phương pháp.
 *
 * Chỉ mô tả ba mức 1, 3, 5. Mức 2 và 4 dành cho trường hợp nằm giữa, người chấm tự cân.
 * Mô tả đủ năm mức nghe thì chặt hơn nhưng thực tế không ai phân biệt nổi.
 */
const MO_TA_MUC_DIEM = {
  1: 'Không có chức năng này, hoặc có mà không dùng được',
  3: 'Có nhưng hạn chế: thiếu bước, phải làm thủ công, hoặc khó tìm',
  5: 'Làm trọn vẹn ngay trên website, không cần gọi điện hay chờ phản hồi',
};

const TIEU_CHI = [
  'Tìm kiếm và lọc',
  'Minh bạch giá',
  'Đặt trực tuyến trọn vẹn',
  'Xem tình trạng còn trống',
  'Ưu đãi',
  'Đánh giá của khách',
  'Tài khoản và tra cứu đơn',
  'Thanh toán trực tuyến',
  'Nội dung và SEO',
  'Hỗ trợ tức thì',
];

/*
 * Điểm khảo sát.
 *
 * Ba website đầu lấy từ Bảng 1 của tiểu luận, khảo sát ngày 27/09/2026.
 * Hai website cuối để trống (null) — bạn tự khảo sát rồi điền vào.
 *
 * Cột Vườn Sen là điểm tại thời điểm 27/09. Hai tiêu chí đã đổi kể từ đó:
 *   - Thanh toán trực tuyến: lúc đó mới ghi nhận cọc thủ công nên chấm 2.
 *     Nay đã nối cổng VNPay sandbox, nên cần chấm lại.
 *   - Ưu đãi: lúc đó có dữ liệu giảm giá nhưng khách không xem được nên chấm 4.
 *     Nay dải khuyến mãi đã hiện ngoài trang chủ.
 * Chấm lại hai ô đó thì nhớ sửa luôn ngày khảo sát ghi trong tiểu luận.
 */
const DIEM = {
  //                          Tìm  Giá  Đặt  Trống Ưuđãi Đánh TàiK  Trả  SEO  HỗTrợ
  'Vườn Sen':               [  4,   5,   5,    4,    4,    4,   5,   2,   3,   4 ],
  "Pizza 4P's":             [  3,   4,   5,    5,    3,    2,   3,   4,   5,   2 ],
  'Riverside Palace':       [  1,   1,   1,    1,    4,    2,   1,   1,   4,   3 ],
  'PasGo':                  [  5,   3,   4,    3,    5,    5,   4,   3,   5,   4 ],

  // Hai trang bổ sung để đủ năm. Thay tên và điền điểm sau khi khảo sát.
  'Website thứ tư':         [ null, null, null, null, null, null, null, null, null, null ],
  'Website thứ năm':        [ null, null, null, null, null, null, null, null, null, null ],
};

const GOC = 'Vườn Sen';
const DIEM_TOI_DA = 5;
const CHENH_TOI_DA = DIEM_TOI_DA - 1;

/* Mức một website chấm ngẫu nhiên đạt được, tính bằng cách quét hết tổ hợp điểm.
 * In kèm để con số tương đồng không bị đọc thành "giống nhau tới mức đó". */
const MOC_NGAU_NHIEN = 61.0;

/* Chênh từ 1 điểm trở xuống coi như hai trang cùng một mức, vì thang này chỉ có ba mốc
 * thật là 1, 3, 5; lệch 1 điểm nằm trong khoảng hai người chấm có thể khác nhau. */
const NGUONG_CUNG_MUC = 1;

/* Từ 4 điểm trở lên coi là làm tốt tiêu chí đó. */
const NGUONG_MANH = 4;

/* ---------------------------------------------------------------- tính toán */

const goc = DIEM[GOC];
const daCham = (d) => d.every((x) => typeof x === 'number');
const tong = (v) => v.reduce((a, b) => a + b, 0);
const chenhTong = (d) => d.reduce((t, x, i) => t + Math.abs(x - goc[i]), 0);

const cacTrang = Object.keys(DIEM).filter((t) => t !== GOC);
const daXong = cacTrang.filter((t) => daCham(DIEM[t]));
const chuaXong = cacTrang.filter((t) => !daCham(DIEM[t]));

console.log('SO SÁNH ' + GOC.toUpperCase() + ' VỚI CÁC WEBSITE CÙNG LĨNH VỰC');
console.log('='.repeat(72));

console.log('\nChuỗi việc làm giống hệt nhau trên mọi website:');
CHUOI_VIEC.forEach((v, i) => console.log('  ' + (i + 1) + '. ' + v));

console.log('\nThang điểm:');
Object.entries(MO_TA_MUC_DIEM).forEach(([m, t]) => console.log('  ' + m + ' — ' + t));

if (chuaXong.length) {
  console.log('\nChưa khảo sát: ' + chuaXong.join(', '));
}

/* --- Kết quả chính: khoảng trống --- */

console.log('\n\n■ PHÂN TÍCH KHOẢNG TRỐNG  (phần dùng để viết tiểu luận)');
console.log('-'.repeat(72));
console.log('  ' + 'Trang'.padEnd(20) + 'Cùng mức  Mình hơn  Mình kém   Tổng điểm');
for (const ten of daXong) {
  const d = DIEM[ten];
  const cung = d.filter((v, i) => Math.abs(v - goc[i]) <= NGUONG_CUNG_MUC).length;
  const hon = d.filter((v, i) => goc[i] > v).length;
  const kem = d.filter((v, i) => goc[i] < v).length;
  console.log('  ' + ten.padEnd(20)
    + (cung + '/' + TIEU_CHI.length).padEnd(10)
    + String(hon).padEnd(10) + String(kem).padEnd(11)
    + tong(d) + '/' + TIEU_CHI.length * DIEM_TOI_DA);
}
console.log('  ' + GOC.padEnd(20) + '—'.padEnd(10) + '—'.padEnd(10) + '—'.padEnd(11)
  + tong(goc) + '/' + TIEU_CHI.length * DIEM_TOI_DA);

/* Tiêu chí mọi đối thủ đều làm tốt mà mình thì không: điểm yếu thật, phải nêu trong
 * phần hạn chế trước khi hội đồng hỏi. */
const yeuToanDien = TIEU_CHI.filter((_, i) =>
  daXong.length && daXong.every((t) => DIEM[t][i] >= NGUONG_MANH) && goc[i] < NGUONG_MANH);
if (yeuToanDien.length) {
  console.log('\n  Mọi đối thủ đều hơn mình ở:');
  yeuToanDien.forEach((t) => {
    const i = TIEU_CHI.indexOf(t);
    console.log('    - ' + t + '  (mình ' + goc[i] + ' điểm)');
  });
  console.log('    → Đây là hạn chế thật. Nêu trước trong tiểu luận, đừng chờ bị hỏi.');
}

/* Tiêu chí không đối thủ nào làm được mà mình làm được: chính là đóng góp của đề tài. */
const khoangTrong = TIEU_CHI.filter((_, i) =>
  daXong.length && daXong.every((t) => DIEM[t][i] < NGUONG_MANH) && goc[i] >= NGUONG_MANH);
if (khoangTrong.length) {
  console.log('\n  Không đối thủ nào làm được, mình làm được:');
  khoangTrong.forEach((t) => {
    const i = TIEU_CHI.indexOf(t);
    console.log('    - ' + t + '  (mình ' + goc[i] + ' điểm)');
  });
  console.log('    → Đây là đóng góp của đề tài. Nhấn mạnh phần này khi bảo vệ.');
}

/* --- Bảng chi tiết --- */

console.log('\n\n■ CHÊNH LỆCH TỪNG TIÊU CHÍ');
console.log('-'.repeat(72));
const tenNgan = (t) => (t.length > 10 ? t.slice(0, 10) : t);
console.log('  ' + ('Tiêu chí [' + GOC + ']').padEnd(28)
  + daXong.map((t) => tenNgan(t).padStart(12)).join(''));
TIEU_CHI.forEach((tc, i) => {
  const hang = daXong.map((t) => {
    const d = DIEM[t][i] - goc[i];
    return (DIEM[t][i] + ' (' + (d > 0 ? '+' : '') + d + ')').padStart(12);
  }).join('');
  console.log('  ' + (tc + ' [' + goc[i] + ']').padEnd(28) + hang);
});
console.log('\n  Số trong ngoặc là chênh so với ' + GOC + '. Dấu cộng là họ hơn.');

/* --- Phụ: phần trăm tương đồng --- */

console.log('\n\n■ ĐỘ TƯƠNG ĐỒNG  (tham khảo, đừng dùng làm kết luận)');
console.log('-'.repeat(72));
console.log('  Hệ số tương đồng Gower cho biến định lượng, với R = '
  + CHENH_TOI_DA + ' cho mọi tiêu chí:');
console.log('');
console.log('      S = 1 − ( tổng |điểm họ − điểm mình| ) ÷ ( '
  + TIEU_CHI.length + ' × ' + CHENH_TOI_DA + ' )');
console.log('');
console.log('  Nguồn: Gower, J. C. (1971). "A General Coefficient of Similarity');
console.log('         and Some of Its Properties". Biometrics, 27(4), tr. 857-871.');
console.log('         DOI 10.2307/2528823');
console.log('');
for (const ten of daXong) {
  const chenh = chenhTong(DIEM[ten]);
  const pt = (1 - chenh / (TIEU_CHI.length * CHENH_TOI_DA)) * 100;
  const ghiChu = pt < MOC_NGAU_NHIEN ? 'dưới mức ngẫu nhiên' : '';
  console.log('  ' + ten.padEnd(20) + (pt.toFixed(1) + '%').padEnd(9)
    + '(chênh ' + chenh + '/' + TIEU_CHI.length * CHENH_TOI_DA + ')  ' + ghiChu);
}
console.log('\n  Mốc so sánh: một website chấm ngẫu nhiên trung bình đạt '
  + MOC_NGAU_NHIEN.toFixed(1) + '%,');
console.log('  một trang rỗng hoàn toàn vẫn đạt 25%. Nên con số này không bắt đầu từ 0');
console.log('  và không phân biệt được "giống nhau" với "hơn hẳn". Đọc bảng khoảng trống.');
