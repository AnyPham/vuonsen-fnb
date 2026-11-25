/*
 * Chạy bộ kiểm thử Postman mà không cần cài newman.
 *
 * Đọc collection v2.1, thay biến {{...}}, gửi request bằng fetch của Node, chạy các đoạn
 * pre-request và test với một đối tượng pm rút gọn (đủ cho các phép kiểm tra trong bộ này).
 * Kết quả ghi ra ket-qua/ket-qua-api.json để đưa vào báo cáo.
 *
 * Chạy: node chay-postman.js [collection] [environment] [tệp kết quả trong ket-qua/]
 */
const fs = require('fs');
const path = require('path');
const vm = require('vm');

const thuMuc = __dirname;
const col = JSON.parse(fs.readFileSync(process.argv[2] || path.join(thuMuc, 'VuonSen_KiemThuAPI.postman_collection.json'), 'utf8'));
const env = JSON.parse(fs.readFileSync(process.argv[3] || path.join(thuMuc, 'VuonSen_CucBo.postman_environment.json'), 'utf8'));

const bien = {};
env.values.forEach((v) => { bien[v.key] = v.value; });
(col.variable || []).forEach((v) => { if (!(v.key in bien)) bien[v.key] = v.value; });

const thay = (s) => (typeof s === 'string' ? s.replace(/\{\{(\w+)\}\}/g, (_, k) => (k in bien ? String(bien[k]) : '')) : s);

// expect rút gọn theo cú pháp chai mà các đoạn test dùng
function expect(gt) {
  const loi = (m) => { throw new Error(m); };
  const c = {
    include: (x) => (String(gt).includes(x) ? c : loi(`mong đợi chứa "${x}", thực tế: ${String(gt).slice(0, 120)}`)),
    eql: (x) => (JSON.stringify(gt) === JSON.stringify(x) ? c : loi(`mong đợi ${JSON.stringify(x)}, thực tế ${JSON.stringify(gt)}`)),
    oneOf: (ds) => (ds.includes(gt) ? c : loi(`mong đợi một trong ${ds}, thực tế ${gt}`)),
    above: (x) => (gt > x ? c : loi(`mong đợi lớn hơn ${x}, thực tế ${gt}`)),
    an: (kieu) => (typeof gt === kieu && gt !== null ? c : loi(`mong đợi kiểu ${kieu}`)),
    get true() { return gt === true ? c : loi(`mong đợi true, thực tế ${gt}`); },
    get false() { return gt === false ? c : loi(`mong đợi false, thực tế ${gt}`); },
  };
  c.to = c; c.be = c; c.have = c;
  return c;
}

async function chay() {
  const ketQua = [];
  const nhom = col.item;
  for (const n of nhom) {
    for (const it of n.item) {
      const kiemTra = [];
      const pm = {
        collectionVariables: { set: (k, v) => { bien[k] = v; }, get: (k) => bien[k] },
        environment: { get: (k) => bien[k] },
        expect,
        test: (ten, fn) => {
          try { fn(); kiemTra.push({ ten, dat: true }); } catch (e) { kiemTra.push({ ten, dat: false, loi: e.message }); }
        },
      };
      const chayScript = (loai) => {
        const ev = (it.event || []).find((e) => e.listen === loai);
        if (ev) vm.runInNewContext(ev.script.exec.join('\n'), { pm, String, Number, Date, JSON });
      };
      chayScript('prerequest');
      const r = it.request;
      const url = thay(r.url.raw).replace(/ /g, '%20');
      const headers = {};
      (r.header || []).forEach((h) => { headers[h.key] = thay(h.value); });
      let body;
      if (r.body && r.body.raw) {
        body = thay(r.body.raw).replace(/"(\d+)"(?=\s*[,}])/g, (m, so, off, s) => {
          // biến số như dishId được thay vào trong ngoặc kép: đổi lại thành số
          const truoc = s.slice(Math.max(0, off - 20), off);
          return /"(dishId|categoryId)"\s*:\s*$/.test(truoc) ? so : m;
        });
      }
      const bd = Date.now();
      let res; let text = '';
      try {
        res = await fetch(url, { method: r.method, headers, body });
        text = await res.text();
      } catch (e) {
        kiemTra.push({ ten: 'Kết nối', dat: false, loi: e.message });
      }
      const tg = Date.now() - bd;
      if (res) {
        pm.response = {
          code: res.status,
          json: () => JSON.parse(text),
          text: () => text,
          to: { have: { status: (s) => { if (res.status !== s) throw new Error(`mong đợi HTTP ${s}, thực tế ${res.status}: ${text.slice(0, 160)}`); } } },
        };
        chayScript('test');
      }
      const dat = kiemTra.length > 0 && kiemTra.every((k) => k.dat);
      ketQua.push({ nhom: n.name, ten: it.name, method: r.method, url, ma: res ? res.status : null, thoiGianMs: tg, dat, kiemTra });
      console.log(`${dat ? 'ĐẠT ' : 'TRƯỢT'} ${String(res ? res.status : '---').padEnd(4)} ${String(tg).padStart(5)}ms  ${it.name}`);
      kiemTra.filter((k) => !k.dat).forEach((k) => console.log(`        ✗ ${k.ten}: ${k.loi}`));
    }
  }
  fs.mkdirSync(path.join(thuMuc, 'ket-qua'), { recursive: true });
  const tom = { thoiDiem: new Date().toISOString(), tong: ketQua.length, dat: ketQua.filter((k) => k.dat).length, ketQua };
  fs.writeFileSync(path.join(thuMuc, 'ket-qua', process.argv[4] || 'ket-qua-api.json'), JSON.stringify(tom, null, 2));
  console.log(`\nTổng: ${tom.dat}/${tom.tong} request đạt`);
  process.exitCode = tom.dat === tom.tong ? 0 : 1;
}

chay();
