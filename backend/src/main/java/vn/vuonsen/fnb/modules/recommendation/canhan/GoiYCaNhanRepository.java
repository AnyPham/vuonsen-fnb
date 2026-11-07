package vn.vuonsen.fnb.modules.recommendation.canhan;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import vn.vuonsen.fnb.modules.booking.Booking;
import vn.vuonsen.fnb.modules.menu.Dish;

import java.time.LocalDateTime;
import java.util.List;

/*
 * Mọi truy vấn dữ liệu của gợi ý thông minh gom về một chỗ, để chỉ ra được hồ sơ người dùng
 * lấy từ những bảng nào.
 *
 * Đơn đã hủy không tính ở đâu cả: khách hủy nghĩa là cuối cùng không chọn, đưa vào hồ sơ là
 * dạy hệ thống điều ngược với ý khách.
 */
public interface GoiYCaNhanRepository extends Repository<Booking, Long> {

    /* Lịch sử đặt tiệc của mọi khách có tài khoản: khách, sảnh, loại tiệc, số khách, trạng thái. */
    @Query("""
            SELECT b.user.id, b.space.id, b.eventType, b.guestCount, b.status FROM Booking b
            WHERE b.user IS NOT NULL
              AND b.status <> vn.vuonsen.fnb.modules.booking.BookingStatus.CANCELLED
            """)
    List<Object[]> lichSuDatTiec();

    @Query("""
            SELECT b.user.id, b.space.id, b.eventType, b.guestCount, b.status FROM Booking b
            WHERE b.user.id = :userId
              AND b.status <> vn.vuonsen.fnb.modules.booking.BookingStatus.CANCELLED
            """)
    List<Object[]> lichSuDatTiecCua(@Param("userId") Long userId);

    /* Số đơn của từng sảnh kể từ một mốc, để xếp "sảnh nổi bật" cho khách mới. */
    @Query("""
            SELECT b.space.id, COUNT(b) FROM Booking b
            WHERE b.status <> vn.vuonsen.fnb.modules.booking.BookingStatus.CANCELLED
              AND b.createdAt >= :tuLuc
            GROUP BY b.space.id
            """)
    List<Object[]> soDonTheoSanh(@Param("tuLuc") LocalDateTime tuLuc);

    /* Số lần một khách mở trang từng sảnh trong khoảng gần đây. */
    @Query("""
            SELECT x.doiTuongId, COUNT(x.id) FROM LuotXem x
            WHERE x.userId = :userId
              AND x.loaiDoiTuong = :loai
              AND x.xemLuc >= :tuLuc
            GROUP BY x.doiTuongId
            """)
    List<Object[]> luotXemCua(@Param("userId") Long userId,
                             @Param("loai") vn.vuonsen.fnb.modules.assistant.hanhvi.LuotXem.LoaiDoiTuong loai,
                             @Param("tuLuc") LocalDateTime tuLuc);

    /*
     * Món mọi khách đã gọi, kèm số đơn có món đó. Hai nguồn: đơn đặt món lẻ và thực đơn tự
     * chọn của tiệc sảnh nhỏ; bên gọi cộng dồn hai nguồn.
     */
    @Query("""
            SELECT o.user.id, i.dish.id, COUNT(DISTINCT o.id)
            FROM DishOrderItem i JOIN i.order o
            WHERE o.user IS NOT NULL AND i.dish IS NOT NULL
              AND o.status <> vn.vuonsen.fnb.modules.dishorder.DishOrderStatus.CANCELLED
            GROUP BY o.user.id, i.dish.id
            """)
    List<Object[]> monDaGoiTrongDonMon();

    @Query("""
            SELECT b.user.id, d.dish.id, COUNT(DISTINCT b.id)
            FROM BookingDish d JOIN d.booking b
            WHERE b.user IS NOT NULL
              AND b.status <> vn.vuonsen.fnb.modules.booking.BookingStatus.CANCELLED
            GROUP BY b.user.id, d.dish.id
            """)
    List<Object[]> monDaGoiTrongDonTiec();

    @Query("""
            SELECT o.user.id, i.dish.id, COUNT(DISTINCT o.id)
            FROM DishOrderItem i JOIN i.order o
            WHERE o.user.id = :userId AND i.dish IS NOT NULL
              AND o.status <> vn.vuonsen.fnb.modules.dishorder.DishOrderStatus.CANCELLED
            GROUP BY o.user.id, i.dish.id
            """)
    List<Object[]> monDaGoiTrongDonMonCua(@Param("userId") Long userId);

    @Query("""
            SELECT b.user.id, d.dish.id, COUNT(DISTINCT b.id)
            FROM BookingDish d JOIN d.booking b
            WHERE b.user.id = :userId
              AND b.status <> vn.vuonsen.fnb.modules.booking.BookingStatus.CANCELLED
            GROUP BY b.user.id, d.dish.id
            """)
    List<Object[]> monDaGoiTrongDonTiecCua(@Param("userId") Long userId);

    /* Số đơn có từng món kể từ một mốc, để xếp "món được gọi nhiều nhất" cho khách mới. */
    @Query("""
            SELECT i.dish.id, COUNT(DISTINCT o.id)
            FROM DishOrderItem i JOIN i.order o
            WHERE i.dish IS NOT NULL AND o.createdAt >= :tuLuc
              AND o.status <> vn.vuonsen.fnb.modules.dishorder.DishOrderStatus.CANCELLED
            GROUP BY i.dish.id
            """)
    List<Object[]> soDonTheoMonTrongDonMon(@Param("tuLuc") LocalDateTime tuLuc);

    @Query("""
            SELECT d.dish.id, COUNT(DISTINCT b.id)
            FROM BookingDish d JOIN d.booking b
            WHERE b.createdAt >= :tuLuc
              AND b.status <> vn.vuonsen.fnb.modules.booking.BookingStatus.CANCELLED
            GROUP BY d.dish.id
            """)
    List<Object[]> soDonTheoMonTrongDonTiec(@Param("tuLuc") LocalDateTime tuLuc);

    /* Món đang bán, nạp sẵn danh mục để khỏi truy vấn thêm từng món. */
    @Query("SELECT d FROM Dish d JOIN FETCH d.category WHERE d.available = true")
    List<Dish> monDangBan();
}
