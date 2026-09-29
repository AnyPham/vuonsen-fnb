/*
 * Vòng tra cuối: loại những ảnh sai chủ đề rồi tra lại có kiểm tên.
 *
 * Vòng Openverse trước lấy kết quả đầu tiên hợp giấy phép, nên vẫn lọt nhiều ảnh lệch:
 * "Toffee Bananas" cho sườn ram mặn, "Bertie Bott's Every Flavour Beans" cho ếch xào lăn,
 * "Fish-fragrant Eggplant" cho cá hấp. Vòng này thêm một điều kiện: tiêu đề ảnh phải chứa
 * ít nhất một từ khóa bắt buộc do người viết chỉ định cho từng món.
 *
 * Danh sách dưới đây được duyệt bằng mắt từ kết quả vòng trước — đọc tiêu đề từng ảnh rồi
 * đánh dấu ảnh nào sai. Đây là bước không tự động hóa được: máy không biết con tem khác đĩa
 * đồ ăn, nhưng người đọc tiêu đề thì biết ngay.
 *
 * Món nào vẫn không tìm được ảnh đúng thì để trống. Giao diện có sẵn khối giữ chỗ kèm biểu
 * tượng nên trang vẫn gọn, và thực đơn thiếu ảnh còn hơn thực đơn dán ảnh sai món.
 *
 * Chạy: node cong-cu/du-lieu-mau/mon-an/tra-anh-lan-cuoi.js
 */
const fs = require('fs');
const path = require('path');
const https = require('https');

/*
 * Món cần tra lại: [từ khóa tra, các từ bắt buộc phải có trong tiêu đề ảnh].
 * Chỉ cần khớp một trong các từ bắt buộc là đạt.
 */
const TRA_LAI = {
  'hoanh-thanh-chien-gion': ['fried wonton', ['wonton', 'wantan']],
  'dau-hu-sot-ca-chua': ['tofu tomato sauce', ['tofu']],
  'rau-muong-xao-toi': ['stir fried morning glory garlic', ['morning glory', 'water spinach', 'kangkong', 'rau muong']],
  'lau-hai-san-chua-cay': ['seafood hotpot spicy soup', ['hot pot', 'hotpot', 'lau', 'steamboat']],
  'lau-ca-hoi-mang-chua': ['salmon hotpot soup', ['hot pot', 'hotpot', 'salmon', 'lau']],
  'muc-nuong-sa-te': ['grilled squid', ['squid', 'calamari', 'muc']],
  'ca-nuc-nuong-giay-bac': ['grilled mackerel fish', ['mackerel', 'grilled fish']],
  'nam-nuong-sa-te': ['grilled mushrooms skewer', ['mushroom']],
  'tra-dao-cam-sa': ['peach iced tea', ['tea', 'tra']],
  'nuoc-chanh-day': ['passion fruit juice', ['passion']],
  'tra-atiso-da-lat': ['herbal tea cup hot', ['tea', 'tisane', 'tra']],
  'suon-ram-man': ['braised pork ribs vietnamese', ['rib', 'suon', 'pork']],
  'muc-chien-nuoc-mam': ['salt and pepper squid', ['squid', 'calamari', 'muc']],
  'tom-su-hap-nuoc-dua': ['steamed prawns plate', ['prawn', 'shrimp', 'tom']],
  'ca-hap-hong-kong': ['steamed whole fish cantonese', ['steamed fish', 'whole fish', 'ca hap']],
  'ech-xao-lan': ['frog legs cooked', ['frog', 'ech']],
  'ga-hap-la-sen': ['steamed chicken lotus leaf', ['chicken', 'ga ']],
  'bo-nuong-la-cach': ['grilled beef wrapped leaf vietnamese', ['beef', 'bo ', 'la lot']],
  'lau-bo-nhung-dam': ['beef hotpot vietnamese', ['hot pot', 'hotpot', 'lau', 'beef']],
  'tom-hum-nuong-pho-mai': ['grilled lobster', ['lobster']],
  'tra-sen-vang': ['lotus tea cup', ['tea', 'lotus', 'tra']],
  'banh-chuoi-hap-nuoc-cot-dua': ['steamed banana cake', ['banana']],
  'bo-luc-lac-khoai-tay': ['shaking beef vietnamese', ['shaking beef', 'bo luc lac', 'beef cube']],
  'dau-hu-chien-sa-ot': ['fried tofu lemongrass', ['tofu']],
  'nom-hoa-chuoi': ['banana blossom salad', ['banana blossom', 'banana flower']],
};

