import { lazy, Suspense, useEffect } from 'react';
import { useDispatch } from 'react-redux';
import { Route, Routes } from 'react-router-dom';
import { restoreSession } from '@/features/auth/authSlice';
import Layout from '@/components/layout/Layout';
import ProtectedRoute from '@/routes/ProtectedRoute';
import { Loading } from '@/components/common/StateBlock';

import HomePage from '@/pages/HomePage';
import SpacesPage from '@/pages/SpacesPage';
import SpaceDetailPage from '@/pages/SpaceDetailPage';
import MenuPage from '@/pages/MenuPage';
import DishDetailPage from '@/pages/DishDetailPage';
import DishOrderPage from '@/pages/DishOrderPage';
import TrackDishOrderPage from '@/pages/TrackDishOrderPage';
import PackagesPage from '@/pages/PackagesPage';
import PromotionsPage from '@/pages/PromotionsPage';
import PaymentPage from '@/pages/PaymentPage';
import ForgotPasswordPage from '@/pages/ForgotPasswordPage';
import ResetPasswordPage from '@/pages/ResetPasswordPage';
import PaymentResultPage from '@/pages/PaymentResultPage';
import GalleryPage from '@/pages/GalleryPage';
import BookingPage from '@/pages/BookingPage';
import TrackBookingPage from '@/pages/TrackBookingPage';
import LoginPage from '@/pages/LoginPage';
import RegisterPage from '@/pages/RegisterPage';
import MyBookingsPage from '@/pages/MyBookingsPage';
import ProfilePage from '@/pages/ProfilePage';
import AdminBookingsPage from '@/pages/admin/AdminBookingsPage';
import AdminDishOrdersPage from '@/pages/admin/AdminDishOrdersPage';
import AdminPaymentsPage from '@/pages/admin/AdminPaymentsPage';
import AdminUsersPage from '@/pages/admin/AdminUsersPage';
import AdminReviewsPage from '@/pages/admin/AdminReviewsPage';
import AdminMenuPage from '@/pages/admin/AdminMenuPage';
import AdminPackagesPage from '@/pages/admin/AdminPackagesPage';
import AdminSpacesPage from '@/pages/admin/AdminSpacesPage';
import AdminHolidaysPage from '@/pages/admin/AdminHolidaysPage';
import ReviewsPage from '@/pages/ReviewsPage';
import NotFoundPage from '@/pages/NotFoundPage';

/*
 * Trang thống kê tải riêng, không gói chung vào tệp chính.
 *
 * Trang này kéo theo thư viện biểu đồ recharts, nặng khoảng 500KB. Gói chung thì mọi
 * khách vào xem thực đơn cũng phải tải, trong khi chỉ quản trị và nhân viên mới mở tới.
 * Tách ra thì thư viện chỉ được tải đúng lúc ai đó mở trang thống kê.
 */
const AdminStatsPage = lazy(() => import('@/pages/admin/AdminStatsPage'));

export default function App() {
  const dispatch = useDispatch();

  // Khôi phục phiên đăng nhập từ token lưu trong localStorage
  useEffect(() => {
    dispatch(restoreSession());
  }, [dispatch]);

  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/khong-gian" element={<SpacesPage />} />
        <Route path="/khong-gian/:slug" element={<SpaceDetailPage />} />
        <Route path="/thuc-don" element={<MenuPage />} />
        <Route path="/thuc-don/:slug" element={<DishDetailPage />} />
        <Route path="/dat-mon" element={<DishOrderPage />} />
        <Route path="/tra-cuu-mon" element={<TrackDishOrderPage />} />
        <Route path="/goi-tiec" element={<PackagesPage />} />
        <Route path="/uu-dai" element={<PromotionsPage />} />
        <Route path="/thu-vien" element={<GalleryPage />} />
        <Route path="/danh-gia" element={<ReviewsPage />} />
        <Route path="/dat-tiec" element={<BookingPage />} />
        <Route path="/tra-cuu" element={<TrackBookingPage />} />
        <Route path="/thanh-toan" element={<PaymentPage />} />
        <Route path="/thanh-toan/ket-qua" element={<PaymentResultPage />} />
        <Route path="/dang-nhap" element={<LoginPage />} />
        <Route path="/dang-ky" element={<RegisterPage />} />
        <Route path="/quen-mat-khau" element={<ForgotPasswordPage />} />
        <Route path="/dat-lai-mat-khau" element={<ResetPasswordPage />} />

        <Route
          path="/ho-so"
          element={
            <ProtectedRoute>
              <ProfilePage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/don-cua-toi"
          element={
            <ProtectedRoute>
              <MyBookingsPage />
            </ProtectedRoute>
          }
        />
        {/* Thống kê doanh thu để cả nhân viên xem được, vì đây chỉ là số liệu tổng hợp */}
        <Route
          path="/quan-tri/thong-ke"
          element={
            <ProtectedRoute roles={['ADMIN', 'STAFF']}>
              <Suspense fallback={<Loading label="Đang tải biểu đồ…" />}>
                <AdminStatsPage />
              </Suspense>
            </ProtectedRoute>
          }
        />
        <Route
          path="/quan-tri/don-dat-tiec"
          element={
            <ProtectedRoute roles={['ADMIN', 'STAFF']}>
              <AdminBookingsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/quan-tri/don-dat-mon"
          element={
            <ProtectedRoute roles={['ADMIN', 'STAFF']}>
              <AdminDishOrdersPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/quan-tri/tai-khoan"
          element={
            <ProtectedRoute roles={['ADMIN']}>
              <AdminUsersPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/quan-tri/thanh-toan"
          element={
            <ProtectedRoute roles={['ADMIN', 'STAFF']}>
              <AdminPaymentsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/quan-tri/danh-gia"
          element={
            <ProtectedRoute roles={['ADMIN', 'STAFF']}>
              <AdminReviewsPage />
            </ProtectedRoute>
          }
        />
        {/* Sửa thực đơn và bảng giá là việc của quản trị, nhân viên không vào được */}
        <Route
          path="/quan-tri/thuc-don"
          element={
            <ProtectedRoute roles={['ADMIN']}>
              <AdminMenuPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/quan-tri/goi-tiec"
          element={
            <ProtectedRoute roles={['ADMIN']}>
              <AdminPackagesPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/quan-tri/khong-gian"
          element={
            <ProtectedRoute roles={['ADMIN']}>
              <AdminSpacesPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/quan-tri/ngay-le"
          element={
            <ProtectedRoute roles={['ADMIN']}>
              <AdminHolidaysPage />
            </ProtectedRoute>
          }
        />

        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  );
}
