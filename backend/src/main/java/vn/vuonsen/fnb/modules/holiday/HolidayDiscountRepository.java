package vn.vuonsen.fnb.modules.holiday;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface HolidayDiscountRepository extends JpaRepository<HolidayDiscount, Long> {

    // Các dịp đang bật có chứa ngày này. Hai dịp trùng khoảng thì dịp giảm nhiều hơn đứng đầu.
    @Query("""
            SELECT h FROM HolidayDiscount h
            WHERE h.active = true AND h.startDate <= :date AND h.endDate >= :date
            ORDER BY h.discountRate DESC
            """)
    List<HolidayDiscount> findActiveOn(@Param("date") LocalDate date);

    // Trang quản trị: dịp mới nhất lên đầu
    List<HolidayDiscount> findAllByOrderByStartDateDesc();

    // Các dịp đang bật chưa kết thúc tính tới ngày cho trước, dịp gần nhất lên đầu
    List<HolidayDiscount> findByActiveTrueAndEndDateGreaterThanEqualOrderByStartDateAsc(LocalDate date, Pageable pageable);
}
