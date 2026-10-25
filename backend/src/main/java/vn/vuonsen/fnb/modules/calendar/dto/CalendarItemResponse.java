package vn.vuonsen.fnb.modules.calendar.dto;

import vn.vuonsen.fnb.modules.booking.Booking;
import vn.vuonsen.fnb.modules.dishorder.DishOrder;

import java.math.BigDecimal;

/*
 * Một mục trên lịch. Dùng chung cho cả đơn tiệc lẫn đơn món nên các trường riêng của
 * từng loại để rỗng khi không áp dụng, thay vì tách thành hai kiểu trả về.
 */
public record CalendarItemResponse(
        String loai,
        String maDon,
        String tenKhach,
        String moTa,
        String buoi,
        String gio,
        String trangThai,
        String maTrangThai,
        BigDecimal tongTien,
        int thuTu) {

    public static CalendarItemResponse tuDonTiec(Booking don) {
        String khongGian = don.getSpace() != null ? don.getSpace().getName() : "Chưa chọn không gian";
        String goi = don.getPartyPackage() != null
                ? don.getPartyPackage().getName()
                : "Chỉ thuê không gian";
        return new CalendarItemResponse(
                "TIEC",
                don.getCode(),
                don.getCustomerName(),
                khongGian + " · " + goi + " · " + don.getGuestCount() + " khách",
                don.getTimeSlot() != null ? don.getTimeSlot().getLabel() : "",
                "",
                don.getStatus().getLabel(),
                don.getStatus().name(),
                don.getTotalAmount(),
                /* Xếp theo buổi để một ngày đọc từ sáng xuống tối. */
                don.getTimeSlot() != null ? don.getTimeSlot().ordinal() : 9);
    }

    public static CalendarItemResponse tuDonMon(DishOrder don) {
        return new CalendarItemResponse(
                "MON",
                don.getCode(),
                don.getCustomerName(),
                don.getFulfillmentType().getLabel(),
                "",
                don.getServeAt() != null ? don.getServeAt().toLocalTime().toString() : "",
                don.getStatus().getLabel(),
                don.getStatus().name(),
                don.getTotal(),
                /* Đơn món xếp sau đơn tiệc trong cùng một ngày. */
                20 + (don.getServeAt() != null ? don.getServeAt().getHour() : 0));
    }
}
