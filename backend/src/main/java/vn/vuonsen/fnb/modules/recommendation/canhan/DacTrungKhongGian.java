package vn.vuonsen.fnb.modules.recommendation.canhan;

import vn.vuonsen.fnb.modules.booking.EventType;
import vn.vuonsen.fnb.modules.recommendation.canhan.SoThichGoiY.KieuKhongGian;
import vn.vuonsen.fnb.modules.recommendation.canhan.SoThichGoiY.TamGia;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Biểu diễn sảnh và hồ sơ khách thành vector cùng một không gian 14 chiều, để so được với
 * nhau bằng cosine.
 *
 *   Nhóm            Chiều  Vector của sảnh                    Vector hồ sơ khách
 *   Loại tiệc         5    mức phù hợp từng loại, quy về 0..1  tỉ lệ các loại tiệc khách đã tổ chức
 *   Dải số khách      4    các dải sảnh chứa được              dải số khách các tiệc khách đã đặt
 *   Kiểu không gian   2    trong nhà hay ngoài trời            kiểu của các sảnh khách đã chọn
 *   Tầm giá           3    bậc giá thuê của sảnh               bậc giá các sảnh khách đã chọn
 *
 * Nhóm 1 và 2 của hồ sơ lấy từ chính đơn đặt (khách tổ chức tiệc gì, mời bao nhiêu người), còn
 * nhóm 3 và 4 lấy từ sảnh khách đã chọn. Tức hồ sơ vừa mô tả nhu cầu vừa mô tả gu.
 *
 * Bốn dải số khách: 1–60, 61–150, 151–299, từ 300. Chia theo sức chứa thật của sáu sảnh để
 * mỗi dải phân biệt được vài sảnh: Nhà Rường Gỗ chỉ nằm ở dải đầu, Sảnh Ven Sông chỉ ở dải cuối.
 */
public final class DacTrungKhongGian {

    public static final KhungVector KHUNG = new KhungVector(List.of(
            new KhungVector.Nhom("LOAI_TIEC", 0, 5),
            new KhungVector.Nhom("SO_KHACH", 5, 4),
            new KhungVector.Nhom("KIEU_KHONG_GIAN", 9, 2),
            new KhungVector.Nhom("TAM_GIA", 11, 3)));

    public static final KhungVector.Nhom LOAI_TIEC = KHUNG.cacNhom().get(0);
    public static final KhungVector.Nhom SO_KHACH = KHUNG.cacNhom().get(1);
    public static final KhungVector.Nhom KIEU_KHONG_GIAN = KHUNG.cacNhom().get(2);
    public static final KhungVector.Nhom TAM_GIA = KHUNG.cacNhom().get(3);

    private static final EventType[] CAC_LOAI_TIEC = EventType.values();
    private static final int[] CAN_DUOI = {1, 61, 151, 300};
    private static final int[] CAN_TREN = {60, 150, 299, Integer.MAX_VALUE};
    public static final String[] NHAN_DAI_SO_KHACH = {"1-60", "61-150", "151-299", "300+"};

    private DacTrungKhongGian() {
    }

    /* Thông tin của một sảnh, đủ để dựng vector và trả về cho giao diện. */
    public record Sanh(
            Long id,
            String slug,
            String ten,
            String tenEn,
            String anh,
            int sucChuaMin,
            int sucChuaMax,
            BigDecimal giaThue,
            String donViGia,
            int thuTu,
            Map<EventType, Integer> mucPhuHop,
            KieuKhongGian kieu,
            TamGia tamGia
    ) {
        public int mucPhuHop(EventType loai) {
            return mucPhuHop.getOrDefault(loai, 3);
        }

        public boolean chuaDuocDai(int dai) {
            return sucChuaMin <= CAN_TREN[dai] && sucChuaMax >= CAN_DUOI[dai];
        }
    }

