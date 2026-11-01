/*
 * Bo do hieu nang duoi tai dong thoi.
 *
 * Viet bang Node thuan, khong phu thuoc thu vien ngoai, de chay duoc ngay tren may cham
 * de tai ma khong phai cai them gi. Cach do giong JMeter o cho quan trong nhat: giu dung
 * N yeu cau chay song song lien tuc trong mot khoang thoi gian, roi doc phan vi do tre.
 *
 * Vi sao doc phan vi chu khong doc trung binh: trung binh giau mat duoi cua nguoi dung
 * cham nhat. Mot API trung binh 50ms nhung p99 la 3 giay nghia la cu 100 khach thi co
 * mot nguoi cho 3 giay, ma do moi la nguoi se bo trang.
 *
 * Co giai doan lam nong truoc khi do. May ao Java bien dich lai ma nong sau vai nghin
 * luot goi, do ngay tu luot dau thi con so do la con so cua luc chua toi uu, khong phan
 * anh dung luc chay that.
 *
 * Hai kieu tai, ung voi hai cach hieu "1000 nguoi dung trong 1 phut":
 *   Dong thoi: N nguoi cung gui lien tuc suot soGiay, xong yeu cau nay gui ngay yeu cau
 *     khac, khong nghi. Tuong duong Thread Group cua JMeter khong co Timer. Day la muc
 *     nang nhat, moi nguoi o day nang bang nhieu nguoi that vi nguoi that con doc trang.
 *   Rai deu (--rai): N nguoi vao rai deu trong soGiay, moi nguoi goi mot lan. Tuong duong
 *     Thread Group co Ramp-up = soGiay va Loop Count = 1.
 *
 * Chay:
 *   node do-tai.js [so giay moi muc tai] [cac muc tai] [--rai]
 *   node do-tai.js 60 100,1000
 *   node do-tai.js 60 100,1000 --rai
 */
const http = require('http');
const { monitorEventLoopDelay } = require('perf_hooks');

const MAY_CHU = { hostname: 'localhost', port: 8080 };
const GIAY_MOI_MUC = Number(process.argv[2] || 10);
const CAC_MUC_TAI = process.argv[3] && !process.argv[3].startsWith('--')
  ? process.argv[3].split(',').map(Number)
  : [1, 10, 25, 50, 100];
const RAI_DEU = process.argv.includes('--rai');
const GIAY_LAM_NONG = 5;
const QUA_HAN_MS = 30000; // qua 30 giay thi tinh la loi, dung de mot yeu cau treo giu ca phep do

/*
 * May do va may chu nam chung mot laptop. Neu chinh Node bi nghen thi do tre do duoc se
 * cao hon that, nen do luon do tre vong lap su kien cua may do de biet so lieu con tin duoc.
 */
const treMayDo = monitorEventLoopDelay({ resolution: 10 });
treMayDo.enable();

/* Mot luot goi, tra ve do tre tinh bang mili giay va ma trang thai. */
function goi(duongDan, cachGoi = 'GET', than = null, the = null) {
  return new Promise((xong) => {
    const batDau = process.hrtime.bigint();
    const tuyChon = {
      ...MAY_CHU,
      path: duongDan,
      method: cachGoi,
      headers: {},
    };
    if (than) {
      tuyChon.headers['Content-Type'] = 'application/json';
      tuyChon.headers['Content-Length'] = Buffer.byteLength(than);
    }
    if (the) {
      tuyChon.headers.Authorization = 'Bearer ' + the;
    }

    const yc = http.request(tuyChon, (pt) => {
      pt.resume(); // phai doc het than, khong thi ket noi khong duoc tra lai
      pt.on('end', () => {
        const ms = Number(process.hrtime.bigint() - batDau) / 1e6;
        xong({ ms, ma: pt.statusCode });
      });
    });
    yc.setTimeout(QUA_HAN_MS, () => yc.destroy());
    yc.on('error', () => xong({ ms: 0, ma: 0 }));
    if (than) yc.write(than);
    yc.end();
  });
}

/*
 * Giu dung soLuong yeu cau chay song song trong soGiay giay.
 *
 * Moi luong khi xong mot yeu cau thi phat ngay yeu cau ke tiep, nen so yeu cau dang bay
 * luon bang soLuong. Day la cach mo phong dung "N nguoi dung dong thoi", khac voi viec
 * ban mot loat N yeu cau roi cho tat ca xong.
 */
async function chayTai(kichBan, soLuong, soGiay) {
  const ketThuc = Date.now() + soGiay * 1000;
  const doTre = [];
  let loi = 0;

  const luong = async () => {
    while (Date.now() < ketThuc) {
      const kq = await kichBan();
      if (kq.ma >= 200 && kq.ma < 400) doTre.push(kq.ms);
      else loi++;
    }
  };

  const batDau = Date.now();
  await Promise.all(Array.from({ length: soLuong }, luong));
  const giayThuc = (Date.now() - batDau) / 1000;

  return { doTre, loi, giayThuc };
}

