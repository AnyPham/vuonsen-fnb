import { Link } from 'react-router-dom';
import { useI18n } from '@/i18n';

export default function NotFoundPage() {
  const { t } = useI18n();

  return (
    <section className="section">
      <div className="wrap state">
        <h2>{t('notFound.title')}</h2>
        <p className="muted" style={{ marginBottom: 22 }}>
          {t('notFound.desc')}
        </p>
        <Link to="/" className="btn btn-dark">
          {t('notFound.home')}
        </Link>
      </div>
    </section>
  );
}
