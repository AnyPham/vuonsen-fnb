package vn.vuonsen.fnb.modules.holiday.dto;

import vn.vuonsen.fnb.common.i18n.NoiDungSongNgu;
import vn.vuonsen.fnb.modules.holiday.HolidayDiscount;

import java.math.BigDecimal;
import java.time.LocalDate;

public record HolidayDiscountResponse(
        Long id,
        String name,
        String nameEn,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal discountRate,
        boolean active
) {
    public static HolidayDiscountResponse from(HolidayDiscount h) {
        return new HolidayDiscountResponse(
                h.getId(), h.getName(), NoiDungSongNgu.ngayLe(h.getName()),
                h.getStartDate(), h.getEndDate(),
                h.getDiscountRate(), h.isActive());
    }
}
