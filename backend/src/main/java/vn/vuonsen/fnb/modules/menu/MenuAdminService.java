package vn.vuonsen.fnb.modules.menu;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vuonsen.fnb.common.exception.BusinessException;
import vn.vuonsen.fnb.common.exception.ResourceNotFoundException;
import vn.vuonsen.fnb.modules.menu.dto.DishAdminResponse;
import vn.vuonsen.fnb.modules.menu.dto.DishRequest;

import java.util.List;

// Thêm, sửa, ngừng bán món ăn từ trang quản trị
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuAdminService {

    private final DishRepository dishRepository;
    private final DishCategoryRepository categoryRepository;

    // Trang quản trị xem cả món đã ngừng bán, khác với thực đơn của khách
    public List<DishAdminResponse> listAll() {
        return dishRepository.findAllWithCategory().stream()
                .map(DishAdminResponse::from)
                .toList();
    }

    @Transactional
    public DishAdminResponse create(DishRequest request) {
        Dish dish = new Dish();
        apply(dish, request);
        return DishAdminResponse.from(dishRepository.save(dish));
    }

    @Transactional
    public DishAdminResponse update(Long id, DishRequest request) {
        Dish dish = getEntity(id);
        apply(dish, request);
        return DishAdminResponse.from(dishRepository.save(dish));
    }

    // Ngừng bán chứ không xóa hẳn, vì các đơn cũ vẫn cần tên món để tra cứu
    @Transactional
    public void deactivate(Long id) {
        Dish dish = getEntity(id);
        dish.setAvailable(false);
        dishRepository.save(dish);
    }

    private Dish getEntity(Long id) {
        return dishRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("món ăn", id));
    }

    private void apply(Dish dish, DishRequest r) {
        // Món tính giá linh hoạt thì để trống giá nhưng phải ghi chú cách tính,
        // nếu không khách sẽ không biết món đó bao nhiêu tiền
        if (r.price() == null && (r.priceNote() == null || r.priceNote().isBlank())) {
            throw new BusinessException("Món chưa có giá thì phải ghi chú cách tính giá");
        }

        DishCategory category = categoryRepository.findById(r.categoryId())
                .orElseThrow(() -> ResourceNotFoundException.of("danh mục món", r.categoryId()));

        dish.setCategory(category);
        dish.setName(r.name());
        dish.setDescription(r.description());
        dish.setPrice(r.price());
        dish.setPriceNote(r.priceNote());
        dish.setImageUrl(r.imageUrl());
        dish.setBestSeller(r.bestSeller() != null && r.bestSeller());
        dish.setAvailable(r.available() == null || r.available());
        dish.setSortOrder(r.sortOrder() == null ? 0 : r.sortOrder());
    }
}
