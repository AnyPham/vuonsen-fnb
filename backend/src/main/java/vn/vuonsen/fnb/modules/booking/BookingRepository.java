package vn.vuonsen.fnb.modules.booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByCode(String code);

    Page<Booking> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    boolean existsByCode(String code);

    // Các tiệc đã xác nhận của một không gian trong một ngày, dùng để kiểm tra trùng lịch
    List<Booking> findBySpaceIdAndEventDateAndStatus(
            Long spaceId, LocalDate eventDate, BookingStatus status);

    // Các đơn còn chờ duyệt của cùng không gian, cùng ngày, cùng buổi
    List<Booking> findBySpaceIdAndEventDateAndTimeSlotAndStatus(
            Long spaceId, LocalDate eventDate, TimeSlot timeSlot, BookingStatus status);

    /*
     * Toàn bộ tiệc đã xác nhận trong một ngày, không phân biệt không gian.
     *
     * Dùng để dựng bảng tình trạng trống cho khách xem: lấy một lần rồi gom theo không gian,
     * thay vì hỏi cơ sở dữ liệu một lần cho mỗi không gian.
     */
    @Query("""
            SELECT b FROM Booking b
            JOIN FETCH b.space
            LEFT JOIN FETCH b.partyPackage
            WHERE b.eventDate = :ngay AND b.status = :trangThai
            """)
    List<Booking> findTrongNgay(@Param("ngay") LocalDate ngay,
                                @Param("trangThai") BookingStatus trangThai);

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

    /*
     * Nạp đơn trong khoảng ngày để dựng thống kê.
     *
     * Dùng JOIN FETCH cho không gian và gói tiệc vì hai quan hệ này khai là LAZY, mà
     * open-in-view đang tắt. Không nạp sẵn thì mỗi đơn lại sinh thêm một truy vấn khi
     * phần thống kê đọc tên không gian và tên gói.
     *
     * Gói tiệc phải LEFT JOIN: đơn chỉ thuê không gian không có gói, JOIN thường sẽ
     * âm thầm loại các đơn đó ra khỏi thống kê.
     */
    @Query("""
            SELECT b FROM Booking b
            JOIN FETCH b.space
            LEFT JOIN FETCH b.partyPackage
            WHERE b.eventDate BETWEEN :from AND :to
            ORDER BY b.eventDate
            """)
    List<Booking> thongKeTheoKhoang(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
