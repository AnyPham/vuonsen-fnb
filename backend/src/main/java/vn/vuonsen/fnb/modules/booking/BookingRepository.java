package vn.vuonsen.fnb.modules.booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByCode(String code);

    Page<Booking> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // Kiểm tra không gian đã có tiệc trong buổi đó chưa
    boolean existsBySpaceIdAndEventDateAndTimeSlotAndStatus(
            Long spaceId, LocalDate eventDate, TimeSlot timeSlot, BookingStatus status);

    // Đếm số đơn trong ngày để đánh số thứ tự cho mã đơn
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.createdAt >= :from AND b.createdAt < :to")
    long countCreatedBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // Tìm kiếm đơn cho trang quản trị
    @Query("""
            SELECT b FROM Booking b
            WHERE (:status IS NULL OR b.status = :status)
              AND (:from   IS NULL OR b.eventDate >= :from)
              AND (:to     IS NULL OR b.eventDate <= :to)
              AND (:keyword IS NULL
                   OR LOWER(b.customerName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR b.customerPhone LIKE CONCAT('%', :keyword, '%')
                   OR LOWER(b.code) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY b.createdAt DESC
            """)
    Page<Booking> search(@Param("status") BookingStatus status,
                         @Param("from") LocalDate from,
                         @Param("to") LocalDate to,
                         @Param("keyword") String keyword,
                         Pageable pageable);

    long countByStatus(BookingStatus status);
}
