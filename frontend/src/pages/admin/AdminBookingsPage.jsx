import { useCallback, useEffect, useState } from 'react';
import { adminApi } from '@/api/endpoints';
import { formatCurrency, formatDate } from '@/utils/format';
import { Empty, ErrorBlock, Loading } from '@/components/common/StateBlock';
import Pagination from '@/components/common/Pagination';

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

// Đơn còn thiếu cọc thì làm nổi lên, vì đó là việc nhân viên phải đi đòi
function TinhTrangCoc({ booking }) {
  const phaiDong = Number(booking.depositAmount || 0);
  const daDong = Number(booking.depositPaid || 0);

  if (daDong <= 0) {
    return (
      <>
        <span className="tag tag-PENDING" data-test="deposit-state">Chưa cọc</span>
        <br />
        <small className="muted">cần {formatCurrency(phaiDong)}</small>
      </>
    );
  }

  return (
    <>
      <span className={`tag tag-${daDong >= phaiDong ? 'CONFIRMED' : 'PENDING'}`} data-test="deposit-state">
        {daDong >= phaiDong ? 'Đã cọc' : 'Cọc thiếu'}
      </span>
      <br />
      <small className="muted">
        {formatCurrency(daDong)} / {formatCurrency(phaiDong)}
      </small>
    </>
  );
}

export default function AdminBookingsPage() {
  const [filters, setFilters] = useState({ status: '', keyword: '' });
  const [page, setPage] = useState(null);
  const [stats, setStats] = useState(null);
  const [trang, setTrang] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Đơn đang mở ô ghi nhận cọc, null là không mở ô nào
  const [dangGhiCoc, setDangGhiCoc] = useState(null);

  // Đổi bộ lọc thì quay về trang đầu, không thì đang ở trang 3 mà lọc còn 5 dòng sẽ ra bảng trống
  const doiLoc = (patch) => {
    setFilters((truoc) => ({ ...truoc, ...patch }));
    setTrang(0);
  };
  const [formCoc, setFormCoc] = useState({ amount: '', method: 'TRANSFER' });

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const params = { page: trang, size: 20 };
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
  }, [filters, trang]);

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

  // Mở ô ghi cọc, điền sẵn phần còn thiếu để nhân viên chỉ việc bấm xác nhận
  const moOGhiCoc = (booking) => {
    const conThieu = Number(booking.depositAmount || 0) - Number(booking.depositPaid || 0);
    setDangGhiCoc(booking);
    setFormCoc({ amount: conThieu > 0 ? String(conThieu) : '', method: 'TRANSFER' });
    setError(null);
  };

  const ghiNhanCoc = async (event) => {
    event.preventDefault();
    try {
      await adminApi.recordDeposit(dangGhiCoc.id, {
        amount: Number(formCoc.amount),
        method: formCoc.method,
      });
      setDangGhiCoc(null);
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
                data-test="admin-filter-status"
                value={filters.status}
                onChange={(e) => doiLoc({ status: e.target.value })}
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
                data-test="admin-filter-keyword"
                value={filters.keyword}
                placeholder="VS-2026… hoặc Nguyễn Văn A"
                onChange={(e) => doiLoc({ keyword: e.target.value })}
              />
            </div>
          </div>
        </div>

        {dangGhiCoc && (
          <form className="card" style={{ marginBottom: 24 }} onSubmit={ghiNhanCoc} data-test="deposit-form">
            <div className="card-body">
              <h3 style={{ marginBottom: 4 }}>Ghi nhận cọc cho đơn {dangGhiCoc.code}</h3>
              <p className="muted" style={{ fontSize: '0.88rem', marginBottom: 16 }}>
                {dangGhiCoc.customerName} · tổng đơn {formatCurrency(dangGhiCoc.totalAmount)} · cần cọc{' '}
                {formatCurrency(dangGhiCoc.depositAmount)}
                {Number(dangGhiCoc.depositPaid) > 0 && ` · đã thu ${formatCurrency(dangGhiCoc.depositPaid)}`}
              </p>

              <div className="form-row">
                {/* Bước nhảy phải là 1: tiền cọc bằng 30% tổng đơn nên hiếm khi tròn nghìn,
                    để bước 1000 thì trình duyệt coi số lẻ là không hợp lệ và chặn gửi mà
                    không báo gì, bấm nút như không có chuyện gì xảy ra. */}
                <div className="fgroup" style={{ marginBottom: 0 }}>
                  <label htmlFor="coc-amount">Số tiền vừa nhận</label>
                  <input
                    id="coc-amount"
                    data-test="deposit-amount"
                    type="number"
                    min="1000"
                    step="1"
                    required
                    value={formCoc.amount}
                    onChange={(e) => setFormCoc({ ...formCoc, amount: e.target.value })}
                  />
                </div>

                <div className="fgroup" style={{ marginBottom: 0 }}>
                  <label htmlFor="coc-method">Hình thức</label>
                  <select
                    id="coc-method"
                    data-test="deposit-method"
                    value={formCoc.method}
                    onChange={(e) => setFormCoc({ ...formCoc, method: e.target.value })}
                  >
                    <option value="TRANSFER">Chuyển khoản</option>
                    <option value="CASH">Tiền mặt tại quầy</option>
                  </select>
                </div>
              </div>

              <div style={{ display: 'flex', gap: 10, marginTop: 18 }}>
                <button type="submit" className="btn btn-dark btn-sm" data-test="deposit-submit">
                  Lưu khoản cọc
                </button>
                <button type="button" className="btn btn-ghost btn-sm" onClick={() => setDangGhiCoc(null)}>
                  Bỏ qua
                </button>
              </div>
            </div>
          </form>
        )}

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
                  <th>Cọc</th>
                  <th>Trạng thái</th>
                  <th>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {page.content.map((booking) => (
                  <tr key={booking.id} data-test="admin-booking-row">
                    <td data-test="admin-booking-code">{booking.code}</td>
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
                      <TinhTrangCoc booking={booking} />
                    </td>
                    <td>
                      <span className={`tag tag-${booking.status}`} data-test="admin-booking-status">{booking.statusLabel}</span>
                    </td>
                    <td>
                      <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
                        {booking.status !== 'CANCELLED'
                          && Number(booking.depositPaid || 0) < Number(booking.depositAmount || 0) && (
                          <button
                            type="button"
                            className="btn btn-sm btn-outline"
                            onClick={() => moOGhiCoc(booking)}
                            data-test="action-deposit"
                          >
                            Ghi cọc
                          </button>
                        )}
                        {NEXT_ACTIONS[booking.status].map((action) => (
                          <button
                            key={action.status}
                            type="button"
                            className={`btn btn-sm ${action.className}`}
                            onClick={() => changeStatus(booking, action.status, action.label)}
                            data-test={`action-${action.status}`}
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

        <Pagination trang={page} doiTrang={setTrang} donVi="đơn đặt tiệc" />
      </div>
    </section>
  );
}
