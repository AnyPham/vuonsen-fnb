package vn.vuonsen.fnb.modules.partypackage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PartyPackageRepository extends JpaRepository<PartyPackage, Long> {

    List<PartyPackage> findByActiveTrueOrderBySortOrderAsc();

    Optional<PartyPackage> findByCode(String code);

    // Trang quản trị lấy cả gói đã ngừng bán
    List<PartyPackage> findAllByOrderBySortOrderAsc();

    /*
     * Lọc gói tiệc theo ngân sách mỗi mâm, số món và thời gian dùng không gian.
     * Tham số nào null thì bỏ qua, giống hệt cách bộ lọc không gian làm.
     *
     * Hai cột dish_count và hours_included cho phép bỏ trống. Gói nào bỏ trống mà khách
     * lại đặt điều kiện đúng vào cột đó thì phép so sánh ra NULL nên gói bị loại. Đó là
     * kết quả đúng: chưa khai số món thì không khẳng định được gói đủ số món khách cần.
     */
    @Query("""
            SELECT p FROM PartyPackage p
            WHERE p.active = true
              AND (:maxPrice  IS NULL OR p.pricePerTable <= :maxPrice)
              AND (:minDishes IS NULL OR p.dishCount     >= :minDishes)
              AND (:minHours  IS NULL OR p.hoursIncluded >= :minHours)
            ORDER BY p.sortOrder ASC
            """)
    List<PartyPackage> search(@Param("maxPrice") BigDecimal maxPrice,
                              @Param("minDishes") Integer minDishes,
                              @Param("minHours") Integer minHours);
}
