import { useEffect, useMemo, useState } from 'react';
import { menuApi } from '@/api/endpoints';
import { useI18n } from '@/i18n';
import { useDinhDang } from '@/i18n/dinhDang';
import { ErrorBlock, Loading } from '@/components/common/StateBlock';

/*
 * Chọn từng món cho tiệc nhỏ ở sảnh nhỏ.
 *
 * Khác đặt món lẻ ở chỗ món chọn ở đây gắn vào đơn đặt tiệc, nên vẫn có phí thuê không
 * gian và vẫn hưởng giảm giá dịp lễ như một đơn tiệc bình thường.
 *
 * Giới hạn số khách do máy chủ quyết định và kiểm lại lúc gửi đơn; ở đây chỉ báo trước
 * cho khách biết để khỏi điền xong mới bị từ chối.
 */
export default function ChonMonTuChon({ soKhach, gioiHanKhach, daChon, onDoiSo }) {
  const { t, tDb } = useI18n();
  const dd = useDinhDang();
  const [danhMuc, setDanhMuc] = useState([]);
  const [mon, setMon] = useState([]);
  const [dangTai, setDangTai] = useState(true);
  const [loi, setLoi] = useState(null);
  const [locDanhMuc, setLocDanhMuc] = useState('');
  const [tim, setTim] = useState('');

  useEffect(() => {
    let huy = false;
    Promise.all([menuApi.categories(), menuApi.dishes()])
      .then(([dm, ds]) => {
        if (huy) return;
        setDanhMuc(Array.isArray(dm) ? dm : []);
        setMon(Array.isArray(ds) ? ds : ds?.content || []);
      })
      .catch((e) => !huy && setLoi(e.message || t('common.error')))
      .finally(() => !huy && setDangTai(false));
    return () => {
      huy = true;
    };
  }, [t]);

  const hienThi = useMemo(() => {
    const khoa = tim.trim().toLowerCase();
    return mon.filter((m) => {
      if (locDanhMuc && String(m.categoryId) !== String(locDanhMuc)) return false;
      if (khoa && !tDb(m, 'name').toLowerCase().includes(khoa)) return false;
      return true;
    });
  }, [mon, locDanhMuc, tim, tDb]);

  const soPhan = (id) => daChon.find((d) => d.dishId === id)?.soLuong || 0;

  /*
   * Chỉ báo lên trên là cộng hay trừ, phép tính để kho trạng thái lo.
   * Tự tính ở đây thì bấm nhanh nhiều lần sẽ mất lần bấm, vì mỗi lần đều đọc cùng một
   * bản danh sách cũ của lần vẽ trước.
   */
  const doiSo = (m, buoc) => onDoiSo(m.id, buoc);

  const tongPhan = daChon.reduce((s, d) => s + d.soLuong, 0);
  const tongTien = daChon.reduce((s, d) => {
    const m = mon.find((x) => x.id === d.dishId);
    return s + (m ? Number(m.price) * d.soLuong : 0);
  }, 0);

  const vuotKhach = Number(soKhach) > gioiHanKhach;

  if (dangTai) return <Loading label={t('booking.dangTaiThucDon')} />;
  if (loi) return <ErrorBlock message={loi} />;

  return (
    <div className="tuchon" data-test="custom-menu">
      {vuotKhach && (
        <p className="err" data-test="custom-menu-too-many">
          {t('booking.tuChonQuaDong', { n: gioiHanKhach })}
        </p>
      )}

      <div className="tuchon-loc">
        <select
          value={locDanhMuc}
          onChange={(e) => setLocDanhMuc(e.target.value)}
          aria-label={t('booking.tuChonLocDanhMuc')}
          data-test="custom-menu-category"
        >
          <option value="">{t('booking.tuChonTatCa')}</option>
          {danhMuc.map((d) => (
            <option key={d.id} value={d.id}>
              {tDb(d, 'name')}
            </option>
          ))}
        </select>
        <input
          type="search"
          value={tim}
          onChange={(e) => setTim(e.target.value)}
          placeholder={t('booking.tuChonTimMon')}
          aria-label={t('booking.tuChonTimMon')}
          data-test="custom-menu-search"
        />
      </div>

      <ul className="tuchon-ds">
        {hienThi.map((m) => {
          const n = soPhan(m.id);
          return (
            <li key={m.id} className={n > 0 ? 'tuchon-mon tuchon-mon-chon' : 'tuchon-mon'} data-test="custom-menu-row">
              <span className="tuchon-ten">{tDb(m, 'name')}</span>
              <span className="tuchon-gia">{dd.tien(m.price)}</span>
              <span className="tuchon-nut">
                <button
                  type="button"
                  onClick={() => doiSo(m, -1)}
                  disabled={n === 0}
                  aria-label={t('booking.tuChonBot', { mon: tDb(m, 'name') })}
                  data-test="custom-menu-minus"
                >
                  −
                </button>
                <span className="tuchon-so" data-test="custom-menu-qty">{n}</span>
                <button
                  type="button"
                  onClick={() => doiSo(m, 1)}
                  aria-label={t('booking.tuChonThem', { mon: tDb(m, 'name') })}
                  data-test="custom-menu-plus"
                >
                  +
                </button>
              </span>
            </li>
          );
        })}
        {hienThi.length === 0 && <li className="muted">{t('common.empty')}</li>}
      </ul>

      <p className="tuchon-tong" data-test="custom-menu-total">
        {t('booking.tuChonTong', { mon: daChon.length, phan: tongPhan })} · {dd.tien(tongTien)}
      </p>
    </div>
  );
}
