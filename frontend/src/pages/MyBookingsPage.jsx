import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { bookingApi, dishOrderApi } from '@/api/endpoints';
import { Empty, ErrorBlock, Loading } from '@/components/common/StateBlock';
import { useI18n } from '@/i18n';
import { useDinhDang } from '@/i18n/dinhDang';
import Pagination from '@/components/common/Pagination';

export default function MyBookingsPage() {
  const [page, setPage] = useState(null);
  const [trang, setTrang] = useState(0);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);
  /*
   * Đơn đặt món lẻ để riêng vì hai loại đơn khác nhau hẳn: đơn tiệc có ngày tổ chức,
   * không gian và số mâm, còn đơn món lẻ chỉ có giờ nhận và danh sách món. Nhồi chung
   * một bảng thì quá nửa số cột bỏ trống.
   */
  const [donMon, setDonMon] = useState(null);
  const [trangMon, setTrangMon] = useState(0);
  const [loiMon, setLoiMon] = useState(null);
  const [dangTaiMon, setDangTaiMon] = useState(true);
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

  // Gọi tách khỏi đơn tiệc để một bên hỏng thì bên kia vẫn hiện được
  useEffect(() => {
    setDangTaiMon(true);
    dishOrderApi
      .mine({ page: trangMon, size: 10 })
      .then(setDonMon)
      .catch((err) => setLoiMon(err.message))
      .finally(() => setDangTaiMon(false));
  }, [trangMon]);

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
                    {/*
                      Nối sang trang tra cứu, vì Header chỉ hiện mục "Tra cứu đơn" cho
                      khách chưa đăng nhập. Không có liên kết này thì người đã đăng nhập
                      chỉ xem được chi tiết đúng một lần ngay sau khi đặt, sau đó mất lối.
                    */}
                    <td data-test="my-booking-code">
                      <Link to={`/tra-cuu?code=${booking.code}`}>{booking.code}</Link>
                    </td>
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

        <div className="section-head" style={{ marginTop: 56 }}>
          <h2>{t('myDishOrders.title')}</h2>
        </div>

        {dangTaiMon && <Loading />}
        {loiMon && <ErrorBlock message={loiMon} />}
        {donMon && donMon.content.length === 0 && <Empty label={t('myDishOrders.empty')} />}

        {donMon && donMon.content.length > 0 && (
          <div className="table-wrap card">
            <table>
              <thead>
                <tr>
                  <th>{t('myDishOrders.code')}</th>
                  <th>{t('myDishOrders.serveAt')}</th>
                  <th>{t('myDishOrders.fulfillment')}</th>
                  <th>{t('myDishOrders.dishes')}</th>
                  <th>{t('myDishOrders.total')}</th>
                  <th>{t('myDishOrders.status')}</th>
                </tr>
              </thead>
              <tbody>
                {donMon.content.map((don) => (
                  <tr key={don.id} data-test="my-dish-order-row">
                    <td data-test="my-dish-order-code">
                      <Link to={`/tra-cuu-mon?ma=${don.code}`}>{don.code}</Link>
                    </td>
                    <td>{dd.ngayGio(don.serveAt)}</td>
                    <td>
                      {lang === 'en'
                        ? don.fulfillmentLabelEn || don.fulfillmentLabel
                        : don.fulfillmentLabel}
                    </td>
                    {/* Đếm số món khác nhau, không cộng số phần: ba phần một món vẫn là một món */}
                    <td>{don.items.length}</td>
                    <td>{dd.tien(don.total)}</td>
                    <td>
                      <span className={`tag tag-${don.status}`}>
                        {lang === 'en' ? don.statusLabelEn || don.statusLabel : don.statusLabel}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        <Pagination trang={donMon} doiTrang={setTrangMon} donVi={t('myDishOrders.unit')} />
      </div>
    </section>
  );
}
