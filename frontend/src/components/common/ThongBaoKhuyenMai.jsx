import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { holidayApi } from '@/api/endpoints';
import { useI18n } from '@/i18n';
import { batThongBao, dangBat, tatThongBao, trangThaiQuyen } from '@/utils/thongBaoDay';

const KHOA_DA_DONG = 'vuonsen.khuyenmai.daDong';

const hai = (n) => String(n).padStart(2, '0');
const isoHomNay = () => {
  const d = new Date();
  return `${d.getFullYear()}-${hai(d.getMonth() + 1)}-${hai(d.getDate())}`;
};

/* Số ngày từ hôm nay tới ngày cho trước. Tính trên chuỗi yyyy-MM-dd nên không lệch múi giờ. */
function soNgayToi(ngayIso) {
  const [n, t, g] = ngayIso.split('-').map(Number);
  const dich = new Date(n, t - 1, g);
  const nay = new Date();
  nay.setHours(0, 0, 0, 0);
  return Math.round((dich - nay) / 86400000);
}

/*
 * Chọn dịp đáng báo nhất: dịp đang diễn ra được ưu tiên, sau đó tới dịp sắp tới gần nhất.
 *
 * Chỉ báo một dịp thôi. Liệt kê cả danh sách lên trang chủ thì thành bảng giá thứ hai,
 * khách đọc xong không biết nên làm gì; một dịp kèm một nút thì rõ việc.
 */
function dipDangBao(cacDip) {
  const nay = isoHomNay();
  const con = cacDip
    .filter((d) => d.active && d.endDate >= nay)
    .sort((a, b) => a.startDate.localeCompare(b.startDate));
  const dangChay = con.filter((d) => d.startDate <= nay);
  if (dangChay.length > 0) {
    /* Nhiều dịp chồng nhau thì lấy mức giảm cao nhất, đúng như cách hệ thống tính tiền. */
    return dangChay.reduce((a, b) => (b.discountRate > a.discountRate ? b : a));
  }
  return con[0] || null;
}

function docDaDong() {
  try {
    return JSON.parse(localStorage.getItem(KHOA_DA_DONG) || '[]');
  } catch {
    return [];
  }
}

/*
 * Dải thông báo khuyến mãi ở đầu trang chủ.
 *
 * Khách đóng rồi thì nhớ theo mã dịp chứ không nhớ chung một cờ: đóng thông báo Tết xong,
 * tới dịp Quốc khánh vẫn phải hiện lại, không thì khách tắt một lần là mất luôn mọi khuyến
 * mãi về sau.
 *
 * Không dùng cửa sổ bật lên che màn hình. Khuyến mãi là tin vui chứ không phải việc gấp,
 * chặn đường đọc trang chỉ làm khách bực rồi tắt mà không đọc.
 */
export default function ThongBaoKhuyenMai() {
  const { t, lang } = useI18n();
  const [dip, setDip] = useState(null);
  const [daDong, setDaDong] = useState(docDaDong);
  const [batTB, setBatTB] = useState(false);
  const [nhanTB, setNhanTB] = useState(null);
  const [dangXu, setDangXu] = useState(false);

  useEffect(() => {
    dangBat().then(setBatTB).catch(() => {});
  }, []);

  const doiThongBao = async () => {
    setDangXu(true);
    setNhanTB(null);
    try {
      if (batTB) {
        await tatThongBao();
        setBatTB(false);
      } else {
        const ket = await batThongBao(lang);
        if (ket === 'da-bat') {
          setBatTB(true);
          setNhanTB(t('khuyenMai.daBat'));
        } else if (ket === 'tu-choi') {
          setNhanTB(t('khuyenMai.tuChoi'));
        } else if (ket === 'khong-ho-tro') {
          setNhanTB(t('khuyenMai.khongHoTro'));
        } else {
          setNhanTB(t('khuyenMai.mayChuChuaBat'));
        }
      }
    } catch {
      setNhanTB(t('khuyenMai.loi'));
    } finally {
      setDangXu(false);
    }
  };

  useEffect(() => {
    let huy = false;
    holidayApi
      .list()
      .then((ds) => {
        if (!huy) setDip(dipDangBao(Array.isArray(ds) ? ds : []));
      })
      /* Hỏng thì im lặng. Đây là khối phụ, không được phép làm hỏng trang chủ. */
      .catch(() => {});
    return () => {
      huy = true;
    };
  }, []);

  const thongTin = useMemo(() => {
    if (!dip) return null;
    const nay = isoHomNay();
    const dangChay = dip.startDate <= nay;
    const conNgay = soNgayToi(dip.startDate);
    return {
      ten: lang === 'en' && dip.nameEn ? dip.nameEn : dip.name,
      phanTram: Math.round(dip.discountRate * 100),
      dangChay,
      conNgay,
    };
  }, [dip, lang]);

  if (!dip || !thongTin || daDong.includes(dip.id)) return null;

  const dong = () => {
    const moi = [...daDong, dip.id];
    setDaDong(moi);
    try {
      localStorage.setItem(KHOA_DA_DONG, JSON.stringify(moi));
    } catch {
      /* Trình duyệt chặn lưu thì vẫn đóng được, chỉ là lần sau hiện lại. */
    }
  };

  const nhan = thongTin.dangChay
    ? t('khuyenMai.dangApDung')
    : thongTin.conNgay <= 1
      ? t('khuyenMai.batDauMai')
      : t('khuyenMai.conNgay', { n: thongTin.conNgay });

  return (
    <aside className="km-dai" role="status" aria-live="polite" aria-label={t('khuyenMai.nhanAria')}>
      <div className="wrap km-trong">
        <span className="km-huy" aria-hidden="true">🎉</span>

        <span className="km-muc">
          <strong>{t('khuyenMai.giam', { n: thongTin.phanTram })}</strong>
        </span>

        <span className="km-chu">
          <span className="km-ten">{thongTin.ten}</span>
          <span className="km-phu">{nhan}</span>
        </span>

        <Link to="/uu-dai" className="km-nut">
          {t('khuyenMai.xemChiTiet')}
        </Link>

        {/* Trình duyệt đã chặn hẳn thì ẩn nút đi, bấm cũng không làm gì được nữa. */}
        {trangThaiQuyen() !== 'khong-ho-tro' && trangThaiQuyen() !== 'denied' && (
          <button
            type="button"
            className={batTB ? 'km-chuong km-chuong-bat' : 'km-chuong'}
            onClick={doiThongBao}
            disabled={dangXu}
            aria-pressed={batTB}
            title={batTB ? t('khuyenMai.tatDi') : undefined}
          >
            {batTB ? t('khuyenMai.dangBat') : t('khuyenMai.bat')}
          </button>
        )}

        <button type="button" className="km-dong" onClick={dong} aria-label={t('khuyenMai.dongAria')}>
          ×
        </button>

        {nhanTB && (
          <p className="km-nhan" role="status">
            {nhanTB}
          </p>
        )}
      </div>
    </aside>
  );
}
