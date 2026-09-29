import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import { menuApi, packageApi, spaceApi } from '@/api/endpoints';

// Dữ liệu chỉ để xem: không gian, thực đơn, gói tiệc

export const fetchSpaces = createAsyncThunk('catalog/spaces', async (filters = {}) =>
  spaceApi.list(filters),
);

export const fetchCategories = createAsyncThunk('catalog/categories', async () =>
  menuApi.categories(),
);

export const fetchDishes = createAsyncThunk('catalog/dishes', async (params = {}) =>
  menuApi.dishes(params),
);

export const fetchPackages = createAsyncThunk('catalog/packages', async () => packageApi.list());

const initialState = {
  spaces: { items: [], status: 'idle', error: null, requestId: null },
  categories: { items: [], status: 'idle', requestId: null },
  dishes: { items: [], status: 'idle', activeCategory: null, requestId: null },
  packages: { items: [], status: 'idle', requestId: null },
};

/*
 * Viết gọn 3 case pending/fulfilled/rejected lặp đi lặp lại.
 *
 * Mỗi lần gọi chỉ nhận kết quả nếu nó là lần gọi mới nhất của danh sách đó. Khách đổi bộ lọc
 * nhanh tay thì có hai lần gọi cùng chạy; lần gọi cũ đôi khi về sau lần mới và ghi đè, làm
 * danh sách hiện kết quả của bộ lọc đã bỏ. So requestId để bỏ qua kết quả về muộn.
 */
const attach = (builder, thunk, key, onSuccess) => {
  builder
    .addCase(thunk.pending, (state, action) => {
      state[key].status = 'loading';
      state[key].requestId = action.meta.requestId;
    })
    .addCase(thunk.fulfilled, (state, action) => {
      if (state[key].requestId !== action.meta.requestId) return;
      state[key].status = 'succeeded';
      state[key].items = action.payload;
      if (onSuccess) onSuccess(state, action);
    })
    .addCase(thunk.rejected, (state, action) => {
      if (state[key].requestId !== action.meta.requestId) return;
      state[key].status = 'failed';
      state[key].error = action.error?.message;
    });
};

const catalogSlice = createSlice({
  name: 'catalog',
  initialState,
  reducers: {},
  extraReducers: (builder) => {
    attach(builder, fetchSpaces, 'spaces');
    attach(builder, fetchCategories, 'categories');
    attach(builder, fetchPackages, 'packages');
    attach(builder, fetchDishes, 'dishes', (state, action) => {
      state.dishes.activeCategory = action.meta.arg?.category ?? null;
    });
  },
});

export const selectSpaces = (state) => state.catalog.spaces;
export const selectCategories = (state) => state.catalog.categories.items;
export const selectDishes = (state) => state.catalog.dishes;
export const selectPackages = (state) => state.catalog.packages;

export default catalogSlice.reducer;
