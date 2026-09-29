import { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { authApi } from '@/api/endpoints';
import { ErrorBlock } from '@/components/common/StateBlock';
import { useI18n } from '@/i18n';

/*
 * Đặt mật khẩu mới từ liên kết trong email.
 *
 * Mã nằm trên địa chỉ web dưới dạng ?ma=... Không có mã thì hiện luôn lời nhắc xin liên kết
 * mới, đỡ để khách điền xong mật khẩu rồi mới báo thiếu mã.
 */
export default function ResetPasswordPage() {
  const [thamSo] = useSearchParams();
  const dieuHuong = useNavigate();
  const ma = thamSo.get('ma') || '';

  const [matKhau, setMatKhau] = useState('');
  const [nhapLai, setNhapLai] = useState('');
  const [dangGui, setDangGui] = useState(false);
  const [loi, setLoi] = useState(null);
  const [xong, setXong] = useState(false);
  const { t } = useI18n();

  const gui = async (event) => {
    event.preventDefault();
    if (matKhau !== nhapLai) {
      setLoi(t('reset.mismatch'));
      return;
    }
    setDangGui(true);
    setLoi(null);
    try {
      await authApi.resetPassword(ma, matKhau);
      setXong(true);
      // Đặt xong thì đưa về đăng nhập sau một nhịp, để khách kịp đọc dòng báo
      setTimeout(() => dieuHuong('/dang-nhap'), 2500);
    } catch (e) {
      setLoi(e.message || t('reset.failed'));
    } finally {
      setDangGui(false);
    }
  };

  return (
    <section className="section">
      <div className="wrap" style={{ maxWidth: 480 }}>
        <div className="section-head center">
          <div className="eyebrow center">{t('account.eyebrow')}</div>
          <h2>{t('reset.title')}</h2>
        </div>

        {!ma && (
          <div className="card">
            <div className="card-body">
              <p className="muted">
                {t('reset.missingCode')}
              </p>
              <Link to="/quen-mat-khau" className="btn btn-dark btn-sm" style={{ marginTop: 14 }}>
                {t('reset.askNewLink')}
              </Link>
            </div>
          </div>
        )}

        {ma && xong && (
          <div className="card" data-test="reset-done">
            <div className="card-body">
              <h3 style={{ marginBottom: 10 }}>{t('reset.doneTitle')}</h3>
              <p className="muted">
                {t('reset.doneNote')}
              </p>
            </div>
          </div>
        )}

        {ma && !xong && (
          <form className="card" onSubmit={gui}>
            <div className="card-body">
              {loi && <ErrorBlock message={loi} />}

              <div className="fgroup">
                <label htmlFor="rp-pass">{t('reset.newPassword')}</label>
                <input
                  id="rp-pass"
                  data-test="reset-password"
                  type="password"
                  required
                  minLength={6}
                  value={matKhau}
                  placeholder={t('reset.minChars')}
                  onChange={(e) => setMatKhau(e.target.value)}
                />
              </div>

              <div className="fgroup">
                <label htmlFor="rp-pass2">{t('reset.repeatPassword')}</label>
                <input
                  id="rp-pass2"
                  data-test="reset-password-again"
                  type="password"
                  required
                  minLength={6}
                  value={nhapLai}
                  onChange={(e) => setNhapLai(e.target.value)}
                />
              </div>

              <button type="submit" className="btn btn-dark" disabled={dangGui} data-test="reset-submit">
                {dangGui ? t('reset.saving') : t('reset.submit')}
              </button>
            </div>
          </form>
        )}
      </div>
    </section>
  );
}
