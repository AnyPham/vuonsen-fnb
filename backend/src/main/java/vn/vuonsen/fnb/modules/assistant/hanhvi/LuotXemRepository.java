package vn.vuonsen.fnb.modules.assistant.hanhvi;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LuotXemRepository extends JpaRepository<LuotXem, Long> {

    /*
     * Những thứ khách xem nhiều nhất mà chưa từng đặt và trả tiền.
     *
     * Đây là truy vấn mang ý nghĩa nghiệp vụ chính của cả bảng. Xem nhiều lần là tín hiệu
     * quan tâm; chưa có đơn đã thanh toán nghĩa là mối quan tâm đó chưa thành giao dịch.
     * Giao điểm của hai điều kiện là chỗ trợ lý nên lên tiếng, vì nói đúng thứ khách đang
     * phân vân thì hữu ích, còn nhắc lại thứ khách đã đặt rồi thì thừa.
     *
     * Chỉ tính luot xem trong khoảng gần đây: khách xem một sảnh từ tám tháng trước thì
     * mối quan tâm đó đã nguội, nhắc lại là nhắc sai.
     *
     * Trả id kèm số lần xem, xếp theo số lần giảm dần.
     */
    @Query("""
            SELECT x.doiTuongId, COUNT(x.id) FROM LuotXem x
            WHERE x.userId = :userId
              AND x.loaiDoiTuong = :loai
              AND x.xemLuc >= :tuLuc
              AND x.doiTuongId NOT IN (
                  SELECT b.space.id FROM Payment p JOIN p.booking b
                  WHERE b.user.id = :userId
                    AND p.status = vn.vuonsen.fnb.modules.payment.PaymentStatus.CONFIRMED
                    AND b.space IS NOT NULL
              )
            GROUP BY x.doiTuongId
            ORDER BY COUNT(x.id) DESC
            """)
    List<Object[]> khongGianQuanTamChuaDat(@Param("userId") Long userId,
                                           @Param("loai") LuotXem.LoaiDoiTuong loai,
                                           @Param("tuLuc") LocalDateTime tuLuc);

    /* Tổng số lượt xem của một khách, để biết hồ sơ hành vi đã đủ dày để dùng hay chưa. */
    long countByUserId(Long userId);
}
