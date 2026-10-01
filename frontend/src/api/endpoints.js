import axiosClient from './axiosClient';

// Gom tất cả đường dẫn API vào một chỗ, sau này đổi API chỉ sửa ở đây
export const authApi = {
  register: (body) => axiosClient.post('/api/auth/register', body),
  login: (body) => axiosClient.post('/api/auth/login', body),
  logout: () => axiosClient.post('/api/auth/logout'),
  me: () => axiosClient.get('/api/v1/me'),
  updateProfile: (body) => axiosClient.put('/api/v1/me', body),
  // Quên mật khẩu: xin liên kết rồi đặt lại bằng mã trong liên kết đó
  forgotPassword: (email) => axiosClient.post('/api/auth/forgot-password', { email }),
  resetPassword: (token, newPassword) =>
    axiosClient.post('/api/auth/reset-password', { token, newPassword }),
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
  detail: (slug) => axiosClient.get(`/api/v1/menu/dishes/${slug}`),
};

export const dishOrderApi = {
  quote: (body) => axiosClient.post('/api/v1/dish-orders/quote', body),
  create: (body) => axiosClient.post('/api/v1/dish-orders', body),
  track: (code) => axiosClient.get(`/api/v1/dish-orders/track/${code}`),
  mine: (params) => axiosClient.get('/api/v1/dish-orders/my', { params }),
};

export const packageApi = {
  list: (params) => axiosClient.get('/api/v1/packages', { params }),
};

export const recommendationApi = {
  suggest: (body) => axiosClient.post('/api/v1/recommendations', body),
};

export const assistantApi = {
  // history: các lượt trước dạng { role: 'user' | 'assistant', content }, cũ trước mới sau
  ask: (question, history = []) => axiosClient.post('/api/v1/assistant/ask', { question, history }),
};

export const bookingApi = {
  options: () => axiosClient.get('/api/v1/bookings/options'),
  quote: (body) => axiosClient.post('/api/v1/bookings/quote', body),
  create: (body) => axiosClient.post('/api/v1/bookings', body),
  track: (code) => axiosClient.get(`/api/v1/bookings/track/${code}`),
  // Buổi đã kín của từng không gian trong một ngày
  availability: (date) => axiosClient.get('/api/v1/bookings/availability', { params: { date } }),
  mine: (params) => axiosClient.get('/api/v1/bookings/my', { params }),
};

export const reviewApi = {
  list: (params) => axiosClient.get('/api/v1/reviews', { params }),
  summary: () => axiosClient.get('/api/v1/reviews/summary'),
  create: (body) => axiosClient.post('/api/v1/reviews', body),
};

export const paymentApi = {
  tinhHinh: (maDon) => axiosClient.get(`/api/v1/payments/order/${maDon}`),
  taoYeuCau: (orderCode, purpose) => axiosClient.post('/api/v1/payments', { orderCode, purpose }),
  // Chỉ chạy được khi máy chủ bật chế độ thử
  giaLapBaoCo: (maDon) => axiosClient.post(`/api/v1/payments/sandbox/${maDon}/xac-nhan`),
};

export const vnpayApi = {
  trangThai: () => axiosClient.get('/api/v1/payments/vnpay/trang-thai'),
  tao: (orderCode, purpose) => axiosClient.post('/api/v1/payments/vnpay', { orderCode, purpose }),
  xacNhan: (thamSo) => axiosClient.post('/api/v1/payments/vnpay/verify', thamSo),
};

export const holidayApi = {
  // Các dịp lễ đang áp dụng hoặc sắp tới, cho trang Ưu đãi
  list: () => axiosClient.get('/api/v1/holidays'),
};

export const galleryApi = {
  list: (params) => axiosClient.get('/api/v1/gallery', { params }),
};

export const adminApi = {
  bookings: (params) => axiosClient.get('/api/v1/admin/bookings', { params }),
  // Đếm nhanh số đơn theo trạng thái, dùng cho mấy ô nhỏ trên trang danh sách đơn
  statistics: () => axiosClient.get('/api/v1/admin/bookings/statistics'),
  // Số liệu đầy đủ cho trang thống kê biểu đồ. Bỏ trống params thì lấy 12 tháng gần nhất.
  dashboard: (params) => axiosClient.get('/api/v1/admin/statistics', { params }),
  changeStatus: (id, body) => axiosClient.patch(`/api/v1/admin/bookings/${id}/status`, body),
  // Ghi nhận một lần thu tiền cọc: { amount, method } với method là CASH hoặc TRANSFER
  recordDeposit: (id, body) => axiosClient.patch(`/api/v1/admin/bookings/${id}/deposit`, body),
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

  // Quản trị đơn đặt món. Trạng thái mới đi theo tham số truy vấn, không nằm trong thân yêu cầu
  dishOrders: (params) => axiosClient.get('/api/v1/admin/dish-orders', { params }),
  dishOrderStats: () => axiosClient.get('/api/v1/admin/dish-orders/stats'),
  changeDishOrderStatus: (id, status) =>
    axiosClient.patch(`/api/v1/admin/dish-orders/${id}/status`, null, { params: { status } }),

  // Quản trị tài khoản người dùng
  users: (params) => axiosClient.get('/api/v1/admin/users', { params }),
  createUser: (body) => axiosClient.post('/api/v1/admin/users', body),
  changeUserRole: (id, role) =>
    axiosClient.patch(`/api/v1/admin/users/${id}/role`, null, { params: { role } }),
  changeUserEnabled: (id, enabled) =>
    axiosClient.patch(`/api/v1/admin/users/${id}/enabled`, null, { params: { enabled } }),
  resetUserPassword: (id, newPassword) =>
    axiosClient.patch(`/api/v1/admin/users/${id}/password`, null, { params: { newPassword } }),

  // Quản trị sổ thanh toán
  payments: (params) => axiosClient.get('/api/v1/admin/payments', { params }),
  paymentStats: () => axiosClient.get('/api/v1/admin/payments/stats'),
  confirmPayment: (id, reference) =>
    axiosClient.patch(`/api/v1/admin/payments/${id}/confirm`, null, { params: { reference } }),
  cancelPayment: (id, reason) =>
    axiosClient.patch(`/api/v1/admin/payments/${id}/cancel`, null, { params: { reason } }),
  recordCashPayment: (body) => axiosClient.post('/api/v1/admin/payments', body),

  // Quản trị giảm giá ngày lễ
  holidays: () => axiosClient.get('/api/v1/admin/holidays'),
  createHoliday: (body) => axiosClient.post('/api/v1/admin/holidays', body),
  updateHoliday: (id, body) => axiosClient.put(`/api/v1/admin/holidays/${id}`, body),
  deleteHoliday: (id) => axiosClient.delete(`/api/v1/admin/holidays/${id}`),
};
