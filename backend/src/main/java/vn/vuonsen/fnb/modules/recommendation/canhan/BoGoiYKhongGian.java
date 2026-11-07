package vn.vuonsen.fnb.modules.recommendation.canhan;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * Bộ xếp hạng sảnh cho một khách, kết hợp hai cách gợi ý.
 *
 * 1. Lọc theo nội dung (content-based filtering)
 *      diemNoiDung(s) = cos(hồ sơ khách, vector sảnh s)
 *    Sảnh càng giống hồ sơ thì điểm càng cao. Trả lời câu "sảnh nào tương đồng với hồ sơ
 *    của người dùng".
 *
 * 2. Lọc cộng tác theo người dùng (user-based collaborative filtering)
 *    Tìm k khách có hồ sơ giống khách này nhất (láng giềng), rồi
 *      diemCongTac(s) = Σ sim(u, v) · [v đã đặt s]  /  Σ sim(u, v),   v chạy trên k láng giềng
 *    tức tỉ lệ láng giềng đã đặt sảnh s, láng giềng càng giống thì phiếu càng nặng. Trả lời
 *    câu "so sánh sự tương đồng của hai hồ sơ".
 *
 *    Độ tương đồng giữa hai khách tính trên vector hồ sơ (cosine hoặc Pearson), không tính
 *    trên bảng ai đặt sảnh nào như lọc cộng tác cổ điển. Lý do: Vườn Sen chỉ có sáu sảnh,
 *    bảng khách × sảnh quá thưa, hai khách mỗi người đặt một lần thì độ giống chỉ ra 0 hoặc 1.
 *    So trên hồ sơ thì hai khách cùng hay tổ chức tiệc cưới khoảng 250 khách vẫn được coi là
 *    giống nhau dù chưa đặt trùng sảnh nào. Cách làm này có tên "collaboration via content"
 *    (Pazzani, 1999). Độ đo Jaccard trên tập sảnh đã đặt vẫn giữ lại để so sánh trong phần
 *    đánh giá.
 *
 * 3. Kết hợp: diem = α · diemNoiDung + (1 − α) · diemCongTac
 *    α đọc từ application.yml. Không tìm được láng giềng nào thì dùng riêng diemNoiDung.
 *
 * Lớp này không đụng tới cơ sở dữ liệu: nhận vector vào, trả thứ hạng ra. Nhờ vậy dùng được
 * nguyên lớp này cho cả website lẫn bài đánh giá ngoại tuyến DanhGiaGoiYKhongGianTest.
 */
public final class BoGoiYKhongGian {

    public enum DoDo { COSINE, PEARSON, JACCARD }

    public record CauHinh(double alpha, int soLangGieng, DoDo doDo) {
    }

    /* Một khách khác trong hệ thống: vector hồ sơ và tập sảnh đã đặt. */
    public record NguoiKhac(Long userId, double[] vector, Set<Long> daDat) {
    }

    public record LangGieng(Long userId, double doTuongDong, Set<Long> daDat) {
    }

    public record KetQua(Long sanhId, double diem, double diemNoiDung, double diemCongTac,
                         int soLangGiengDaDat) {
    }

    private BoGoiYKhongGian() {
    }

    /*
     * Xếp hạng toàn bộ sảnh cho một khách.
     *
     * phoBien chỉ dùng để phân thứ hạng khi hai sảnh bằng điểm, ví dụ khách chỉ khai mỗi tầm
     * giá thì hai sảnh cùng bậc giá sẽ bằng điểm nhau; khi đó sảnh được đặt nhiều hơn đứng trước.
     */
    public static List<KetQua> xepHang(double[] hoSo, Set<Long> daDatCuaKhach,
                                       Map<Long, double[]> vectorSanh,
                                       List<NguoiKhac> nguoiKhac,
                                       CauHinh cauHinh,
                                       Map<Long, Long> phoBien) {

        List<LangGieng> langGieng = timLangGieng(hoSo, daDatCuaKhach, nguoiKhac, cauHinh);
        double tongDoTuongDong = langGieng.stream().mapToDouble(LangGieng::doTuongDong).sum();

        List<KetQua> kq = new ArrayList<>();
        for (Map.Entry<Long, double[]> e : vectorSanh.entrySet()) {
            Long sanhId = e.getKey();

            // 1. Lọc theo nội dung: so hồ sơ với sảnh
            double noiDung = DoTuongDong.cosine(hoSo, e.getValue());

            // 2. Lọc cộng tác: láng giềng đã đặt sảnh này nhiều tới đâu
            double congTac = 0;
            int soDaDat = 0;
            for (LangGieng v : langGieng) {
                if (v.daDat().contains(sanhId)) {
                    congTac += v.doTuongDong();
                    soDaDat++;
                }
            }
            if (tongDoTuongDong > 0) {
                congTac /= tongDoTuongDong;
            }

            // 3. Kết hợp
            double diem = langGieng.isEmpty()
                    ? noiDung
                    : cauHinh.alpha() * noiDung + (1 - cauHinh.alpha()) * congTac;
            kq.add(new KetQua(sanhId, diem, noiDung, congTac, soDaDat));
        }

        kq.sort(Comparator.comparingDouble(KetQua::diem).reversed()
                .thenComparing(k -> -phoBien.getOrDefault(k.sanhId(), 0L))
                .thenComparing(KetQua::sanhId));
        return kq;
    }

    /*
     * k khách giống nhất, chỉ lấy người có độ tương đồng dương và đã từng đặt ít nhất một sảnh.
     * Người chưa đặt gì không có phiếu nào để bầu, đưa vào chỉ làm loãng mẫu số.
     */
    public static List<LangGieng> timLangGieng(double[] hoSo, Set<Long> daDatCuaKhach,
                                               List<NguoiKhac> nguoiKhac, CauHinh cauHinh) {
        List<LangGieng> ds = new ArrayList<>();
        for (NguoiKhac v : nguoiKhac) {
            if (v.daDat().isEmpty()) {
                continue;
            }
            double sim = switch (cauHinh.doDo()) {
                case COSINE -> DoTuongDong.cosine(hoSo, v.vector());
                case PEARSON -> DoTuongDong.pearson(hoSo, v.vector());
                case JACCARD -> DoTuongDong.jaccard(daDatCuaKhach, v.daDat());
            };
            if (sim > 0) {
                ds.add(new LangGieng(v.userId(), sim, v.daDat()));
            }
        }
        ds.sort(Comparator.comparingDouble(LangGieng::doTuongDong).reversed()
                .thenComparing(LangGieng::userId));
        return ds.size() > cauHinh.soLangGieng() ? ds.subList(0, cauHinh.soLangGieng()) : ds;
    }
}
