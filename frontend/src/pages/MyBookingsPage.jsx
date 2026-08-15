import { useEffect, useState } from 'react';
import { bookingApi } from '@/api/endpoints';
import { formatCurrency, formatDate } from '@/utils/format';
import { Empty, ErrorBlock, Loading } from '@/components/common/StateBlock';

export default function MyBookingsPage() {
  const [page, setPage] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    bookingApi
      .mine({ page: 0, size: 20 })
      .then(setPage)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  return (
    <section className="section">
      <div className="wrap">
        <div className="section-head">
          <div className="eyebrow">Tài khoản</div>
          <h2>Đơn đặt tiệc của tôi</h2>
        </div>

        {loading && <Loading />}
        {error && <ErrorBlock message={error} />}
        {page && page.content.length === 0 && <Empty label="Bạn chưa có đơn đặt tiệc nào." />}

        {page && page.content.length > 0 && (
          <div className="table-wrap card">
            <table>
              <thead>
                <tr>
                  <th>Mã đơn</th>
                  <th>Ngày tổ chức</th>
                  <th>Không gian</th>
                  <th>Số khách</th>
                  <th>Tổng tiền</th>
                  <th>Trạng thái</th>
                </tr>
              </thead>
              <tbody>
                {page.content.map((booking) => (
                  <tr key={booking.id}>
                    <td>{booking.code}</td>
                    <td>
                      {formatDate(booking.eventDate)}
                      <br />
                      <small className="muted">{booking.timeSlotLabel}</small>
                    </td>
                    <td>{booking.spaceName}</td>
                    <td>
                      {booking.guestCount} khách
                      <br />
                      <small className="muted">{booking.tableCount} mâm</small>
                    </td>
                    <td>{formatCurrency(booking.totalAmount)}</td>
                    <td>
                      <span className={`tag tag-${booking.status}`}>{booking.statusLabel}</span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </section>
  );
}
