package vn.vuonsen.fnb.common.concurrency;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.function.LongPredicate;
import java.util.function.LongSupplier;

/*
 * Cấp số thứ tự trong ngày cho mã đơn và mã phiếu thu, mỗi lần cấp một số chưa ai nhận.
 *
 * Mã có dạng TIỀN TỐ-ngày-số thứ tự, ví dụ VS-20261102-0017, số thứ tự chạy lại mỗi ngày.
 * Trước đây mỗi nơi tự tính "số bản ghi trong ngày cộng một" rồi dò tiếp nếu số đó đã có.
 * Một người dùng thì đúng. Nhiều người cùng lúc thì mọi người đếm ra cùng một số vì chưa ai
 * kịp lưu, cùng ghi, và database chặn người sau vì trùng ràng buộc UNIQUE. ThuLaiKhiTrungMa
 * cứu được khi va chạm hiếm, nhưng không cứu được khi tải dồn: mỗi vòng thử lại chỉ một người
 * thắng, những người còn lại lại tranh đúng số kế tiếp. Đo tải ghi nhận 20 lượt đặt tiệc cùng
 * lúc thì 6 lượt hỏng, 14 phiếu thu cùng lúc thì 12 phiếu hỏng.
 *
 * Cách giải là cấp số ở một chỗ có khóa. Mỗi lần cấp lấy số lớn hơn của hai nguồn: số bản ghi
 * đã lưu trong database, và số vừa cấp trong bộ nhớ mà có thể chưa kịp lưu, rồi cộng một.
 * Người tới sau chờ người tới trước cấp xong mới được cấp, nên không bao giờ nhận trùng số.
 * Đoạn có khóa chỉ gồm một câu truy vấn đếm, thời gian chờ không đáng kể.
 *
 * Số đã cấp mà giao dịch sau đó bị hủy, ví dụ đơn bị từ chối vì trùng lịch, thì bỏ trống luôn.
 * Dãy số có thể hở vài chỗ, đổi lại không bao giờ trùng, giống số hóa đơn ở quầy.
 *
 * Giới hạn: khóa nằm trong một tiến trình máy chủ. Hệ thống hiện chạy đúng một máy chủ. Nếu sau
 * này chạy nhiều máy chủ song song thì hai máy vẫn có thể cấp trùng; khi đó ThuLaiKhiTrungMa ở
 * controller vẫn là lưới an toàn, và nên chuyển sang bộ đếm đặt trong database.
 */
@Component
public class CapSoThuTu {

    /** Số đã cấp gần nhất của từng tiền tố, kèm ngày cấp để sang ngày mới thì đếm lại từ đầu. */
    private record DaCap(LocalDate ngay, long so) {
    }

    private final Map<String, DaCap> soDaCap = new HashMap<>();

    /**
     * Cấp số thứ tự tiếp theo cho một tiền tố trong một ngày.
     *
     * @param tienTo  tiền tố mã, mỗi tiền tố một dãy số riêng, ví dụ VS, DM, TT-B
     * @param ngay    ngày của mã
     * @param soDaLuu số bản ghi mang tiền tố này đã lưu trong ngày, làm điểm xuất phát
     * @param daDung  số này đã có bản ghi mang mã đó trong database chưa
     */
    public synchronized long cap(String tienTo, LocalDate ngay, LongSupplier soDaLuu, LongPredicate daDung) {
        DaCap truoc = soDaCap.get(tienTo);
        long daCapHomNay = truoc != null && truoc.ngay().equals(ngay) ? truoc.so() : 0;

        long so = Math.max(soDaLuu.getAsLong(), daCapHomNay) + 1;
        while (daDung.test(so)) {
            so++;
        }
        soDaCap.put(tienTo, new DaCap(ngay, so));
        return so;
    }
}
