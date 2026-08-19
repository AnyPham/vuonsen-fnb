  import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import {
  fetchCategories,
  fetchDishes,
  selectCategories,
  selectDishes,
} from '@/features/catalog/catalogSlice';
import { formatCurrency } from '@/utils/format';
import { Empty, Loading } from '@/components/common/StateBlock';

// Thực đơn chia tab theo danh mục món
export default function MenuPage() {
  const dispatch = useDispatch();
  const categories = useSelector(selectCategories);
  const { items, status, activeCategory } = useSelector(selectDishes);

  useEffect(() => {
    dispatch(fetchCategories());
    dispatch(fetchDishes({}));
  }, [dispatch]);

  return (
    <section className="section">
      <div className="wrap">
        <div className="section-head center">
          <div className="eyebrow center">Thực đơn</div>
          <h2>Món quê nấu bằng tay, dọn nóng tại bàn</h2>
          <p className="muted">
            Thực đơn thay đổi theo mùa. Giá dưới đây áp dụng cho phần ăn tại nhà hàng; tiệc theo mâm
            xem ở phần Gói tiệc.
          </p>
        </div>

        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 10, justifyContent: 'center', marginBottom: 34 }}>
          <button
            type="button"
            className={`btn btn-sm ${!activeCategory ? 'btn-dark' : 'btn-outline'}`}
            onClick={() => dispatch(fetchDishes({}))}
          >
            Tất cả
          </button>
          {categories.map((category) => (
            <button
              key={category.code}
              type="button"
              className={`btn btn-sm ${activeCategory === category.code ? 'btn-dark' : 'btn-outline'}`}
              onClick={() => dispatch(fetchDishes({ category: category.code }))}
            >
              {category.name}
            </button>
          ))}
        </div>

        {status === 'loading' && <Loading />}
        {status === 'succeeded' && items.length === 0 && <Empty label="Danh mục này chưa có món." />}

        <div className="grid grid-2">
          {items.map((dish) => (
            <div
              key={dish.id}
              className="card"
              style={{ display: 'flex', justifyContent: 'space-between', gap: 16, padding: '16px 20px' }}
            >
              <div>
                <strong>
                  {dish.name}
                  {dish.bestSeller && (
                    <span className="tag tag-PENDING" style={{ marginLeft: 8 }}>
                      Best
                    </span>
                  )}
                </strong>
                <p className="muted" style={{ fontSize: '0.88rem' }}>
                  {dish.description}
                </p>
              </div>
              <strong style={{ whiteSpace: 'nowrap', color: 'var(--green-800)' }}>
                {dish.price ? formatCurrency(dish.price) : dish.priceNote}
              </strong>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
