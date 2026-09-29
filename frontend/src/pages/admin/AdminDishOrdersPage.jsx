import { useCallback, useEffect, useState } from 'react';
import { adminApi } from '@/api/endpoints';
import { formatCurrency, formatDateTime } from '@/utils/format';
import { Empty, ErrorBlock, Loading } from '@/components/common/StateBlock';
import Pagination from '@/components/common/Pagination';

const STATUS_OPTIONS = [
  { value: '', label: 'Tất cả trạng thái' },
  { value: 'PENDING', label: 'Chờ xác nhận' },
  { value: 'CONFIRMED', label: 'Đã xác nhận' },
  { value: 'COMPLETED', label: 'Đã hoàn thành' },
  { value: 'CANCELLED', label: 'Đã hủy' },
];

const TYPE_OPTIONS = [
  { value: '', label: 'Cả hai hình thức' },
  { value: 'DELIVERY', label: 'Giao tận nhà' },
  { value: 'DINE_IN', label: 'Ăn tại chỗ' },
];

// Các bước chuyển trạng thái hợp lệ, phải khớp với DishOrderStatus bên backend
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

// Bếp cần nhìn thấy món ngay trên bảng, không phải mở từng đơn ra xem
function DanhSachMon({ items }) {
  return (
    <ul style={{ margin: 0, paddingLeft: 16, fontSize: '0.86rem' }}>
      {items.map((item, i) => (
        <li key={`${item.dishName}-${i}`}>
          {item.dishName} <span className="muted">× {item.quantity}</span>
        </li>
      ))}
    </ul>
  );
}

export default function AdminDishOrdersPage() {
  const [filters, setFilters] = useState({ status: '', type: '', keyword: '' });
  const [page, setPage] = useState(null);
  const [stats, setStats] = useState(null);
  const [trang, setTrang] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Đổi bộ lọc thì quay về trang đầu, không thì đang ở trang 2 mà lọc còn ít dòng sẽ ra bảng trống
  const doiLoc = (patch) => {
    setFilters((truoc) => ({ ...truoc, ...patch }));
    setTrang(0);
  };

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const params = { page: trang, size: 20 };
      if (filters.status) params.status = filters.status;
      if (filters.type) params.type = filters.type;
      if (filters.keyword) params.keyword = filters.keyword;

      const [orders, statistics] = await Promise.all([
        adminApi.dishOrders(params),
        adminApi.dishOrderStats(),
      ]);
      setPage(orders);
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

  const changeStatus = async (order, status, label) => {
    if (!window.confirm(`${label} đơn ${order.code}?`)) return;
    try {
      await adminApi.changeDishOrderStatus(order.id, status);
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
          <h2>Đơn đặt món</h2>
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
              <label htmlFor="d-status">Trạng thái</label>
              <select
                id="d-status"
                data-test="admin-dish-filter-status"
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
              <label htmlFor="d-type">Hình thức nhận</label>
              <select
                id="d-type"
                data-test="admin-dish-filter-type"
                value={filters.type}
                onChange={(e) => doiLoc({ type: e.target.value })}
              >
                {TYPE_OPTIONS.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
            </div>

            <div className="fgroup" style={{ marginBottom: 0 }}>
              <label htmlFor="d-keyword">Tìm theo mã / tên / SĐT</label>
              <input
                id="d-keyword"
                data-test="admin-dish-filter-keyword"
                value={filters.keyword}
                placeholder="DM-2026… hoặc Nguyễn Văn A"
                onChange={(e) => doiLoc({ keyword: e.target.value })}
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
                  <th>Nhận món</th>
                  <th>Món đã đặt</th>
                  <th>Tổng tiền</th>
                  <th>Trạng thái</th>
                  <th>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {page.content.map((order) => (
                  <tr key={order.id} data-test="admin-dish-order-row">
                    <td data-test="admin-dish-order-code">{order.code}</td>
                    <td>
                      {order.customerName}
                      <br />
                      <small className="muted">{order.customerPhone}</small>
                    </td>
                    <td>
                      {order.fulfillmentLabel}
                      <br />
                      <small className="muted">{formatDateTime(order.serveAt)}</small>
                      {order.deliveryAddress && (
                        <>
                          <br />
                          <small className="muted">{order.deliveryAddress}</small>
                        </>
                      )}
                      {order.guestCount != null && (
                        <>
                          <br />
                          <small className="muted">{order.guestCount} khách</small>
                        </>
                      )}
                    </td>
                    <td>
                      <DanhSachMon items={order.items} />
                    </td>
                    <td>{formatCurrency(order.total)}</td>
                    <td>
                      <span className={`tag tag-${order.status}`} data-test="admin-dish-order-status">
                        {order.statusLabel}
                      </span>
                    </td>
                    <td>
                      <div style={{ display: 'flex', gap: 6 }}>
                        {NEXT_ACTIONS[order.status].map((action) => (
                          <button
                            key={action.status}
                            type="button"
                            className={`btn btn-sm ${action.className}`}
                            onClick={() => changeStatus(order, action.status, action.label)}
                            data-test={`dish-action-${action.status}`}
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

        <Pagination trang={page} doiTrang={setTrang} donVi="đơn đặt món" />
      </div>
    </section>
  );
}
