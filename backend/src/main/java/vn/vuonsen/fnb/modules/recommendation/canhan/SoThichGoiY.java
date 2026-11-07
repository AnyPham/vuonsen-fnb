package vn.vuonsen.fnb.modules.recommendation.canhan;

import vn.vuonsen.fnb.modules.booking.EventType;
import vn.vuonsen.fnb.modules.space.SpaceType;

import java.util.Set;

/*
 * Sở thích khách tự khai trong trang hồ sơ, ở dạng mà hệ gợi ý dùng.
 *
 * Mọi trường đều có thể trống: khách khai tới đâu dùng tới đó. Đây là phần "thông tin hồ sơ
 * kê khai" trong yêu cầu của cô hướng dẫn, khác với phần lịch sử đặt tiệc mà hệ thống tự ghi.
 */
public record SoThichGoiY(
        Set<EventType> loaiTiec,
        Integer soKhach,
        KieuKhongGian kieuKhongGian,
        TamGia tamGia,
        boolean anChay
) {

    public boolean trong() {
        return (loaiTiec == null || loaiTiec.isEmpty()) && soKhach == null
                && kieuKhongGian == null && tamGia == null && !anChay;
    }

    /*
     * Sáu loại không gian gom về hai kiểu khách hay phân vân: trong nhà hay ngoài trời.
     *
     * Không dùng thẳng sáu loại vì mỗi sảnh của Vườn Sen đang mang một loại riêng, giữ nguyên
     * thì nhóm này thành ra "đúng sảnh đó hay không", lặp lại việc đếm sảnh đã đặt chứ không
     * nói được gì về khẩu vị. Chòi lá trên mặt nước tính là ngoài trời vì không có vách và
     * máy lạnh.
     */
    public enum KieuKhongGian {
        TRONG_NHA, NGOAI_TROI;

        public static KieuKhongGian cua(SpaceType loai) {
            if (loai == null) {
                return null;
            }
            return switch (loai) {
                case OUTDOOR, GARDEN, HUT -> NGOAI_TROI;
                case INDOOR, PRIVATE, CONFERENCE -> TRONG_NHA;
            };
        }
    }

    /* Ba tầm giá. Sảnh và món tự chia ba bậc theo giá của chính nhà hàng, xem DacTrungKhongGian. */
    public enum TamGia {
        TIET_KIEM, VUA_PHAI, CAO_CAP
    }
}
