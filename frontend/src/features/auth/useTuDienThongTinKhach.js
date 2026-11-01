import { useEffect, useRef } from 'react';
import { useSelector } from 'react-redux';
import { selectUser } from './authSlice';

/*
 * Tự điền họ tên, số điện thoại và email của người đang đăng nhập vào biểu mẫu đặt hàng.
 *
 * Vì sao cần: khách đã đăng nhập thì hệ thống biết thừa tên và số điện thoại của họ, bắt
 * gõ lại mỗi lần đặt là vô lý. Gõ lại còn hay sai, rồi đơn mang một số điện thoại khác
 * với số trong hồ sơ, tới lúc nhà hàng gọi xác nhận thì không khớp.
 *
 * Ba quy tắc giữ cho việc tự điền không phá thứ khách đang gõ:
 *
 * Thứ nhất, chỉ điền vào ô ĐANG TRỐNG. Khách tự gõ tên người khác, ví dụ đặt giúp bạn,
 * thì không được ghi đè.
 *
 * Thứ hai, chỉ điền MỘT LẦN cho mỗi lần đăng nhập. Không có chốt này thì khách xóa trắng
 * ô tên để sửa, hiệu ứng chạy lại và điền tên cũ vào ngay, thành ra không xóa được.
 *
 * Thứ ba, đăng xuất rồi đăng nhập tài khoản khác thì mở chốt cho điền lại, nếu không ô
 * vẫn giữ thông tin của người trước.
 */
export default function useTuDienThongTinKhach(form, dat) {
  const user = useSelector(selectUser);
  const daDienCho = useRef(null);

  useEffect(() => {
    if (!user) {
      daDienCho.current = null;
      return;
    }
    if (daDienCho.current === user.id) {
      return;
    }
    daDienCho.current = user.id;

    const them = {};
    if (!form.customerName?.trim() && user.fullName) {
      them.customerName = user.fullName;
    }
    if (!form.customerPhone?.trim() && user.phone) {
      them.customerPhone = user.phone;
    }
    if (!form.customerEmail?.trim() && user.email) {
      them.customerEmail = user.email;
    }
    if (Object.keys(them).length > 0) {
      dat(them);
    }
    // Cố ý chỉ theo dõi user: thêm form vào đây thì mỗi lần khách gõ một ký tự
    // hiệu ứng lại chạy, trong khi việc điền chỉ cần làm đúng một lần.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user]);
}
