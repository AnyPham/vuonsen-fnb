/*
 * Định dạng tiền và ngày theo ngôn ngữ đang chọn.
 *
 * Tham số lang để mặc định là 'vi' nên mọi chỗ gọi cũ vẫn chạy y như trước, không phải
 * sửa hàng loạt. Trang nào đã dịch thì lấy bộ định dạng đã gắn sẵn ngôn ngữ qua useDinhDang().
 */

const BO_TIEN = {
  // Tiếng Việt: 15.000.000 ₫ — dấu chấm phân nhóm, ký hiệu đứng sau
  vi: new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }),
  /*
   * Tiếng Anh: 15,000,000 VND — dấu phẩy phân nhóm, chữ VND đứng sau số.
   *
   * Không dùng ký hiệu ₫ vì khách nước ngoài phần lớn không biết đó là tiền gì, cũng
   * không quy đổi sang đô la vì tỉ giá thay đổi hằng ngày, hiện số quy đổi cũ thì thành
   * báo giá sai. Chỉ định dạng số rồi tự nối chữ VND vào sau: để Intl tự chèn mã tiền tệ
   * thì nó ra "VND 15,000,000", đúng chuẩn ISO nhưng các trang du lịch và khách sạn ở
   * Việt Nam đều viết số trước, khách đọc quen kiểu đó hơn.
   */
  en: new Intl.NumberFormat('en-US', { maximumFractionDigits: 0 }),
};

const MA_VUNG = { vi: 'vi-VN', en: 'en-US' };

export const formatCurrency = (value, lang = 'vi') => {
  if (value === null || value === undefined || value === '') return '—';
  if (lang === 'en') return BO_TIEN.en.format(Number(value)) + ' VND';
  return BO_TIEN.vi.format(Number(value));
};

// "2026-08-15" thành "15/08/2026" (vi) hoặc "August 15, 2026" (en)
export const formatDate = (value, lang = 'vi') => {
  if (!value) return '—';
  const d = new Date(value);
  if (lang === 'en') {
    return d.toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' });
  }
  return d.toLocaleDateString('vi-VN');
};

export const formatDateTime = (value, lang = 'vi') => {
  if (!value) return '—';
  return new Date(value).toLocaleString(MA_VUNG[lang] || MA_VUNG.vi);
};
