import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Link } from 'react-router-dom';
import { fetchPackages, fetchSpaces, selectPackages, selectSpaces } from '@/features/catalog/catalogSlice';
import { galleryApi, menuApi, reviewApi } from '@/api/endpoints';
import Thumb from '@/components/common/Thumb';
import { useI18n } from '@/i18n';
import { useDinhDang } from '@/i18n/dinhDang';

// Vẽ số sao từ điểm đánh giá
function Stars({ value }) {
  return (
    <span style={{ color: 'var(--gold)', letterSpacing: 2 }}>
      {'★'.repeat(value)}
      <span style={{ opacity: 0.3 }}>{'★'.repeat(5 - value)}</span>
    </span>
  );
}

export default function HomePage() {
  const dispatch = useDispatch();
  const spaces = useSelector(selectSpaces);
  const packages = useSelector(selectPackages);

  /*
   * Ba khối dưới trang lấy dữ liệu riêng chứ không qua Redux.
   *
   * Chúng chỉ xuất hiện ở trang chủ và không trang nào khác dùng lại, nên giữ trong state
   * của trang cho gọn. Khối nào gọi hỏng thì để rỗng và không vẽ, trang chủ vẫn chạy.
   */
  const [monNoiBat, setMonNoiBat] = useState([]);
  const [anh, setAnh] = useState([]);
  const [danhGia, setDanhGia] = useState([]);
  const [diemTrungBinh, setDiemTrungBinh] = useState(null);
  const { t, tDb, tDbList } = useI18n();
  const dd = useDinhDang();

  useEffect(() => {
    dispatch(fetchSpaces());
    dispatch(fetchPackages());

    menuApi.bestSellers().then(setMonNoiBat).catch(() => setMonNoiBat([]));
    galleryApi.list().then(setAnh).catch(() => setAnh([]));
    reviewApi
      .list({ page: 0, size: 3 })
      .then((trang) => setDanhGia(trang.content))
      .catch(() => setDanhGia([]));
    reviewApi
      .summary()
      .then((tom) => setDiemTrungBinh(tom.average))
      .catch(() => setDiemTrungBinh(null));
  }, [dispatch]);

  // Ảnh bìa lấy từ thư viện thay vì ghi cứng, đổi ảnh chỉ cần sửa trong trang quản trị
  const anhBia = anh[0]?.url;
  const nenHero = anhBia
    ? {
        backgroundImage:
          `linear-gradient(120deg, rgba(20,48,32,0.92) 0%, rgba(20,48,32,0.72) 45%, rgba(20,48,32,0.35) 100%), url(${anhBia})`,
        backgroundSize: 'cover',
        backgroundPosition: 'center',
      }
    : undefined;

  return (
    <>
      <section className="hero" style={nenHero}>
        <div className="wrap">
          <div className="eyebrow">{t('home.heroEyebrow')}</div>
          <h1>{t('home.heroTitle')}</h1>
          <p>
            {t('home.heroDesc')}
          </p>
          <div className="actions">
            <Link to="/dat-tiec" className="btn btn-gold">
              {t('home.freeQuote')}
            </Link>
            <Link to="/khong-gian" className="btn btn-outline" style={{ borderColor: 'var(--cream)', color: 'var(--cream)' }}>
              {t('home.viewSpaces')}
            </Link>
          </div>
        </div>
      </section>

      <section className="section">
        <div className="wrap">
          <div className="section-head center">
            <div className="eyebrow center">{t('spaces.eyebrow')}</div>
            <h2>{t('home.spacesTitle')}</h2>
            <p className="muted">
              {t('home.spacesDesc')}
            </p>
          </div>

          <div className="grid grid-3">
            {spaces.items.slice(0, 6).map((space) => (
              <article key={space.id} className="card">
                <Thumb url={space.thumbnailUrl} icon="🌿" label={tDb(space, 'name')} alt={tDb(space, 'name')} />
                <div className="card-body">
                  <h3>{tDb(space, 'name')}</h3>
                  <p className="muted" style={{ fontSize: '0.92rem', margin: '8px 0 14px' }}>
                    {tDb(space, 'shortDesc')}
                  </p>
                  <div>
                    {tDbList(space.amenities, space.amenitiesEn).map((amenity) => (
                      <span key={amenity} className="chip">
                        {amenity}
                      </span>
                    ))}
                  </div>
                  <div style={{ marginTop: 14, fontWeight: 600, color: 'var(--green-800)' }}>
                    {dd.tien(space.rentalFee)}
                    <small className="muted"> / {t('spaces.perSession')}</small>
                  </div>
                </div>
              </article>
            ))}
          </div>
        </div>
      </section>

      <section className="section" style={{ background: 'var(--cream-2)' }}>
        <div className="wrap">
          <div className="section-head center">
            <div className="eyebrow center">{t('packages.eyebrow')}</div>
            <h2>{t('packages.title')}</h2>
            <p className="muted">
              {t('home.packagesDesc')}
            </p>
          </div>

          <div className="grid grid-3">
            {packages.items.map((pkg) => (
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
                    <span style={{ fontSize: '0.9rem' }} className="muted">
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

      {monNoiBat.length > 0 && (
        <section className="section">
          <div className="wrap">
            <div className="section-head center">
              <div className="eyebrow center">{t('home.kitchenEyebrow')}</div>
              <h2>{t('home.kitchenTitle')}</h2>
              <p className="muted">
                {t('home.kitchenDesc')}
              </p>
            </div>

            <div className="grid grid-3">
              {monNoiBat.slice(0, 6).map((mon) => (
                <article key={mon.id} className="card">
                  <Thumb url={mon.imageUrl} icon="🍲" label={tDb(mon, 'name')} alt={tDb(mon, 'name')} />
                  <div className="card-body">
                    <h3 style={{ fontSize: '1.1rem' }}>{tDb(mon, 'name')}</h3>
                    <p className="muted" style={{ fontSize: '0.9rem', margin: '8px 0 14px' }}>
                      {tDb(mon, 'description')}
                    </p>
                    <div style={{ fontWeight: 600, color: 'var(--green-800)' }}>
                      {mon.price ? dd.tien(mon.price) : tDb(mon, 'priceNote')}
                    </div>
                  </div>
                </article>
              ))}
            </div>

            <div className="center" style={{ marginTop: 28 }}>
              <Link to="/thuc-don" className="btn btn-outline">
                {t('home.viewFullMenu')}
              </Link>
            </div>
          </div>
        </section>
      )}

      {anh.length > 0 && (
        <section className="section" style={{ background: 'var(--cream-2)' }}>
          <div className="wrap">
            <div className="section-head center">
              <div className="eyebrow center">{t('gallery.eyebrow')}</div>
              <h2>{t('home.galleryTitle')}</h2>
            </div>

            <div className="grid grid-3">
              {anh.slice(0, 6).map((tam) => (
                <Thumb key={tam.id} url={tam.url} alt={tDb(tam, 'caption')} label={tDb(tam, 'caption')} icon="📷" />
              ))}
            </div>

            <div className="center" style={{ marginTop: 28 }}>
              <Link to="/thu-vien" className="btn btn-outline">
                {t('home.morePhotos')}
              </Link>
            </div>
          </div>
        </section>
      )}

      {danhGia.length > 0 && (
        <section className="section">
          <div className="wrap">
            <div className="section-head center">
              <div className="eyebrow center">{t('home.reviewsEyebrow')}</div>
              <h2>{t('home.reviewsTitle')}</h2>
              {diemTrungBinh != null && (
                <p className="muted">{t('home.averageScore', { diem: diemTrungBinh.toFixed(1) })}</p>
              )}
            </div>

            <div className="grid grid-3">
              {danhGia.map((y) => (
                <div key={y.id} className="card">
                  <div className="card-body">
                    <Stars value={y.rating} />
                    <p style={{ fontSize: '0.94rem', margin: '12px 0 14px' }}>{y.content}</p>
                    <div className="muted" style={{ fontSize: '0.88rem' }}>
                      {y.customerName}
                    </div>
                  </div>
                </div>
              ))}
            </div>

            <div className="center" style={{ marginTop: 28 }}>
              <Link to="/danh-gia" className="btn btn-outline">
                {t('home.allReviews')}
              </Link>
            </div>
          </div>
        </section>
      )}

      <section className="section" style={{ background: 'var(--green-900)', color: 'var(--cream)' }}>
        <div className="wrap center">
          <div className="eyebrow center">{t('home.ctaEyebrow')}</div>
          <h2 style={{ color: 'var(--cream)' }}>{t('home.ctaTitle')}</h2>
          <p style={{ maxWidth: 560, margin: '16px auto 28px', opacity: 0.9 }}>
            {t('home.ctaDesc')}
          </p>
          <div className="actions" style={{ justifyContent: 'center' }}>
            <Link to="/dat-tiec" className="btn btn-gold">
              {t('home.freeQuote')}
            </Link>
            <a
              href="tel:+842812345678"
              className="btn btn-outline"
              style={{ borderColor: 'var(--cream)', color: 'var(--cream)' }}
            >
              {t('home.call')}
            </a>
          </div>
        </div>
      </section>
    </>
  );
}
