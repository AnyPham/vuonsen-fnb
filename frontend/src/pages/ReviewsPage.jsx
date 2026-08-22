import { useEffect, useState } from 'react';
import { reviewApi } from '@/api/endpoints';
import { formatDate } from '@/utils/format';
import { Empty, ErrorBlock, Loading } from '@/components/common/StateBlock';

// Vẽ số sao từ điểm đánh giá
function Stars({ value }) {
  return (
    <span style={{ color: 'var(--gold)', letterSpacing: 2 }}>
      {'★'.repeat(value)}
      <span style={{ opacity: 0.3 }}>{'★'.repeat(5 - value)}</span>
    </span>
  );
}

// Trang đánh giá: xem nhận xét đã duyệt và gửi nhận xét mới
export default function ReviewsPage() {
  const [reviews, setReviews] = useState([]);
  const [average, setAverage] = useState(0);
  const [loading, setLoading] = useState(true);

  const [form, setForm] = useState({ bookingCode: '', customerName: '', rating: 5, content: '' });
  const [status, setStatus] = useState('idle');
  const [error, setError] = useState(null);

  const load = () => {
    setLoading(true);
    Promise.all([reviewApi.list({ page: 0, size: 20 }), reviewApi.summary()])
      .then(([page, sum]) => {
        setReviews(page.content);
        setAverage(sum.average);
      })
      .catch(() => setReviews([]))
      .finally(() => setLoading(false));
  };

  useEffect(load, []);

  const set = (patch) => {
    setForm((prev) => ({ ...prev, ...patch }));
    setStatus('idle');
    setError(null);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setStatus('sending');
    setError(null);
    try {
      await reviewApi.create({
        bookingCode: form.bookingCode.trim(),
        customerName: form.customerName.trim(),
        rating: Number(form.rating),
        content: form.content.trim(),
      });
      setStatus('sent');
      setForm({ bookingCode: '', customerName: '', rating: 5, content: '' });
    } catch (err) {
      setStatus('failed');
      setError(err.message);
    }
  };

  return (
    <section className="section">
      <div className="wrap">
        <div className="section-head center">
          <div className="eyebrow center">Khách hàng nói gì</div>
          <h2>Đánh giá dịch vụ</h2>
          {average > 0 && (
            <p className="muted">
              Điểm trung bình <strong>{average.toFixed(1)}</strong> / 5 từ {reviews.length} đánh giá
            </p>
          )}
        </div>

        <div className="grid grid-2" style={{ alignItems: 'start' }}>
          <div>
            {loading && <Loading />}
            {!loading && reviews.length === 0 && (
              <Empty label="Chưa có đánh giá nào được duyệt." />
            )}

            {reviews.map((review) => (
              <div key={review.id} className="card" style={{ marginBottom: 16 }}>
                <div className="card-body">
                  <div
                    style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}
                  >
                    <strong>{review.customerName}</strong>
                    <Stars value={review.rating} />
                  </div>
                  <p className="muted" style={{ margin: '10px 0' }}>
                    {review.content}
                  </p>
                  <small className="muted">{formatDate(review.createdAt)}</small>
                </div>
              </div>
            ))}
          </div>

          <aside className="card">
            <div className="card-body">
              <h3 style={{ marginBottom: 6 }}>Gửi đánh giá</h3>
              <p className="muted" style={{ fontSize: '0.88rem', marginBottom: 16 }}>
                Chỉ khách đã tổ chức tiệc mới đánh giá được. Nhập mã đơn nhận khi đặt tiệc.
              </p>

              {error && <ErrorBlock message={error} />}
              {status === 'sent' && (
                <div className="alert alert-success">
                  Đã gửi. Nhận xét sẽ hiển thị sau khi nhà hàng duyệt.
                </div>
              )}

              <form onSubmit={handleSubmit}>
                <div className="fgroup">
                  <label htmlFor="bookingCode">Mã đơn *</label>
                  <input
                    id="bookingCode"
                    required
                    placeholder="VS-20260815-0001"
                    value={form.bookingCode}
                    onChange={(e) => set({ bookingCode: e.target.value })}
                  />
                </div>

                <div className="fgroup">
                  <label htmlFor="customerName">Tên hiển thị *</label>
                  <input
                    id="customerName"
                    required
                    value={form.customerName}
                    onChange={(e) => set({ customerName: e.target.value })}
                  />
                </div>

                <div className="fgroup">
                  <label htmlFor="rating">Số sao *</label>
                  <select
                    id="rating"
                    value={form.rating}
                    onChange={(e) => set({ rating: e.target.value })}
                  >
                    {[5, 4, 3, 2, 1].map((n) => (
                      <option key={n} value={n}>
                        {n} sao
                      </option>
                    ))}
                  </select>
                </div>

                <div className="fgroup">
                  <label htmlFor="content">Nhận xét *</label>
                  <textarea
                    id="content"
                    required
                    value={form.content}
                    placeholder="Món ăn, không gian, thái độ phục vụ…"
                    onChange={(e) => set({ content: e.target.value })}
                  />
                </div>

                <button
                  type="submit"
                  className="btn btn-dark"
                  style={{ width: '100%' }}
                  disabled={status === 'sending'}
                >
                  {status === 'sending' ? 'Đang gửi…' : 'Gửi đánh giá'}
                </button>
              </form>
            </div>
          </aside>
        </div>
      </div>
    </section>
  );
}
