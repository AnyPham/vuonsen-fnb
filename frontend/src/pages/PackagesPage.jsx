import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Link } from 'react-router-dom';
import { fetchPackages, selectPackages } from '@/features/catalog/catalogSlice';
import { Empty, Loading } from '@/components/common/StateBlock';
import { useI18n } from '@/i18n';
import { useDinhDang } from '@/i18n/dinhDang';

/*
 * Mốc "nửa ngày" và "trọn ngày" của bộ lọc thời gian.
 *
 * Con số 8 không tự nghĩ ra: nó khớp với app.booking.full-day-package-hours bên backend,
 * tức ngưỡng mà hệ thống tính tiền coi là thuê trọn ngày. Để lệch hai nơi thì khách lọc
 * "trọn ngày" ra một gói mà lúc tính tiền lại không được tính là trọn ngày.
 */
const GIO_NUA_NGAY = 4;
const GIO_TRON_NGAY = 8;

const BO_LOC_RONG = { maxPrice: '', minDishes: '', minHours: '' };

export default function PackagesPage() {
  const dispatch = useDispatch();
  const { items, status } = useSelector(selectPackages);
  const [filters, setFilters] = useState(BO_LOC_RONG);
  const { t, tDb, tDbList } = useI18n();
  const dd = useDinhDang();

  useEffect(() => {
    const params = {};
    if (filters.maxPrice) params.maxPrice = Number(filters.maxPrice);
    if (filters.minDishes) params.minDishes = Number(filters.minDishes);
    if (filters.minHours) params.minHours = Number(filters.minHours);
    dispatch(fetchPackages(params));
  }, [dispatch, filters]);

  const set = (patch) => setFilters((prev) => ({ ...prev, ...patch }));
  const dangLoc = Object.values(filters).some(Boolean);

  return (
    <section className="section">
      <div className="wrap">
        <div className="section-head center">
          <div className="eyebrow center">{t('packages.eyebrow')}</div>
          <h2>{t('packages.title')}</h2>
        </div>

        <div className="card" style={{ marginBottom: 32 }}>
          <div className="card-body form-row">
            <div className="fgroup" style={{ marginBottom: 0 }}>
              <label htmlFor="f-pkg-price">{t('packages.filterMaxPrice')}</label>
              <input
                id="f-pkg-price"
                data-test="filter-package-max-price"
                type="number"
                min="0"
                step="100000"
                placeholder={t('packages.pricePlaceholder')}
                value={filters.maxPrice}
                onChange={(e) => set({ maxPrice: e.target.value })}
              />
            </div>

            <div className="fgroup" style={{ marginBottom: 0 }}>
              <label htmlFor="f-pkg-dishes">{t('packages.filterMinDishes')}</label>
              <input
                id="f-pkg-dishes"
                data-test="filter-package-min-dishes"
                type="number"
                min="1"
                placeholder={t('packages.dishesPlaceholder')}
                value={filters.minDishes}
                onChange={(e) => set({ minDishes: e.target.value })}
              />
            </div>

            <div className="fgroup" style={{ marginBottom: 0 }}>
              <label htmlFor="f-pkg-hours">{t('packages.filterHours')}</label>
              <select
                id="f-pkg-hours"
                data-test="filter-package-hours"
                value={filters.minHours}
                onChange={(e) => set({ minHours: e.target.value })}
              >
                <option value="">{t('packages.hoursAll')}</option>
                <option value={GIO_NUA_NGAY}>{t('packages.hoursHalfDay')}</option>
                <option value={GIO_TRON_NGAY}>{t('packages.hoursFullDay')}</option>
              </select>
            </div>

            {/* Nút chỉ hiện khi có cái để bỏ, tránh bày một nút bấm vào không đổi gì */}
            {dangLoc && (
              <div className="fgroup" style={{ marginBottom: 0, justifyContent: 'flex-end' }}>
                <button
                  type="button"
                  className="btn btn-outline"
                  data-test="filter-package-clear"
                  onClick={() => setFilters(BO_LOC_RONG)}
                >
                  {t('packages.clear')}
                </button>
              </div>
            )}
          </div>
        </div>

        {status === 'loading' && <Loading />}

        {/*
          Chỉ hiện số lượng khi khách đang lọc. Lúc chưa lọc thì câu "3 gói phù hợp"
          không nói thêm được gì ngoài thứ đã bày ngay bên dưới.
        */}
        {status === 'succeeded' && dangLoc && items.length > 0 && (
          <p className="muted" data-test="package-count" style={{ marginBottom: 16 }}>
            {t('packages.count', { n: items.length })}
          </p>
        )}

        {status === 'succeeded' && items.length === 0 && <Empty label={t('packages.empty')} />}

        <div className="grid grid-3" data-test="package-list">
          {items.map((pkg) => (
            <div
              key={pkg.id}
              className="card"
              data-test="package-card"
              data-code={pkg.code}
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
