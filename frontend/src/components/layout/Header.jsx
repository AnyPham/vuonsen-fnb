import { useEffect, useRef, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { NavLink, useNavigate } from 'react-router-dom';
import { logout, selectIsAdmin, selectUser } from '@/features/auth/authSlice';
import { chonSoMon } from '@/features/dishorder/cartSlice';
import { useI18n } from '@/i18n';
import LanguageSwitcher from './LanguageSwitcher';

const NAV_ITEMS = [
  { to: '/khong-gian', khoa: 'nav.spaces' },
  { to: '/thuc-don', khoa: 'nav.menu' },
  { to: '/goi-tiec', khoa: 'nav.packages' },
  { to: '/uu-dai', khoa: 'nav.promotions' },
  { to: '/thu-vien', khoa: 'nav.gallery' },
  { to: '/danh-gia', khoa: 'nav.reviews' },
];

// Gom hết mục quản trị vào một menu xổ xuống, để trên thanh chỉ chiếm một chỗ
const ADMIN_ITEMS = [
  { to: '/quan-tri/thong-ke', khoa: 'admin.stats' },
  { to: '/quan-tri/don-dat-tiec', khoa: 'admin.bookings' },
  { to: '/quan-tri/don-dat-mon', khoa: 'admin.dishOrders' },
  { to: '/quan-tri/thanh-toan', khoa: 'admin.payments' },
  { to: '/quan-tri/danh-gia', khoa: 'admin.reviews' },
  { to: '/quan-tri/thuc-don', khoa: 'admin.menu' },
  { to: '/quan-tri/goi-tiec', khoa: 'admin.packages' },
  { to: '/quan-tri/khong-gian', khoa: 'admin.spaces' },
  { to: '/quan-tri/ngay-le', khoa: 'admin.holidays' },
  { to: '/quan-tri/tai-khoan', khoa: 'admin.users' },
];

export default function Header() {
  const [open, setOpen] = useState(false);
  const [adminOpen, setAdminOpen] = useState(false);
  const adminRef = useRef(null);

  const user = useSelector(selectUser);
  const isAdmin = useSelector(selectIsAdmin);
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const soMonTrongGio = useSelector(chonSoMon);
  const { t } = useI18n();

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

        <div className="nav-right">
        <nav className={`menu ${open ? 'open' : ''}`}>
          {NAV_ITEMS.map((item) => (
            <NavLink key={item.to} to={item.to} onClick={close}>
              {t(item.khoa)}
            </NavLink>
          ))}

          {/* Tra cứu bằng mã đơn dành cho khách không có tài khoản.
              Ai đã đăng nhập thì xem ở mục Đơn của tôi. */}
          {!user && (
            <NavLink to="/tra-cuu" onClick={close}>
              {t('nav.track')}
            </NavLink>
          )}

          {user ? (
            <>
              <NavLink to="/ho-so" onClick={close} data-test="nav-profile">
                {t('nav.profile')}
              </NavLink>

              {/* Quản trị xem toàn bộ đơn ở trang quản trị nên không cần mục này */}
              {!isAdmin && (
                <NavLink to="/don-cua-toi" onClick={close} data-test="nav-my-orders">
                  {t('nav.myOrders')}
                </NavLink>
              )}

              {isAdmin && (
                <div className={`dropdown ${adminOpen ? 'open' : ''}`} ref={adminRef}>
                  <button
                    type="button"
                    aria-expanded={adminOpen}
                    data-test="nav-admin"
                    onClick={() => setAdminOpen((v) => !v)}
                  >
                    {t('nav.admin')} ▾
                  </button>
                  {adminOpen && (
                    <div className="dropdown-panel">
                      {ADMIN_ITEMS.map((item) => (
                        <NavLink key={item.to} to={item.to} onClick={close}>
                          {t(item.khoa)}
                        </NavLink>
                      ))}
                    </div>
                  )}
                </div>
              )}

              <button type="button" className="btn btn-ghost btn-sm" onClick={handleLogout} data-test="nav-logout">
                {t('nav.logout')}
              </button>
            </>
          ) : (
            <NavLink to="/dang-nhap" onClick={close} data-test="nav-login">
              {t('nav.login')}
            </NavLink>
          )}

          {/* Chỉ hiện khi giỏ có món, để thanh menu không thêm một mục thừa
              với những khách chỉ vào xem tiệc */}
          {soMonTrongGio > 0 && (
            <NavLink to="/dat-mon" className="nut-gio" onClick={close} aria-label={t('nav.cart')}>
              🛒
              <span className="so-mon">{soMonTrongGio}</span>
            </NavLink>
          )}

          <NavLink to="/dat-tiec" className="btn btn-gold btn-sm" onClick={close}>
            {t('nav.book')}
          </NavLink>
        </nav>

        <LanguageSwitcher />

        <button
          type="button"
          className="burger"
          aria-label={t('nav.openMenu')}
          aria-expanded={open}
          onClick={() => setOpen((v) => !v)}
        >
          ☰
        </button>
        </div>
      </div>
    </header>
  );
}
