package vn.vuonsen.fnb.modules.recommendation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.vuonsen.fnb.modules.booking.EventType;

import java.util.List;

public interface RecommendationLogRepository extends JpaRepository<RecommendationLog, Long> {

    @Query("SELECT COUNT(r) FROM RecommendationLog r WHERE r.accepted = true")
    long countAccepted();

    // Tổ hợp không gian và gói tiệc được đặt nhiều nhất cho một loại sự kiện.
    // Đây là phần gợi ý dựa trên lịch sử thay vì chỉ dựa trên tiêu chí.
    @Query("""
            SELECT b.space.id, b.partyPackage.id, COUNT(b)
            FROM Booking b
            WHERE b.eventType = :eventType
              AND b.status <> vn.vuonsen.fnb.modules.booking.BookingStatus.CANCELLED
            GROUP BY b.space.id, b.partyPackage.id
            ORDER BY COUNT(b) DESC
            """)
    List<Object[]> popularCombinations(@Param("eventType") EventType eventType);
}
