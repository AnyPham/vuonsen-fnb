import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import { bookingApi } from '@/api/endpoints';

// State của form đặt tiệc 3 bước.
// Tiền tạm tính luôn lấy từ API, không tự tính ở đây để tránh lệch số.

export const fetchOptions = createAsyncThunk('booking/options', async () => bookingApi.options());

export const fetchQuote = createAsyncThunk(
  'booking/quote',
  async (payload, { rejectWithValue }) => {
    try {
      return await bookingApi.quote(payload);
    } catch (error) {
      return rejectWithValue(error.message);
    }
  },
);

export const submitBooking = createAsyncThunk(
  'booking/submit',
  async (payload, { rejectWithValue }) => {
    try {
      return await bookingApi.create(payload);
    } catch (error) {
      return rejectWithValue({ message: error.message, fieldErrors: error.fieldErrors });
    }
  },
);

const emptyForm = {
  eventType: '',
  eventDate: '',
  timeSlot: 'EVENING',
  guestCount: '',
  spaceId: null,
  packageId: null,
  noPackage: false, // true khi khách chọn chỉ thuê không gian, không kèm gói tiệc
  customerName: '',
  customerPhone: '',
  customerEmail: '',
  note: '',
};

const bookingSlice = createSlice({
  name: 'booking',
  initialState: {
    step: 1,
    form: { ...emptyForm },
    options: { eventTypes: [], timeSlots: [] },
    quote: null,
    quoteStatus: 'idle',
    submitStatus: 'idle',
    result: null,
    error: null,
    fieldErrors: null,
  },
  reducers: {
    updateForm: (state, action) => {
      state.form = { ...state.form, ...action.payload };
    },
    /*
     * Đổi số phần của một món tự chọn.
     *
     * Phép cộng trừ nằm trong reducer chứ không ở thành phần giao diện. Lý do: khách bấm
     * dấu cộng ba lần liên tiếp thì cả ba lần cùng đọc một bản danh sách cũ trong closure
     * của lần vẽ lại trước, kết quả chỉ cộng được một phần. Reducer thì luôn chạy trên
     * trạng thái mới nhất nên không mất lần bấm nào.
     */
    doiSoPhanMon: (state, action) => {
      const { dishId, buoc } = action.payload;
      const ds = state.form.monTuChon || [];
      const cu = ds.find((d) => d.dishId === dishId);
      const moi = Math.max(0, Math.min(200, (cu ? cu.soLuong : 0) + buoc));
      const con = ds.filter((d) => d.dishId !== dishId);
      state.form.monTuChon = moi === 0 ? con : [...con, { dishId, soLuong: moi }];
    },
    goToStep: (state, action) => {
      state.step = Math.min(3, Math.max(1, action.payload));
    },
    resetBooking: (state) => {
      state.step = 1;
      state.form = { ...emptyForm };
      state.quote = null;
      state.quoteStatus = 'idle';
      state.submitStatus = 'idle';
      state.result = null;
      state.error = null;
      state.fieldErrors = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchOptions.fulfilled, (state, action) => {
        state.options = action.payload;
      })
      .addCase(fetchQuote.pending, (state) => {
        state.quoteStatus = 'loading';
      })
      .addCase(fetchQuote.fulfilled, (state, action) => {
        state.quoteStatus = 'succeeded';
        state.quote = action.payload;
        state.error = null;
      })
      .addCase(fetchQuote.rejected, (state, action) => {
        state.quoteStatus = 'failed';
        state.quote = null;
        state.error = action.payload;
      })
      .addCase(submitBooking.pending, (state) => {
        state.submitStatus = 'loading';
        state.error = null;
        state.fieldErrors = null;
      })
      .addCase(submitBooking.fulfilled, (state, action) => {
        state.submitStatus = 'succeeded';
        state.result = action.payload;
      })
      .addCase(submitBooking.rejected, (state, action) => {
        state.submitStatus = 'failed';
        state.error = action.payload?.message;
        state.fieldErrors = action.payload?.fieldErrors;
      });
  },
});

export const { updateForm, doiSoPhanMon, goToStep, resetBooking } = bookingSlice.actions;

export const selectBooking = (state) => state.booking;

export default bookingSlice.reducer;
