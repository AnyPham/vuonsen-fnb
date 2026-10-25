package vn.vuonsen.fnb.modules.calendar;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vuonsen.fnb.modules.booking.Booking;
import vn.vuonsen.fnb.modules.booking.BookingRepository;
import vn.vuonsen.fnb.modules.calendar.dto.CalendarDayResponse;
import vn.vuonsen.fnb.modules.calendar.dto.CalendarItemResponse;
import vn.vuonsen.fnb.modules.dishorder.DishOrder;
import vn.vuonsen.fnb.modules.dishorder.DishOrderRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/*
 * Gom đơn đặt tiệc và đơn đặt món vào cùng một lịch theo ngày.
 *
 * Trước khi có màn hình này, nhân viên muốn biết ngày mai có gì thì phải mở hai danh sách
 * rồi tự đối chiếu, mà đơn tiệc xếp theo ngày tổ chức còn đơn món xếp theo giờ nhận. Gom
 * về một chỗ để nhìn phát biết ngay ngày nào kín, ngày nào còn trống.
 */
@Service
@RequiredArgsConstructor
public class CalendarService {

    private final BookingRepository bookingRepository;
    private final DishOrderRepository dishOrderRepository;

    /*
     * Trả về từng ngày trong khoảng, kể cả ngày không có đơn nào.
     *
     * Giữ cả ngày trống để giao diện vẽ lưới tháng không phải tự chèn ô rỗng, và để chỗ
     * trống trên lịch cũng là một thông tin: đó là ngày còn nhận khách.
     */
    @Transactional(readOnly = true)
    public List<CalendarDayResponse> trongKhoang(LocalDate tuNgay, LocalDate denNgay) {
        if (denNgay.isBefore(tuNgay)) {
            throw new IllegalArgumentException("Ngày kết thúc không được trước ngày bắt đầu");
        }

        Map<LocalDate, List<CalendarItemResponse>> theoNgay = new TreeMap<>();
        for (LocalDate ngay = tuNgay; !ngay.isAfter(denNgay); ngay = ngay.plusDays(1)) {
            theoNgay.put(ngay, new ArrayList<>());
        }

        for (Booking don : bookingRepository.findByEventDateBetween(tuNgay, denNgay)) {
            List<CalendarItemResponse> cua = theoNgay.get(don.getEventDate());
            if (cua != null) {
                cua.add(CalendarItemResponse.tuDonTiec(don));
            }
        }

        LocalDateTime dau = tuNgay.atStartOfDay();
        LocalDateTime cuoi = denNgay.atTime(LocalTime.MAX);
        for (DishOrder don : dishOrderRepository.findByServeAtBetween(dau, cuoi)) {
            List<CalendarItemResponse> cua = theoNgay.get(don.getServeAt().toLocalDate());
            if (cua != null) {
                cua.add(CalendarItemResponse.tuDonMon(don));
            }
        }

        List<CalendarDayResponse> ketQua = new ArrayList<>();
        theoNgay.forEach((ngay, muc) -> {
            muc.sort(Comparator.comparing(CalendarItemResponse::thuTu));
            ketQua.add(new CalendarDayResponse(ngay, muc));
        });
        return ketQua;
    }
}
