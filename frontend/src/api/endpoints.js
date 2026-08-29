import axiosClient from './axiosClient';

// Gom tất cả đường dẫn API vào một chỗ, sau này đổi API chỉ sửa ở đây
export const authApi = {
  register: (body) => axiosClient.post('/api/auth/register', body),
  login: (body) => axiosClient.post('/api/auth/login', body),
  logout: () => axiosClient.post('/api/auth/logout'),
  me: () => axiosClient.get('/api/v1/me'),
  updateProfile: (body) => axiosClient.put('/api/v1/me', body),
};

export const spaceApi = {
  list: (params) => axiosClient.get('/api/v1/spaces', { params }),
  types: () => axiosClient.get('/api/v1/spaces/types'),
  detail: (slug) => axiosClient.get(`/api/v1/spaces/${slug}`),
};

export const menuApi = {
  categories: () => axiosClient.get('/api/v1/menu/categories'),
  dishes: (params) => axiosClient.get('/api/v1/menu/dishes', { params }),
  bestSellers: () => axiosClient.get('/api/v1/menu/best-sellers'),
};

export const packageApi = {
  list: () => axiosClient.get('/api/v1/packages'),
};

export const recommendationApi = {
  suggest: (body) => axiosClient.post('/api/v1/recommendations', body),
};

export const bookingApi = {
  options: () => axiosClient.get('/api/v1/bookings/options'),
  quote: (body) => axiosClient.post('/api/v1/bookings/quote', body),
  create: (body) => axiosClient.post('/api/v1/bookings', body),
  track: (code) => axiosClient.get(`/api/v1/bookings/track/${code}`),
  mine: (params) => axiosClient.get('/api/v1/bookings/my', { params }),
};

export const reviewApi = {
  list: (params) => axiosClient.get('/api/v1/reviews', { params }),
  summary: () => axiosClient.get('/api/v1/reviews/summary'),
  create: (body) => axiosClient.post('/api/v1/reviews', body),
};

export const galleryApi = {
  list: (params) => axiosClient.get('/api/v1/gallery', { params }),
};

export const adminApi = {
  bookings: (params) => axiosClient.get('/api/v1/admin/bookings', { params }),
  statistics: () => axiosClient.get('/api/v1/admin/bookings/statistics'),
  changeStatus: (id, body) => axiosClient.patch(`/api/v1/admin/bookings/${id}/status`, body),
  reviews: (params) => axiosClient.get('/api/v1/admin/reviews', { params }),
  approveReview: (id) => axiosClient.patch(`/api/v1/admin/reviews/${id}/approve`),
  rejectReview: (id) => axiosClient.delete(`/api/v1/admin/reviews/${id}`),

  // Quản trị thực đơn
  dishes: () => axiosClient.get('/api/v1/admin/dishes'),
  createDish: (body) => axiosClient.post('/api/v1/admin/dishes', body),
  updateDish: (id, body) => axiosClient.put(`/api/v1/admin/dishes/${id}`, body),
  stopDish: (id) => axiosClient.delete(`/api/v1/admin/dishes/${id}`),

  // Quản trị không gian
  spaces: () => axiosClient.get('/api/v1/admin/spaces'),
  createSpace: (body) => axiosClient.post('/api/v1/admin/spaces', body),
  updateSpace: (id, body) => axiosClient.put(`/api/v1/admin/spaces/${id}`, body),
  stopSpace: (id) => axiosClient.delete(`/api/v1/admin/spaces/${id}`),

  // Quản trị gói tiệc
  packages: () => axiosClient.get('/api/v1/admin/packages'),
  createPackage: (body) => axiosClient.post('/api/v1/admin/packages', body),
  updatePackage: (id, body) => axiosClient.put(`/api/v1/admin/packages/${id}`, body),
  stopPackage: (id) => axiosClient.delete(`/api/v1/admin/packages/${id}`),
};
