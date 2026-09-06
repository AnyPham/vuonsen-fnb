package vn.vuonsen.fnb.modules.dishorder;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface DishOrderRepository extends JpaRepository<DishOrder, Long> {

    Optional<DishOrder> findByCode(String code);

    boolean existsByCode(String code);

    // Đếm đơn tạo trong ngày để sinh số thứ tự cho mã đơn
    @Query("SELECT COUNT(o) FROM DishOrder o WHERE o.createdAt >= :from AND o.createdAt < :to")
    long countCreatedBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /*
     * Danh sách cho trang quản trị, lọc theo trạng thái, hình thức nhận và từ khóa.
     * Tham số nào để trống thì bỏ qua điều kiện đó.
     */
    @Query("""
            SELECT o FROM DishOrder o
            WHERE (:status IS NULL OR o.status = :status)
              AND (:type IS NULL OR o.fulfillmentType = :type)
              AND (:keyword IS NULL
                   OR LOWER(o.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(o.customerName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR o.customerPhone LIKE CONCAT('%', :keyword, '%'))
            ORDER BY o.createdAt DESC
            """)
    Page<DishOrder> search(@Param("status") DishOrderStatus status,
                           @Param("type") FulfillmentType type,
                           @Param("keyword") String keyword,
                           Pageable pageable);

    Page<DishOrder> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByStatus(DishOrderStatus status);
}
