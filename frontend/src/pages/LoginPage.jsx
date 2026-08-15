import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { clearError, login, selectAuthStatus, selectUser } from '@/features/auth/authSlice';
import { ErrorBlock } from '@/components/common/StateBlock';

export default function LoginPage() {
  const [form, setForm] = useState({ email: '', password: '' });
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const location = useLocation();
  const user = useSelector(selectUser);
  const status = useSelector(selectAuthStatus);
  const error = useSelector((state) => state.auth.error);

  useEffect(() => {
    dispatch(clearError());
  }, [dispatch]);

  // Đăng nhập xong thì quay lại đúng trang người dùng định vào
  useEffect(() => {
    if (user) navigate(location.state?.from || '/', { replace: true });
  }, [user, navigate, location.state]);

  const handleSubmit = (event) => {
    event.preventDefault();
    dispatch(login(form));
  };

  return (
    <section className="section">
      <div className="wrap" style={{ maxWidth: 440 }}>
        <div className="card">
          <div className="card-body">
            <h2 style={{ marginBottom: 20 }}>Đăng nhập</h2>

            {error && <ErrorBlock message={error} />}

            <form onSubmit={handleSubmit}>
              <div className="fgroup">
                <label htmlFor="email">Email</label>
                <input
                  id="email"
                  type="email"
                  required
                  value={form.email}
                  onChange={(e) => setForm({ ...form, email: e.target.value })}
                />
              </div>

              <div className="fgroup">
                <label htmlFor="password">Mật khẩu</label>
                <input
                  id="password"
                  type="password"
                  required
                  value={form.password}
                  onChange={(e) => setForm({ ...form, password: e.target.value })}
                />
              </div>

              <button type="submit" className="btn btn-dark" style={{ width: '100%' }} disabled={status === 'loading'}>
                {status === 'loading' ? 'Đang xử lý…' : 'Đăng nhập'}
              </button>
            </form>

            <p className="muted center" style={{ marginTop: 18, fontSize: '0.9rem' }}>
              Chưa có tài khoản? <Link to="/dang-ky">Đăng ký</Link>
            </p>
          </div>
        </div>
      </div>
    </section>
  );
}
