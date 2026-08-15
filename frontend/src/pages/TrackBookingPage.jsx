import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { bookingApi } from '@/api/endpoints';
import { formatCurrency, formatDate } from '@/utils/format';
import { ErrorBlock } from '@/components/common/StateBlock';

// Tra cứu đơn bằng mã, dành cho khách không có tài khoản
export default function TrackBookingPage() {
  const [searchParams] = useSearchParams();
  const [code, setCode] = useState(searchParams.get('code') || '');
  const [booking, setBooking] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  const search = async (value) => {
    if (!value.trim()) return;
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
          <div className="eyebrow center">Tra cứu</div>
          <h2>Kiểm tra đơn đặt tiệc</h2>
          <p className="muted">Nhập mã đơn dạng VS-20260815-0001 đã nhận khi gửi yêu cầu.</p>
        </div>

        <form
          onSubmit={(e) => {
            e.preventDefault();
            search(code);
          }}
          style={{ display: 'flex', gap: 12, marginBottom: 26 }}
        >
          <input
            aria-label="Mã đơn"
            value={code}
            placeholder="VS-20260815-0001"
            onChange={(e) => setCode(e.target.value)}
            style={{ flex: 1, padding: '12px 14px', border: '1px solid var(--line)', borderRadius: 10, font: 'inherit' }}
          />
          <button type="submit" className="btn btn-dark" disabled={loading}>
            {loading ? 'Đang tìm…' : 'Tra cứu'}
          </button>
        </form>

        {error && <ErrorBlock message={error} />}

        {booking && (
          <div className="card">
            <div className="card-body">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
                <h3>{booking.code}</h3>
                <span className={`tag tag-${booking.status}`}>{booking.statusLabel}</span>
              </div>

              <table>
                <tbody>
                  <tr>
                    <th>Loại hình</th>
                    <td>{booking.eventTypeLabel}</td>
                  </tr>
                  <tr>
                    <th>Ngày & buổi</th>
                    <td>
                      {formatDate(booking.eventDate)} · {booking.timeSlotLabel}
                    </td>
                  </tr>
                  <tr>
                    <th>Số khách</th>
                    <td>
                      {booking.guestCount} khách ({booking.tableCount} mâm)
                    </td>
                  </tr>
                  <tr>
                    <th>Không gian</th>
                    <td>{booking.spaceName}</td>
                  </tr>
                  <tr>
                    <th>Gói tiệc</th>
                    <td>{booking.packageName}</td>
                  </tr>
                  <tr>
                    <th>Tiền ăn</th>
                    <td>{formatCurrency(booking.foodAmount)}</td>
                  </tr>
                  <tr>
                    <th>Thuê không gian</th>
                    <td>{Number(booking.spaceFee) === 0 ? 'Miễn phí' : formatCurrency(booking.spaceFee)}</td>
                  </tr>
                  {Number(booking.discountAmount) > 0 && (
                    <tr>
                      <th>Giảm giá</th>
                      <td>− {formatCurrency(booking.discountAmount)}</td>
                    </tr>
                  )}
                  <tr>
                    <th>VAT</th>
                    <td>{formatCurrency(booking.vatAmount)}</td>
                  </tr>
                  <tr>
                    <th>Tổng cộng</th>
                    <td>
                      <strong>{formatCurrency(booking.totalAmount)}</strong>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>
    </section>
  );
}
