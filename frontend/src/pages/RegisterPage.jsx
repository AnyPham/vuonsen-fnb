import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Link, useNavigate } from 'react-router-dom';
import { clearError, register, selectAuthStatus, selectUser } from '@/features/auth/authSlice';
import { ErrorBlock } from '@/components/common/StateBlock';

export default function RegisterPage() {
  const [form, setForm] = useState({ fullName: '', email: '', phone: '', password: '' });
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const user = useSelector(selectUser);
  const status = useSelector(selectAuthStatus);
  const error = useSelector((state) => state.auth.error);

  useEffect(() => {
    dispatch(clearError());
  }, [dispatch]);

  useEffect(() => {
    if (user) navigate('/', { replace: true });
  }, [user, navigate]);

  const set = (patch) => setForm((prev) => ({ ...prev, ...patch }));

  const handleSubmit = (event) => {
    event.preventDefault();
    dispatch(register({ ...form, phone: form.phone || null }));
  };

  return (
    <section className="section">
      <div className="wrap" style={{ maxWidth: 480 }}>
        <div className="card">
          <div className="card-body">
            <h2 style={{ marginBottom: 20 }}>Tạo tài khoản</h2>

            {error && <ErrorBlock message={error} />}

            <form onSubmit={handleSubmit}>
              <div className="fgroup">
                <label htmlFor="fullName">Họ và tên *</label>
                <input
                  id="fullName"
                  required
                  minLength={2}
                  value={form.fullName}
                  onChange={(e) => set({ fullName: e.target.value })}
                />
              </div>

              <div className="fgroup">
                <label htmlFor="r-email">Email *</label>
                <input
                  id="r-email"
                  type="email"
                  required
                  value={form.email}
                  onChange={(e) => set({ email: e.target.value })}
                />
              </div>

              <div className="fgroup">
                <label htmlFor="phone">Số điện thoại</label>
                <input
                  id="phone"
                  type="tel"
                  placeholder="09xxxxxxxx"
                  value={form.phone}
                  onChange={(e) => set({ phone: e.target.value })}
                />
              </div>

              <div className="fgroup">
                <label htmlFor="r-password">Mật khẩu * (tối thiểu 6 ký tự)</label>
                <input
                  id="r-password"
                  type="password"
                  required
                  minLength={6}
                  value={form.password}
                  onChange={(e) => set({ password: e.target.value })}
                />
              </div>

              <button type="submit" className="btn btn-dark" style={{ width: '100%' }} disabled={status === 'loading'}>
                {status === 'loading' ? 'Đang xử lý…' : 'Đăng ký'}
              </button>
            </form>

            <p className="muted center" style={{ marginTop: 18, fontSize: '0.9rem' }}>
              Đã có tài khoản? <Link to="/dang-nhap">Đăng nhập</Link>
            </p>
          </div>
        </div>
      </div>
    </section>
  );
}
