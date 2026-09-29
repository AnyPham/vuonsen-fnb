import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { clearError, login, selectAuthStatus, selectUser } from '@/features/auth/authSlice';
import { ErrorBlock } from '@/components/common/StateBlock';
import { useI18n } from '@/i18n';

export default function LoginPage() {
  const [form, setForm] = useState({ email: '', password: '' });
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const location = useLocation();
  const user = useSelector(selectUser);
  const status = useSelector(selectAuthStatus);
  const error = useSelector((state) => state.auth.error);
  const { t } = useI18n();

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
            <h2 style={{ marginBottom: 20 }}>{t('login.title')}</h2>

            {error && <ErrorBlock message={error} />}

            <form onSubmit={handleSubmit} data-test="login-form">
              <div className="fgroup">
                <label htmlFor="email">{t('common.email')}</label>
                <input
                  id="email"
                  data-test="email"
                  type="email"
                  required
                  value={form.email}
                  onChange={(e) => setForm({ ...form, email: e.target.value })}
                />
              </div>

              <div className="fgroup">
                <label htmlFor="password">{t('common.password')}</label>
                <input
                  id="password"
                  data-test="password"
                  type="password"
                  required
                  value={form.password}
                  onChange={(e) => setForm({ ...form, password: e.target.value })}
                />
              </div>

              <button
                type="submit"
                data-test="submit-login"
                className="btn btn-dark"
                style={{ width: '100%' }}
                disabled={status === 'loading'}
              >
                {status === 'loading' ? t('common.processing') : t('login.submit')}
              </button>
            </form>

            <p className="muted center" style={{ marginTop: 18, fontSize: '0.9rem' }}>
              <Link to="/quen-mat-khau" data-test="link-forgot">{t('login.forgot')}</Link>
            </p>

            <p className="muted center" style={{ marginTop: 8, fontSize: '0.9rem' }}>
              {t('login.noAccount')} <Link to="/dang-ky">{t('login.register')}</Link>
            </p>
          </div>
        </div>
      </div>
    </section>
  );
}
