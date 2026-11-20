package vn.vuonsen.fnb.common.util;

/*
 * Chuẩn hóa từ khóa trước khi đưa vào mệnh đề LIKE.
 *
 * Trong LIKE, % khớp mọi chuỗi và _ khớp mọi ký tự. Đưa nguyên từ khóa khách gõ vào thì
 * gõ "%" là ra toàn bộ dữ liệu (lỗi KAN-65). Ở đây thoát hai ký tự đó bằng dấu "!" để
 * chúng được hiểu theo nghĩa đen; câu truy vấn đi kèm phải khai ESCAPE '!'.
 *
 * Chọn "!" thay vì "\" vì MySQL coi "\" trong chuỗi là ký tự thoát của chính nó, viết
 * ESCAPE '\' sẽ lỗi cú pháp, còn H2 lại hiểu khác; "!" chạy giống nhau trên cả hai.
 */
public final class TuKhoaTimKiem {

    public static final char KY_TU_THOAT = '!';

    private TuKhoaTimKiem() {
    }

    /** Trả về null khi không có từ khóa, để câu truy vấn bỏ qua điều kiện lọc. */
    public static String choLike(String tuKhoa) {
        if (tuKhoa == null || tuKhoa.isBlank()) {
            return null;
        }
        String s = tuKhoa.trim();
        StringBuilder kq = new StringBuilder(s.length() + 4);
        for (char c : s.toCharArray()) {
            if (c == KY_TU_THOAT || c == '%' || c == '_') {
                kq.append(KY_TU_THOAT);
            }
            kq.append(c);
        }
        return kq.toString();
    }
}
