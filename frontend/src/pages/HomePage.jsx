import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Link } from 'react-router-dom';
import { fetchPackages, fetchSpaces, selectPackages, selectSpaces } from '@/features/catalog/catalogSlice';
import { formatCurrency } from '@/utils/format';

export default function HomePage() {
  const dispatch = useDispatch();
  const spaces = useSelector(selectSpaces);
  const packages = useSelector(selectPackages);

  useEffect(() => {
    dispatch(fetchSpaces());
    dispatch(fetchPackages());
  }, [dispatch]);

  return (
    <>
      <section className="hero">
        <div className="wrap">
          <div className="eyebrow">Khu ẩm thực & tiệc sân vườn</div>
          <h1>Sáu không gian, sáu cách kể chuyện</h1>
          <p>
            Không gian sân vườn ven sông, ẩm thực miền quê và dịch vụ tổ chức tiệc trọn gói từ 20 đến
            800 khách.
          </p>
          <div className="actions">
            <Link to="/dat-tiec" className="btn btn-gold">
              Nhận báo giá miễn phí
            </Link>
            <Link to="/khong-gian" className="btn btn-outline" style={{ borderColor: 'var(--cream)', color: 'var(--cream)' }}>
              Xem không gian
            </Link>
          </div>
        </div>
      </section>

      <section className="section">
        <div className="wrap">
          <div className="section-head center">
            <div className="eyebrow center">Cho thuê không gian</div>
            <h2>Không gian tiệc</h2>
            <p className="muted">
              Mỗi khu vực có lối vào riêng, khu tiếp khách và bếp phụ riêng — tiệc của bạn không bị
              ảnh hưởng bởi khách khác.
            </p>
          </div>

          <div className="grid grid-3">
            {spaces.items.slice(0, 6).map((space) => (
              <article key={space.id} className="card">
                <div className="ph">
                  <span>🌿</span>
                  <span>{space.name}</span>
                </div>
                <div className="card-body">
                  <h3>{space.name}</h3>
                  <p className="muted" style={{ fontSize: '0.92rem', margin: '8px 0 14px' }}>
                    {space.shortDesc}
                  </p>
                  <div>
                    {space.amenities.map((amenity) => (
                      <span key={amenity} className="chip">
                        {amenity}
                      </span>
                    ))}
                  </div>
                  <div style={{ marginTop: 14, fontWeight: 600, color: 'var(--green-800)' }}>
                    {formatCurrency(space.rentalFee)}
                    <small className="muted"> / buổi</small>
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
            <div className="eyebrow center">Bảng giá combo</div>
            <h2>Gói tiệc tính theo mâm 10 khách</h2>
            <p className="muted">
              Giá đã gồm phục vụ, khăn bàn, nước sâm và trà đá. Chưa gồm phí thuê không gian và VAT.
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
                  {pkg.featured && <span className="tag tag-CONFIRMED">Được chọn nhiều nhất</span>}
                  <h3 style={{ marginTop: 10 }}>{pkg.name}</h3>
                  <p className="muted" style={{ fontSize: '0.9rem' }}>
                    {pkg.tagline}
                  </p>
                  <div
                    style={{
                      fontFamily: 'var(--serif)',
                      fontSize: '1.9rem',
                      color: 'var(--green-800)',
                      margin: '14px 0',
                    }}
                  >
                    {formatCurrency(pkg.pricePerTable)}
                    <span style={{ fontSize: '0.9rem' }} className="muted">
                      {' '}
                      / mâm
                    </span>
                  </div>
                  <ul style={{ paddingLeft: 18, fontSize: '0.92rem' }}>
                    {pkg.features.map((feature) => (
                      <li key={feature}>{feature}</li>
                    ))}
                  </ul>
                  <Link
                    to="/dat-tiec"
                    className={`btn ${pkg.featured ? 'btn-gold' : 'btn-outline'}`}
                    style={{ marginTop: 18 }}
                  >
                    Chọn gói này
                  </Link>
                </div>
              </div>
            ))}
          </div>

          <p className="muted center" style={{ marginTop: 28, fontSize: '0.9rem' }}>
            Tiền ăn đạt mức tối thiểu của từng không gian thì miễn phí tiền thuê · Đặt trước 60 ngày giảm thêm 5%
          </p>
        </div>
      </section>
    </>
  );
}
