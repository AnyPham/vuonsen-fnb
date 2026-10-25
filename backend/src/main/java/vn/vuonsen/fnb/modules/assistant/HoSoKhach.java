package vn.vuonsen.fnb.modules.assistant;

import vn.vuonsen.fnb.modules.space.Space;

import java.time.LocalDate;
import java.util.List;

/*
 * Những gì trợ lý biết về người đang mở hộp thoại.
 *
 * Dựng từ dữ liệu thật của khách đó chứ không phải từ câu khách gõ, nên phải lấy danh
 * tính ở phía máy chủ. Khách chưa đăng nhập thì dùng khachLa(), không có thông tin nào.
 *
 * Mốc tính một đơn là "đã đặt": đơn có ít nhất một phiếu thu đã đối soát. Xem
 * PaymentRepository.timDonTiecDaThanhToan để biết vì sao chọn mốc đó.
 */
public record HoSoKhach(
        boolean daDangNhap,
        String tenKhach,
        int soDonDaThanhToan,
        // Không gian khách từng đặt và đã trả tiền, gần đây trước, đã bỏ trùng
        List<SanhDaDat> cacSanhDaDat,
        /*
         * Món khách đã gọi và đã trả tiền, gọi nhiều nhất trước.
         *
         * Gộp từ hai đường: đặt món lẻ và tự chọn thực đơn cho tiệc sảnh nhỏ. Khách gọi
         * một món qua cả hai đường thì cộng dồn số phần, vì với việc gợi ý thì điều đáng
         * quan tâm là họ thích món đó tới mức nào, không phải họ gọi qua đường nào.
         */
        List<MonDaGoi> cacMonDaGoi
) {

    /* Một không gian khách từng đặt, kèm lần gần nhất để trợ lý nhắc cho có mốc. */
    public record SanhDaDat(Long spaceId, String tenSanh, LocalDate lanGanNhat, int soLan) {
    }

    /* Một món khách đã gọi, kèm tổng số phần đã gọi qua mọi lần. */
    public record MonDaGoi(Long dishId, String tenMon, Long danhMucId, int soPhan) {
    }

    public static HoSoKhach khachLa() {
        return new HoSoKhach(false, null, 0, List.of(), List.of());
    }

    /*
     * Khách quen là khách đã trả tiền ít nhất một lần.
     *
     * Không đặt ngưỡng hai hay ba lần: khách trả tiền lần đầu mà lần sau vào vẫn bị chào
     * như người lạ thì còn khó chịu hơn là không cá nhân hóa gì cả.
     */
    public boolean laKhachQuen() {
        return daDangNhap && soDonDaThanhToan > 0;
    }

    public static SanhDaDat sanh(Space khongGian, LocalDate lanGanNhat, int soLan) {
        return new SanhDaDat(khongGian.getId(), khongGian.getName(), lanGanNhat, soLan);
    }
}
