import GoogleMap from '@/components/common/GoogleMap';
import { useI18n } from '@/i18n';

export default function Footer() {
  const { t } = useI18n();

  return (
    <footer className="footer">
      <div className="wrap">
        <div className="grid grid-3">
          <div>
            <h4>🌿 Vườn Sen</h4>
            <p>{t('footer.about')}</p>
          </div>

          <div>
            <h4>{t('footer.contact')}</h4>
            <p>{t('footer.address')}</p>
            <p>
              {t('footer.phoneLabel')}: <a href="tel:+842812345678">(028) 1234 5678</a>
            </p>
            <p>
              {t('footer.emailLabel')}: <a href="mailto:datban@vuonsen.vn">datban@vuonsen.vn</a>
            </p>
            <p>{t('footer.hours')}</p>
          </div>

          <div>
            <h4>{t('footer.location')}</h4>
            <GoogleMap height={180} />
          </div>
        </div>

        <div className="footer-bottom">
          © {new Date().getFullYear()} Vườn Sen · {t('footer.credit')}
        </div>
      </div>
    </footer>
  );
}
