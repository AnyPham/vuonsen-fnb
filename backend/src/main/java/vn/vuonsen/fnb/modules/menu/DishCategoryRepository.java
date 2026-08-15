package vn.vuonsen.fnb.modules.menu;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DishCategoryRepository extends JpaRepository<DishCategory, Long> {

    List<DishCategory> findByActiveTrueOrderBySortOrderAsc();

    Optional<DishCategory> findByCode(String code);
}
