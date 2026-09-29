import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { spaceApi } from '@/api/endpoints';
import { ErrorBlock, Loading } from '@/components/common/StateBlock';
import { useI18n } from '@/i18n';
import { useDinhDang } from '@/i18n/dinhDang';
import GoogleMap from '@/components/common/GoogleMap';
import Thumb from '@/components/common/Thumb';

// Trang chi tiết một không gian, vào bằng đường dẫn /khong-gian/<slug>
export default function SpaceDetailPage() {
  const { slug } = useParams();
  const [space, setSpace] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);
  // Ảnh đang xem ở khung lớn. Để null nghĩa là đang xem ảnh đại diện.
  const [anhDangXem, setAnhDangXem] = useState(null);
  const { t, tDb, tDbList, lang } = useI18n();
  const dd = useDinhDang();

  useEffect(() => {
    setLoading(true);
    setError(null);
    setAnhDangXem(null);
    spaceApi
      .detail(slug)
      .then(setSpace)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [slug]);

  if (loading) return <Loading label={t('spaceDetail.loading')} />;

  if (error) {
    return (
      <section className="section">
        <div className="wrap" style={{ maxWidth: 640 }}>
          <ErrorBlock message={error} />
          <Link to="/khong-gian" className="btn btn-outline">
            {t('spaceDetail.backToList')}
          </Link>
        </div>
      </section>
    );
  }

  if (!space) return null;

  // Số mâm tối thiểu suy ra từ sức chứa, để khách biết trước mức tính tiền
  const feeUnitLabel = space.feeUnit === 'HUT' ? t('spaces.perHut') : t('spaces.perSession');
  // Nhãn loại không gian do backend gửi kèm cả hai thứ tiếng
  const nhanLoai = lang === 'en' && space.typeLabelEn ? space.typeLabelEn : space.typeLabel;

  return (
    <section className="section">
      <div className="wrap">
        <p style={{ marginBottom: 18 }}>
          <Link to="/khong-gian" className="muted">
            {t('spaceDetail.list')}
          </Link>
        </p>

        <div className="grid grid-2" style={{ alignItems: 'start' }}>
          <div>
            <Thumb
              url={anhDangXem?.url || space.thumbnailUrl}
              test="main-image"
              variant="v2"
              icon="🏛️"
              label={tDb(space, 'name')}
              alt={anhDangXem?.caption || tDb(space, 'name')}
              style={{ borderRadius: 'var(--r)', marginBottom: anhDangXem?.caption ? 8 : 12 }}
            />

            {anhDangXem?.caption && (
              <p className="muted" style={{ fontSize: '0.84rem', marginBottom: 12 }}>
                {anhDangXem.caption}
              </p>
            )}

            {/* Dải ảnh nhỏ, bấm vào thì đổi ảnh ở khung lớn phía trên.
                Ảnh đại diện luôn đứng đầu để quay lại được. */}
            {space.images?.length > 0 && (
              <div className="dai-anh" style={{ marginBottom: 22 }}>
                <button
                  type="button"
                  className={anhDangXem === null ? 'dang-chon' : ''}
                  onClick={() => setAnhDangXem(null)}
                  aria-label={t('spaceDetail.mainImage')}
                >
                  <Thumb url={space.thumbnailUrl} icon="🏛️" alt={tDb(space, 'name')} />
                </button>

                {space.images.map((anh) => (
                  <button
                    type="button"
                    key={anh.url}
                    className={anhDangXem?.url === anh.url ? 'dang-chon' : ''}
                    onClick={() => setAnhDangXem(anh)}
                    data-test="thumb"
                    aria-label={anh.caption || t('spaceDetail.viewImage')}
                  >
                    <Thumb url={anh.url} icon="🏛️" alt={anh.caption || tDb(space, 'name')} />
                  </button>
                ))}
              </div>
            )}

            <div className="section-head" style={{ marginBottom: 18 }}>
              <div className="eyebrow">{nhanLoai}</div>
              <h2 data-test="space-name">{tDb(space, 'name')}</h2>
            </div>

            <p className="muted">{tDb(space, 'description') || tDb(space, 'shortDesc')}</p>

            <h3 style={{ marginTop: 26, marginBottom: 10 }}>{t('spaceDetail.amenities')}</h3>
            <div>
              {tDbList(space.amenities, space.amenitiesEn).map((amenity) => (
                <span key={amenity} className="chip" data-test="amenity">
                  {amenity}
                </span>
              ))}
            </div>
          </div>

          <aside>
            <div className="card" style={{ marginBottom: 22 }}>
              <div className="card-body">
                <h3 style={{ marginBottom: 14 }}>{t('spaceDetail.specs')}</h3>
                <table>
                  <tbody>
                    <tr>
                      <th>{t('spaceDetail.capacity')}</th>
                      <td data-test="capacity">
                        {t('spaceDetail.guestRange', { min: space.capacityMin, max: space.capacityMax })}
                      </td>
                    </tr>
                    <tr>
                      <th>{t('spaceDetail.rentalFee')}</th>
                      <td data-test="rental-fee">
                        {dd.tien(space.rentalFee)} / {feeUnitLabel}
                      </td>
                    </tr>
                    {space.unitCapacity && (
                      <tr>
                        <th>{t('spaceDetail.unitCapacity')}</th>
                        <td>{t('spaceDetail.guests', { n: space.unitCapacity })}</td>
                      </tr>
                    )}
                    <tr>
                      <th>{t('spaceDetail.type')}</th>
                      <td>{nhanLoai}</td>
                    </tr>
                  </tbody>
                </table>

                <p className="muted" style={{ fontSize: '0.86rem', marginTop: 14 }}>
                  {t('spaceDetail.feeNote')}
                </p>

                <Link to={`/dat-tiec?khong-gian=${slug}`} data-test="book-space" className="btn btn-gold" style={{ marginTop: 18, width: '100%' }}>
                  {t('spaceDetail.book')}
                </Link>
              </div>
            </div>

            <div className="card">
              <div className="card-body">
                <h3 style={{ marginBottom: 14 }}>{t('spaceDetail.location')}</h3>
                <GoogleMap
                  lat={space.latitude}
                  lng={space.longitude}
                  height={220}
                  title={tDb(space, 'name')}
                />
              </div>
            </div>
          </aside>
        </div>
      </div>
    </section>
  );
}
