package vn.vuonsen.fnb.modules.holiday;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

/*
 * Tra mức giảm giá dịp lễ của một ngày.
 *
 * Tách thành interface để phần tính giá tiệc và tính tiền đơn món không phụ thuộc thẳng
 * vào cơ sở dữ liệu. Kiểm thử công thức tính giá chỉ cần truyền vào một hàm trả sẵn kết
 * quả, không phải dựng cả ứng dụng.
 */
@FunctionalInterface
public interface HolidayDiscountLookup {

    // Dịp lễ áp dụng cho ngày này, rỗng nếu là ngày thường
    Optional<AppliedHoliday> find(LocalDate date);

    record AppliedHoliday(String name, BigDecimal rate) {
    }
}
