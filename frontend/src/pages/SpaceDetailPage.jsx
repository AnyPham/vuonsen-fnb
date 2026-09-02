import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { spaceApi } from '@/api/endpoints';
import { formatCurrency } from '@/utils/format';
import { ErrorBlock, Loading } from '@/components/common/StateBlock';
import GoogleMap from '@/components/common/GoogleMap';

// Trang chi tiết một không gian, vào bằng đường dẫn /khong-gian/<slug>
export default function SpaceDetailPage() {
  const { slug } = useParams();
  const [space, setSpace] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    setError(null);
    spaceApi
      .detail(slug)
      .then(setSpace)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [slug]);

  if (loading) return <Loading label="Đang tải thông tin không gian…" />;

  if (error) {
    return (
      <section className="section">
        <div className="wrap" style={{ maxWidth: 640 }}>
          <ErrorBlock message={error} />
          <Link to="/khong-gian" className="btn btn-outline">
            ← Về danh sách không gian
          </Link>
        </div>
      </section>
    );
  }

  if (!space) return null;

  // Số mâm tối thiểu suy ra từ sức chứa, để khách biết trước mức tính tiền
  const feeUnitLabel = space.feeUnit === 'HUT' ? 'chòi' : 'buổi';

  return (
    <section className="section">
      <div className="wrap">
        <p style={{ marginBottom: 18 }}>
          <Link to="/khong-gian" className="muted">
            ← Danh sách không gian
          </Link>
        </p>

        <div className="grid grid-2" style={{ alignItems: 'start' }}>
          <div>
            <div className="ph v2" style={{ borderRadius: 'var(--r)', marginBottom: 22 }}>
              <span>🏛️</span>
              <span>{space.name}</span>
            </div>

            <div className="section-head" style={{ marginBottom: 18 }}>
              <div className="eyebrow">{space.typeLabel}</div>
              <h2>{space.name}</h2>
            </div>

            <p className="muted">{space.description || space.shortDesc}</p>

            <h3 style={{ marginTop: 26, marginBottom: 10 }}>Tiện ích đi kèm</h3>
            <div>
              {space.amenities.map((amenity) => (
                <span key={amenity} className="chip">
                  {amenity}
                </span>
              ))}
            </div>
          </div>

          <aside>
            <div className="card" style={{ marginBottom: 22 }}>
              <div className="card-body">
                <h3 style={{ marginBottom: 14 }}>Thông số</h3>
                <table>
                  <tbody>
                    <tr>
                      <th>Sức chứa</th>
                      <td>
                        {space.capacityMin} – {space.capacityMax} khách
                      </td>
                    </tr>
                    <tr>
                      <th>Giá thuê</th>
                      <td>
                        {formatCurrency(space.rentalFee)} / {feeUnitLabel}
                      </td>
                    </tr>
                    {space.unitCapacity && (
                      <tr>
                        <th>Sức chứa mỗi chòi</th>
                        <td>{space.unitCapacity} khách</td>
                      </tr>
                    )}
                    <tr>
                      <th>Loại không gian</th>
                      <td>{space.typeLabel}</td>
                    </tr>
                  </tbody>
                </table>

                <p className="muted" style={{ fontSize: '0.86rem', marginTop: 14 }}>
                  Tiền ăn đạt mức tối thiểu của sảnh thì được miễn phí tiền thuê. Chi phí cụ thể
                  xem ở bước đặt tiệc.
                </p>

                <Link to="/dat-tiec" className="btn btn-gold" style={{ marginTop: 18, width: '100%' }}>
                  Đặt giữ chỗ
                </Link>
              </div>
            </div>

            <div className="card">
              <div className="card-body">
                <h3 style={{ marginBottom: 14 }}>Vị trí</h3>
                <GoogleMap
                  lat={space.latitude}
                  lng={space.longitude}
                  height={220}
                  title={space.name}
                />
              </div>
            </div>
          </aside>
        </div>
      </div>
    </section>
  );
}
