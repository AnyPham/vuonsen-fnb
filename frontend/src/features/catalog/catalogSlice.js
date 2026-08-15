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
  spaces: { items: [], status: 'idle', error: null },
  categories: { items: [], status: 'idle' },
  dishes: { items: [], status: 'idle', activeCategory: null },
  packages: { items: [], status: 'idle' },
};

// Viết gọn 3 case pending/fulfilled/rejected lặp đi lặp lại
const attach = (builder, thunk, key, onSuccess) => {
  builder
    .addCase(thunk.pending, (state) => {
      state[key].status = 'loading';
    })
    .addCase(thunk.fulfilled, (state, action) => {
      state[key].status = 'succeeded';
      state[key].items = action.payload;
      if (onSuccess) onSuccess(state, action);
    })
    .addCase(thunk.rejected, (state, action) => {
      state[key].status = 'failed';
      state[key].error = action.error?.message;
    });
};

const catalogSlice = createSlice({
  name: 'catalog',
  initialState,
  reducers: {
    setActiveCategory: (state, action) => {
      state.dishes.activeCategory = action.payload;
    },
  },
  extraReducers: (builder) => {
    attach(builder, fetchSpaces, 'spaces');
    attach(builder, fetchCategories, 'categories');
    attach(builder, fetchPackages, 'packages');
    attach(builder, fetchDishes, 'dishes', (state, action) => {
      state.dishes.activeCategory = action.meta.arg?.category ?? null;
    });
  },
});

export const { setActiveCategory } = catalogSlice.actions;

export const selectSpaces = (state) => state.catalog.spaces;
export const selectCategories = (state) => state.catalog.categories.items;
export const selectDishes = (state) => state.catalog.dishes;
export const selectPackages = (state) => state.catalog.packages;

export default catalogSlice.reducer;
