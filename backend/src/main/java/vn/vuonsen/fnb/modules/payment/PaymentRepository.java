package vn.vuonsen.fnb.modules.payment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByCode(String code);

    boolean existsByCode(String code);

    long countByStatus(PaymentStatus status);

    // Lịch sử thu tiền của một đơn đặt tiệc, cũ trước mới sau cho dễ đọc
    List<Payment> findByBookingIdOrderByCreatedAtAsc(Long bookingId);

    List<Payment> findByDishOrderIdOrderByCreatedAtAsc(Long dishOrderId);

    /*
     * Phiếu còn chờ đối soát của một đơn.
     *
     * Khách bấm thanh toán hai lần thì không tạo hai phiếu chờ cho cùng một khoản, mà đưa
     * lại phiếu cũ kèm đúng mã QR cũ.
     */
    Optional<Payment> findFirstByBookingIdAndPurposeAndStatus(
            Long bookingId, PaymentPurpose purpose, PaymentStatus status);

    Optional<Payment> findFirstByDishOrderIdAndPurposeAndStatus(
            Long dishOrderId, PaymentPurpose purpose, PaymentStatus status);

    // Màn hình đối soát: lọc theo trạng thái và tìm theo mã phiếu, mã đơn hoặc nội dung
    @Query("""
            SELECT p FROM Payment p
            LEFT JOIN p.booking b
            LEFT JOIN p.dishOrder d
            WHERE (:status IS NULL OR p.status = :status)
              AND (:keyword IS NULL
                   OR LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(p.reference) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(b.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(d.code) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY p.createdAt DESC
            """)
    Page<Payment> search(@Param("status") PaymentStatus status,
                         @Param("keyword") String keyword,
                         Pageable pageable);
}
