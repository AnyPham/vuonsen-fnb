package vn.vuonsen.fnb.modules.holiday;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vuonsen.fnb.common.exception.BusinessException;
import vn.vuonsen.fnb.common.exception.ResourceNotFoundException;
import vn.vuonsen.fnb.modules.holiday.dto.HolidayDiscountRequest;
import vn.vuonsen.fnb.modules.holiday.dto.HolidayDiscountResponse;

import java.util.List;

// Thêm, sửa, xóa dịp lễ giảm giá từ trang quản trị
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HolidayAdminService {

    private final HolidayDiscountRepository repository;

    public List<HolidayDiscountResponse> listAll() {
        return repository.findAllByOrderByStartDateDesc().stream()
                .map(HolidayDiscountResponse::from)
                .toList();
    }

    @Transactional
    public HolidayDiscountResponse create(HolidayDiscountRequest request) {
        HolidayDiscount holiday = new HolidayDiscount();
        apply(holiday, request);
        return HolidayDiscountResponse.from(repository.save(holiday));
    }

    @Transactional
    public HolidayDiscountResponse update(Long id, HolidayDiscountRequest request) {
        HolidayDiscount holiday = getEntity(id);
        apply(holiday, request);
        return HolidayDiscountResponse.from(repository.save(holiday));
    }

    /*
     * Xóa hẳn, không chỉ ngừng như gói tiệc.
     *
     * Đơn đã đặt lưu sẵn số tiền giảm đã chốt lúc đặt, không trỏ tới dịp lễ nào, nên xóa
     * một dịp không làm đổi đơn cũ. Muốn tạm ngưng mà vẫn giữ lại thì tắt "Đang áp dụng".
     */
    @Transactional
    public void delete(Long id) {
        repository.delete(getEntity(id));
    }

    private HolidayDiscount getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("dịp lễ", id));
    }

    private void apply(HolidayDiscount holiday, HolidayDiscountRequest r) {
        if (r.endDate().isBefore(r.startDate())) {
            throw new BusinessException("Ngày kết thúc không được trước ngày bắt đầu");
        }
        holiday.setName(r.name().trim());
        holiday.setStartDate(r.startDate());
        holiday.setEndDate(r.endDate());
        holiday.setDiscountRate(r.discountRate());
        holiday.setActive(r.active() == null || r.active());
    }
}
