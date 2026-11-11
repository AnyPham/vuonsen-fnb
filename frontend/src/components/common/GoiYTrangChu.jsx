import { useEffect, useState } from 'react';
import { useSelector } from 'react-redux';
import { Link } from 'react-router-dom';
import { goiYApi } from '@/api/endpoints';
import { selectUser } from '@/features/auth/authSlice';
import Thumb from '@/components/common/Thumb';
import { useI18n } from '@/i18n';
import { useDinhDang } from '@/i18n/dinhDang';

/*
 * Khối gợi ý thông minh ở đầu trang chủ: sảnh và món hợp với khách đang xem.
 *
 * Một lần gọi API trả cả hai phần. Mỗi phần tự cho biết kiểu của nó:
 *   CA_NHAN — khách đã có hồ sơ, tiêu đề "phù hợp với nhu cầu của bạn" kèm căn cứ.
 *   NOI_BAT — khách chưa có thông tin, tiêu đề "nổi bật" và số tiệc đã đặt.
 *
 * Gọi lại mỗi khi đăng nhập hoặc đăng xuất, vì cùng một trang chủ mà người xem đã khác.
 * Không hiện điểm phù hợp, chỉ hiện lý do: khách cần biết vì sao, không cần một con số.
 */
export default function GoiYTrangChu() {
  const nguoiDung = useSelector(selectUser);
  const { t, tDb, lang } = useI18n();
  const dd = useDinhDang();
  const [goiY, setGoiY] = useState(null);

  useEffect(() => {
    let conHieuLuc = true;
    goiYApi
      .trangChu()
      .then((kq) => conHieuLuc && setGoiY(kq))
      .catch(() => conHieuLuc && setGoiY(null));
    return () => {
      conHieuLuc = false;
    };
  }, [nguoiDung?.id]);

  if (!goiY) return null;

  const giaTri = (lyDo) => (lang === 'en' ? lyDo.giaTriEn || lyDo.giaTri : lyDo.giaTri);

  // Dịch một lý do dạng mã sang câu đọc được
  const cauLyDo = (lyDo) => {
    const g = giaTri(lyDo);
    switch (lyDo.ma) {
      case 'LOAI_TIEC':
        return t('goiY.lyDo.LOAI_TIEC', { loai: t('goiY.loaiTiec.' + g) });
      case 'KIEU_KHONG_GIAN':
        return t('goiY.lyDo.KIEU_KHONG_GIAN', { kieu: t('goiY.kieu.' + g) });
      case 'TAM_GIA':
        return t('goiY.lyDo.TAM_GIA', { muc: t('goiY.tamGia.' + g) });
      case 'CUNG_LOAI':
        // Tên danh mục viết hoa đầu câu trong thực đơn, giữa câu thì viết thường
        return t('goiY.lyDo.CUNG_LOAI', { ten: lang === 'vi' ? g.toLowerCase() : g });
      case 'SO_KHACH':
        return t('goiY.lyDo.SO_KHACH', { dai: g.replace('-', '–') });
      default:
        return t('goiY.lyDo.' + lyDo.ma, { n: g, ten: g, loai: g });
    }
  };

  const canCuSanh = () => {
    const c = goiY.khongGian.canCu;
    const phan = [];
    if (c.soTiec > 0) phan.push(t('goiY.canCu.soTiec', { n: c.soTiec }));
    if (c.soSanhDaXem > 0) phan.push(t('goiY.canCu.soSanhDaXem', { n: c.soSanhDaXem }));
    if (c.coSoThich) phan.push(t('goiY.canCu.soThich'));
    // "A, B và C": phẩy giữa các ý, chữ "và" trước ý cuối
    const ds = phan.length > 1
      ? phan.slice(0, -1).join(t('goiY.canCu.noi')) + t('goiY.canCu.va') + phan[phan.length - 1]
      : phan[0];
    return t('goiY.canCu.mo', { ds });
  };

  const sanhCaNhan = goiY.khongGian.kieu === 'CA_NHAN';
  const monCaNhan = goiY.monAn.kieu === 'CA_NHAN';

  return (
    <section className="section" style={{ background: 'var(--cream-2)' }} data-test="goi-y-trang-chu">
      <div className="wrap">
        <div className="section-head center">
          <div className="eyebrow center">{sanhCaNhan ? t('goiY.eyebrowCaNhan') : t('goiY.eyebrowNoiBat')}</div>
          <h2 data-test="goi-y-tieu-de" data-kieu={goiY.khongGian.kieu}>
            {sanhCaNhan ? t('goiY.tieuDeCaNhan') : t('goiY.tieuDeNoiBat')}
          </h2>
          <p className="muted" data-test="goi-y-can-cu">
            {sanhCaNhan ? canCuSanh() : t('goiY.moTaNoiBat')}
          </p>
        </div>

        <div className="grid grid-3">
          {goiY.khongGian.items.map((s) => (
            <article key={s.id} className="card" data-test="goi-y-sanh" data-slug={s.slug}>
              <Thumb url={s.thumbnailUrl} icon="🌿" label={tDb(s, 'name')} alt={tDb(s, 'name')} />
              <div className="card-body">
                {s.daDat > 0 && (
                  <span className="tag tag-CONFIRMED" data-test="goi-y-da-dat">
                    {t('goiY.daDat', { n: s.daDat })}
                  </span>
                )}
                <h3 style={{ marginTop: s.daDat > 0 ? 8 : 0 }}>
                  <Link to={`/khong-gian/${s.slug}`} className="full-link">
                    {tDb(s, 'name')}
                  </Link>
                </h3>
                <div className="muted" style={{ fontSize: '0.88rem', margin: '4px 0 10px' }}>
                  {s.capacityMax
                    ? t('goiY.sucChua', { min: s.capacityMin, max: s.capacityMax })
                    : t('goiY.sucChuaTu', { min: s.capacityMin })}
                </div>
                <ul className="goi-y-ly-do" style={{ margin: '0 0 12px', paddingLeft: 18, fontSize: '0.9rem' }}>
                  {s.lyDo.map((ld) => (
                    <li key={ld.ma} data-test="goi-y-ly-do" data-ma={ld.ma}>
                      {cauLyDo(ld)}
                    </li>
                  ))}
                </ul>
                <strong style={{ color: 'var(--green-800)' }}>
                  {dd.tien(s.rentalFee)}
                  <small className="muted">
                    {' '}
                    / {s.feeUnit === 'HUT' ? t('spaces.perHut') : t('spaces.perSession')}
                  </small>
                </strong>
              </div>
            </article>
          ))}
        </div>

        {goiY.monAn.items.length > 0 && (
          <>
            <h3 style={{ margin: '36px 0 6px' }} data-test="goi-y-mon-tieu-de" data-kieu={goiY.monAn.kieu}>
              {monCaNhan ? t('goiY.monCaNhan') : t('goiY.monNoiBat')}
            </h3>
            <p className="muted" style={{ marginBottom: 16 }}>
              {monCaNhan
                ? t('goiY.monCanCu', { n: goiY.monAn.canCu.soMonDaGoi })
                : t('goiY.monMoTaNoiBat')}
            </p>
            <div className="grid grid-3">
              {goiY.monAn.items.map((m) => (
                <article key={m.id} className="card" data-test="goi-y-mon">
                  <Thumb url={m.imageUrl} icon="🍲" label={tDb(m, 'name')} alt={tDb(m, 'name')} />
                  <div className="card-body">
                    {m.vegetarian && <span className="chip" data-test="goi-y-mon-chay">{t('goiY.chay')}</span>}
                    <h3 style={{ fontSize: '1.05rem' }}>
                      <Link to={`/thuc-don/${m.slug || m.id}`} className="full-link">
                        {tDb(m, 'name')}
                      </Link>
                    </h3>
                    <ul style={{ margin: '6px 0 10px', paddingLeft: 18, fontSize: '0.88rem' }}>
                      {m.lyDo.map((ld) => (
                        <li key={ld.ma} data-test="goi-y-ly-do-mon" data-ma={ld.ma}>
                          {cauLyDo(ld)}
                        </li>
                      ))}
                    </ul>
                    <div style={{ fontWeight: 600, color: 'var(--green-800)' }}>
                      {m.price ? dd.tien(m.price) : tDb(m, 'priceNote')}
                    </div>
                  </div>
                </article>
              ))}
            </div>
          </>
        )}

        {!sanhCaNhan && (
          <p className="muted center" style={{ marginTop: 24, fontSize: '0.9rem' }} data-test="goi-y-moi-khai">
            {nguoiDung ? (
              <>
                {t('goiY.moiKhaiSoThich')} <Link to="/ho-so">{t('goiY.diToiHoSo')}</Link>
              </>
            ) : (
              <>
                {t('goiY.moiDangNhap')} <Link to="/dang-nhap">{t('goiY.dangNhap')}</Link>
              </>
            )}
          </p>
        )}
      </div>
    </section>
  );
}
