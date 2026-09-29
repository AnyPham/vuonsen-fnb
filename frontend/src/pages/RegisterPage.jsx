import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Link, useNavigate } from 'react-router-dom';
import { clearError, register, selectAuthStatus, selectUser } from '@/features/auth/authSlice';
import { ErrorBlock } from '@/components/common/StateBlock';
import { useI18n } from '@/i18n';

export default function RegisterPage() {
  const [form, setForm] = useState({ fullName: '', email: '', phone: '', password: '' });
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const user = useSelector(selectUser);
  const status = useSelector(selectAuthStatus);
  const error = useSelector((state) => state.auth.error);
  const { t } = useI18n();

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
            <h2 style={{ marginBottom: 20 }}>{t('register.title')}</h2>

            {error && <ErrorBlock message={error} />}

            <form onSubmit={handleSubmit} data-test="register-form">
              <div className="fgroup">
                <label htmlFor="fullName">{t('register.fullName')}</label>
                <input
                  id="fullName"
                  data-test="full-name"
                  required
                  minLength={2}
                  value={form.fullName}
                  onChange={(e) => set({ fullName: e.target.value })}
                />
              </div>

              <div className="fgroup">
                <label htmlFor="r-email">{t('register.email')}</label>
                <input
                  id="r-email"
                  data-test="email"
                  type="email"
                  required
                  value={form.email}
                  onChange={(e) => set({ email: e.target.value })}
                />
              </div>

              <div className="fgroup">
                <label htmlFor="phone">{t('register.phone')}</label>
                <input
                  id="phone"
                  data-test="phone"
                  type="tel"
                  placeholder="09xxxxxxxx"
                  value={form.phone}
                  onChange={(e) => set({ phone: e.target.value })}
                />
              </div>

              <div className="fgroup">
                <label htmlFor="r-password">{t('register.password')}</label>
                <input
                  id="r-password"
                  data-test="password"
                  type="password"
                  required
                  minLength={6}
                  value={form.password}
                  onChange={(e) => set({ password: e.target.value })}
                />
              </div>

              <button type="submit" data-test="submit-register" className="btn btn-dark" style={{ width: '100%' }} disabled={status === 'loading'}>
                {status === 'loading' ? t('common.processing') : t('register.submit')}
              </button>
            </form>

            <p className="muted center" style={{ marginTop: 18, fontSize: '0.9rem' }}>
              {t('register.haveAccount')} <Link to="/dang-nhap">{t('register.login')}</Link>
            </p>
          </div>
        </div>
      </div>
    </section>
  );
}
