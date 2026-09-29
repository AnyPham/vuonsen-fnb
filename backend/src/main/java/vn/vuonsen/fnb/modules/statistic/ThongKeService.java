package vn.vuonsen.fnb.modules.statistic;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vuonsen.fnb.common.exception.BusinessException;
import vn.vuonsen.fnb.modules.booking.BookingRepository;
import vn.vuonsen.fnb.modules.dishorder.DishOrderRepository;
import vn.vuonsen.fnb.modules.space.SpaceRepository;
import vn.vuonsen.fnb.modules.statistic.dto.ThongKeResponse;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

/*
 * Nạp dữ liệu cho trang thống kê rồi giao cho ThongKeAggregator gộp lại.
 *
 * Lớp này chỉ lo việc đọc cơ sở dữ liệu và kiểm tra khoảng ngày. Toàn bộ cách tính nằm
 * ở aggregator để kiểm thử được mà không cần dựng ứng dụng.
 */
@Service
@RequiredArgsConstructor
public class ThongKeService {

    /*
     * Giới hạn khoảng thống kê.
     *
     * Thống kê nạp toàn bộ đơn trong khoảng vào bộ nhớ rồi mới gộp. Cách này gọn và dễ
     * kiểm thử, đổi lại phải chặn khoảng quá dài để một lần bấm nhầm không kéo cả chục
     * năm dữ liệu lên. Ba năm là quá đủ cho một nhà hàng.
     */
    private static final int SO_NGAY_TOI_DA = 366 * 3;

    /** Mặc định xem 12 tháng gần nhất, tính cả tháng hiện tại. */
    private static final int SO_THANG_MAC_DINH = 11;

    private final BookingRepository bookingRepository;
    private final DishOrderRepository dishOrderRepository;
    private final SpaceRepository spaceRepository;
    private final ThongKeAggregator aggregator;

    @Transactional(readOnly = true)
    public ThongKeResponse thongKe(LocalDate tuNgay, LocalDate denNgay) {
        LocalDate den = denNgay != null ? denNgay : LocalDate.now().withDayOfMonth(1).plusMonths(1).minusDays(1);
        LocalDate tu = tuNgay != null ? tuNgay : den.withDayOfMonth(1).minusMonths(SO_THANG_MAC_DINH);

        kiemTraKhoang(tu, den);

        return aggregator.gop(
                tu, den,
                bookingRepository.thongKeTheoKhoang(tu, den),
                // Đơn đặt món lưu theo giờ, nên phải trải khoảng ngày ra thành khoảng
                // thời điểm: từ 0h00 ngày đầu tới 23h59:59 ngày cuối.
                dishOrderRepository.thongKeTheoKhoang(tu.atStartOfDay(), den.atTime(LocalTime.MAX)),
                spaceRepository.findAllByOrderBySortOrderAsc());
    }

    private void kiemTraKhoang(LocalDate tu, LocalDate den) {
        if (tu.isAfter(den)) {
            throw new BusinessException("Ngày bắt đầu phải trước ngày kết thúc");
        }
        long soNgay = ChronoUnit.DAYS.between(tu, den) + 1;
        if (soNgay > SO_NGAY_TOI_DA) {
            throw new BusinessException(
                    "Khoảng thống kê tối đa 3 năm, khoảng bạn chọn dài %d ngày".formatted(soNgay));
        }
    }
}
