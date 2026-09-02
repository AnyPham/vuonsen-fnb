import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Link } from 'react-router-dom';
import { fetchSpaces, selectSpaces } from '@/features/catalog/catalogSlice';
import { spaceApi } from '@/api/endpoints';
import { formatCurrency } from '@/utils/format';
import { Empty, ErrorBlock, Loading } from '@/components/common/StateBlock';
import Thumb from '@/components/common/Thumb';

// Trang cho thuê không gian, lọc theo số khách, loại và giá
export default function SpacesPage() {
  const dispatch = useDispatch();
  const { items, status, error } = useSelector(selectSpaces);
  const [types, setTypes] = useState([]);
  const [filters, setFilters] = useState({ guests: '', type: '', maxPrice: '' });

  useEffect(() => {
    spaceApi.types().then(setTypes).catch(() => setTypes([]));
  }, []);

  useEffect(() => {
    const params = {};
    if (filters.guests) params.guests = Number(filters.guests);
    if (filters.type) params.type = filters.type;
    if (filters.maxPrice) params.maxPrice = Number(filters.maxPrice);
    dispatch(fetchSpaces(params));
  }, [dispatch, filters]);

  const set = (patch) => setFilters((prev) => ({ ...prev, ...patch }));

  return (
    <section className="section">
      <div className="wrap">
        <div className="section-head">
          <div className="eyebrow">Cho thuê không gian</div>
          <h2>Chọn không gian phù hợp</h2>
        </div>

        <div className="card" style={{ marginBottom: 32 }}>
          <div className="card-body form-row">
            <div className="fgroup" style={{ marginBottom: 0 }}>
              <label htmlFor="f-guests">Số khách</label>
              <input
                id="f-guests"
                type="number"
                min="1"
                placeholder="Ví dụ: 150"
                value={filters.guests}
                onChange={(e) => set({ guests: e.target.value })}
              />
            </div>

            <div className="fgroup" style={{ marginBottom: 0 }}>
              <label htmlFor="f-type">Loại không gian</label>
              <select id="f-type" value={filters.type} onChange={(e) => set({ type: e.target.value })}>
                <option value="">Tất cả</option>
                {types.map((type) => (
                  <option key={type.value} value={type.value}>
                    {type.label}
                  </option>
                ))}
              </select>
            </div>

            <div className="fgroup" style={{ marginBottom: 0 }}>
              <label htmlFor="f-price">Giá thuê tối đa</label>
              <input
                id="f-price"
                type="number"
                step="1000000"
                placeholder="Ví dụ: 10000000"
                value={filters.maxPrice}
                onChange={(e) => set({ maxPrice: e.target.value })}
              />
            </div>
          </div>
        </div>

        {status === 'loading' && <Loading />}
        {status === 'failed' && <ErrorBlock message={error} />}
        {status === 'succeeded' && items.length === 0 && (
          <Empty label="Không có không gian nào phù hợp với bộ lọc. Thử nới rộng điều kiện xem sao." />
        )}

        <div className="grid grid-3">
          {items.map((space) => (
            <article key={space.id} className="card card-clickable">
              <Thumb
                url={space.thumbnailUrl}
                variant="v2"
                icon="🏛️"
                label={space.typeLabel}
                alt={space.name}
              />
              <div className="card-body">
                <h3>
                  {/* full-link làm cả thẻ bấm được, xem class trong global.css */}
                  <Link to={`/khong-gian/${space.slug}`} className="full-link">
                    {space.name}
                  </Link>
                </h3>
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
                <div
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    marginTop: 16,
                  }}
                >
                  <strong style={{ color: 'var(--green-800)' }}>
                    {formatCurrency(space.rentalFee)}
                    <small className="muted">
                      {' '}
                      / {space.feeUnit === 'HUT' ? 'chòi' : 'buổi'}
                    </small>
                  </strong>
                  <Link to={`/khong-gian/${space.slug}`} className="btn btn-outline btn-sm">
                    Xem chi tiết →
                  </Link>
                </div>
              </div>
            </article>
          ))}
        </div>
      </div>
    </section>
  );
}
