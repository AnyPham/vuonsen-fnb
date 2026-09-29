import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { dishOrderApi } from '@/api/endpoints';
import { useI18n } from '@/i18n';
import { useDinhDang } from '@/i18n/dinhDang';
import { ErrorBlock } from '@/components/common/StateBlock';
import Thumb from '@/components/common/Thumb';
import {
  boMon, chonGioMon, chonHinhThuc, doiHinhThuc, doiSoLuong, xoaGio,
} from '@/features/dishorder/cartSlice';

// Giờ mặc định gợi ý: tròn giờ kế tiếp cách hiện tại 3 tiếng, đủ cho hầu hết món
function gioMacDinh() {
  const t = new Date(Date.now() + 3 * 60 * 60 * 1000);
  t.setMinutes(0, 0, 0);
  const p = (n) => String(n).padStart(2, '0');
  return `${t.getFullYear()}-${p(t.getMonth() + 1)}-${p(t.getDate())}T${p(t.getHours())}:00`;
}

/*
 * Trang đặt món lẻ.
 *
 * Khác trang đặt tiệc ở chỗ khách tự chọn từng món thay vì lấy trọn một gói. Bảng
 * tạm tính hỏi lại máy chủ mỗi khi giỏ hoặc hình thức nhận đổi, để con số khách thấy
 * luôn là con số máy chủ sẽ tính, không phải ước lượng phía trình duyệt.
 */
