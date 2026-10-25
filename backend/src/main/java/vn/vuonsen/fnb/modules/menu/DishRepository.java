package vn.vuonsen.fnb.modules.menu;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DishRepository extends JpaRepository<Dish, Long> {

    /*
     * Tham số chay chỉ nhận hai giá trị có nghĩa: null là không quan tâm, true là chỉ lấy
     * món chay. Cố ý không dùng false để lọc riêng món mặn, vì không ai vào thực đơn tìm
     * riêng món có thịt; ô lọc ngoài giao diện cũng chỉ có bật và tắt.
     */
    @Query("""
            SELECT d FROM Dish d JOIN FETCH d.category c
            WHERE d.available = true
              AND (:categoryCode IS NULL OR c.code = :categoryCode)
              AND (:keyword IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:chay IS NULL OR d.vegetarian = :chay)
            ORDER BY c.sortOrder ASC, d.sortOrder ASC
            """)
    List<Dish> search(@Param("categoryCode") String categoryCode, @Param("keyword") String keyword,
                      @Param("chay") Boolean chay);

    /** Dạng rút gọn cho những chỗ không quan tâm món chay, để các lời gọi cũ khỏi phải sửa. */
    default List<Dish> search(String categoryCode, String keyword) {
        return search(categoryCode, keyword, null);
    }

    List<Dish> findByBestSellerTrueAndAvailableTrueOrderBySortOrderAsc();

    /*
     * Ba món mới nhất đang phục vụ, cho màn mở đầu của trợ lý.
     *
     * Xếp theo ngày tạo bản ghi chứ không theo sortOrder, vì sortOrder do nhà hàng sắp cho
     * đẹp trang thực đơn, không nói lên món nào mới thêm.
     */
    List<Dish> findTop3ByAvailableTrueOrderByCreatedAtDesc();

    // Trang chi tiet mot mon. Lay kem danh muc de khoi phai truy van them lan nua.
    @Query("SELECT d FROM Dish d JOIN FETCH d.category WHERE d.slug = :slug AND d.available = true")
    Optional<Dish> findBySlugForDetail(@Param("slug") String slug);

    // Món đã từng nằm trong đơn thì không xóa hẳn được, hóa đơn cũ còn phải tra ra tên món
    @Query("SELECT COUNT(i) FROM DishOrderItem i WHERE i.dish.id = :dishId")
    long demLanDuocDat(@Param("dishId") Long dishId);

    // Trang quản trị lấy cả món đã ngừng bán nên không lọc theo available
    @Query("""
            SELECT d FROM Dish d JOIN FETCH d.category c
            ORDER BY c.sortOrder ASC, d.sortOrder ASC
            """)
    List<Dish> findAllWithCategory();

    /*
     * Món cùng loại với những món khách đã gọi, nhưng khách chưa thử bao giờ.
     *
     * Đây là phép đo "giống nhau" đơn giản nhất mà vẫn đúng với nghiệp vụ nhà hàng: cùng
     * danh mục thì cùng kiểu món, khách thích món lẩu này thì đưa món lẩu khác. Không cần
     * tới mô hình nào, và quan trọng hơn là giải thích được vì sao món đó được gợi ý.
     *
     * Hai điều kiện loại trừ đều cần thiết: bỏ món khách đã gọi rồi, vì gợi ý lại thứ họ
     * đã biết thì vô nghĩa; và chỉ lấy món đang phục vụ, để không mời khách gọi món bếp
     * đã ngừng làm.
     *
     * Xếp món bán chạy lên trước: trong cùng một danh mục, món nhiều người gọi là lựa
     * chọn an toàn hơn để giới thiệu.
     */
    @Query("""
            SELECT d FROM Dish d
            WHERE d.available = true
              AND d.category.id IN :danhMucIds
              AND d.id NOT IN :daGoiIds
            ORDER BY d.bestSeller DESC, d.sortOrder ASC
            """)
    List<Dish> monCungLoaiChuaThu(@Param("danhMucIds") Collection<Long> danhMucIds,
                                  @Param("daGoiIds") Collection<Long> daGoiIds);
}
