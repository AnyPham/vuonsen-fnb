import { useCallback, useEffect, useState } from 'react';
import { adminApi } from '@/api/endpoints';
import { formatCurrency, formatDate } from '@/utils/format';
import { Empty, ErrorBlock, Loading } from '@/components/common/StateBlock';

const STATUS_OPTIONS = [
  { value: '', label: 'Tất cả trạng thái' },
  { value: 'PENDING', label: 'Chờ xác nhận' },
  { value: 'CONFIRMED', label: 'Đã xác nhận' },
  { value: 'COMPLETED', label: 'Đã hoàn thành' },
  { value: 'CANCELLED', label: 'Đã hủy' },
];

// Các bước chuyển trạng thái hợp lệ, phải khớp với BookingStatus bên backend
const NEXT_ACTIONS = {
  PENDING: [
    { status: 'CONFIRMED', label: 'Xác nhận', className: 'btn-dark' },
    { status: 'CANCELLED', label: 'Hủy', className: 'btn-ghost' },
  ],
  CONFIRMED: [
    { status: 'COMPLETED', label: 'Hoàn thành', className: 'btn-dark' },
    { status: 'CANCELLED', label: 'Hủy', className: 'btn-ghost' },
  ],
  COMPLETED: [],
  CANCELLED: [],
};

export default function AdminBookingsPage() {
  const [filters, setFilters] = useState({ status: '', keyword: '' });
  const [page, setPage] = useState(null);
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const params = { page: 0, size: 30 };
      if (filters.status) params.status = filters.status;
      if (filters.keyword) params.keyword = filters.keyword;

      const [bookings, statistics] = await Promise.all([
        adminApi.bookings(params),
        adminApi.statistics(),
      ]);
      setPage(bookings);
      setStats(statistics);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [filters]);

  useEffect(() => {
    load();
  }, [load]);

  const changeStatus = async (booking, status, label) => {
    if (!window.confirm(`${label} đơn ${booking.code}?`)) return;
    try {
      await adminApi.changeStatus(booking.id, { status, note: `${label} bởi quản trị` });
      await load();
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <section className="section">
      <div className="wrap">
        <div className="section-head">
          <div className="eyebrow">Quản trị</div>
          <h2>Đơn đặt tiệc</h2>
        </div>

        {stats && (
          <div className="grid grid-3" style={{ marginBottom: 30 }}>
            {STATUS_OPTIONS.slice(1).map((option) => (
              <div key={option.value} className="card">
                <div className="card-body">
                  <div className="muted" style={{ fontSize: '0.85rem' }}>
                    {option.label}
                  </div>
                  <div style={{ fontFamily: 'var(--serif)', fontSize: '2rem', color: 'var(--green-800)' }}>
                    {stats[option.value] ?? 0}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}

        <div className="card" style={{ marginBottom: 24 }}>
          <div className="card-body form-row">
            <div className="fgroup" style={{ marginBottom: 0 }}>
              <label htmlFor="a-status">Trạng thái</label>
              <select
                id="a-status"
                value={filters.status}
                onChange={(e) => setFilters({ ...filters, status: e.target.value })}
              >
                {STATUS_OPTIONS.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
            </div>

            <div className="fgroup" style={{ marginBottom: 0 }}>
              <label htmlFor="a-keyword">Tìm theo mã / tên / SĐT</label>
              <input
                id="a-keyword"
                value={filters.keyword}
                placeholder="VS-2026… hoặc Nguyễn Văn A"
                onChange={(e) => setFilters({ ...filters, keyword: e.target.value })}
              />
            </div>
          </div>
        </div>

        {error && <ErrorBlock message={error} />}
        {loading && <Loading />}
        {page && page.content.length === 0 && !loading && <Empty label="Không có đơn nào khớp bộ lọc." />}

        {page && page.content.length > 0 && (
          <div className="table-wrap card">
            <table>
              <thead>
                <tr>
                  <th>Mã đơn</th>
                  <th>Khách hàng</th>
                  <th>Sự kiện</th>
                  <th>Không gian / Gói</th>
                  <th>Tổng tiền</th>
                  <th>Trạng thái</th>
                  <th>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {page.content.map((booking) => (
                  <tr key={booking.id}>
                    <td>{booking.code}</td>
                    <td>
                      {booking.customerName}
                      <br />
                      <small className="muted">{booking.customerPhone}</small>
                    </td>
                    <td>
                      {booking.eventTypeLabel}
                      <br />
                      <small className="muted">
                        {formatDate(booking.eventDate)} · {booking.guestCount} khách
                      </small>
                    </td>
                    <td>
                      {booking.spaceName}
                      <br />
                      <small className="muted">{booking.packageName}</small>
                    </td>
                    <td>{formatCurrency(booking.totalAmount)}</td>
                    <td>
                      <span className={`tag tag-${booking.status}`}>{booking.statusLabel}</span>
                    </td>
                    <td>
                      <div style={{ display: 'flex', gap: 6 }}>
                        {NEXT_ACTIONS[booking.status].map((action) => (
                          <button
                            key={action.status}
                            type="button"
                            className={`btn btn-sm ${action.className}`}
                            onClick={() => changeStatus(booking, action.status, action.label)}
                          >
                            {action.label}
                          </button>
                        ))}
                      </div>
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
