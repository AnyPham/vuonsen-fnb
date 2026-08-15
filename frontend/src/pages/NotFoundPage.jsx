import { Link } from 'react-router-dom';

export default function NotFoundPage() {
  return (
    <section className="section">
      <div className="wrap state">
        <h2>Không tìm thấy trang</h2>
        <p className="muted" style={{ marginBottom: 22 }}>
          Đường dẫn bạn truy cập không tồn tại hoặc đã được chuyển đi.
        </p>
        <Link to="/" className="btn btn-dark">
          Về trang chủ
        </Link>
      </div>
    </section>
  );
}
