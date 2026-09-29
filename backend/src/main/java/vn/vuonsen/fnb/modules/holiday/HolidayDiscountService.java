package vn.vuonsen.fnb.modules.holiday;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// Tra mức giảm giá dịp lễ từ cơ sở dữ liệu, dùng khi tính giá tiệc và tính tiền đơn món
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HolidayDiscountService implements HolidayDiscountLookup {

    private final HolidayDiscountRepository repository;

    @Override
    public Optional<AppliedHoliday> find(LocalDate date) {
        if (date == null) {
            return Optional.empty();
        }
        return repository.findActiveOn(date).stream()
                .findFirst()
                .map(h -> new AppliedHoliday(h.getName(), h.getDiscountRate()));
    }

    /*
     * Các dịp lễ đang bật chưa kết thúc, dịp gần nhất lên đầu.
     *
     * Trợ lý tư vấn dùng để báo khách. Dịp đang diễn ra vẫn được tính vì khách hỏi giữa dịp
     * thì vẫn còn được giảm; dịp đã qua bị loại để trợ lý không báo nhầm một ưu đãi đã hết.
     */
    public List<HolidayDiscount> sapToi(LocalDate tuNgay, int toiDa) {
        return repository.findByActiveTrueAndEndDateGreaterThanEqualOrderByStartDateAsc(
                tuNgay, PageRequest.of(0, toiDa));
    }
}
