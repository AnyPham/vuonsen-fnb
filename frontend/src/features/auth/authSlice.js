import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import { authApi } from '@/api/endpoints';
import { tokenStorage } from '@/api/axiosClient';

export const login = createAsyncThunk('auth/login', async (body, { rejectWithValue }) => {
  try {
    const data = await authApi.login(body);
    tokenStorage.save(data);
    return data.user;
  } catch (error) {
    return rejectWithValue(error.message);
  }
});

/*
 * Đăng ký KHÔNG còn đăng nhập luôn.
 *
 * Máy chủ trả về { email, canXacThuc, message } chứ không trả token, vì tài khoản phải
 * nhập mã gửi về email mới dùng được. Trả nguyên gói đó cho trang đăng ký tự chuyển sang
 * màn nhập mã, chưa đụng tới người dùng trong Redux.
 */
export const register = createAsyncThunk('auth/register', async (body, { rejectWithValue }) => {
  try {
    return await authApi.register(body);
  } catch (error) {
    return rejectWithValue(error.message);
  }
});

// Nhập đúng mã thì tài khoản bật lên và đăng nhập luôn, khỏi gõ lại mật khẩu vừa đặt
export const xacThucEmail = createAsyncThunk(
  'auth/xacThucEmail',
  async (body, { rejectWithValue }) => {
    try {
      const data = await authApi.xacThucEmail(body);
      tokenStorage.save(data);
      return data.user;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  },
);

// Gọi khi tải lại trang, còn token thì khôi phục phiên đăng nhập
export const restoreSession = createAsyncThunk('auth/restore', async (_, { rejectWithValue }) => {
  if (!tokenStorage.getAccess()) return rejectWithValue(null);
  try {
    return await authApi.me();
  } catch (error) {
    tokenStorage.clear();
    return rejectWithValue(error.message);
  }
});

// Cập nhật hồ sơ rồi lưu lại vào Redux để tên trên thanh menu đổi theo ngay
export const updateProfile = createAsyncThunk(
  'auth/updateProfile',
  async (body, { rejectWithValue }) => {
    try {
      return await authApi.updateProfile(body);
    } catch (error) {
      return rejectWithValue({ message: error.message, fieldErrors: error.fieldErrors });
    }
  },
);

export const logout = createAsyncThunk('auth/logout', async () => {

  try {
    await authApi.logout();
  } finally {
    tokenStorage.clear();
  }
});

const authSlice = createSlice({
  name: 'auth',
  initialState: {
    user: null,
    status: 'idle', // idle | loading | succeeded | failed
    error: null,
    initialized: false,
  },
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(restoreSession.fulfilled, (state, action) => {
        state.user = action.payload;
        state.initialized = true;
      })
      .addCase(restoreSession.rejected, (state) => {
        state.user = null;
        state.initialized = true;
      })
      .addCase(updateProfile.fulfilled, (state, action) => {
        state.user = action.payload;
      })
      .addCase(logout.fulfilled, (state) => {
        state.user = null;
        state.status = 'idle';
      });

    /*
     * register không còn nằm trong nhóm này.
     *
     * Ba thunk dưới đây đều kết thúc bằng việc có người dùng đăng nhập, nên dùng chung
     * cách xử lý. Còn register chỉ gửi mã đi, chưa có ai đăng nhập cả; để chung thì
     * state.user bị gán bằng gói { email, canXacThuc, message }, giao diện tưởng đã đăng
     * nhập rồi cho vào thẳng.
     */
    builder
      .addCase(register.pending, (state) => {
        state.status = 'loading';
        state.error = null;
      })
      .addCase(register.fulfilled, (state) => {
        state.status = 'succeeded';
      })
      .addCase(register.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.payload;
      });

    // login và xác thực email đều kết thúc bằng một phiên đăng nhập
    [login, xacThucEmail].forEach((thunk) => {
      builder
        .addCase(thunk.pending, (state) => {
          state.status = 'loading';
          state.error = null;
        })
        .addCase(thunk.fulfilled, (state, action) => {
          state.status = 'succeeded';
          state.user = action.payload;
          state.initialized = true;
        })
        .addCase(thunk.rejected, (state, action) => {
          state.status = 'failed';
          state.error = action.payload;
        });
    });
  },
});

export const { clearError } = authSlice.actions;

export const selectUser = (state) => state.auth.user;
export const selectIsAdmin = (state) => ['ADMIN', 'STAFF'].includes(state.auth.user?.role);
export const selectAuthStatus = (state) => state.auth.status;

export default authSlice.reducer;
