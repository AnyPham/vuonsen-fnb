import { useCallback, useEffect, useState } from 'react';
import { adminApi } from '@/api/endpoints';
import { formatDate } from '@/utils/format';
import { Empty, ErrorBlock, Loading } from '@/components/common/StateBlock';
import Pagination from '@/components/common/Pagination';

// Màn hình duyệt đánh giá của khách trước khi cho hiển thị lên website
export default function AdminReviewsPage() {
  const [approved, setApproved] = useState(false);
  const [page, setPage] = useState(null);
  const [trang, setTrang] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setPage(await adminApi.reviews({ approved, page: trang, size: 20 }));
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [approved, trang]);

  useEffect(() => {
    load();
  }, [load]);

  const approve = async (review) => {
    try {
      await adminApi.approveReview(review.id);
      await load();
    } catch (err) {
      setError(err.message);
    }
  };

  const reject = async (review) => {
    if (!window.confirm(`Xóa đánh giá của ${review.customerName}?`)) return;
    try {
      await adminApi.rejectReview(review.id);
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
          <h2>Duyệt đánh giá</h2>
        </div>

        <div style={{ display: 'flex', gap: 10, marginBottom: 22 }}>
          <button
            type="button"
            className={`btn btn-sm ${!approved ? 'btn-dark' : 'btn-outline'}`}
            onClick={() => setApproved(false)}
            data-test="tab-pending"
          >
            Chờ duyệt
          </button>
          <button
            type="button"
            className={`btn btn-sm ${approved ? 'btn-dark' : 'btn-outline'}`}
            onClick={() => setApproved(true)}
            data-test="tab-approved"
          >
            Đã duyệt
          </button>
        </div>

        {error && <ErrorBlock message={error} />}
        {loading && <Loading />}
        {page && page.content.length === 0 && !loading && (
          <Empty label={approved ? 'Chưa có đánh giá nào được duyệt.' : 'Không có đánh giá nào chờ duyệt.'} />
        )}

        {page && page.content.length > 0 && (
          <div className="table-wrap card">
            <table>
              <thead>
                <tr>
                  <th>Khách hàng</th>
                  <th>Mã đơn</th>
                  <th>Sao</th>
                  <th>Nội dung</th>
                  <th>Ngày gửi</th>
                  <th>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {page.content.map((review) => (
                  <tr key={review.id} data-test="admin-review-row">
                    <td>{review.customerName}</td>
                    <td>{review.bookingCode || '—'}</td>
                    <td>{review.rating}/5</td>
                    <td style={{ maxWidth: 320 }} data-test="admin-review-content">{review.content}</td>
                    <td>{formatDate(review.createdAt)}</td>
                    <td>
                      <div style={{ display: 'flex', gap: 6 }}>
                        {!review.approved && (
                          <button
                            type="button"
                            className="btn btn-sm btn-dark"
                            onClick={() => approve(review)}
                            data-test="action-approve"
                          >
                            Duyệt
                          </button>
                        )}
                        <button
                          type="button"
                          className="btn btn-sm btn-ghost"
                          onClick={() => reject(review)}
                        >
                          Xóa
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        <Pagination trang={page} doiTrang={setTrang} donVi="đánh giá" />
      </div>
    </section>
  );
}
