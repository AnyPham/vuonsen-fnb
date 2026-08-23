import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Link } from 'react-router-dom';
import { selectUser, updateProfile } from '@/features/auth/authSlice';
import { ErrorBlock, Loading } from '@/components/common/StateBlock';

// Nhãn tiếng Việt cho quyền, tránh hiện ADMIN hay CUSTOMER ra ngoài giao diện
const ROLE_LABELS = {
  CUSTOMER: 'Khách hàng',
  STAFF: 'Nhân viên',
  ADMIN: 'Quản trị viên',
};

// Trang hồ sơ cá nhân, chỉ vào được khi đã đăng nhập
export default function ProfilePage() {
  const dispatch = useDispatch();
  const user = useSelector(selectUser);

  const [form, setForm] = useState({ fullName: '', phone: '', address: '' });
  const [status, setStatus] = useState('idle');
  const [error, setError] = useState(null);
  const [fieldErrors, setFieldErrors] = useState({});

  // Đổ dữ liệu tài khoản vào form khi mở trang
  useEffect(() => {
    if (user) {
      setForm({
        fullName: user.fullName || '',
        phone: user.phone || '',
        address: user.address || '',
      });
    }
  }, [user]);

  if (!user) return <Loading label="Đang tải hồ sơ…" />;

  const set = (patch) => {
    setForm((prev) => ({ ...prev, ...patch }));
    setStatus('idle');
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setStatus('saving');
    setError(null);
    setFieldErrors({});

    const result = await dispatch(
      updateProfile({
        fullName: form.fullName.trim(),
        phone: form.phone.trim() || null,
        address: form.address.trim() || null,
      }),
    );

    if (updateProfile.fulfilled.match(result)) {
      setStatus('saved');
    } else {
      setStatus('failed');
      setError(result.payload?.message);
      setFieldErrors(result.payload?.fieldErrors || {});
    }
  };

  const changed =
    form.fullName !== (user.fullName || '') ||
    form.phone !== (user.phone || '') ||
    form.address !== (user.address || '');

  return (
    <section className="section">
      <div className="wrap" style={{ maxWidth: 720 }}>
        <div className="section-head">
          <div className="eyebrow">Tài khoản</div>
          <h2>Hồ sơ cá nhân</h2>
          <p className="muted">
            Thông tin này được điền sẵn khi bạn đặt tiệc, đỡ phải nhập lại mỗi lần.
          </p>
        </div>

        <div className="card">
          <div className="card-body">
            {error && <ErrorBlock message={error} />}
            {status === 'saved' && (
              <div className="alert alert-success">Đã lưu thay đổi.</div>
            )}

            <form onSubmit={handleSubmit}>
              <div className="fgroup">
                <label htmlFor="fullName">Họ và tên *</label>
                <input
                  id="fullName"
                  value={form.fullName}
                  onChange={(e) => set({ fullName: e.target.value })}
                />
                {fieldErrors.fullName && <div className="err">{fieldErrors.fullName}</div>}
              </div>

              <div className="form-row">
                <div className="fgroup">
                  <label htmlFor="phone">Số điện thoại</label>
                  <input
                    id="phone"
                    type="tel"
                    placeholder="09xxxxxxxx"
                    value={form.phone}
                    onChange={(e) => set({ phone: e.target.value })}
                  />
                  {fieldErrors.phone && <div className="err">{fieldErrors.phone}</div>}
                </div>

                <div className="fgroup">
                  <label htmlFor="email">Email</label>
                  <input id="email" value={user.email} disabled />
                  <div className="muted" style={{ fontSize: '0.82rem', marginTop: 6 }}>
                    Email dùng để đăng nhập nên không đổi được
                  </div>
                </div>
              </div>

              <div className="fgroup">
                <label htmlFor="address">Địa chỉ</label>
                <input
                  id="address"
                  placeholder="Số nhà, đường, phường, quận"
                  value={form.address}
                  onChange={(e) => set({ address: e.target.value })}
                />
                {fieldErrors.address && <div className="err">{fieldErrors.address}</div>}
              </div>

              <div className="fgroup">
                <label>Quyền tài khoản</label>
                <div>
                  <span className="chip">{ROLE_LABELS[user.role] || user.role}</span>
                </div>
              </div>

              <div className="fnav">
                <Link to="/don-cua-toi" className="btn btn-ghost">
                  Xem đơn của tôi
                </Link>
                <button
                  type="submit"
                  className="btn btn-dark"
                  disabled={status === 'saving' || !changed}
                >
                  {status === 'saving' ? 'Đang lưu…' : 'Lưu thay đổi'}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </section>
  );
}