    /*
     * Chia sảnh thành ba bậc giá theo giá thuê, từ rẻ tới đắt, mỗi bậc khoảng một phần ba số
     * sảnh. Tính theo thứ hạng chứ không theo ngưỡng tiền cố định, để nhà hàng đổi giá thì bậc
     * tự đổi theo.
     */
    public static Map<Long, TamGia> chiaBacGia(Map<Long, BigDecimal> giaTheoSanh) {
        List<Map.Entry<Long, BigDecimal>> theoGia = new ArrayList<>(giaTheoSanh.entrySet());
        theoGia.sort(Map.Entry.comparingByValue(Comparator.nullsFirst(Comparator.naturalOrder())));
        Map<Long, TamGia> kq = new HashMap<>();
        int n = theoGia.size();
        for (int i = 0; i < n; i++) {
            kq.put(theoGia.get(i).getKey(), TamGia.values()[Math.min(2, i * 3 / n)]);
        }
        return kq;
    }

    public static int daiSoKhach(int soKhach) {
        for (int i = 0; i < CAN_DUOI.length; i++) {
            if (soKhach <= CAN_TREN[i]) {
                return i;
            }
        }
        return CAN_DUOI.length - 1;
    }

    /*
     * Vector thô của sảnh, chưa chuẩn hóa.
     *
     * Mức phù hợp chấm từ 1 đến 5 trong bảng space_event_types được quy về (mức − 1) / 4, để
     * sảnh chấm 1 điểm cho tiệc cưới có giá trị 0 ở chiều đó, tức "không hợp" thật sự. Để
     * nguyên 1..5 thì sảnh nào cũng có nền 0,2 ở mọi chiều và cosine giữa các sảnh bị kéo gần
     * nhau, khó phân biệt.
     */
    public static double[] vectorTho(Sanh s) {
        double[] v = new double[KHUNG.soChieu()];
        for (int i = 0; i < CAC_LOAI_TIEC.length; i++) {
            v[LOAI_TIEC.batDau() + i] = (s.mucPhuHop(CAC_LOAI_TIEC[i]) - 1) / 4.0;
        }
        for (int d = 0; d < CAN_DUOI.length; d++) {
            if (s.chuaDuocDai(d)) {
                v[SO_KHACH.batDau() + d] = 1;
            }
        }
        if (s.kieu() != null) {
            v[KIEU_KHONG_GIAN.batDau() + s.kieu().ordinal()] = 1;
        }
        if (s.tamGia() != null) {
            v[TAM_GIA.batDau() + s.tamGia().ordinal()] = 1;
        }
        return v;
    }

    public static double[] vector(Sanh s, double[] trongSoNhom) {
        return KHUNG.chuanHoa(vectorTho(s), trongSoNhom);
    }

    /*
     * Hồ sơ của một khách, cộng dồn từ ba nguồn: tiệc đã đặt, sảnh đã xem nhiều lần và sở
     * thích tự khai. Mỗi nguồn cộng vào với một trọng số, đọc từ application.yml.
     *
     * Đây là chỗ "so khớp với lịch sử người dùng": lịch sử đi vào hồ sơ ở đây, rồi hồ sơ được
     * so với từng sảnh trong BoGoiYKhongGian.
     */
    public static final class HoSo {

        private final double[] tho = new double[KHUNG.soChieu()];
        /*
         * Bản chỉ gồm đơn đã đặt và sở thích tự khai, không có lượt xem. Dùng để viết lý do:
         * khách mới chỉ xem Vườn Cau hai lần thì không được nói "hợp với tiệc sinh nhật bạn hay
         * tổ chức", vì khách chưa tổ chức gì cả.
         */
        private final double[] thoDaKhai = new double[KHUNG.soChieu()];
        private double tongTrongSo;

        private void cong(int viTri, double giaTri) {
            tho[viTri] += giaTri;
            thoDaKhai[viTri] += giaTri;
        }

