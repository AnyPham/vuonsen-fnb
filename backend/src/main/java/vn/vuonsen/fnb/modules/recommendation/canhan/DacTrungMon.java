package vn.vuonsen.fnb.modules.recommendation.canhan;

import vn.vuonsen.fnb.modules.recommendation.canhan.SoThichGoiY.TamGia;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Vector đặc trưng của món và hồ sơ khẩu vị của khách, 10 chiều:
 *
 *   Nhóm        Chiều  Vector của món           Vector hồ sơ khách
 *   Danh mục      5    danh mục của món          tỉ lệ các danh mục khách đã gọi
 *   Chay, mặn     2    món chay hay món mặn       tỉ lệ món chay trong các món đã gọi
 *   Tầm giá       3    bậc giá của món            bậc giá các món khách đã gọi
 *
 * Món tính giá theo cân không có giá cố định nên nhóm tầm giá để trống, cosine tự bỏ qua nhóm
 * đó thay vì đoán một bậc giá.
 */
public final class DacTrungMon {

    public static final List<String> CAC_DANH_MUC = List.of("khaivi", "chinh", "lau", "trangmieng", "douong");

    public static final KhungVector KHUNG = new KhungVector(List.of(
            new KhungVector.Nhom("DANH_MUC", 0, 5),
            new KhungVector.Nhom("CHAY", 5, 2),
            new KhungVector.Nhom("TAM_GIA", 7, 3)));

    public static final KhungVector.Nhom DANH_MUC = KHUNG.cacNhom().get(0);
    public static final KhungVector.Nhom CHAY = KHUNG.cacNhom().get(1);
    public static final KhungVector.Nhom TAM_GIA = KHUNG.cacNhom().get(2);

    private DacTrungMon() {
    }

    public record Mon(
            Long id,
            String slug,
            String ten,
            String tenEn,
            String anh,
            BigDecimal gia,
            String ghiChuGia,
            String ghiChuGiaEn,
            boolean chay,
            boolean banChay,
            int thuTu,
            String maDanhMuc,
            String tenDanhMuc,
            String tenDanhMucEn,
            TamGia tamGia
    ) {
    }

    /* Chia món có giá thành ba bậc theo thứ hạng giá, như cách chia bậc giá sảnh. */
    public static Map<Long, TamGia> chiaBacGia(Map<Long, BigDecimal> giaTheoMon) {
        List<Map.Entry<Long, BigDecimal>> coGia = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> e : giaTheoMon.entrySet()) {
            if (e.getValue() != null) {
                coGia.add(e);
            }
        }
        coGia.sort(Map.Entry.comparingByValue(Comparator.naturalOrder()));
        Map<Long, TamGia> kq = new HashMap<>();
        int n = coGia.size();
        for (int i = 0; i < n; i++) {
            kq.put(coGia.get(i).getKey(), TamGia.values()[Math.min(2, i * 3 / n)]);
        }
        return kq;
    }

    public static double[] vectorTho(Mon m) {
        double[] v = new double[KHUNG.soChieu()];
        int dm = CAC_DANH_MUC.indexOf(m.maDanhMuc());
        if (dm >= 0) {
            v[DANH_MUC.batDau() + dm] = 1;
        }
        v[CHAY.batDau() + (m.chay() ? 0 : 1)] = 1;
        if (m.tamGia() != null) {
            v[TAM_GIA.batDau() + m.tamGia().ordinal()] = 1;
        }
        return v;
    }

    public static double[] vector(Mon m, double[] trongSoNhom) {
        return KHUNG.chuanHoa(vectorTho(m), trongSoNhom);
    }

    /* Hồ sơ khẩu vị, cộng dồn từ các món đã gọi và sở thích tự khai. */
    public static final class HoSo {

        private final double[] tho = new double[KHUNG.soChieu()];
        private double tongTrongSo;

        /* Một món đã gọi, trọng số là số đơn có món đó: gọi lại nhiều lần thì nặng hơn. */
        public void themMon(Mon m, double trongSo) {
            if (m == null || trongSo <= 0) {
                return;
            }
            double[] v = vectorTho(m);
            for (int i = 0; i < v.length; i++) {
                tho[i] += v[i] * trongSo;
            }
            tongTrongSo += trongSo;
        }

        /* Khai ăn chay thì cộng vào chiều chay; khai tầm giá thì dùng chung cho cả món. */
        public void themSoThich(SoThichGoiY st, double trongSo) {
            if (st == null || trongSo <= 0) {
                return;
            }
            boolean coThem = false;
            if (st.anChay()) {
                tho[CHAY.batDau()] += trongSo;
                coThem = true;
            }
            if (st.tamGia() != null) {
                tho[TAM_GIA.batDau() + st.tamGia().ordinal()] += trongSo;
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

        public String danhMucChinh() {
            int i = KHUNG.viTriLonNhat(tho, DANH_MUC);
            return i < 0 ? null : CAC_DANH_MUC.get(i);
        }

        /* Khách thiên về món chay khi phần chay lớn hơn phần mặn. */
        public boolean thichChay() {
            return tho[CHAY.batDau()] > tho[CHAY.batDau() + 1];
        }

        public TamGia tamGiaChinh() {
            int i = KHUNG.viTriLonNhat(tho, TAM_GIA);
            return i < 0 ? null : TamGia.values()[i];
        }
    }
}
