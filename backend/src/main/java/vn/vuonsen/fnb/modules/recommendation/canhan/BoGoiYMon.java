package vn.vuonsen.fnb.modules.recommendation.canhan;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * Bộ xếp hạng món cho một khách, kết hợp hai cách gợi ý.
 *
 * 1. Lọc theo nội dung: diemNoiDung(d) = cos(hồ sơ khẩu vị, vector món d).
 *
 * 2. Lọc cộng tác theo món (item-based collaborative filtering), kiểu "khách gọi món này
 *    cũng hay gọi món kia" của các sàn thương mại điện tử (Sarwar và cộng sự, 2001; Linden và
 *    cộng sự, 2003). Độ giống của hai món là cosine giữa hai tập khách đã gọi chúng:
 *      sim(d, j) = |K(d) ∩ K(j)| / √(|K(d)| · |K(j)|)
 *    rồi
 *      diemCongTac(d) = trung bình sim(d, j) trên các món j khách đã gọi.
 *
 *    Món dùng cách theo món còn sảnh dùng cách theo người là có chủ ý. Thực đơn có hơn trăm
 *    món, mỗi món có đủ người gọi để hai món so được với nhau; sảnh chỉ có sáu, so sảnh với
 *    sảnh thì ma trận quá nhỏ, nên sảnh phải so người với người.
 *
 * 3. Kết hợp: diem = α · diemNoiDung + (1 − α) · diemCongTac. Khách chưa gọi món nào (chỉ có
 *    sở thích tự khai) thì dùng riêng diemNoiDung.
 *
 * Món khách đã gọi bị loại khỏi danh sách: thực đơn đủ rộng để gợi ý món mới, còn món đã gọi
 * thì khách đã biết. Khác với sảnh, nơi khách đặt lại chỗ cũ là chuyện thường.
 */
public final class BoGoiYMon {

    public record KetQua(Long monId, double diem, double diemNoiDung, double diemCongTac,
                         Long monGiongNhat) {
    }

    private BoGoiYMon() {
    }

    public static List<KetQua> xepHang(double[] hoSo, Set<Long> daGoi,
                                       Map<Long, double[]> vectorMon,
                                       Map<Long, Set<Long>> khachTheoMon,
                                       double alpha,
                                       Map<Long, Long> phoBien) {
        List<KetQua> kq = new ArrayList<>();
        for (Map.Entry<Long, double[]> e : vectorMon.entrySet()) {
            Long monId = e.getKey();
            if (daGoi.contains(monId)) {
                continue;
            }

            double noiDung = DoTuongDong.cosine(hoSo, e.getValue());

            double congTac = 0;
            Long giongNhat = null;
            double simLonNhat = 0;
            if (!daGoi.isEmpty()) {
                Set<Long> khachCuaMon = khachTheoMon.getOrDefault(monId, Set.of());
                for (Long j : daGoi) {
                    double sim = DoTuongDong.cosineTapHop(khachCuaMon, khachTheoMon.getOrDefault(j, Set.of()));
                    congTac += sim;
                    if (sim > simLonNhat) {
                        simLonNhat = sim;
                        giongNhat = j;
                    }
                }
                congTac /= daGoi.size();
            }

            double diem = daGoi.isEmpty() ? noiDung : alpha * noiDung + (1 - alpha) * congTac;
            kq.add(new KetQua(monId, diem, noiDung, congTac, giongNhat));
        }

        kq.sort(Comparator.comparingDouble(KetQua::diem).reversed()
                .thenComparing(k -> -phoBien.getOrDefault(k.monId(), 0L))
                .thenComparing(KetQua::monId));
        return kq;
    }
}