        /* Một tiệc đã đặt: loại tiệc và số khách lấy từ đơn, kiểu và tầm giá lấy từ sảnh. */
        public void themTiec(EventType loai, Integer soKhach, Sanh sanh, double trongSo) {
            if (trongSo <= 0) {
                return;
            }
            if (loai != null) {
                cong(LOAI_TIEC.batDau() + loai.ordinal(), trongSo);
            }
            if (soKhach != null && soKhach > 0) {
                cong(SO_KHACH.batDau() + daiSoKhach(soKhach), trongSo);
            }
            if (sanh != null && sanh.kieu() != null) {
                cong(KIEU_KHONG_GIAN.batDau() + sanh.kieu().ordinal(), trongSo);
            }
            if (sanh != null && sanh.tamGia() != null) {
                cong(TAM_GIA.batDau() + sanh.tamGia().ordinal(), trongSo);
            }
            tongTrongSo += trongSo;
        }

        /*
         * Một sảnh khách xem đi xem lại: cộng chính vector của sảnh đó với trọng số nhỏ.
         * Xem chỉ nói khách quan tâm, chưa nói khách sẽ tổ chức tiệc gì, nên nhẹ hơn một đơn.
         */
        public void themLuotXem(Sanh sanh, double trongSo) {
            if (sanh == null || trongSo <= 0) {
                return;
            }
            double[] v = vectorTho(sanh);
            for (int i = 0; i < v.length; i++) {
                tho[i] += v[i] * trongSo;
            }
            tongTrongSo += trongSo;
        }

        /* Sở thích tự khai. Chọn nhiều loại tiệc thì chia đều trọng số cho các loại đã chọn. */
        public void themSoThich(SoThichGoiY st, double trongSo) {
            if (st == null || st.trong() || trongSo <= 0) {
                return;
            }
            boolean coThem = false;
            if (st.loaiTiec() != null && !st.loaiTiec().isEmpty()) {
                for (EventType e : st.loaiTiec()) {
                    cong(LOAI_TIEC.batDau() + e.ordinal(), trongSo / st.loaiTiec().size());
                }
                coThem = true;
            }
            if (st.soKhach() != null && st.soKhach() > 0) {
                cong(SO_KHACH.batDau() + daiSoKhach(st.soKhach()), trongSo);
                coThem = true;
            }
            if (st.kieuKhongGian() != null) {
                cong(KIEU_KHONG_GIAN.batDau() + st.kieuKhongGian().ordinal(), trongSo);
                coThem = true;
            }
            if (st.tamGia() != null) {
                cong(TAM_GIA.batDau() + st.tamGia().ordinal(), trongSo);
                coThem = true;
            }
            if (coThem) {
                tongTrongSo += trongSo;
            }
        }

        public boolean rong() {
            return tongTrongSo == 0;
        }

        public double[] vector(double[] trongSoNhom) {
            return KHUNG.chuanHoa(tho, trongSoNhom);
        }

        public double[] tho() {
            return tho.clone();
        }

        /* Loại tiệc khách tổ chức nhiều nhất, để viết lý do; null nếu chưa biết. */
        public EventType loaiTiecChinh() {
            int i = KHUNG.viTriLonNhat(thoDaKhai, LOAI_TIEC);
            return i < 0 ? null : CAC_LOAI_TIEC[i];
        }

        public int daiSoKhachChinh() {
            return KHUNG.viTriLonNhat(thoDaKhai, SO_KHACH);
        }

        public KieuKhongGian kieuChinh() {
            int i = KHUNG.viTriLonNhat(thoDaKhai, KIEU_KHONG_GIAN);
            return i < 0 ? null : KieuKhongGian.values()[i];
        }

        public TamGia tamGiaChinh() {
            int i = KHUNG.viTriLonNhat(thoDaKhai, TAM_GIA);
            return i < 0 ? null : TamGia.values()[i];
        }
    }
}
