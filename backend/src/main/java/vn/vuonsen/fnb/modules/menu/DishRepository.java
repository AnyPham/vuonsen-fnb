package vn.vuonsen.fnb.modules.menu;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DishRepository extends JpaRepository<Dish, Long> {

    @Query("""
            SELECT d FROM Dish d JOIN FETCH d.category c
            WHERE d.available = true
              AND (:categoryCode IS NULL OR c.code = :categoryCode)
              AND (:keyword IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY c.sortOrder ASC, d.sortOrder ASC
            """)
    List<Dish> search(@Param("categoryCode") String categoryCode, @Param("keyword") String keyword);

    List<Dish> findByBestSellerTrueAndAvailableTrueOrderBySortOrderAsc();

    // Trang quản trị lấy cả món đã ngừng bán nên không lọc theo available
    @Query("""
            SELECT d FROM Dish d JOIN FETCH d.category c
            ORDER BY c.sortOrder ASC, d.sortOrder ASC
            """)
    List<Dish> findAllWithCategory();
}
