import { configureStore } from '@reduxjs/toolkit';
import authReducer from '@/features/auth/authSlice';
import catalogReducer from '@/features/catalog/catalogSlice';
import bookingReducer from '@/features/booking/bookingSlice';
import cartReducer from '@/features/dishorder/cartSlice';

export const store = configureStore({
  reducer: {
    auth: authReducer,
    catalog: catalogReducer,
    booking: bookingReducer,
    cart: cartReducer,
  },
  devTools: import.meta.env.DEV,
});
