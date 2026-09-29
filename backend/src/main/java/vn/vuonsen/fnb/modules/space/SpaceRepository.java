package vn.vuonsen.fnb.modules.space;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface SpaceRepository extends JpaRepository<Space, Long> {

    Optional<Space> findBySlug(String slug);

    Optional<Space> findByCode(String code);

    /*
     * Khóa dòng không gian cho tới khi giao dịch kết thúc (SELECT ... FOR UPDATE).
     *
     * Dùng khi xác nhận đơn: hai người cùng xác nhận hai đơn của cùng một không gian thì
     * người sau phải đứng chờ người trước làm xong rồi mới được kiểm tra lịch. Khóa cả
     * không gian chứ không riêng từng buổi, vì luật thuê trọn ngày làm một đơn đụng tới
     * mọi buổi trong ngày. Nhà hàng xác nhận vài đơn mỗi ngày nên chờ nhau không đáng kể.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Space s WHERE s.id = :id")
    Optional<Space> khoaDeXacNhanDon(@Param("id") Long id);

    List<Space> findByActiveTrueOrderBySortOrderAsc();

    // Trang quản trị lấy cả không gian đã ngừng kinh doanh
    List<Space> findAllByOrderBySortOrderAsc();

    // Lọc không gian theo số khách, loại và giá. Tham số nào null thì bỏ qua.
    @Query("""
            SELECT s FROM Space s
            WHERE s.active = true
              AND (:guests   IS NULL OR :guests <= s.capacityMax)
              AND (:type     IS NULL OR s.spaceType = :type)
              AND (:maxPrice IS NULL OR s.rentalFee <= :maxPrice)
            ORDER BY s.sortOrder ASC
            """)
    List<Space> search(@Param("guests") Integer guests,
                       @Param("type") SpaceType type,
                       @Param("maxPrice") BigDecimal maxPrice);
}
