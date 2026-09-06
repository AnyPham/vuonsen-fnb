import { createSlice } from '@reduxjs/toolkit';

const KHOA_LUU = 'vuonsen-gio-mon';

/*
 * Giỏ món của khách.
 *
 * Giữ trong localStorage để khách bấm thêm món ở trang này, đi xem trang khác rồi
 * quay lại vẫn còn. Đặt món thường phải đắn đo vài lượt, mất giỏ giữa chừng là mất
 * luôn khách.
 *
 * Chỉ lưu mã món, tên và giá để hiện tạm; số tiền thật luôn hỏi lại máy chủ. Nếu tin
 * con số trong máy khách thì ai cũng sửa được thành 1 đồng.
 */
function doc() {
  try {
    const t = localStorage.getItem(KHOA_LUU);
    return t ? JSON.parse(t) : [];
  } catch {
    return [];
  }
}

function ghi(items) {
  try {
    localStorage.setItem(KHOA_LUU, JSON.stringify(items));
  } catch {
    // Trình duyệt chặn lưu trữ thì giỏ chỉ sống trong phiên này, không phải lỗi chặn đường
  }
}

const cartSlice = createSlice({
  name: 'cart',
  initialState: {
    items: doc(),
    // DELIVERY hoặc DINE_IN, nhớ lựa chọn lần trước cho đỡ phải chọn lại
    fulfillmentType: 'DELIVERY',
  },
  reducers: {
    themMon(state, action) {
      const { dishId, name, price, slug, imageUrl } = action.payload;
      const co = state.items.find((i) => i.dishId === dishId);
      if (co) {
        co.quantity += action.payload.quantity ?? 1;
      } else {
        state.items.push({
          dishId, name, price, slug, imageUrl,
          quantity: action.payload.quantity ?? 1,
        });
      }
      ghi(state.items);
    },

    doiSoLuong(state, action) {
      const { dishId, quantity } = action.payload;
      const mon = state.items.find((i) => i.dishId === dishId);
      if (!mon) return;
      if (quantity <= 0) {
        state.items = state.items.filter((i) => i.dishId !== dishId);
      } else {
        mon.quantity = quantity;
      }
      ghi(state.items);
    },

    boMon(state, action) {
      state.items = state.items.filter((i) => i.dishId !== action.payload);
      ghi(state.items);
    },

    xoaGio(state) {
      state.items = [];
      ghi(state.items);
    },

    doiHinhThuc(state, action) {
      state.fulfillmentType = action.payload;
    },
  },
});

export const { themMon, doiSoLuong, boMon, xoaGio, doiHinhThuc } = cartSlice.actions;

export const chonGioMon = (state) => state.cart.items;
export const chonHinhThuc = (state) => state.cart.fulfillmentType;
export const chonSoMon = (state) => state.cart.items.reduce((s, i) => s + i.quantity, 0);

export default cartSlice.reducer;
