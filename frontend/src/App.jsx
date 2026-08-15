import { useEffect } from 'react';
import { useDispatch } from 'react-redux';
import { Route, Routes } from 'react-router-dom';
import { restoreSession } from '@/features/auth/authSlice';
import Layout from '@/components/layout/Layout';
import ProtectedRoute from '@/routes/ProtectedRoute';

import HomePage from '@/pages/HomePage';
import SpacesPage from '@/pages/SpacesPage';
import MenuPage from '@/pages/MenuPage';
import PackagesPage from '@/pages/PackagesPage';
import GalleryPage from '@/pages/GalleryPage';
import BookingPage from '@/pages/BookingPage';
import TrackBookingPage from '@/pages/TrackBookingPage';
import LoginPage from '@/pages/LoginPage';
import RegisterPage from '@/pages/RegisterPage';
import MyBookingsPage from '@/pages/MyBookingsPage';
import AdminBookingsPage from '@/pages/admin/AdminBookingsPage';
import NotFoundPage from '@/pages/NotFoundPage';

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
        <Route path="/thuc-don" element={<MenuPage />} />
        <Route path="/goi-tiec" element={<PackagesPage />} />
        <Route path="/thu-vien" element={<GalleryPage />} />
        <Route path="/dat-tiec" element={<BookingPage />} />
        <Route path="/tra-cuu" element={<TrackBookingPage />} />
        <Route path="/dang-nhap" element={<LoginPage />} />
        <Route path="/dang-ky" element={<RegisterPage />} />

        <Route
          path="/don-cua-toi"
          element={
            <ProtectedRoute>
              <MyBookingsPage />
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

        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  );
}
