import axios from 'axios';

// Axios dùng chung cho cả ứng dụng.
// Tự gắn token vào header, gặp lỗi 401 thì xin token mới rồi gọi lại.
const TOKEN_KEY = 'vs_access_token';
const REFRESH_KEY = 'vs_refresh_token';

export const tokenStorage = {
  getAccess: () => localStorage.getItem(TOKEN_KEY),
  getRefresh: () => localStorage.getItem(REFRESH_KEY),
  save: ({ accessToken, refreshToken }) => {
    localStorage.setItem(TOKEN_KEY, accessToken);
    localStorage.setItem(REFRESH_KEY, refreshToken);
  },
  clear: () => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(REFRESH_KEY);
  },
};

const axiosClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000,
});

axiosClient.interceptors.request.use((config) => {
  const token = tokenStorage.getAccess();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

let refreshing = null;

axiosClient.interceptors.response.use(
  (response) => response.data,
  async (error) => {
    const { response, config } = error;

    if (response?.status === 401 && !config._retried && tokenStorage.getRefresh()) {
      config._retried = true;
      try {
        // Nhiều request lỗi 401 cùng lúc thì chỉ gọi refresh một lần
        refreshing =
          refreshing ||
          axios.post(`${axiosClient.defaults.baseURL}/api/auth/refresh`, {
            refreshToken: tokenStorage.getRefresh(),
          });
        const { data } = await refreshing;
        refreshing = null;
        tokenStorage.save(data);
        config.headers.Authorization = `Bearer ${data.accessToken}`;
        return axiosClient(config);
      } catch (refreshError) {
        refreshing = null;
        tokenStorage.clear();
        if (!window.location.pathname.startsWith('/dang-nhap')) {
          window.location.href = '/dang-nhap';
        }
        return Promise.reject(refreshError);
      }
    }

    // Đưa lỗi về một dạng chung cho dễ hiển thị
    return Promise.reject({
      status: response?.status ?? 0,
      message: response?.data?.message || 'Không kết nối được máy chủ, vui lòng thử lại',
      fieldErrors: response?.data?.fieldErrors || null,
    });
  },
);

export default axiosClient;
