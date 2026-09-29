import { useState } from 'react';
import { Link } from 'react-router-dom';
import { authApi } from '@/api/endpoints';
import { ErrorBlock } from '@/components/common/StateBlock';
import { useI18n } from '@/i18n';

/*
 * Xin liên kết đặt lại mật khẩu.
 *
 * Gửi xong luôn hiện cùng một câu, dù email có tồn tại hay không. Báo khác nhau giữa hai
 * trường hợp thì người ngoài gõ thử vài email là dò ra ai đã đăng ký trên hệ thống.
 */
export default function ForgotPasswordPage() {
  const [email, setEmail] = useState('');
  const [dangGui, setDangGui] = useState(false);
  const [daGui, setDaGui] = useState(false);
  const [loi, setLoi] = useState(null);
  const { t } = useI18n();

  const gui = async (event) => {
    event.preventDefault();
    setDangGui(true);
    setLoi(null);
    try {
      await authApi.forgotPassword(email.trim());
      setDaGui(true);
    } catch (e) {
      setLoi(e.message || t('forgot.failed'));
    } finally {
      setDangGui(false);
    }
  };

  return (
    <section className="section">
      <div className="wrap" style={{ maxWidth: 480 }}>
        <div className="section-head center">
          <div className="eyebrow center">{t('account.eyebrow')}</div>
          <h2>{t('forgot.title')}</h2>
        </div>

        {daGui ? (
          <div className="card" data-test="forgot-sent">
            <div className="card-body">
              <h3 style={{ marginBottom: 10 }}>{t('forgot.sentTitle')}</h3>
              <p className="muted">
                {t('forgot.sentPrefix')} <strong>{email}</strong> {t('forgot.sentNote')}
              </p>
              <p className="muted" style={{ fontSize: '0.88rem', marginTop: 12 }}>
                {t('forgot.spamNote')}
              </p>
              <Link to="/dang-nhap" className="btn btn-outline btn-sm" style={{ marginTop: 16 }}>
                {t('forgot.backToLogin')}
              </Link>
            </div>
          </div>
        ) : (
          <form className="card" onSubmit={gui}>
            <div className="card-body">
              <p className="muted" style={{ marginBottom: 18 }}>
                {t('forgot.intro')}
              </p>

              {loi && <ErrorBlock message={loi} />}

              <div className="fgroup">
                <label htmlFor="fp-email">{t('common.email')}</label>
                <input
                  id="fp-email"
                  data-test="forgot-email"
                  type="email"
                  required
                  value={email}
                  placeholder="email@example.com"
                  onChange={(e) => setEmail(e.target.value)}
                />
              </div>

              <button type="submit" className="btn btn-dark" disabled={dangGui} data-test="forgot-submit">
                {dangGui ? t('forgot.sending') : t('forgot.submit')}
              </button>

              <p className="muted" style={{ fontSize: '0.88rem', marginTop: 16 }}>
                {t('forgot.remembered')} <Link to="/dang-nhap">{t('login.submit')}</Link>
              </p>
            </div>
          </form>
        )}
      </div>
    </section>
  );
}
