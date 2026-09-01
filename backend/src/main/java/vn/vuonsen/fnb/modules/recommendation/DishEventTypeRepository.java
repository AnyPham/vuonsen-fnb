package vn.vuonsen.fnb.modules.recommendation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.vuonsen.fnb.modules.booking.EventType;

import java.util.List;

public interface DishEventTypeRepository extends JpaRepository<DishEventType, Long> {

    // Lấy kèm món và danh mục để khỏi truy vấn thêm khi chấm điểm
    @Query("""
            SELECT det FROM DishEventType det
            JOIN FETCH det.dish d
            JOIN FETCH d.category
            WHERE det.eventType = :eventType AND d.available = true
            """)
    List<DishEventType> findForEvent(@Param("eventType") EventType eventType);
}
