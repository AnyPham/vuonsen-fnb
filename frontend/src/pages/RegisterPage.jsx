import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Link, useNavigate } from 'react-router-dom';
import {
  clearError, register, selectAuthStatus, selectUser, xacThucEmail,
} from '@/features/auth/authSlice';
import { authApi } from '@/api/endpoints';
import { ErrorBlock } from '@/components/common/StateBlock';
import { useI18n } from '@/i18n';

export default function RegisterPage() {
  const [form, setForm] = useState({ fullName: '', email: '', phone: '', password: '' });
  /*
   * Trang này có hai màn. Màn nhập thông tin, rồi màn nhập mã gửi về email.
   *
   * Gộp vào một trang thay vì tách thành một tuyến riêng, vì hai bước thuộc cùng một việc.
   * Tách tuyến thì khách tải lại trang giữa chừng là mất ngữ cảnh, phải đăng ký lại từ đầu.
   */
  const [choXacThuc, setChoXacThuc] = useState(null);
  const [ma, setMa] = useState('');
  const [tinNhan, setTinNhan] = useState(null);
  const [dangGuiLai, setDangGuiLai] = useState(false);

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
    dispatch(register({ ...form, phone: form.phone || null }))
      .unwrap()
      .then((kq) => {
        setChoXacThuc(kq.email);
        setTinNhan(kq.message);
      })
      .catch(() => { /* lỗi đã nằm trong state.auth.error, ErrorBlock hiện rồi */ });
  };

  const guiMa = (event) => {
    event.preventDefault();
    dispatch(xacThucEmail({ email: choXacThuc, code: ma }));
  };

  const guiLaiMa = async () => {
    setDangGuiLai(true);
    try {
      const kq = await authApi.guiLaiMa({ email: choXacThuc });
      setTinNhan(kq.message);
      setMa('');
    } catch (e) {
      setTinNhan(e.message);
    } finally {
      setDangGuiLai(false);
    }
  };

  // ---------------- Màn nhập mã ----------------
  if (choXacThuc) {
    return (
      <section className="section">
        <div className="wrap" style={{ maxWidth: 480 }}>
          <div className="card">
            <div className="card-body">
              <h2 style={{ marginBottom: 12 }}>{t('verify.title')}</h2>
              {tinNhan && <p className="muted" data-test="verify-message">{tinNhan}</p>}

              {error && <ErrorBlock message={error} />}

              <form onSubmit={guiMa} data-test="verify-form">
                <div className="fgroup">
                  <label htmlFor="v-code">{t('verify.code')}</label>
                  <input
                    id="v-code"
                    data-test="verify-code"
                    inputMode="numeric"
                    autoComplete="one-time-code"
                    required
                    pattern="[0-9]{6}"
                    maxLength={6}
                    value={ma}
                    onChange={(e) => setMa(e.target.value.replace(/\D/g, ''))}
                    style={{ letterSpacing: '0.4em', fontSize: '1.2rem', textAlign: 'center' }}
                  />
                </div>

                <button
                  type="submit"
                  data-test="submit-verify"
                  className="btn btn-dark"
                  style={{ width: '100%' }}
                  disabled={status === 'loading' || ma.length !== 6}
                >
                  {status === 'loading' ? t('common.processing') : t('verify.submit')}
                </button>
              </form>

              <button
                type="button"
                data-test="resend-code"
                className="btn btn-outline"
                style={{ width: '100%', marginTop: 10 }}
                onClick={guiLaiMa}
                disabled={dangGuiLai}
              >
                {dangGuiLai ? t('common.processing') : t('verify.resend')}
              </button>
            </div>
          </div>
        </div>
      </section>
    );
  }

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
                  minLength={8}
                  value={form.password}
                  onChange={(e) => set({ password: e.target.value })}
                />
                {/*
                  Nêu luật ngay dưới ô nhập, không đợi bấm gửi mới báo lỗi. Khách gõ xong
                  mật khẩu rồi mới biết mình sai thì phải nghĩ lại từ đầu, dễ bỏ cuộc.
                */}
                <small className="muted" data-test="password-rule">
                  {t('register.passwordRule')}
                </small>
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
