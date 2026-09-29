package vn.vuonsen.fnb.common.concurrency;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/*
 * Thử lại một thao tác ghi khi đụng độ với người khác đang ghi cùng lúc.
 *
 * Dùng cho việc sinh mã đơn. Mã đơn tính bằng "số đơn trong ngày + 1", nên hai khách
 * gửi đơn cùng lúc có thể cùng tính ra một mã: cả hai đếm được cùng một số vì chưa bên
 * nào kịp lưu. Ràng buộc UNIQUE trong database chặn được đơn thứ hai, nhưng nếu không
 * xử lý thì khách đó nhận lỗi và mất đơn.
 *
 * Cách xử lý ở đây là thử lại: lần sau đơn thứ nhất đã lưu xong, đếm lại sẽ ra số mới.
 * Không khóa từ trước vì va chạm rất hiếm; khóa mọi lần gửi đơn chỉ để phòng một
 * trường hợp hiếm là quá tay. Kiểu làm này gọi là khóa lạc quan: cứ làm, đụng thì làm lại.
 *
 * Mỗi lần thử phải là một giao dịch mới. Sau khi database báo vi phạm ràng buộc, giao
 * dịch cũ đã bị đánh dấu hủy, thử lại bên trong nó là vô ích. Vì vậy phải gọi lớp này
 * từ bên ngoài phương thức @Transactional, ví dụ từ controller.
 */
@Slf4j
@Component
public class ThuLaiKhiTrungMa {

    static final int SO_LAN_THU = 5;

    public <T> T chay(String tenRangBuoc, Supplier<T> thaoTac) {
        for (int lan = 1; ; lan++) {
            try {
                return thaoTac.get();
            } catch (DataIntegrityViolationException | ConcurrencyFailureException e) {
                if (lan >= SO_LAN_THU || !nenThuLai(e, tenRangBuoc)) {
                    throw e;
                }
                log.warn("Đụng độ khi ghi ({}) ở lần thử {}, thử lại", e.getClass().getSimpleName(), lan);
                choNgauNhien(e);
            }
        }
    }

    /**
     * Lỗi này có đúng là do vi phạm ràng buộc có tên cho trước hay không.
     *
     * Tên ràng buộc nằm trong câu báo lỗi gốc của database. MariaDB viết thường, H2 viết
     * hoa kèm hậu tố, nên so khớp không phân biệt hoa thường.
     */
    public static boolean viPham(DataIntegrityViolationException e, String tenRangBuoc) {
        String thongBao = e.getMostSpecificCause().getMessage();
        return thongBao != null
                && thongBao.toLowerCase(Locale.ROOT).contains(tenRangBuoc.toLowerCase(Locale.ROOT));
    }

    private static boolean nenThuLai(DataAccessException e, String tenRangBuoc) {
        // Khóa chết hoặc chờ khóa quá lâu: xung đột tạm thời, thử lại là qua
        if (e instanceof ConcurrencyFailureException) {
            return true;
        }
        // Trùng giá trị UNIQUE: chỉ thử lại khi trùng đúng ràng buộc được giao. Trùng
        // chỗ khác (ví dụ email) thì thử lại bao nhiêu lần cũng vẫn trùng.
        return viPham((DataIntegrityViolationException) e, tenRangBuoc);
    }

    /*
     * Chờ một khoảng ngắn ngẫu nhiên trước khi thử lại. Nếu hai bên cùng thử lại ngay lập
     * tức thì dễ lại đụng nhau y như lần trước.
     */
    private void choNgauNhien(DataAccessException loiGoc) {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextLong(10, 60));
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw loiGoc;
        }
    }
}
