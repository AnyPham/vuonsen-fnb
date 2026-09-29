import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import { menuApi } from '@/api/endpoints';
import { ErrorBlock, Loading } from '@/components/common/StateBlock';
import { useI18n } from '@/i18n';
import { useDinhDang } from '@/i18n/dinhDang';
import Thumb from '@/components/common/Thumb';
import { themMon } from '@/features/dishorder/cartSlice';

/*
 * Trang chi tiết một món, vào bằng đường dẫn /thuc-don/<slug>.
 *
 * Trước đây khách chỉ thấy tên món, một dòng mô tả và giá. Muốn biết món có gì
 * trong đó, làm mất bao lâu, có cay không thì phải gọi điện hỏi. Trang này trả lời
 * sẵn những câu đó.
 */
export default function DishDetailPage() {
  const { slug } = useParams();
  const [mon, setMon] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);
  const [anhDangXem, setAnhDangXem] = useState(null);
  const [soLuong, setSoLuong] = useState(1);
  // Hiện chữ đã thêm một lát rồi trả nút về như cũ, để khách biết bấm đã ăn
  const [daThem, setDaThem] = useState(false);
  const dispatch = useDispatch();
  const { t, tDb } = useI18n();
  const dd = useDinhDang();

  useEffect(() => {
    setLoading(true);
    setError(null);
    setAnhDangXem(null);
    setSoLuong(1);
    setDaThem(false);
    menuApi
      .detail(slug)
      .then(setMon)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [slug]);

  const themVaoGio = () => {
    dispatch(themMon({
      dishId: mon.id, name: tDb(mon, 'name'), price: mon.price,
      slug: mon.slug, imageUrl: mon.imageUrl, quantity: soLuong,
    }));
    setDaThem(true);
    setTimeout(() => setDaThem(false), 2000);
  };

  if (loading) return <Loading label={t('dish.loading')} />;

  if (error) {
    return (
      <section className="section">
        <div className="wrap" style={{ maxWidth: 640 }}>
          <ErrorBlock message={error} />
          <p style={{ marginTop: 16 }}>
            <Link to="/thuc-don">{t('dish.backToMenu')}</Link>
          </p>
        </div>
      </section>
    );
  }

  if (!mon) return null;

  return (
    <section className="section">
      <div className="wrap">
        <p style={{ marginBottom: 14 }}>
          <Link to="/thuc-don">{t('dish.menu')}</Link>
        </p>

        <div className="grid grid-2" style={{ alignItems: 'start' }}>
          <div>
            <Thumb
              url={anhDangXem?.url || mon.imageUrl}
              test="main-image"
              variant="v2"
              icon="🍲"
              label={tDb(mon, 'name')}
              alt={anhDangXem?.caption || tDb(mon, 'name')}
              style={{ borderRadius: 'var(--r)', marginBottom: anhDangXem?.caption ? 8 : 12 }}
            />

            {anhDangXem?.caption && (
              <p className="muted" style={{ fontSize: '0.84rem', marginBottom: 12 }}>
                {anhDangXem.caption}
              </p>
            )}

            {mon.images?.length > 0 && (
              <div className="dai-anh" style={{ marginBottom: 22 }}>
                <button
                  type="button"
                  className={anhDangXem === null ? 'dang-chon' : ''}
                  onClick={() => setAnhDangXem(null)}
                  aria-label={t('dish.mainImage')}
                >
                  <Thumb url={mon.imageUrl} icon="🍲" alt={tDb(mon, 'name')} />
                </button>
                {mon.images.map((anh) => (
                  <button
                    type="button"
                    key={anh.url}
                    className={anhDangXem?.url === anh.url ? 'dang-chon' : ''}
                    onClick={() => setAnhDangXem(anh)}
                    data-test="thumb"
                    aria-label={anh.caption || t('dish.viewImage')}
                  >
                    <Thumb url={anh.url} icon="🍲" alt={anh.caption || tDb(mon, 'name')} />
                  </button>
                ))}
              </div>
            )}

            <div className="section-head" style={{ marginBottom: 14 }}>
              <div className="eyebrow">{tDb(mon, 'categoryName')}</div>
              <h2 data-test="dish-name">{tDb(mon, 'name')}</h2>
            </div>

            {mon.description && <p className="muted">{tDb(mon, 'description')}</p>}

            {mon.ingredients && (
              <>
                <h3 style={{ marginTop: 24, marginBottom: 8 }}>{t('dish.ingredients')}</h3>
                <p className="muted" data-test="ingredients">{tDb(mon, 'ingredients')}</p>
              </>
            )}

            {mon.preparation && (
              <>
                <h3 style={{ marginTop: 24, marginBottom: 8 }}>{t('dish.preparation')}</h3>
                <p className="muted" data-test="preparation">{tDb(mon, 'preparation')}</p>
              </>
            )}
          </div>

          <div>
            <div className="card" style={{ marginBottom: 18 }}>
              <div className="card-body">
                <h3 style={{ marginTop: 0 }}>{t('dish.orderInfo')}</h3>
                <table className="bang-thong-so">
                  <tbody>
                    <tr>
                      <th>{t('dish.price')}</th>
                      <td data-test="dish-price">
                        {mon.price != null ? dd.tien(mon.price) : tDb(mon, 'priceNote') || t('dish.contactForPrice')}
                      </td>
                    </tr>
                    {mon.portionDesc && (
                      <tr>
                        <th>{t('dish.portion')}</th>
                        <td data-test="portion">{tDb(mon, 'portionDesc')}</td>
                      </tr>
                    )}
                    {mon.prepMinutes != null && (
                      <tr>
                        <th>{t('dish.kitchenNeeds')}</th>
                        <td data-test="prep-time">
                          {mon.prepMinutes >= 60
                            ? t('dish.aboutHours', { n: Math.round((mon.prepMinutes / 60) * 10) / 10 })
                            : t('dish.aboutMinutes', { n: mon.prepMinutes })}
                        </td>
                      </tr>
                    )}
                    <tr>
                      <th>{t('dish.category')}</th>
                      <td>{tDb(mon, 'categoryName')}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            {/* Phần dễ bị bỏ qua nhất nhưng lại hay gây phiền nhất nếu khách
                không đọc, nên để riêng một khối có viền cho nổi. */}
            {mon.orderNote && (
              <div className="luu-y-dat-mon" data-test="order-note">
                <strong>{t('dish.noteTitle')}</strong>
                <p>{tDb(mon, 'orderNote')}</p>
              </div>
            )}

            {/* Món tính giá theo cân không chốt được tiền lúc đặt nên không cho
                thêm vào giỏ, chỉ đường sang đặt tiệc thay vì để khách đặt rồi mới báo lại. */}
            {mon.price != null ? (
              <>
                <div className="them-vao-gio">
                  <div className="dieu-chinh-so">
                    <button type="button" onClick={() => setSoLuong((n) => Math.max(1, n - 1))} aria-label={t('dish.decrease')}>
                      −
                    </button>
                    <span>{soLuong}</span>
                    <button type="button" onClick={() => setSoLuong((n) => n + 1)} aria-label={t('dish.increase')}>
                      +
                    </button>
                  </div>
                  <button type="button" className="btn btn-dark" style={{ flex: 1 }} onClick={themVaoGio} data-test="add-to-cart">
                    {daThem ? t('dish.added') : t('dish.addToOrder')}
                  </button>
                </div>
                <Link className="btn btn-outline btn-sm" to="/dat-mon" style={{ width: '100%' }}>
                  {t('dish.viewCart')}
                </Link>
              </>
            ) : (
              <>
                <p className="muted" style={{ fontSize: '0.86rem' }}>
                  {t('dish.byWeightNote')}
                </p>
                <Link className="btn btn-dark" to="/dat-tiec" style={{ width: '100%' }} data-test="book-with-dish">
                  {t('dish.bookWithDish')}
                </Link>
              </>
            )}
          </div>
        </div>
      </div>
    </section>
  );
}
