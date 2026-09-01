import { useEffect, useRef, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { NavLink, useNavigate } from 'react-router-dom';
import { logout, selectIsAdmin, selectUser } from '@/features/auth/authSlice';

const NAV_ITEMS = [
  { to: '/khong-gian', label: 'Không gian' },
  { to: '/thuc-don', label: 'Thực đơn' },
  { to: '/goi-tiec', label: 'Gói tiệc' },
  { to: '/thu-vien', label: 'Thư viện' },
  { to: '/danh-gia', label: 'Đánh giá' },
];

// Gom hết mục quản trị vào một menu xổ xuống, để trên thanh chỉ chiếm một chỗ
const ADMIN_ITEMS = [
  { to: '/quan-tri/don-dat-tiec', label: 'Đơn đặt tiệc' },
  { to: '/quan-tri/danh-gia', label: 'Duyệt đánh giá' },
  { to: '/quan-tri/thuc-don', label: 'Thực đơn' },
  { to: '/quan-tri/goi-tiec', label: 'Gói tiệc' },
  { to: '/quan-tri/khong-gian', label: 'Không gian' },
];

export default function Header() {
  const [open, setOpen] = useState(false);
  const [adminOpen, setAdminOpen] = useState(false);
  const adminRef = useRef(null);

  const user = useSelector(selectUser);
  const isAdmin = useSelector(selectIsAdmin);
  const dispatch = useDispatch();
  const navigate = useNavigate();

  const close = () => {
    setOpen(false);
    setAdminOpen(false);
  };

  // Bấm ra ngoài hoặc nhấn Esc thì đóng menu quản trị
  useEffect(() => {
    if (!adminOpen) return undefined;

    const onClickOutside = (event) => {
      if (adminRef.current && !adminRef.current.contains(event.target)) {
        setAdminOpen(false);
      }
    };
    const onEsc = (event) => {
      if (event.key === 'Escape') setAdminOpen(false);
    };

    document.addEventListener('mousedown', onClickOutside);
    document.addEventListener('keydown', onEsc);
    return () => {
      document.removeEventListener('mousedown', onClickOutside);
      document.removeEventListener('keydown', onEsc);
    };
  }, [adminOpen]);

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

          {/* Tra cứu bằng mã đơn dành cho khách không có tài khoản.
              Ai đã đăng nhập thì xem ở mục Đơn của tôi. */}
          {!user && (
            <NavLink to="/tra-cuu" onClick={close}>
              Tra cứu đơn
            </NavLink>
          )}

          {user ? (
            <>
              <NavLink to="/ho-so" onClick={close}>
                Hồ sơ
              </NavLink>

              {/* Quản trị xem toàn bộ đơn ở trang quản trị nên không cần mục này */}
              {!isAdmin && (
                <NavLink to="/don-cua-toi" onClick={close}>
                  Đơn của tôi
                </NavLink>
              )}

              {isAdmin && (
                <div className={`dropdown ${adminOpen ? 'open' : ''}`} ref={adminRef}>
                  <button
                    type="button"
                    aria-expanded={adminOpen}
                    onClick={() => setAdminOpen((v) => !v)}
                  >
                    Quản trị ▾
                  </button>
                  {adminOpen && (
                    <div className="dropdown-panel">
                      {ADMIN_ITEMS.map((item) => (
                        <NavLink key={item.to} to={item.to} onClick={close}>
                          {item.label}
                        </NavLink>
                      ))}
                    </div>
                  )}
                </div>
              )}

              <button type="button" className="btn btn-ghost btn-sm" onClick={handleLogout}>
                Đăng xuất
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
