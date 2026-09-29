import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { dishOrderApi } from '@/api/endpoints';
import { ErrorBlock, Loading } from '@/components/common/StateBlock';
import { useI18n } from '@/i18n';
import { useDinhDang } from '@/i18n/dinhDang';

/*
 * Tra cứu đơn đặt món bằng mã đơn.
 *
 * Tách khỏi trang tra cứu đơn đặt tiệc vì hai loại đơn có mã khác tiền tố và nội
 * dung hiển thị khác hẳn nhau: đơn tiệc có không gian và gói, đơn món có danh sách
 * từng phần. Gộp một trang thì phải rẽ nhánh gần như toàn bộ nội dung.
 *
 * Gửi đơn xong sẽ nhảy thẳng sang đây kèm mã trên đường dẫn, khách không phải chép tay.
 */
export default function TrackDishOrderPage() {
  const [params, setParams] = useSearchParams();
  const maTrenDuongDan = params.get('ma') || '';

  const [ma, setMa] = useState(maTrenDuongDan);
  const [don, setDon] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const { t, lang } = useI18n();
  const dd = useDinhDang();

  // Nhãn enum do backend gửi kèm cả hai thứ tiếng
  const nhan = (viet, anh) => (lang === 'en' && anh ? anh : viet);

  const traCuu = (maCanTim) => {
    if (!maCanTim.trim()) return;
    setLoading(true);
    setError(null);
    dishOrderApi
      .track(maCanTim.trim())
      .then(setDon)
      .catch((err) => {
        setDon(null);
        setError(err.message);
      })
      .finally(() => setLoading(false));
  };

  // Vào trang kèm mã trên đường dẫn thì tra luôn, khỏi bắt bấm thêm lần nữa
  useEffect(() => {
    if (maTrenDuongDan) traCuu(maTrenDuongDan);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [maTrenDuongDan]);

  return (
    <section className="section">
      <div className="wrap" style={{ maxWidth: 720 }}>
        <div className="section-head" style={{ marginBottom: 18 }}>
          <div className="eyebrow">{t('track.eyebrow')}</div>
          <h2>{t('trackDish.title')}</h2>
        </div>

        <form
          onSubmit={(e) => {
            e.preventDefault();
            setParams(ma.trim() ? { ma: ma.trim() } : {});
            traCuu(ma);
          }}
          style={{ display: 'flex', gap: 10, marginBottom: 20 }}
        >
          <input
            value={ma}
            onChange={(e) => setMa(e.target.value)}
            placeholder={t('trackDish.placeholder')}
            data-test="track-code"
            style={{ flex: 1 }}
          />
          <button type="submit" className="btn btn-dark" disabled={loading} data-test="track-submit">
            {t('track.submit')}
          </button>
        </form>

        {loading && <Loading label={t('trackDish.searching')} />}
        {error && <ErrorBlock message={error} />}

        {don && (
          <div className="card">
            <div className="card-body">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <strong style={{ fontSize: '1.05rem' }} data-test="result-code">{don.code}</strong>
                  <div className="muted" style={{ fontSize: '0.86rem' }}>
                    {t('trackDish.placedAt', { luc: dd.ngayGio(don.createdAt) })}
                  </div>
                </div>
                <span className={`tag tag-${don.status}`}>{nhan(don.statusLabel, don.statusLabelEn)}</span>
              </div>

              <table className="bang-thong-so" style={{ marginTop: 16 }}>
                <tbody>
                  <tr>
                    <th>{t('trackDish.fulfillment')}</th>
                    <td data-test="result-fulfillment">{nhan(don.fulfillmentLabel, don.fulfillmentLabelEn)}</td>
                  </tr>
                  <tr>
                    <th>{t('trackDish.customer')}</th>
                    <td>{don.customerName} — {don.customerPhone}</td>
                  </tr>
                  {don.deliveryAddress && (
                    <tr>
                      <th>{t('trackDish.deliveryAddress')}</th>
                      <td>{don.deliveryAddress}</td>
                    </tr>
                  )}
                  {don.guestCount != null && (
                    <tr>
                      <th>{t('track.guests')}</th>
                      <td>{t('trackDish.people', { n: don.guestCount })}</td>
                    </tr>
                  )}
                  <tr>
                    <th>{t('trackDish.serveAt')}</th>
                    <td>{dd.ngayGio(don.serveAt)}</td>
                  </tr>
                  {don.note && (
                    <tr>
                      <th>{t('trackDish.note')}</th>
                      <td>{don.note}</td>
                    </tr>
                  )}
                </tbody>
              </table>

              <h3 style={{ marginTop: 20, marginBottom: 8 }}>{t('trackDish.itemsTitle')}</h3>
              <table className="bang-thong-so">
                <tbody>
                  {don.items.map((d, i) => (
                    <tr key={i} data-test="result-item">
                      <th style={{ fontWeight: 400 }}>
                        {d.dishName} × {d.quantity}
                      </th>
                      <td style={{ textAlign: 'right' }}>{dd.tien(d.lineTotal)}</td>
                    </tr>
                  ))}
                  <tr>
                    <th>{t('trackDish.subtotal')}</th>
                    <td style={{ textAlign: 'right' }}>{dd.tien(don.subtotal)}</td>
                  </tr>
                  {Number(don.discountAmount) > 0 && (
                    <tr>
                      <th>{t('trackDish.holidayDiscount')}</th>
                      <td style={{ textAlign: 'right' }} data-test="result-discount">− {dd.tien(don.discountAmount)}</td>
                    </tr>
                  )}
                  <tr>
                    <th>{t('trackDish.deliveryFee')}</th>
                    <td style={{ textAlign: 'right' }}>
                      {Number(don.deliveryFee) === 0 ? t('track.free') : dd.tien(don.deliveryFee)}
                    </td>
                  </tr>
                  <tr>
                    <th>{t('trackDish.vat')}</th>
                    <td style={{ textAlign: 'right' }}>{dd.tien(don.vatAmount)}</td>
                  </tr>
                  <tr>
                    <th style={{ fontWeight: 600 }}>{t('track.total')}</th>
                    <td style={{ textAlign: 'right', fontWeight: 600 }} data-test="result-total">
                      {dd.tien(don.total)}
                    </td>
                  </tr>
                </tbody>
              </table>

              <p className="muted" style={{ fontSize: '0.84rem', marginTop: 14 }}>
                {t('trackDish.priceLocked')}
              </p>
            </div>
          </div>
        )}

        <p style={{ marginTop: 18 }}>
          <Link to="/thuc-don">{t('dish.backToMenu')}</Link>
        </p>
      </div>
    </section>
  );
}
