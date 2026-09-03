package vn.vuonsen.fnb.modules.assistant;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/*
 * Định dạng tiền theo kiểu Việt Nam, ví dụ 1.250.000đ.
 *
 * Cả câu trả lời dự phòng lẫn phần dữ liệu gửi cho mô hình ngôn ngữ đều cần in
 * tiền giống nhau, nên gom về một chỗ để không có hai kiểu hiển thị lệch nhau.
 */
final class TienTe {

    private TienTe() {
    }

    static String dinhDang(BigDecimal soTien) {
        if (soTien == null) {
            return "liên hệ";
        }
        DecimalFormatSymbols kyHieu = new DecimalFormatSymbols(Locale.forLanguageTag("vi-VN"));
        kyHieu.setGroupingSeparator('.');
        return new DecimalFormat("#,###", kyHieu).format(soTien.setScale(0, RoundingMode.HALF_UP)) + "đ";
    }
}
