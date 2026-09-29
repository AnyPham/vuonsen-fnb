import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Link } from 'react-router-dom';
import { fetchPackages, selectPackages } from '@/features/catalog/catalogSlice';
import { Loading } from '@/components/common/StateBlock';
import { useI18n } from '@/i18n';
import { useDinhDang } from '@/i18n/dinhDang';

export default function PackagesPage() {
  const dispatch = useDispatch();
  const { items, status } = useSelector(selectPackages);
  const { t, tDb, tDbList } = useI18n();
  const dd = useDinhDang();

  useEffect(() => {
    dispatch(fetchPackages());
  }, [dispatch]);

  return (
    <section className="section">
      <div className="wrap">
        <div className="section-head center">
          <div className="eyebrow center">{t('packages.eyebrow')}</div>
          <h2>{t('packages.title')}</h2>
        </div>

        {status === 'loading' && <Loading />}

        <div className="grid grid-3">
          {items.map((pkg) => (
            <div
              key={pkg.id}
              className="card"
              style={pkg.featured ? { borderColor: 'var(--gold)', borderWidth: 2 } : undefined}
            >
              <div className="card-body">
                {pkg.featured && <span className="tag tag-CONFIRMED">{t('packages.featured')}</span>}
                <h3 style={{ marginTop: 10 }}>{tDb(pkg, 'name')}</h3>
                <p className="muted" style={{ fontSize: '0.9rem' }}>
                  {tDb(pkg, 'tagline')}
                </p>
                <div
                  style={{
                    fontFamily: 'var(--serif)',
                    fontSize: '1.9rem',
                    color: 'var(--green-800)',
                    margin: '14px 0',
                  }}
                >
                  {dd.tien(pkg.pricePerTable)}
                  <span className="muted" style={{ fontSize: '0.9rem' }}>
                    {' '}
                    / {t('packages.perTable')}
                  </span>
                </div>
                <ul style={{ paddingLeft: 18, fontSize: '0.92rem' }}>
                  {tDbList(pkg.features, pkg.featuresEn).map((feature) => (
                    <li key={feature}>{feature}</li>
                  ))}
                </ul>
                <Link
                  to="/dat-tiec"
                  className={`btn ${pkg.featured ? 'btn-gold' : 'btn-outline'}`}
                  style={{ marginTop: 18 }}
                >
                  {t('packages.choose')}
                </Link>
              </div>
            </div>
          ))}
        </div>

        <p className="muted center" style={{ marginTop: 28, fontSize: '0.9rem' }}>
          {t('packages.footnote')}
        </p>
      </div>
    </section>
  );
}
