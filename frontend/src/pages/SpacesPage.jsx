import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Link, useNavigate } from 'react-router-dom';
import { fetchSpaces, selectSpaces } from '@/features/catalog/catalogSlice';
import { goToStep, updateForm } from '@/features/booking/bookingSlice';
import { bookingApi, spaceApi } from '@/api/endpoints';
import { useI18n } from '@/i18n';
import { useDinhDang } from '@/i18n/dinhDang';
import { Empty, ErrorBlock, Loading } from '@/components/common/StateBlock';
import SuggestionBox from '@/components/common/SuggestionBox';
import Thumb from '@/components/common/Thumb';

// Trang cho thuê không gian, lọc theo số khách, loại và giá
export default function SpacesPage() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { items, status, error } = useSelector(selectSpaces);
  const [types, setTypes] = useState([]);
  const [eventTypes, setEventTypes] = useState([]);
  const [filters, setFilters] = useState({ guests: '', type: '', maxPrice: '', eventType: '' });
  const { t, tDb, tDbList, lang } = useI18n();
  const dd = useDinhDang();

  // Backend trả về label (Việt) và labelEn (Anh); chưa có bản tiếng Anh thì dùng bản tiếng Việt
  const nhanTuyChon = (tuyChon) => (lang === 'en' && tuyChon.labelEn ? tuyChon.labelEn : tuyChon.label);

  useEffect(() => {
    spaceApi.types().then(setTypes).catch(() => setTypes([]));
    // Loại sự kiện chỉ dùng cho khối gợi ý, không phải điều kiện lọc không gian
    bookingApi
      .options()
      .then((o) => setEventTypes(o.eventTypes || []))
      .catch(() => setEventTypes([]));
  }, []);

  useEffect(() => {
    const params = {};
    if (filters.guests) params.guests = Number(filters.guests);
    if (filters.type) params.type = filters.type;
    if (filters.maxPrice) params.maxPrice = Number(filters.maxPrice);
    dispatch(fetchSpaces(params));
  }, [dispatch, filters]);

  const set = (patch) => setFilters((prev) => ({ ...prev, ...patch }));

  // Khách bấm chọn một phương án gợi ý thì điền sẵn vào form đặt tiệc rồi
  // chuyển thẳng sang bước 2, khỏi phải nhập lại số khách và loại sự kiện
  const chonPhuongAn = (spaceId, packageId) => {
    dispatch(updateForm({
      spaceId,
      packageId,
      guestCount: filters.guests,
      eventType: filters.eventType,
    }));
    dispatch(goToStep(2));
    navigate('/dat-tiec');
  };

  return (
    <section className="section">
      <div className="wrap">
        <div className="section-head">
          <div className="eyebrow">{t('spaces.eyebrow')}</div>
          <h2>{t('spaces.title')}</h2>
        </div>

        <div className="card" style={{ marginBottom: 32 }}>
          <div className="card-body form-row">
            <div className="fgroup" style={{ marginBottom: 0 }}>
              <label htmlFor="f-guests">{t('spaces.filterGuests')}</label>
              <input
                id="f-guests"
                data-test="filter-guests"
                type="number"
                min="1"
                placeholder={t('spaces.guestsPlaceholder')}
                value={filters.guests}
                onChange={(e) => set({ guests: e.target.value })}
              />
            </div>

            <div className="fgroup" style={{ marginBottom: 0 }}>
              <label htmlFor="f-type">{t('spaces.filterType')}</label>
              <select id="f-type" data-test="filter-type" value={filters.type} onChange={(e) => set({ type: e.target.value })}>
                <option value="">{t('spaces.all')}</option>
                {types.map((type) => (
                  <option key={type.value} value={type.value}>
                    {nhanTuyChon(type)}
                  </option>
                ))}
              </select>
            </div>

            <div className="fgroup" style={{ marginBottom: 0 }}>
              <label htmlFor="f-price">{t('spaces.filterMaxPrice')}</label>
              <input
                id="f-price"
                data-test="filter-max-price"
                type="number"
                step="1000000"
                placeholder={t('spaces.pricePlaceholder')}
                value={filters.maxPrice}
                onChange={(e) => set({ maxPrice: e.target.value })}
              />
            </div>

            <div className="fgroup" style={{ marginBottom: 0 }}>
              <label htmlFor="f-event">{t('spaces.filterEvent')}</label>
              <select
                id="f-event"
                value={filters.eventType}
                onChange={(e) => set({ eventType: e.target.value })}
              >
                <option value="">{t('spaces.notChosen')}</option>
                {eventTypes.map((type) => (
                  <option key={type.value} value={type.value}>
                    {nhanTuyChon(type)}
                  </option>
                ))}
              </select>
              <div className="muted" style={{ fontSize: '0.8rem', marginTop: 6 }}>
                {t('spaces.eventHint')}
              </div>
            </div>
          </div>
        </div>

        {/* Điền số khách là có gợi ý ngay, chọn thêm loại sự kiện thì sát hơn */}
        <SuggestionBox
          guestCount={filters.guests}
          eventType={filters.eventType}
          onPick={chonPhuongAn}
          tieuDe={t('spaces.suggestTitle')}
          nhanNut={t('spaces.suggestButton')}
        />

        {status === 'loading' && <Loading />}
        {status === 'failed' && <ErrorBlock message={error} />}
        {status === 'succeeded' && items.length === 0 && (
          <Empty label={t('spaces.empty')} />
        )}

        <div className="grid grid-3">
          {items.map((space) => (
            <article key={space.id} className="card card-clickable" data-test="space-card">
              <Thumb
                url={space.thumbnailUrl}
                variant="v2"
                icon="🏛️"
                label={lang === 'en' ? space.typeLabelEn || space.typeLabel : space.typeLabel}
                alt={tDb(space, 'name')}
              />
              <div className="card-body">
                <h3>
                  {/* full-link làm cả thẻ bấm được, xem class trong global.css */}
                  <Link to={`/khong-gian/${space.slug}`} className="full-link" data-test="space-card-link">
                    {tDb(space, 'name')}
                  </Link>
                </h3>
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
                <div
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    marginTop: 16,
                  }}
                >
                  <strong style={{ color: 'var(--green-800)' }}>
                    {dd.tien(space.rentalFee)}
                    <small className="muted">
                      {' '}
                      / {space.feeUnit === 'HUT' ? t('spaces.perHut') : t('spaces.perSession')}
                    </small>
                  </strong>
                  <Link to={`/khong-gian/${space.slug}`} className="btn btn-outline btn-sm">
                    {t('spaces.viewDetail')}
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