const GIAY_PHEP_NHAN = new Set(['cc0', 'pdm', 'by', 'by-sa']);
const UA = 'VuonSenThesis/1.0 (tieu luan tot nghiep; lien he qua truong)';
const NGHI_MS = 900;
const nghi = (ms) => new Promise((ok) => setTimeout(ok, ms));

const lay = (url) =>
  new Promise((ok, loi) => {
    https
      .get(url, { headers: { 'User-Agent': UA } }, (r) => {
        let d = '';
        r.on('data', (c) => (d += c));
        r.on('end', () => {
          if (r.statusCode !== 200) return loi(new Error('ma ' + r.statusCode));
          try {
            ok(JSON.parse(d));
          } catch (e) {
            loi(new Error('JSON hong'));
          }
        });
      })
      .on('error', loi);
  });

const songKhong = (url) =>
  new Promise((ok) => {
    const req = https.request(url, { method: 'HEAD', headers: { 'User-Agent': UA } }, (r) =>
      ok(r.statusCode >= 200 && r.statusCode < 300),
    );
    req.on('error', () => ok(false));
    req.setTimeout(20000, () => {
      req.destroy();
      ok(false);
    });
    req.end();
  });

const khongDau = (s) =>
  (s || '').normalize('NFD').replace(/[̀-ͯ]/g, '').replace(/đ/g, 'd').toLowerCase();

(async () => {
  const tepRa = path.join(__dirname, 'anh-mon.json');
  const kq = JSON.parse(fs.readFileSync(tepRa, 'utf8'));

  // Bỏ hẳn những ảnh đã duyệt là sai, để nếu vòng này không tìm được thì ô ảnh bỏ trống
  let boDi = 0;
  for (const slug of Object.keys(TRA_LAI)) {
    if (kq[slug]) {
      delete kq[slug];
      boDi++;
    }
  }
  console.log(`Da bo ${boDi} anh sai chu de, tra lai ${Object.keys(TRA_LAI).length} mon\n`);

  const daDung = new Set(Object.values(kq).map((x) => x.url));
  let them = 0;

  for (const [slug, [tuKhoa, batBuoc]] of Object.entries(TRA_LAI)) {
    let chon = null;
    for (let lan = 1; lan <= 3 && !chon; lan++) {
      try {
        const j = await lay(
          'https://api.openverse.org/v1/images/?q=' + encodeURIComponent(tuKhoa) +
          '&license=cc0,pdm,by,by-sa&page_size=20',
        );
        for (const r of j.results || []) {
          if (!r.url || !GIAY_PHEP_NHAN.has(r.license)) continue;
          if (daDung.has(r.url)) continue;

          const tieuDe = khongDau(r.title || '');
          if (!batBuoc.some((t) => tieuDe.includes(khongDau(t)))) continue;
          if (!(await songKhong(r.url))) continue;

          chon = {
            tep: r.title || '(khong ten)',
            url: r.url,
            trangGoc: r.foreign_landing_url || r.detail_url || '',
            giayPhep: (r.license || '').toUpperCase() + ' ' + (r.license_version || ''),
            tacGia: (r.creator || 'khong ro').slice(0, 80),
            nguon: r.source || 'openverse',
          };
          break;
        }
        break;
      } catch (e) {
        await nghi(NGHI_MS * lan * 2);
      }
    }

    if (chon) {
      daDung.add(chon.url);
      kq[slug] = chon;
      them++;
    }
    console.log(`${slug.padEnd(30)} ${chon ? chon.tep.slice(0, 48) : '(de trong)'}`);
    fs.writeFileSync(tepRa, JSON.stringify(kq, null, 2), 'utf8');
    await nghi(NGHI_MS);
  }

  console.log(`\nTim lai duoc ${them}/${Object.keys(TRA_LAI).length}. Tong cong ${Object.keys(kq).length}/100 mon co anh.`);
})();
