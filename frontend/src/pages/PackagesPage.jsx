import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Link } from 'react-router-dom';
import { fetchPackages, selectPackages } from '@/features/catalog/catalogSlice';
import { formatCurrency } from '@/utils/format';
import { Loading } from '@/components/common/StateBlock';

export default function PackagesPage() {
  const dispatch = useDispatch();
  const { items, status } = useSelector(selectPackages);

  useEffect(() => {
    dispatch(fetchPackages());
  }, [dispatch]);

  return (
    <section className="section">
      <div className="wrap">
        <div className="section-head center">
          <div className="eyebrow center">Bảng giá combo</div>
          <h2>Gói tiệc tính theo mâm 10 khách</h2>
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
                  <span className="muted" style={{ fontSize: '0.9rem' }}>
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
  );
}
