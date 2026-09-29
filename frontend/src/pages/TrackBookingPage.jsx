import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { bookingApi } from '@/api/endpoints';
import { ErrorBlock } from '@/components/common/StateBlock';
import { useI18n } from '@/i18n';
import { useDinhDang } from '@/i18n/dinhDang';

/*
 * Câu mô tả tình trạng cọc của đơn.
 *
 * Nhà hàng thu cọc bằng chuyển khoản hoặc tiền mặt rồi nhân viên ghi nhận lại, nên khách
 * cần chỗ tự kiểm tra xem tiền mình đóng đã được ghi nhận chưa.
 */
function moTaCoc(booking, t, dd) {
  const phaiDong = Number(booking.depositAmount || 0);
  const daDong = Number(booking.depositPaid || 0);

  if (daDong >= phaiDong) {
    return t('track.depositFull', { daDong: dd.tien(daDong) });
  }
  if (daDong > 0) {
    return t('track.depositPartial', { daDong: dd.tien(daDong), conLai: dd.tien(phaiDong - daDong) });
  }
  return t('track.depositNone', { phaiDong: dd.tien(phaiDong) });
}

// Tra cứu đơn bằng mã, dành cho khách không có tài khoản
export default function TrackBookingPage() {
  const [searchParams] = useSearchParams();
  const [code, setCode] = useState(searchParams.get('code') || '');
  const [booking, setBooking] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const { t, tDb, lang } = useI18n();
  const dd = useDinhDang();

  // Nhãn enum do backend gửi kèm cả hai thứ tiếng, chọn theo ngôn ngữ đang dùng
  const nhan = (viet, anh) => (lang === 'en' && anh ? anh : viet);

  const search = async (value) => {
    if (!value.trim()) {
      // Bỏ trống mà im lặng thì khách tưởng hệ thống hỏng; nhắc ngay tại chỗ
      setError(t('track.needCode'));
      return;
    }
    setLoading(true);
    setError(null);
    try {
      setBooking(await bookingApi.track(value.trim()));
    } catch (err) {
      setBooking(null);
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  // Vào trang kèm ?code=... thì tra cứu luôn
  useEffect(() => {
    const initial = searchParams.get('code');
    if (initial) search(initial);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <section className="section">
      <div className="wrap" style={{ maxWidth: 640 }}>
        <div className="section-head center">
          <div className="eyebrow center">{t('track.eyebrow')}</div>
          <h2>{t('track.title')}</h2>
          <p className="muted">{t('track.desc')}</p>
        </div>

        <form
          onSubmit={(e) => {
            e.preventDefault();
            search(code);
          }}
          style={{ display: 'flex', gap: 12, marginBottom: 26 }}
        >
          <input
            aria-label={t('track.codeLabel')}
            data-test="track-code"
            value={code}
            placeholder="VS-20260815-0001"
            onChange={(e) => setCode(e.target.value)}
            style={{ flex: 1, padding: '12px 14px', border: '1px solid var(--line)', borderRadius: 10, font: 'inherit' }}
          />
          <button type="submit" className="btn btn-dark" disabled={loading} data-test="track-submit">
            {loading ? t('track.searching') : t('track.submit')}
          </button>
        </form>

        {error && <ErrorBlock message={error} />}

        {booking && (
          <div className="card">
            <div className="card-body">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
                <h3 data-test="result-code">{booking.code}</h3>
                <span className={`tag tag-${booking.status}`} data-test="result-status">
                  {nhan(booking.statusLabel, booking.statusLabelEn)}
                </span>
              </div>

              <table>
                <tbody>
                  <tr>
                    <th>{t('track.eventType')}</th>
                    <td>{nhan(booking.eventTypeLabel, booking.eventTypeLabelEn)}</td>
                  </tr>
                  <tr>
                    <th>{t('track.dateSlot')}</th>
                    <td>
                      {dd.ngay(booking.eventDate)} · {nhan(booking.timeSlotLabel, booking.timeSlotLabelEn)}
                    </td>
                  </tr>
                  <tr>
                    <th>{t('track.guests')}</th>
                    <td>
                      {booking.tableCount > 0
                        ? t('track.guestsWithTables', { khach: booking.guestCount, mam: booking.tableCount })
                        : t('common.guests', { n: booking.guestCount })}
                    </td>
                  </tr>
                  <tr>
                    <th>{t('track.space')}</th>
                    <td data-test="result-space">{tDb(booking, 'spaceName')}</td>
                  </tr>
                  <tr>
                    <th>{t('track.package')}</th>
                    <td>{tDb(booking, 'packageName')}</td>
                  </tr>
                  {booking.tableCount > 0 && (
                    <tr>
                      <th>{t('track.foodAmount')}</th>
                      <td>{dd.tien(booking.foodAmount)}</td>
                    </tr>
                  )}
                  <tr>
                    <th>{t('track.spaceFee')}</th>
                    <td>{Number(booking.spaceFee) === 0 ? t('track.free') : dd.tien(booking.spaceFee)}</td>
                  </tr>
                  {Number(booking.discountAmount) > 0 && (
                    <tr>
                      <th>{t('track.discount')}</th>
                      <td>− {dd.tien(booking.discountAmount)}</td>
                    </tr>
                  )}
                  <tr>
                    <th>{t('track.vat')}</th>
                    <td>{dd.tien(booking.vatAmount)}</td>
                  </tr>
                  <tr>
                    <th>{t('track.total')}</th>
                    <td>
                      <strong>{dd.tien(booking.totalAmount)}</strong>
                    </td>
                  </tr>
                  {/* Khách hay gọi điện hỏi đã đóng cọc chưa, nên hiện luôn ở đây */}
                  {Number(booking.depositAmount) > 0 && (
                    <tr>
                      <th>{t('track.deposit')}</th>
                      <td data-test="result-deposit">{moTaCoc(booking, t, dd)}</td>
                    </tr>
                  )}
                </tbody>
              </table>

              {Number(booking.depositPaid) < Number(booking.totalAmount)
                && booking.status !== 'CANCELLED' && (
                <div style={{ marginTop: 20 }}>
                  <Link to={`/thanh-toan?ma=${booking.code}`} className="btn btn-gold" data-test="go-pay">
                    {t('track.pay')}
                  </Link>
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </section>
  );
}
