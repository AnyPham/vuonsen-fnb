package vn.vuonsen.fnb.modules.space;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface SpaceRepository extends JpaRepository<Space, Long> {

    Optional<Space> findBySlug(String slug);

    Optional<Space> findByCode(String code);

    List<Space> findByActiveTrueOrderBySortOrderAsc();

    // Lọc không gian theo số khách, loại và giá. Tham số nào null thì bỏ qua.
    @Query("""
            SELECT s FROM Space s
            WHERE s.active = true
              AND (:guests   IS NULL OR (:guests BETWEEN s.capacityMin AND s.capacityMax))
              AND (:type     IS NULL OR s.spaceType = :type)
              AND (:maxPrice IS NULL OR s.rentalFee <= :maxPrice)
            ORDER BY s.sortOrder ASC
            """)
    List<Space> search(@Param("guests") Integer guests,
                       @Param("type") SpaceType type,
                       @Param("maxPrice") BigDecimal maxPrice);
}
