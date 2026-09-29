import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import {
  fetchCategories,
  fetchDishes,
  selectCategories,
  selectDishes,
} from '@/features/catalog/catalogSlice';
import { menuApi } from '@/api/endpoints';
import { Empty, Loading } from '@/components/common/StateBlock';
import Thumb from '@/components/common/Thumb';
import { useI18n } from '@/i18n';
import { useDinhDang } from '@/i18n/dinhDang';

// Thực đơn chia tab theo danh mục món
export default function MenuPage() {
  const dispatch = useDispatch();
  const categories = useSelector(selectCategories);
  const { items, status, activeCategory } = useSelector(selectDishes);

  // Món bán chạy lấy riêng, không qua Redux vì chỉ trang này dùng
  const [bestSellers, setBestSellers] = useState([]);
  const [tuKhoa, setTuKhoa] = useState('');
  const { t, tDb } = useI18n();
  const dd = useDinhDang();

  // Món tính giá linh hoạt thì không có giá, hiện ghi chú cách tính thay cho con số
  const hienGia = (dish) => (dish.price ? dd.tien(dish.price) : tDb(dish, 'priceNote'));

  /*
   * Chờ 350ms sau lần gõ cuối mới gọi máy chủ.
   *
   * Gọi ngay mỗi lần gõ thì tên món mười ký tự sinh ra mười lượt gọi, phần lớn là thừa.
   * Bộ lọc trong Redux đã bỏ qua kết quả về muộn nên không lo hiện nhầm kết quả cũ.
   */
  useEffect(() => {
    if (!tuKhoa) return undefined;
    const hen = setTimeout(() => dispatch(fetchDishes({ keyword: tuKhoa })), 350);
    return () => clearTimeout(hen);
  }, [dispatch, tuKhoa]);

  useEffect(() => {
    dispatch(fetchCategories());
    dispatch(fetchDishes({}));
    menuApi.bestSellers().then(setBestSellers).catch(() => setBestSellers([]));
  }, [dispatch]);

  return (
    <section className="section">
      <div className="wrap">
        <div className="section-head center">
          <div className="eyebrow center">{t('menu.eyebrow')}</div>
          <h2>{t('menu.title')}</h2>
          <p className="muted">
            {t('menu.desc')}
          </p>
        </div>

        {bestSellers.length > 0 && (
          <div style={{ marginBottom: 40 }} data-test="best-sellers">
            <div className="eyebrow center">{t('menu.bestSellers')}</div>
            <div className="grid grid-3">
              {bestSellers.map((dish) => (
                <div key={dish.id} className="card">
                  <Thumb
                    url={dish.imageUrl}
                    variant="v3"
                    icon="🍲"
                    label={tDb(dish, 'categoryName')}
                    alt={tDb(dish, 'name')}
                  />
                  <div className="card-body">
                    <strong>{tDb(dish, 'name')}</strong>
                    <p className="muted" style={{ fontSize: '0.88rem', margin: '8px 0 12px' }}>
                      {tDb(dish, 'description')}
                    </p>
                    <strong style={{ color: 'var(--green-800)' }}>
                      {hienGia(dish)}
                    </strong>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        <div className="fgroup" style={{ maxWidth: 420, margin: '0 auto 20px' }}>
          <label htmlFor="tim-mon">{t('menu.searchLabel')}</label>
          <input
            id="tim-mon"
            data-test="dish-search"
            type="search"
            value={tuKhoa}
            placeholder={t('menu.searchPlaceholder')}
            onChange={(e) => setTuKhoa(e.target.value)}
          />
        </div>

        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 10, justifyContent: 'center', marginBottom: 34 }}>
          <button
            type="button"
            className={`btn btn-sm ${!activeCategory && !tuKhoa ? 'btn-dark' : 'btn-outline'}`}
            onClick={() => { setTuKhoa(''); dispatch(fetchDishes({})); }}
            data-test="category-tab-all"
          >
            {t('menu.all')}
          </button>
          {categories.map((category) => (
            <button
              key={category.code}
              type="button"
              className={`btn btn-sm ${activeCategory === category.code ? 'btn-dark' : 'btn-outline'}`}
              onClick={() => { setTuKhoa(''); dispatch(fetchDishes({ category: category.code })); }}
              data-test={`category-tab-${category.code}`}
            >
              {tDb(category, 'name')}
            </button>
          ))}
        </div>

        {status === 'loading' && <Loading />}
        {status === 'succeeded' && items.length === 0 && (
          <Empty label={tuKhoa ? t('menu.noMatch', { tuKhoa }) : t('menu.emptyCategory')} />
        )}

        <div className="grid grid-2">
          {items.map((dish) => (
            <div
              key={dish.id}
              className="card card-clickable"
              data-test="dish-card"
              style={{ display: 'flex', justifyContent: 'space-between', gap: 16, padding: '16px 20px' }}
            >
              <div>
                <strong>
                  {/* Bấm vào đâu trong thẻ cũng mở được trang chi tiết món */}
                  <Link className="full-link" to={`/thuc-don/${dish.slug}`} data-test="dish-card-link">
                    {tDb(dish, 'name')}
                  </Link>
                  {dish.bestSeller && (
                    <span className="tag tag-PENDING" style={{ marginLeft: 8 }}>
                      {t('menu.best')}
                    </span>
                  )}
                </strong>
                <p className="muted" style={{ fontSize: '0.88rem' }}>
                  {tDb(dish, 'description')}
                </p>
              </div>
              <strong style={{ whiteSpace: 'nowrap', color: 'var(--green-800)' }}>
                {hienGia(dish)}
              </strong>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
