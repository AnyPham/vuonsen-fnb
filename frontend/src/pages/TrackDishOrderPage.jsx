import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { dishOrderApi } from '@/api/endpoints';
import { formatCurrency, formatDateTime } from '@/utils/format';
import { ErrorBlock, Loading } from '@/components/common/StateBlock';

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
          <div className="eyebrow">Tra cứu</div>
          <h2>Đơn đặt món</h2>
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
            placeholder="Nhập mã đơn, ví dụ DM-20260905-0001"
            style={{ flex: 1 }}
          />
          <button type="submit" className="btn btn-dark" disabled={loading}>
            Tra cứu
          </button>
        </form>

        {loading && <Loading label="Đang tìm đơn…" />}
        {error && <ErrorBlock message={error} />}

        {don && (
          <div className="card">
            <div className="card-body">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <strong style={{ fontSize: '1.05rem' }}>{don.code}</strong>
                  <div className="muted" style={{ fontSize: '0.86rem' }}>
                    Đặt lúc {formatDateTime(don.createdAt)}
                  </div>
                </div>
                <span className={`tag tag-${don.status}`}>{don.statusLabel}</span>
              </div>

              <table className="bang-thong-so" style={{ marginTop: 16 }}>
                <tbody>
                  <tr>
                    <th>Hình thức</th>
                    <td>{don.fulfillmentLabel}</td>
                  </tr>
                  <tr>
                    <th>Người đặt</th>
                    <td>{don.customerName} — {don.customerPhone}</td>
                  </tr>
                  {don.deliveryAddress && (
                    <tr>
                      <th>Địa chỉ giao</th>
                      <td>{don.deliveryAddress}</td>
                    </tr>
                  )}
                  {don.guestCount != null && (
                    <tr>
                      <th>Số khách</th>
                      <td>{don.guestCount} người</td>
                    </tr>
                  )}
                  <tr>
                    <th>Thời điểm nhận</th>
                    <td>{formatDateTime(don.serveAt)}</td>
                  </tr>
                  {don.note && (
                    <tr>
                      <th>Ghi chú</th>
                      <td>{don.note}</td>
                    </tr>
                  )}
                </tbody>
              </table>

              <h3 style={{ marginTop: 20, marginBottom: 8 }}>Món đã đặt</h3>
              <table className="bang-thong-so">
                <tbody>
                  {don.items.map((d, i) => (
                    <tr key={i}>
                      <th style={{ fontWeight: 400 }}>
                        {d.dishName} × {d.quantity}
                      </th>
                      <td style={{ textAlign: 'right' }}>{formatCurrency(d.lineTotal)}</td>
                    </tr>
                  ))}
                  <tr>
                    <th>Tiền món</th>
                    <td style={{ textAlign: 'right' }}>{formatCurrency(don.subtotal)}</td>
                  </tr>
                  <tr>
                    <th>Phí giao</th>
                    <td style={{ textAlign: 'right' }}>
                      {Number(don.deliveryFee) === 0 ? 'Miễn phí' : formatCurrency(don.deliveryFee)}
                    </td>
                  </tr>
                  <tr>
                    <th>Thuế giá trị gia tăng</th>
                    <td style={{ textAlign: 'right' }}>{formatCurrency(don.vatAmount)}</td>
                  </tr>
                  <tr>
                    <th style={{ fontWeight: 600 }}>Tổng cộng</th>
                    <td style={{ textAlign: 'right', fontWeight: 600 }}>
                      {formatCurrency(don.total)}
                    </td>
                  </tr>
                </tbody>
              </table>

              <p className="muted" style={{ fontSize: '0.84rem', marginTop: 14 }}>
                Số tiền trên là con số đã chốt lúc đặt. Bảng giá đổi về sau không làm
                thay đổi đơn này.
              </p>
            </div>
          </div>
        )}

        <p style={{ marginTop: 18 }}>
          <Link to="/thuc-don">← Về thực đơn</Link>
        </p>
      </div>
    </section>
  );
}
