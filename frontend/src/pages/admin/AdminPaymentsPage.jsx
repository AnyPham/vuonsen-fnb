import { useCallback, useEffect, useState } from 'react';
import { adminApi } from '@/api/endpoints';
import { formatCurrency, formatDateTime } from '@/utils/format';
import { Empty, ErrorBlock, Loading } from '@/components/common/StateBlock';
import Pagination from '@/components/common/Pagination';

const STATUS_OPTIONS = [
  { value: '', label: 'Tất cả trạng thái' },
  { value: 'PENDING', label: 'Chờ đối soát' },
  { value: 'CONFIRMED', label: 'Đã nhận tiền' },
  { value: 'CANCELLED', label: 'Đã hủy' },
];

/*
 * Sổ thanh toán và màn hình đối soát.
 *
 * Việc hằng ngày của kế toán: mở sao kê ngân hàng, thấy khoản nào về thì tìm phiếu tương
 * ứng ở đây rồi bấm xác nhận. Nội dung chuyển khoản chính là mã đơn nên tìm bằng ô tìm
 * kiếm là ra, không phải dò tay.
 */
export default function AdminPaymentsPage() {
  const [filters, setFilters] = useState({ status: 'PENDING', keyword: '' });
  const [trang, setTrang] = useState(0);
  const [page, setPage] = useState(null);
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Ô ghi nhận khoản thu tại quầy
  const [moGhiThu, setMoGhiThu] = useState(false);
  const [formThu, setFormThu] = useState({
    orderCode: '', amount: '', purpose: 'DEPOSIT', method: 'CASH', note: '',
  });

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
      if (filters.keyword) params.keyword = filters.keyword;

      const [ds, tk] = await Promise.all([adminApi.payments(params), adminApi.paymentStats()]);
      setPage(ds);
      setStats(tk);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [filters, trang]);

  useEffect(() => {
    load();
  }, [load]);

  const xacNhan = async (phieu) => {
    const maGiaoDich = window.prompt(
      `Xác nhận đã nhận ${formatCurrency(phieu.amount)} của đơn ${phieu.orderCode}?\n`
      + 'Nhập mã giao dịch trên sao kê nếu có, để trống cũng được:',
      phieu.reference || '',
    );
    if (maGiaoDich === null) return;
    try {
      await adminApi.confirmPayment(phieu.id, maGiaoDich || undefined);
      await load();
    } catch (err) {
      setError(err.message);
    }
  };

  const huy = async (phieu) => {
    const lyDo = window.prompt(`Hủy phiếu ${phieu.code}? Nhập lý do:`, 'Khách báo không chuyển nữa');
    if (lyDo === null) return;
    try {
      await adminApi.cancelPayment(phieu.id, lyDo);
      await load();
    } catch (err) {
      setError(err.message);
    }
  };

  const ghiThu = async (event) => {
    event.preventDefault();
    try {
      await adminApi.recordCashPayment({ ...formThu, amount: Number(formThu.amount) });
      setMoGhiThu(false);
      setFormThu({ orderCode: '', amount: '', purpose: 'DEPOSIT', method: 'CASH', note: '' });
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
          <h2>Sổ thanh toán</h2>
        </div>

        {stats && (
          <div className="grid grid-3" style={{ marginBottom: 24 }}>
            {STATUS_OPTIONS.slice(1).map((o) => (
              <div key={o.value} className="card">
                <div className="card-body">
                  <div className="muted" style={{ fontSize: '0.85rem' }}>{o.label}</div>
                  <div style={{ fontFamily: 'var(--serif)', fontSize: '2rem', color: 'var(--green-800)' }}>
                    {stats[o.value] ?? 0}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}

        <div className="card" style={{ marginBottom: 24 }}>
          <div className="card-body form-row">
            <div className="fgroup" style={{ marginBottom: 0 }}>
              <label htmlFor="p-status">Trạng thái</label>
              <select
                id="p-status"
                data-test="payment-filter-status"
                value={filters.status}
                onChange={(e) => doiLoc({ status: e.target.value })}
              >
                {STATUS_OPTIONS.map((o) => (
                  <option key={o.value} value={o.value}>{o.label}</option>
                ))}
              </select>
            </div>

            <div className="fgroup" style={{ marginBottom: 0 }}>
              <label htmlFor="p-keyword">Tìm theo mã phiếu, mã đơn hoặc nội dung</label>
              <input
                id="p-keyword"
                data-test="payment-filter-keyword"
                value={filters.keyword}
                placeholder="VS-2026… hoặc TT-B-2026…"
                onChange={(e) => doiLoc({ keyword: e.target.value })}
              />
            </div>

            <div style={{ display: 'flex', alignItems: 'flex-end' }}>
              <button
                type="button"
                className="btn btn-outline btn-sm"
                onClick={() => setMoGhiThu((v) => !v)}
                data-test="open-cash-form"
              >
                Ghi khoản thu tại quầy
              </button>
            </div>
          </div>
        </div>

        {moGhiThu && (
          <form className="card" style={{ marginBottom: 24 }} onSubmit={ghiThu} data-test="cash-form">
            <div className="card-body">
              <h3 style={{ marginBottom: 4 }}>Ghi khoản vừa thu</h3>
              <p className="muted" style={{ fontSize: '0.88rem', marginBottom: 16 }}>
                Dùng khi khách trả tiền mặt tại nhà hàng. Phiếu vào sổ ở trạng thái đã nhận tiền
                luôn, vì tiền đã nằm trong két nên không có gì phải đối soát.
              </p>

              <div className="form-row">
                <div className="fgroup" style={{ marginBottom: 0 }}>
                  <label htmlFor="t-ma">Mã đơn</label>
                  <input
                    id="t-ma"
                    data-test="cash-order-code"
                    required
                    value={formThu.orderCode}
                    placeholder="VS-20260927-0001"
                    onChange={(e) => setFormThu({ ...formThu, orderCode: e.target.value })}
                  />
                </div>
                <div className="fgroup" style={{ marginBottom: 0 }}>
                  <label htmlFor="t-tien">Số tiền</label>
                  <input
                    id="t-tien"
                    data-test="cash-amount"
                    type="number"
                    min="1000"
                    step="1"
                    required
                    value={formThu.amount}
                    onChange={(e) => setFormThu({ ...formThu, amount: e.target.value })}
                  />
                </div>
                <div className="fgroup" style={{ marginBottom: 0 }}>
                  <label htmlFor="t-khoan">Khoản</label>
                  <select
                    id="t-khoan"
                    value={formThu.purpose}
                    onChange={(e) => setFormThu({ ...formThu, purpose: e.target.value })}
                  >
                    <option value="DEPOSIT">Tiền cọc giữ ngày</option>
                    <option value="BALANCE">Phần còn lại</option>
                    <option value="FULL">Toàn bộ đơn</option>
                  </select>
                </div>
                <div className="fgroup" style={{ marginBottom: 0 }}>
                  <label htmlFor="t-hinhthuc">Hình thức</label>
                  <select
                    id="t-hinhthuc"
                    value={formThu.method}
                    onChange={(e) => setFormThu({ ...formThu, method: e.target.value })}
                  >
                    <option value="CASH">Tiền mặt</option>
                    <option value="TRANSFER">Chuyển khoản</option>
                  </select>
                </div>
              </div>

              <div style={{ display: 'flex', gap: 10, marginTop: 18 }}>
                <button type="submit" className="btn btn-dark btn-sm" data-test="cash-submit">
                  Ghi vào sổ
                </button>
                <button type="button" className="btn btn-ghost btn-sm" onClick={() => setMoGhiThu(false)}>
                  Bỏ qua
                </button>
              </div>
            </div>
          </form>
        )}

        {error && <ErrorBlock message={error} />}
        {loading && <Loading />}
        {page && page.content.length === 0 && !loading && (
          <Empty label="Không có phiếu nào khớp bộ lọc." />
        )}

        {page && page.content.length > 0 && (
          <div className="table-wrap card">
            <table>
              <thead>
                <tr>
                  <th>Mã phiếu</th>
                  <th>Đơn</th>
                  <th>Khoản</th>
                  <th>Số tiền</th>
                  <th>Hình thức</th>
                  <th>Trạng thái</th>
                  <th>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {page.content.map((p) => (
                  <tr key={p.id} data-test="admin-payment-row">
                    <td data-test="admin-payment-code">
                      {p.code}
                      <br />
                      <small className="muted">{formatDateTime(p.createdAt)}</small>
                    </td>
                    <td>
                      {p.orderCode}
                      <br />
                      <small className="muted">{p.orderTypeLabel}</small>
                    </td>
                    <td>{p.purposeLabel}</td>
                    <td>{formatCurrency(p.amount)}</td>
                    <td>
                      {p.methodLabel}
                      {p.reference && (
                        <>
                          <br />
                          <small className="muted">{p.reference}</small>
                        </>
                      )}
                    </td>
                    <td>
                      <span
                        className={`tag tag-${p.status === 'CONFIRMED' ? 'COMPLETED' : p.status === 'CANCELLED' ? 'CANCELLED' : 'PENDING'}`}
                        data-test="admin-payment-status"
                      >
                        {p.statusLabel}
                      </span>
                      {p.confirmedBy && (
                        <>
                          <br />
                          <small className="muted">{p.confirmedBy}</small>
                        </>
                      )}
                    </td>
                    <td>
                      {p.status === 'PENDING' && (
                        <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
                          <button
                            type="button"
                            className="btn btn-sm btn-dark"
                            onClick={() => xacNhan(p)}
                            data-test="payment-confirm"
                          >
                            Đã nhận tiền
                          </button>
                          <button
                            type="button"
                            className="btn btn-sm btn-ghost"
                            onClick={() => huy(p)}
                            data-test="payment-cancel"
                          >
                            Hủy
                          </button>
                        </div>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        <Pagination trang={page} doiTrang={setTrang} donVi="phiếu thu" />
      </div>
    </section>
  );
}
