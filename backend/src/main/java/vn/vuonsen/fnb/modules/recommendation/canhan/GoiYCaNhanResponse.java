package vn.vuonsen.fnb.modules.recommendation.canhan;

import java.math.BigDecimal;
import java.util.List;

/*
 * Kết quả gợi ý cho trang chủ, gồm hai phần độc lập: sảnh và món.
 *
 * Mỗi phần tự quyết kiểu của mình. Khách đã đặt tiệc nhưng chưa gọi món nào thì phần sảnh là
 * CA_NHAN còn phần món vẫn là NOI_BAT, vì chưa có căn cứ gì về khẩu vị.
 *
 * Không trả điểm số: giao diện không hiện mức phù hợp, chỉ hiện lý do. Điểm vẫn kiểm được qua
 * GoiYCaNhanService trong các bài kiểm thử.
 */
public record GoiYCaNhanResponse(Phan<SanhGoiY> khongGian, Phan<MonGoiY> monAn) {

    public static final String CA_NHAN = "CA_NHAN";
    public static final String NOI_BAT = "NOI_BAT";

    /* kieu là CA_NHAN hoặc NOI_BAT; canCu cho biết hồ sơ dựng từ những gì. */
    public record Phan<T>(String kieu, CanCu canCu, List<T> items) {
    }

    public record CanCu(int soTiec, int soSanhDaXem, int soMonDaGoi, boolean coSoThich) {
        public static final CanCu TRONG = new CanCu(0, 0, 0, false);
    }

    /*
     * Một lý do, dạng mã kèm giá trị để giao diện tự dịch sang tiếng Việt hoặc tiếng Anh.
     * Ví dụ LOAI_TIEC / WEDDING, KHACH_GIONG / 12, HAY_GOI_CUNG / Gỏi củ hũ dừa.
     */
    public record LyDo(String ma, String giaTri, String giaTriEn) {
        public static LyDo of(String ma, Object giaTri) {
            String s = giaTri == null ? null : giaTri.toString();
            return new LyDo(ma, s, s);
        }
    }

    public record SanhGoiY(
            Long id,
            String slug,
            String name,
            String nameEn,
            String thumbnailUrl,
            Integer capacityMin,
            Integer capacityMax,
            BigDecimal rentalFee,
            String feeUnit,
            int daDat,
            List<LyDo> lyDo
    ) {
    }

    public record MonGoiY(
            Long id,
            String slug,
            String name,
            String nameEn,
            String imageUrl,
            BigDecimal price,
            String priceNote,
            String priceNoteEn,
            boolean vegetarian,
            List<LyDo> lyDo
    ) {
    }
}
