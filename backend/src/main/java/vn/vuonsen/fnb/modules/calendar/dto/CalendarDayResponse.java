package vn.vuonsen.fnb.modules.calendar.dto;

import java.time.LocalDate;
import java.util.List;

/*
 * Một ô ngày trên lịch. Danh sách rỗng nghĩa là ngày đó chưa có đơn nào, và đó cũng là
 * thông tin cần nhìn thấy chứ không phải chỗ để bỏ trống.
 */
public record CalendarDayResponse(LocalDate ngay, List<CalendarItemResponse> cacMuc) {

    public int soDonTiec() {
        return (int) cacMuc.stream().filter(m -> "TIEC".equals(m.loai())).count();
    }

    public int soDonMon() {
        return (int) cacMuc.stream().filter(m -> "MON".equals(m.loai())).count();
    }
}