/*
 * N nguoi vao rai deu trong soGiay giay, moi nguoi goi mot lan.
 *
 * Khac chayTai o cho nguoi sau khong cho nguoi truoc xong. May chu cham thi hang doi dai
 * ra chu tai khong tu giam, dung voi khach that: khach khong biet may chu dang ban.
 */
async function chayRaiDeu(kichBan, soNguoi, soGiay) {
  const doTre = [];
  let loi = 0;
  const batDau = Date.now();

  const nguoi = (i) => new Promise((xong) => {
    setTimeout(async () => {
      const kq = await kichBan();
      if (kq.ma >= 200 && kq.ma < 400) doTre.push(kq.ms);
      else loi++;
      xong();
    }, i * soGiay * 1000 / soNguoi);
  });

  await Promise.all(Array.from({ length: soNguoi }, (_, i) => nguoi(i)));
  return { doTre, loi, giayThuc: (Date.now() - batDau) / 1000 };
}

const phanVi = (mang, p) => {
  if (!mang.length) return 0;
  const sapXep = [...mang].sort((a, b) => a - b);
  return sapXep[Math.min(sapXep.length - 1, Math.floor(sapXep.length * p))];
};

async function dangNhap(email, matKhau) {
  const than = JSON.stringify({ email, password: matKhau });
  return new Promise((xong) => {
    const yc = http.request(
      { ...MAY_CHU, path: '/api/auth/login', method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Content-Length': Buffer.byteLength(than) } },
      (pt) => {
        let d = '';
        pt.on('data', (c) => { d += c; });
        pt.on('end', () => {
          try { xong(JSON.parse(d).accessToken); } catch { xong(null); }
        });
      });
    yc.on('error', () => xong(null));
    yc.write(than);
    yc.end();
  });
}

(async () => {
  const the = await dangNhap('khoa.demo@gmail.com', 'Khach@123');
  if (!the) {
    console.log('Khong dang nhap duoc tai khoan demo, bo qua phep do can xac thuc.');
  }

  const CAC_PHEP_DO = [
    { ten: 'GET /spaces (danh sach khong gian)', chay: () => goi('/api/v1/spaces') },
    { ten: 'GET /menu/dishes (126 mon)', chay: () => goi('/api/v1/menu/dishes') },
    { ten: 'POST /assistant/ask (ngan hang cau hoi)',
      chay: () => goi('/api/v1/assistant/ask', 'POST', JSON.stringify({ question: 'Dat tiec nhu the nao?' })) },
    { ten: 'GET /assistant/loi-mo-dau (ca nhan hoa)',
      chay: () => goi('/api/v1/assistant/loi-mo-dau', 'GET', null, the) },
  ];

  console.log('Lam nong ' + GIAY_LAM_NONG + ' giay...');
  for (const pd of CAC_PHEP_DO) {
    await chayTai(pd.chay, 10, GIAY_LAM_NONG / CAC_PHEP_DO.length);
  }

  for (const pd of CAC_PHEP_DO) {
    console.log('\n=== ' + pd.ten + ' ===');
    console.log('   tai | yeu cau | thong luong |  trung binh |     p50 |     p95 |     p99 |     max |  loi | tre may do');
    console.log('  -----+---------+-------------+-------------+---------+---------+---------+---------+------+-----------');
    for (const muc of CAC_MUC_TAI) {
      treMayDo.reset();
      const { doTre, loi, giayThuc } = await (RAI_DEU ? chayRaiDeu : chayTai)(pd.chay, muc, GIAY_MOI_MUC);
      const thongLuong = (doTre.length / giayThuc).toFixed(0);
      // khong dung Math.max(...doTre): mang vai tram nghin phan tu lam tran ngan xep
      const lonNhat = doTre.reduce((a, b) => Math.max(a, b), 0);
      const trungBinh = doTre.length ? doTre.reduce((a, b) => a + b, 0) / doTre.length : 0;
      console.log('  %s | %s | %s | %s | %s | %s | %s | %s | %s | %s',
        String(muc).padStart(4),
        String(doTre.length).padStart(7),
        (thongLuong + ' req/s').padStart(11),
        (trungBinh.toFixed(0) + 'ms').padStart(11),
        (phanVi(doTre, 0.50).toFixed(0) + 'ms').padStart(7),
        (phanVi(doTre, 0.95).toFixed(0) + 'ms').padStart(7),
        (phanVi(doTre, 0.99).toFixed(0) + 'ms').padStart(7),
        (lonNhat.toFixed(0) + 'ms').padStart(7),
        String(loi).padStart(4),
        ((treMayDo.percentile(99) / 1e6).toFixed(0) + 'ms').padStart(9));
    }
  }
  console.log('\n' + (RAI_DEU ? 'Moi muc tai: N nguoi vao rai deu trong ' : 'Moi muc tai: N nguoi dong thoi trong ')
    + GIAY_MOI_MUC + ' giay. Do tre tinh tu luc gui den luc nhan het than.');
  process.exit(0);
})();
