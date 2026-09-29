import { useEffect, useState } from 'react';
import { reviewApi } from '@/api/endpoints';
import { useI18n } from '@/i18n';
import { useDinhDang } from '@/i18n/dinhDang';
import { Empty, ErrorBlock, Loading } from '@/components/common/StateBlock';
import Pagination from '@/components/common/Pagination';
import { LuoiAnh } from '@/components/common/Lightbox';

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
  const [phanTrang, setPhanTrang] = useState(null);
  const [trang, setTrang] = useState(0);
  const [average, setAverage] = useState(0);
  const [loading, setLoading] = useState(true);

  const [form, setForm] = useState({ bookingCode: '', customerName: '', rating: 5, content: '' });
  const [status, setStatus] = useState('idle');
  const [error, setError] = useState(null);
  const { t } = useI18n();
  const dd = useDinhDang();

  const load = () => {
    setLoading(true);
    Promise.all([reviewApi.list({ page: trang, size: 10 }), reviewApi.summary()])
      .then(([page, sum]) => {
        setReviews(page.content);
        setPhanTrang(page);
        setAverage(sum.average);
      })
      .catch(() => setReviews([]))
      .finally(() => setLoading(false));
  };

  useEffect(load, [trang]);

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
          <div className="eyebrow center">{t('home.reviewsEyebrow')}</div>
          <h2>{t('reviews.title')}</h2>
          {average > 0 && (
            <p className="muted">
              {t('reviews.average')} <strong>{average.toFixed(1)}</strong>{' '}
              {t('reviews.averageSuffix', { n: reviews.length })}
            </p>
          )}
        </div>

        <div className="grid grid-2" style={{ alignItems: 'start' }}>
          <div>
            {loading && <Loading />}
            {!loading && reviews.length === 0 && (
              <Empty label={t('reviews.empty')} />
            )}

            {reviews.map((review) => (
              <div key={review.id} className="card" style={{ marginBottom: 16 }} data-test="review-card">
                <div className="card-body">
                  <div
                    style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}
                  >
                    <strong>{review.customerName}</strong>
                    <Stars value={review.rating} />
                  </div>
                  <p className="muted" style={{ margin: '10px 0' }} data-test="review-text">
                    {review.content}
                  </p>

                  {/* Ảnh khách chụp tại tiệc. Bấm vào phóng to ngay trong trang, không
                      mở đường dẫn gốc ra thẻ mới để khỏi lộ đường dẫn Cloudinary. */}
                  {review.images?.length > 0 && (
                    <LuoiAnh
                      className="anh-danh-gia"
                      images={review.images.map((url, i) => ({
                        url,
                        alt: t('reviews.photoAlt', { i: i + 1, ten: review.customerName }),
                      }))}
                    />
                  )}

                  <small className="muted">{dd.ngay(review.createdAt)}</small>
                </div>
              </div>
            ))}

            <Pagination trang={phanTrang} doiTrang={setTrang} donVi={t('reviews.unit')} />
          </div>

          <aside className="card">
            <div className="card-body">
              <h3 style={{ marginBottom: 6 }}>{t('reviews.formTitle')}</h3>
              <p className="muted" style={{ fontSize: '0.88rem', marginBottom: 16 }}>
                {t('reviews.formDesc')}
              </p>

              {error && <ErrorBlock message={error} />}
              {status === 'sent' && (
                <div className="alert alert-success" data-test="review-sent">
                  {t('reviews.sent')}
                </div>
              )}

              <form onSubmit={handleSubmit}>
                <div className="fgroup">
                  <label htmlFor="bookingCode">{t('reviews.bookingCode')}</label>
                  <input
                    id="bookingCode"
                    data-test="review-booking-code"
                    required
                    placeholder="VS-20260815-0001"
                    value={form.bookingCode}
                    onChange={(e) => set({ bookingCode: e.target.value })}
                  />
                </div>

                <div className="fgroup">
                  <label htmlFor="customerName">{t('reviews.displayName')}</label>
                  <input
                    id="customerName"
                    data-test="review-name"
                    required
                    value={form.customerName}
                    onChange={(e) => set({ customerName: e.target.value })}
                  />
                </div>

                <div className="fgroup">
                  <label htmlFor="rating">{t('reviews.rating')}</label>
                  <select
                    id="rating"
                    data-test="review-rating"
                    value={form.rating}
                    onChange={(e) => set({ rating: e.target.value })}
                  >
                    {[5, 4, 3, 2, 1].map((n) => (
                      <option key={n} value={n}>
                        {t('reviews.stars', { n })}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="fgroup">
                  <label htmlFor="content">{t('reviews.content')}</label>
                  <textarea
                    id="content"
                    data-test="review-content"
                    required
                    value={form.content}
                    placeholder={t('reviews.contentPlaceholder')}
                    onChange={(e) => set({ content: e.target.value })}
                  />
                </div>

                <button
                  type="submit"
                  className="btn btn-dark"
                  style={{ width: '100%' }}
                  disabled={status === 'sending'}
                  data-test="submit-review"
                >
                  {status === 'sending' ? t('reviews.sending') : t('reviews.submit')}
                </button>
              </form>
            </div>
          </aside>
        </div>
      </div>
    </section>
  );
}
