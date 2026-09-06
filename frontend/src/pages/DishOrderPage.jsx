import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { dishOrderApi } from '@/api/endpoints';
import { formatCurrency } from '@/utils/format';
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

  // Hỏi lại máy chủ mỗi khi giỏ hoặc hình thức nhận thay đổi
  useEffect(() => {
    if (gio.length === 0) {
      setTamTinh(null);
      setLoiTamTinh(null);
      return;
    }
    setDangTinh(true);
    setLoiTamTinh(null);
    dishOrderApi
      .quote({
        fulfillmentType: hinhThuc,
        items: gio.map((m) => ({ dishId: m.dishId, quantity: m.quantity })),
      })
      .then(setTamTinh)
      .catch((err) => {
        setTamTinh(null);
        setLoiTamTinh(err.message);
      })
      .finally(() => setDangTinh(false));
  }, [gio, hinhThuc]);

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
          <h2>Giỏ món đang trống</h2>
          <p className="muted">
            Bạn vào thực đơn chọn món, hoặc mở trang chi tiết từng món để xem nguyên
            liệu và lưu ý trước khi đặt.
          </p>
          <Link className="btn btn-dark" to="/thuc-don" style={{ marginTop: 12 }}>
            Xem thực đơn
          </Link>
        </div>
      </section>
    );
  }

  return (
    <section className="section">
      <div className="wrap">
        <div className="section-head" style={{ marginBottom: 20 }}>
          <div className="eyebrow">Đặt món</div>
          <h2>Giỏ món của bạn</h2>
        </div>

        <div className="grid grid-2" style={{ alignItems: 'start' }}>
          {/* ---------- Cột trái: giỏ và thông tin ---------- */}
          <div>
            {gio.map((m) => (
              <div key={m.dishId} className="dong-gio">
                <Thumb url={m.imageUrl} icon="🍲" alt={m.name} />
                <div style={{ flex: 1 }}>
                  <strong>
                    <Link to={`/thuc-don/${m.slug}`}>{m.name}</Link>
                  </strong>
                  <div className="muted" style={{ fontSize: '0.86rem' }}>
                    {formatCurrency(m.price)} / phần
                  </div>
                </div>
                <div className="dieu-chinh-so">
                  <button
                    type="button"
                    onClick={() => dispatch(doiSoLuong({ dishId: m.dishId, quantity: m.quantity - 1 }))}
                    aria-label={`Bớt một phần ${m.name}`}
                  >
                    −
                  </button>
                  <span>{m.quantity}</span>
                  <button
                    type="button"
                    onClick={() => dispatch(doiSoLuong({ dishId: m.dishId, quantity: m.quantity + 1 }))}
                    aria-label={`Thêm một phần ${m.name}`}
                  >
                    +
                  </button>
                </div>
                <button
                  type="button"
                  className="bo-mon"
                  onClick={() => dispatch(boMon(m.dishId))}
                  aria-label={`Bỏ ${m.name} khỏi giỏ`}
                >
                  ×
                </button>
              </div>
            ))}

            <form onSubmit={gui} style={{ marginTop: 24 }}>
              <h3>Hình thức nhận món</h3>
              <div className="chon-hinh-thuc">
                {[
                  ['DELIVERY', '🛵 Giao tận nhà', 'Bên mình mang tới địa chỉ bạn cho'],
                  ['DINE_IN', '🍽️ Tới ăn tại chỗ', 'Đặt trước để bếp chuẩn bị, tới là có ngay'],
                ].map(([ma, nhan, mo]) => (
                  <button
                    type="button"
                    key={ma}
                    className={hinhThuc === ma ? 'dang-chon' : ''}
                    onClick={() => dispatch(doiHinhThuc(ma))}
                  >
                    <strong>{nhan}</strong>
                    <span className="muted">{mo}</span>
                  </button>
                ))}
              </div>

              <div className="form-row" style={{ marginTop: 16 }}>
                <div className="fgroup">
                  <label htmlFor="ten">Tên người đặt *</label>
                  <input
                    id="ten"
                    value={form.customerName}
                    onChange={(e) => set({ customerName: e.target.value })}
                    required
                    maxLength={120}
                  />
                </div>
                <div className="fgroup">
                  <label htmlFor="dienThoai">Số điện thoại *</label>
                  <input
                    id="dienThoai"
                    value={form.customerPhone}
                    onChange={(e) => set({ customerPhone: e.target.value })}
                    required
                    placeholder="0901234567"
                  />
                </div>
              </div>

              <div className="fgroup">
                <label htmlFor="email">Email (không bắt buộc)</label>
                <input
                  id="email"
                  type="email"
                  value={form.customerEmail}
                  onChange={(e) => set({ customerEmail: e.target.value })}
                  maxLength={160}
                />
              </div>

              <div className="form-row">
                {hinhThuc === 'DELIVERY' ? (
                  <div className="fgroup">
                    <label htmlFor="diaChi">Địa chỉ giao *</label>
                    <input
                      id="diaChi"
                      value={form.deliveryAddress}
                      onChange={(e) => set({ deliveryAddress: e.target.value })}
                      required
                      maxLength={400}
                      placeholder="Số nhà, đường, phường, quận"
                    />
                  </div>
                ) : (
                  <div className="fgroup">
                    <label htmlFor="soKhach">Số khách *</label>
                    <input
                      id="soKhach"
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
                    {hinhThuc === 'DELIVERY' ? 'Thời điểm muốn nhận *' : 'Thời điểm tới ăn *'}
                  </label>
                  <input
                    id="thoiDiem"
                    type="datetime-local"
                    value={form.serveAt}
                    onChange={(e) => set({ serveAt: e.target.value })}
                    required
                  />
                </div>
              </div>

              <div className="fgroup">
                <label htmlFor="ghiChu">Ghi chú cho bếp</label>
                <textarea
                  id="ghiChu"
                  rows={3}
                  value={form.note}
                  onChange={(e) => set({ note: e.target.value })}
                  maxLength={600}
                  placeholder="Ví dụ: không cay, ít mặn, có trẻ nhỏ…"
                />
              </div>

              {loiGui && <ErrorBlock message={loiGui} />}

              <button
                type="submit"
                className="btn btn-dark"
                style={{ width: '100%', marginTop: 8 }}
                disabled={dangGui || !tamTinh}
              >
                {dangGui ? 'Đang gửi…' : 'Gửi đơn đặt món'}
              </button>
              <p className="muted" style={{ fontSize: '0.82rem', marginTop: 8 }}>
                Gửi đơn xong bạn nhận mã đơn để tra cứu. Bên mình sẽ gọi xác nhận, chưa
                thu tiền trên website.
              </p>
            </form>
          </div>

          {/* ---------- Cột phải: bảng tạm tính ---------- */}
          <div>
            <div className="card" style={{ position: 'sticky', top: 90 }}>
              <div className="card-body">
                <h3 style={{ marginTop: 0 }}>Tạm tính</h3>

                {loiTamTinh && <ErrorBlock message={loiTamTinh} />}

                {dangTinh && <p className="muted">Đang tính lại…</p>}

                {tamTinh && (
                  <>
                    <table className="bang-thong-so">
                      <tbody>
                        {tamTinh.lines.map((d) => (
                          <tr key={d.dishId}>
                            <th style={{ fontWeight: 400 }}>
                              {d.dishName} × {d.quantity}
                            </th>
                            <td style={{ textAlign: 'right' }}>{formatCurrency(d.lineTotal)}</td>
                          </tr>
                        ))}
                        <tr>
                          <th>Tiền món</th>
                          <td style={{ textAlign: 'right' }}>{formatCurrency(tamTinh.subtotal)}</td>
                        </tr>
                        <tr>
                          <th>Phí giao</th>
                          <td style={{ textAlign: 'right' }}>
                            {Number(tamTinh.deliveryFee) === 0 ? 'Miễn phí' : formatCurrency(tamTinh.deliveryFee)}
                          </td>
                        </tr>
                        <tr>
                          <th>Thuế giá trị gia tăng</th>
                          <td style={{ textAlign: 'right' }}>{formatCurrency(tamTinh.vatAmount)}</td>
                        </tr>
                        <tr>
                          <th style={{ fontWeight: 600 }}>Tổng cộng</th>
                          <td style={{ textAlign: 'right', fontWeight: 600 }}>
                            {formatCurrency(tamTinh.total)}
                          </td>
                        </tr>
                      </tbody>
                    </table>

                    <p className="muted" style={{ fontSize: '0.84rem', marginTop: 12 }}>
                      {tamTinh.deliveryNote}
                    </p>
                    <p className="muted" style={{ fontSize: '0.84rem' }}>
                      {tamTinh.leadTimeNote}
                    </p>
                  </>
                )}

                <button
                  type="button"
                  className="btn btn-outline btn-sm"
                  style={{ width: '100%', marginTop: 10 }}
                  onClick={() => dispatch(xoaGio())}
                >
                  Xóa hết giỏ
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
