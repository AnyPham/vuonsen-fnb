import { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { NavLink, useNavigate } from 'react-router-dom';
import { logout, selectIsAdmin, selectUser } from '@/features/auth/authSlice';

const NAV_ITEMS = [
  { to: '/khong-gian', label: 'Không gian' },
  { to: '/thuc-don', label: 'Thực đơn' },
  { to: '/goi-tiec', label: 'Gói tiệc' },
  { to: '/thu-vien', label: 'Thư viện' },
  { to: '/tra-cuu', label: 'Tra cứu đơn' },
];

export default function Header() {
  const [open, setOpen] = useState(false);
  const user = useSelector(selectUser);
  const isAdmin = useSelector(selectIsAdmin);
  const dispatch = useDispatch();
  const navigate = useNavigate();

  const close = () => setOpen(false);

  const handleLogout = async () => {
    await dispatch(logout());
    close();
    navigate('/');
  };

  return (
    <header className="nav">
      <div className="wrap nav-inner">
        <NavLink to="/" className="logo" onClick={close}>
          🌿 Vườn Sen
        </NavLink>

        <nav className={`menu ${open ? 'open' : ''}`}>
          {NAV_ITEMS.map((item) => (
            <NavLink key={item.to} to={item.to} onClick={close}>
              {item.label}
            </NavLink>
          ))}

          {user ? (
            <>
              <NavLink to="/ho-so" onClick={close}>
                Hồ sơ
              </NavLink>
              <NavLink to="/don-cua-toi" onClick={close}>
                Đơn của tôi
              </NavLink>
              {isAdmin && (
                <NavLink to="/quan-tri/don-dat-tiec" onClick={close}>
                  Quản trị
                </NavLink>
              )}
              <button type="button" className="btn btn-ghost btn-sm" onClick={handleLogout}>
                Đăng xuất ({user.fullName})
              </button>
            </>
          ) : (
            <NavLink to="/dang-nhap" onClick={close}>
              Đăng nhập
            </NavLink>
          )}

          <NavLink to="/dat-tiec" className="btn btn-gold btn-sm" onClick={close}>
            Đặt tiệc
          </NavLink>
        </nav>

        <button
          type="button"
          className="burger"
          aria-label="Mở menu"
          aria-expanded={open}
          onClick={() => setOpen((v) => !v)}
        >
          ☰
        </button>
      </div>
    </header>
  );
}
