import { useSelector } from 'react-redux';
import { Navigate, useLocation } from 'react-router-dom';

// Chặn các trang phải đăng nhập mới vào được.
// roles: danh sách quyền được phép, bỏ trống thì chỉ cần đăng nhập.
export default function ProtectedRoute({ children, roles }) {
  const { user, initialized } = useSelector((state) => state.auth);
  const location = useLocation();

  // Đợi kiểm tra xong phiên đăng nhập rồi mới quyết định
  if (!initialized) {
    return <div className="state">Đang kiểm tra phiên đăng nhập…</div>;
  }

  if (!user) {
    return <Navigate to="/dang-nhap" state={{ from: location.pathname }} replace />;
  }

  if (roles?.length && !roles.includes(user.role)) {
    return (
      <div className="wrap state">
        <h2>Không đủ quyền truy cập</h2>
        <p className="muted">Tài khoản của bạn không được vào trang này.</p>
      </div>
    );
  }

  return children;
}