export default function DishOrderPage() {
  const gio = useSelector(chonGioMon);
  const hinhThuc = useSelector(chonHinhThuc);
  const dispatch = useDispatch();
  const { t, tDb } = useI18n();
  const dd = useDinhDang();
  const navigate = useNavigate();

  const [tamTinh, setTamTinh] = useState(null);
  const [loiTamTinh, setLoiTamTinh] = useState(null);
  const [dangTinh, setDangTinh] = useState(false);

  const [form, setForm] = useState({
    customerName: '', customerPhone: '', customerEmail: '',
    deliveryAddress: '', guestCount: 4, serveAt: gioMacDinh(), note: '',
  });
  const [dangGui, setDangGui] = useState(false);
  const [loiGui, setLoiGui] = useState(null);

  // Hỏi lại máy chủ mỗi khi giỏ, hình thức nhận hoặc giờ nhận thay đổi
  useEffect(() => {
    if (gio.length === 0) {
      setTamTinh(null);
      setLoiTamTinh(null);
      // Yêu cầu đang bay (nếu có) đã bị bỏ qua nên không tự tắt trạng thái đang tính được
      setDangTinh(false);
      return;
    }
    /*
     * Khách đổi lựa chọn liên tiếp thì có nhiều yêu cầu cùng bay, phản hồi không chắc về đúng
     * thứ tự gửi. Phản hồi cũ về sau mà vẫn được ghi thì đè mất kết quả của lựa chọn mới nhất,
     * ví dụ đã chọn ngày lễ mà bảng lại hiện giá không giảm. Lựa chọn đổi thì effect cũ bị dọn,
     * cờ conHieuLuc tắt, phản hồi của nó về lúc nào cũng bị bỏ qua.
     */
    let conHieuLuc = true;
    setDangTinh(true);
    setLoiTamTinh(null);
    dishOrderApi
      .quote({
        fulfillmentType: hinhThuc,
        // Gửi kèm giờ nhận để máy chủ biết ngày đó có được giảm giá dịp lễ không
        serveAt: form.serveAt || null,
        items: gio.map((m) => ({ dishId: m.dishId, quantity: m.quantity })),
      })
      .then((kq) => {
        if (conHieuLuc) setTamTinh(kq);
      })
      .catch((err) => {
        if (!conHieuLuc) return;
        setTamTinh(null);
        setLoiTamTinh(err.message);
      })
      .finally(() => {
        if (conHieuLuc) setDangTinh(false);
      });
    return () => {
      conHieuLuc = false;
    };
  }, [gio, hinhThuc, form.serveAt]);

  const set = (patch) => {
    setForm((p) => ({ ...p, ...patch }));
    setLoiGui(null);
  };

  const gui = async (e) => {
    e.preventDefault();
    setDangGui(true);
    setLoiGui(null);
    try {
      const kq = await dishOrderApi.create({
        fulfillmentType: hinhThuc,
        customerName: form.customerName.trim(),
        customerPhone: form.customerPhone.trim(),
        customerEmail: form.customerEmail.trim() || null,
        deliveryAddress: hinhThuc === 'DELIVERY' ? form.deliveryAddress.trim() : null,
        guestCount: hinhThuc === 'DINE_IN' ? Number(form.guestCount) : null,
        serveAt: form.serveAt,
        note: form.note.trim() || null,
        items: gio.map((m) => ({ dishId: m.dishId, quantity: m.quantity })),
      });
      dispatch(xoaGio());
      navigate(`/tra-cuu-mon?ma=${kq.code}`);
    } catch (err) {
      setLoiGui(err.message);
    } finally {
      setDangGui(false);
    }
  };

  if (gio.length === 0) {
    return (
      <section className="section">
        <div className="wrap" style={{ maxWidth: 640, textAlign: 'center' }}>
          <h2 data-test="cart-empty">{t('order.cartEmpty')}</h2>
          <p className="muted">
            {t('order.cartEmptyDesc')}
          </p>
          <Link className="btn btn-dark" to="/thuc-don" style={{ marginTop: 12 }}>
            {t('order.viewMenu')}
          </Link>
        </div>
      </section>
    );
  }

  return (
    <section className="section">
      <div className="wrap">
        <div className="section-head" style={{ marginBottom: 20 }}>
          <div className="eyebrow">{t('order.eyebrow')}</div>
          <h2>{t('order.cartTitle')}</h2>
        </div>

        <div className="grid grid-2" style={{ alignItems: 'start' }}>
          {/* ---------- Cột trái: giỏ và thông tin ---------- */}
          <div>
            {gio.map((m) => (
              <div key={m.dishId} className="dong-gio" data-test="cart-row">
                <Thumb url={m.imageUrl} icon="🍲" alt={m.name} />
                <div style={{ flex: 1 }}>
                  <strong>
                    <Link to={`/thuc-don/${m.slug}`}>{m.name}</Link>
                  </strong>
                  <div className="muted" style={{ fontSize: '0.86rem' }}>
                    {dd.tien(m.price)} / {t('order.perPortion')}
                  </div>
                </div>
                <div className="dieu-chinh-so">
                  <button
                    type="button"
                    onClick={() => dispatch(doiSoLuong({ dishId: m.dishId, quantity: m.quantity - 1 }))}
                    data-test="cart-minus"
                    aria-label={t('order.decreaseOf', { mon: m.name })}
                  >
                    −
                  </button>
                  <span data-test="cart-qty">{m.quantity}</span>
                  <button
                    type="button"
                    onClick={() => dispatch(doiSoLuong({ dishId: m.dishId, quantity: m.quantity + 1 }))}
                    data-test="cart-plus"
                    aria-label={t('order.increaseOf', { mon: m.name })}
                  >
                    +
                  </button>
                </div>
                <button
                  type="button"
                  className="bo-mon"
                  onClick={() => dispatch(boMon(m.dishId))}
                  data-test="cart-remove"
                  aria-label={t('order.removeFromCart', { mon: m.name })}
                >
                  ×
                </button>
              </div>
            ))}

            <form onSubmit={gui} style={{ marginTop: 24 }}>
              <h3>{t('order.fulfillmentTitle')}</h3>
              <div className="chon-hinh-thuc">
                {[
                  ['DELIVERY', t('order.delivery'), t('order.deliveryDesc')],
                  ['DINE_IN', t('order.dineIn'), t('order.dineInDesc')],
                ].map(([ma, nhan, mo]) => (
                  <button
                    type="button"
                    key={ma}
                    className={hinhThuc === ma ? 'dang-chon' : ''}
                    onClick={() => dispatch(doiHinhThuc(ma))}
                    data-test={`fulfillment-${ma}`}
                  >
                    <strong>{nhan}</strong>
                    <span className="muted">{mo}</span>
                  </button>
                ))}
              </div>

              <div className="form-row" style={{ marginTop: 16 }}>
                <div className="fgroup">
                  <label htmlFor="ten">{t('order.name')}</label>
                  <input
                    id="ten"
                    data-test="order-name"
                    value={form.customerName}
                    onChange={(e) => set({ customerName: e.target.value })}
                    required
                    maxLength={120}
                  />
                </div>
                <div className="fgroup">
                  <label htmlFor="dienThoai">{t('order.phone')}</label>
                  <input
                    id="dienThoai"
                    data-test="order-phone"
                    value={form.customerPhone}
                    onChange={(e) => set({ customerPhone: e.target.value })}
                    required
                    placeholder="0901234567"
                  />
                </div>
              </div>

              <div className="fgroup">
                <label htmlFor="email">{t('order.email')}</label>
                <input
                  id="email"
                  data-test="order-email"
                  type="email"
                  value={form.customerEmail}
                  onChange={(e) => set({ customerEmail: e.target.value })}
                  maxLength={160}
                />
              </div>

              <div className="form-row">
                {hinhThuc === 'DELIVERY' ? (
                  <div className="fgroup">
                    <label htmlFor="diaChi">{t('order.address')}</label>
                    <input
                      id="diaChi"
                      data-test="order-address"
                      value={form.deliveryAddress}
                      onChange={(e) => set({ deliveryAddress: e.target.value })}
                      required
                      maxLength={400}
                      placeholder={t('order.addressPlaceholder')}
                    />
                  </div>
                ) : (
                  <div className="fgroup">
                    <label htmlFor="soKhach">{t('order.guests')}</label>
                    <input
                      id="soKhach"
                      data-test="order-guests"
                      type="number"
                      min={1}
                      max={40}
                      value={form.guestCount}
                      onChange={(e) => set({ guestCount: e.target.value })}
                      required
                    />
                  </div>
                )}

                <div className="fgroup">
                  <label htmlFor="thoiDiem">
                    {hinhThuc === 'DELIVERY' ? t('order.wantAt') : t('order.arriveAt')}
                  </label>
                  <input
                    id="thoiDiem"
                    data-test="order-serve-at"
                    type="datetime-local"
                    value={form.serveAt}
                    onChange={(e) => set({ serveAt: e.target.value })}
                    required
                  />
                </div>
              </div>

              <div className="fgroup">
                <label htmlFor="ghiChu">{t('order.kitchenNote')}</label>
                <textarea
                  id="ghiChu"
                  rows={3}
                  value={form.note}
                  onChange={(e) => set({ note: e.target.value })}
                  maxLength={600}
                  placeholder={t('order.kitchenNotePlaceholder')}
                />
              </div>

              {loiGui && <ErrorBlock message={loiGui} />}

              <button
                type="submit"
                className="btn btn-dark"
                style={{ width: '100%', marginTop: 8 }}
                disabled={dangGui || !tamTinh}
                data-test="submit-order"
              >
                {dangGui ? t('order.sending') : t('order.submit')}
              </button>
              <p className="muted" style={{ fontSize: '0.82rem', marginTop: 8 }}>
                {t('order.afterSubmit')}
              </p>
            </form>
          </div>

          {/* ---------- Cột phải: bảng tạm tính ---------- */}
          <div>
            <div className="card" style={{ position: 'sticky', top: 90 }}>
              <div className="card-body">
                <h3 style={{ marginTop: 0 }}>{t('order.estimateTitle')}</h3>

                {loiTamTinh && <ErrorBlock message={loiTamTinh} />}

                {dangTinh && <p className="muted">{t('order.recalculating')}</p>}

                {tamTinh && (
                  <>
                    <table className="bang-thong-so">
                      <tbody>
                        {tamTinh.lines.map((d) => (
                          <tr key={d.dishId}>
                            <th style={{ fontWeight: 400 }}>
                              {d.dishName} × {d.quantity}
                            </th>
                            <td style={{ textAlign: 'right' }}>{dd.tien(d.lineTotal)}</td>
                          </tr>
                        ))}
                        <tr>
                          <th>{t('trackDish.subtotal')}</th>
                          <td style={{ textAlign: 'right' }} data-test="order-subtotal">{dd.tien(tamTinh.subtotal)}</td>
                        </tr>
                        {Number(tamTinh.discountAmount) > 0 && (
                          <tr>
                            <th>{t('trackDish.holidayDiscount')}</th>
                            <td style={{ textAlign: 'right' }} data-test="order-discount">
                              − {dd.tien(tamTinh.discountAmount)}
                            </td>
                          </tr>
                        )}
                        <tr>
                          <th>{t('trackDish.deliveryFee')}</th>
                          <td style={{ textAlign: 'right' }} data-test="order-delivery-fee">
                            {Number(tamTinh.deliveryFee) === 0 ? t('track.free') : dd.tien(tamTinh.deliveryFee)}
                          </td>
                        </tr>
                        <tr>
                          <th>{t('trackDish.vat')}</th>
                          <td style={{ textAlign: 'right' }} data-test="order-vat">{dd.tien(tamTinh.vatAmount)}</td>
                        </tr>
                        <tr>
                          <th style={{ fontWeight: 600 }}>{t('track.total')}</th>
                          <td style={{ textAlign: 'right', fontWeight: 600 }} data-test="order-total">
                            {dd.tien(tamTinh.total)}
                          </td>
                        </tr>
                      </tbody>
                    </table>

                    {tamTinh.discountNote && (
                      <p className="muted" style={{ fontSize: '0.84rem', marginTop: 12 }} data-test="discount-note">
                        {tDb(tamTinh, 'discountNote')}
                      </p>
                    )}
                    <p className="muted" style={{ fontSize: '0.84rem', marginTop: 12 }} data-test="delivery-note">
                      {tDb(tamTinh, 'deliveryNote')}
                    </p>
                    <p className="muted" style={{ fontSize: '0.84rem' }}>
                      {tDb(tamTinh, 'leadTimeNote')}
                    </p>
                  </>
                )}

                <button
                  type="button"
                  className="btn btn-outline btn-sm"
                  style={{ width: '100%', marginTop: 10 }}
                  onClick={() => dispatch(xoaGio())}
                >
                  {t('order.clearCart')}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
