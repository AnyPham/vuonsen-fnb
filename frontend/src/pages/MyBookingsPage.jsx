import { useEffect, useState } from 'react';
import { bookingApi } from '@/api/endpoints';
import { Empty, ErrorBlock, Loading } from '@/components/common/StateBlock';
import { useI18n } from '@/i18n';
import { useDinhDang } from '@/i18n/dinhDang';
import Pagination from '@/components/common/Pagination';

export default function MyBookingsPage() {
  const [page, setPage] = useState(null);
  const [trang, setTrang] = useState(0);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);
  const { t, tDb, lang } = useI18n();
  const dd = useDinhDang();

  useEffect(() => {
    setLoading(true);
    bookingApi
      .mine({ page: trang, size: 10 })
      .then(setPage)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [trang]);

  return (
    <section className="section">
      <div className="wrap">
        <div className="section-head">
          <div className="eyebrow">{t('account.eyebrow')}</div>
          <h2>{t('myBookings.title')}</h2>
        </div>

        {loading && <Loading />}
        {error && <ErrorBlock message={error} />}
        {page && page.content.length === 0 && <Empty label={t('myBookings.empty')} />}

        {page && page.content.length > 0 && (
          <div className="table-wrap card">
            <table>
              <thead>
                <tr>
                  <th>{t('myBookings.code')}</th>
                  <th>{t('myBookings.date')}</th>
                  <th>{t('myBookings.space')}</th>
                  <th>{t('myBookings.guests')}</th>
                  <th>{t('myBookings.total')}</th>
                  <th>{t('myBookings.status')}</th>
                </tr>
              </thead>
              <tbody>
                {page.content.map((booking) => (
                  <tr key={booking.id} data-test="my-booking-row">
                    <td data-test="my-booking-code">{booking.code}</td>
                    <td>
                      {dd.ngay(booking.eventDate)}
                      <br />
                      <small className="muted">
                        {lang === 'en' ? booking.timeSlotLabelEn || booking.timeSlotLabel : booking.timeSlotLabel}
                      </small>
                    </td>
                    <td>{tDb(booking, 'spaceName')}</td>
                    <td>
                      {t('common.guests', { n: booking.guestCount })}
                      <br />
                      <small className="muted">
                        {booking.tableCount > 0
                          ? t('common.tables', { n: booking.tableCount })
                          : t('common.venueOnly')}
                      </small>
                    </td>
                    <td>{dd.tien(booking.totalAmount)}</td>
                    <td>
                      <span className={`tag tag-${booking.status}`}>
                        {lang === 'en' ? booking.statusLabelEn || booking.statusLabel : booking.statusLabel}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        <Pagination trang={page} doiTrang={setTrang} donVi={t('myBookings.unit')} />
      </div>
    </section>
  );
}
