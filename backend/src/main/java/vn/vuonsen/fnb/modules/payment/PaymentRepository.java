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

    /*
     * Các đơn tiệc của một khách đã thực sự có tiền vào.
     *
     * Mốc "đã thanh toán" lấy ở phiếu thu đã đối soát chứ không lấy ở trạng thái đơn, vì
     * đơn chỉ có bốn trạng thái chờ, xác nhận, hoàn thành, hủy, không trạng thái nào nói
     * được khách đã trả tiền hay chưa. Tính cả phiếu cọc lẫn phiếu trả toàn bộ: khách đã
     * chuyển tiền giữ ngày thì đã là khách thật, không còn là người điền thử biểu mẫu.
     *
     * Dùng DISTINCT vì một đơn có thể có nhiều phiếu thu, chẳng hạn cọc trước rồi trả nốt.
     */
    @Query("""
            SELECT DISTINCT b FROM Payment p JOIN p.booking b
            WHERE b.user.id = :userId
              AND p.status = vn.vuonsen.fnb.modules.payment.PaymentStatus.CONFIRMED
            ORDER BY b.eventDate DESC
            """)
    List<vn.vuonsen.fnb.modules.booking.Booking> timDonTiecDaThanhToan(@Param("userId") Long userId);

    /*
     * Xếp hạng không gian theo số đơn đã thanh toán, nhiều nhất trước.
     *
     * Cùng một mốc với hàm trên, để con số "được đặt nhiều nhất" nói đúng chuyện khách
     * chịu trả tiền chứ không phải chuyện khách bấm gửi biểu mẫu.
     *
     * Trả id kèm số đơn chứ không trả thực thể, để chỗ gọi tự quyết lấy bao nhiêu hạng.
     */
    @Query("""
            SELECT b.space.id, COUNT(DISTINCT b.id) FROM Payment p JOIN p.booking b
            WHERE p.status = vn.vuonsen.fnb.modules.payment.PaymentStatus.CONFIRMED
            GROUP BY b.space.id
            ORDER BY COUNT(DISTINCT b.id) DESC
            """)
    List<Object[]> xepHangKhongGianTheoDonDaThanhToan();

    /* Xếp hạng gói tiệc theo số đơn đã thanh toán. Đơn chỉ thuê trống không có gói nên bỏ qua. */
    @Query("""
            SELECT b.partyPackage.id, COUNT(DISTINCT b.id) FROM Payment p JOIN p.booking b
            WHERE p.status = vn.vuonsen.fnb.modules.payment.PaymentStatus.CONFIRMED
              AND b.partyPackage IS NOT NULL
            GROUP BY b.partyPackage.id
            ORDER BY COUNT(DISTINCT b.id) DESC
            """)
    List<Object[]> xepHangGoiTiecTheoDonDaThanhToan();

    /*
     * Những món khách đã từng gọi và đã trả tiền, kèm số lần gọi.
     *
     * Khách gọi món qua hai đường khác nhau nên phải gộp cả hai, nếu không thì khách đặt
     * món lẻ suốt mà trợ lý bảo chưa từng gọi món nào:
     *   - đặt món lẻ giao tận nhà hoặc ăn tại chỗ, nằm ở dish_order_items
     *   - tự chọn thực đơn cho tiệc sảnh nhỏ, nằm ở booking_dishes
     *
     * Vẫn một mốc với các truy vấn trên: chỉ tính đơn có phiếu thu đã đối soát.
     */
    @Query("""
            SELECT m.id, SUM(i.quantity) FROM Payment p
                JOIN p.dishOrder o
                JOIN DishOrderItem i ON i.order = o
                JOIN i.dish m
            WHERE o.user.id = :userId
              AND p.status = vn.vuonsen.fnb.modules.payment.PaymentStatus.CONFIRMED
            GROUP BY m.id
            ORDER BY SUM(i.quantity) DESC
            """)
    List<Object[]> monDaGoiTrongDonMon(@Param("userId") Long userId);

    @Query("""
            SELECT m.id, SUM(d.soLuong) FROM Payment p
                JOIN p.booking b
                JOIN BookingDish d ON d.booking = b
                JOIN d.dish m
            WHERE b.user.id = :userId
              AND p.status = vn.vuonsen.fnb.modules.payment.PaymentStatus.CONFIRMED
            GROUP BY m.id
            ORDER BY SUM(d.soLuong) DESC
            """)
    List<Object[]> monDaGoiTrongDonTiec(@Param("userId") Long userId);
}
