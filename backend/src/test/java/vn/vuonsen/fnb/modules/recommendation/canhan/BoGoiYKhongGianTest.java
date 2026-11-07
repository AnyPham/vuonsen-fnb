package vn.vuonsen.fnb.modules.recommendation.canhan;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.vuonsen.fnb.modules.recommendation.canhan.BoGoiYKhongGian.CauHinh;
import vn.vuonsen.fnb.modules.recommendation.canhan.BoGoiYKhongGian.DoDo;
import vn.vuonsen.fnb.modules.recommendation.canhan.BoGoiYKhongGian.KetQua;
import vn.vuonsen.fnb.modules.recommendation.canhan.BoGoiYKhongGian.NguoiKhac;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/*
 * Ví dụ tính tay ba sảnh, ba khách khác, in lại trong tiểu luận mục 3.2.5.1.
 *
 *   Sảnh:   S1 = (1, 0)   S2 = (0, 1)   S3 = (1, 1)
 *   Khách đang xem: hồ sơ u = (1, 0)
 *   Khách khác:  U1 = (1, 0) đã đặt {S2}
 *                U2 = (0, 1) đã đặt {S1}
 *                U3 = (1, 1) đã đặt {S2, S3}
 *
 *   Lọc nội dung:  cos(u, S1) = 1   cos(u, S2) = 0   cos(u, S3) = 1/√2 ≈ 0,7071
 *   Láng giềng:    sim(u, U1) = 1   sim(u, U2) = 0 (loại)   sim(u, U3) ≈ 0,7071
 *   Lọc cộng tác:  S1 = 0 ;  S2 = (1 + 0,7071) / 1,7071 = 1 ;  S3 = 0,7071 / 1,7071 ≈ 0,4142
 *   α = 0,6:       S1 = 0,6 ;  S2 = 0,4 ;  S3 ≈ 0,6 · 0,7071 + 0,4 · 0,4142 ≈ 0,5900
 */
class BoGoiYKhongGianTest {

    private static final long S1 = 1L;
    private static final long S2 = 2L;
    private static final long S3 = 3L;

    private static final Map<Long, double[]> SANH = new LinkedHashMap<>();

    static {
        SANH.put(S1, new double[]{1, 0});
        SANH.put(S2, new double[]{0, 1});
        SANH.put(S3, new double[]{1, 1});
    }

    private static final double[] HO_SO = {1, 0};

    private static final List<NguoiKhac> NGUOI_KHAC = List.of(
            new NguoiKhac(11L, new double[]{1, 0}, Set.of(S2)),
            new NguoiKhac(12L, new double[]{0, 1}, Set.of(S1)),
            new NguoiKhac(13L, new double[]{1, 1}, Set.of(S2, S3)));

    private static List<KetQua> xepHang(double alpha, int k, DoDo doDo, Set<Long> daDat, List<NguoiKhac> khac) {
        return BoGoiYKhongGian.xepHang(HO_SO, daDat, SANH, khac, new CauHinh(alpha, k, doDo), Map.of());
    }

    private static KetQua cua(List<KetQua> kq, long sanh) {
        return kq.stream().filter(k -> k.sanhId() == sanh).findFirst().orElseThrow();
    }

    @Test
    @DisplayName("Ví dụ tính tay: điểm nội dung, điểm cộng tác và điểm kết hợp khớp từng con số")
    void viDuTinhTay() {
        List<KetQua> kq = xepHang(0.6, 30, DoDo.COSINE, Set.of(), NGUOI_KHAC);

        assertThat(cua(kq, S1).diemNoiDung()).isCloseTo(1.0, within(1e-9));
        assertThat(cua(kq, S2).diemNoiDung()).isCloseTo(0.0, within(1e-9));
        assertThat(cua(kq, S3).diemNoiDung()).isCloseTo(1 / Math.sqrt(2), within(1e-9));

        double tong = 1 + 1 / Math.sqrt(2);
        assertThat(cua(kq, S1).diemCongTac()).isCloseTo(0.0, within(1e-9));
        assertThat(cua(kq, S2).diemCongTac()).isCloseTo(1.0, within(1e-9));
        assertThat(cua(kq, S3).diemCongTac()).isCloseTo((1 / Math.sqrt(2)) / tong, within(1e-9));

        assertThat(cua(kq, S1).diem()).isCloseTo(0.6, within(1e-9));
        assertThat(cua(kq, S2).diem()).isCloseTo(0.4, within(1e-9));
        assertThat(cua(kq, S3).diem()).isCloseTo(0.5900, within(1e-4));

        assertThat(kq).extracting(KetQua::sanhId).containsExactly(S1, S3, S2);
        assertThat(cua(kq, S2).soLangGiengDaDat()).isEqualTo(2);
    }

    @Test
    @DisplayName("Khách có độ tương đồng 0 không được tính là láng giềng")
    void boQuaKhachKhongGiong() {
        List<BoGoiYKhongGian.LangGieng> lg = BoGoiYKhongGian.timLangGieng(
                HO_SO, Set.of(), NGUOI_KHAC, new CauHinh(0.6, 30, DoDo.COSINE));
        assertThat(lg).extracting(BoGoiYKhongGian.LangGieng::userId).containsExactly(11L, 13L);
    }

    @Test
    @DisplayName("Giảm α thì lọc cộng tác nặng hơn: sảnh láng giềng hay đặt vươn lên đầu")
    void alphaNhoThiCongTacLanAt() {
        List<KetQua> kq = xepHang(0.2, 30, DoDo.COSINE, Set.of(), NGUOI_KHAC);
        assertThat(kq.get(0).sanhId()).isEqualTo(S2);
    }

    @Test
    @DisplayName("α = 1 là lọc theo nội dung thuần, láng giềng không ảnh hưởng")
    void alphaMotLaNoiDungThuan() {
        List<KetQua> kq = xepHang(1.0, 30, DoDo.COSINE, Set.of(), NGUOI_KHAC);
        kq.forEach(k -> assertThat(k.diem()).isCloseTo(k.diemNoiDung(), within(1e-12)));
    }

    @Test
    @DisplayName("Không có láng giềng nào thì dùng riêng điểm nội dung")
    void khongLangGiengThiDungNoiDung() {
        List<KetQua> kq = xepHang(0.6, 30, DoDo.COSINE, Set.of(), List.of());
        kq.forEach(k -> assertThat(k.diem()).isCloseTo(k.diemNoiDung(), within(1e-12)));
    }

    @Test
    @DisplayName("Chỉ lấy k láng giềng giống nhất")
    void gioiHanSoLangGieng() {
        List<KetQua> kq = xepHang(0.6, 1, DoDo.COSINE, Set.of(), NGUOI_KHAC);
        // Còn mỗi U1 (đã đặt S2), nên S3 không còn phiếu cộng tác nào
        assertThat(cua(kq, S3).diemCongTac()).isZero();
        assertThat(cua(kq, S2).diemCongTac()).isCloseTo(1.0, within(1e-9));
    }

    @Test
    @DisplayName("Jaccard so tập sảnh đã đặt: chưa đặt trùng sảnh nào thì không có láng giềng")
    void jaccardTrenTapDaDat() {
        assertThat(BoGoiYKhongGian.timLangGieng(HO_SO, Set.of(S3), NGUOI_KHAC,
                new CauHinh(0.6, 30, DoDo.JACCARD)))
                .extracting(BoGoiYKhongGian.LangGieng::userId).containsExactly(13L);
        assertThat(BoGoiYKhongGian.timLangGieng(HO_SO, Set.of(), NGUOI_KHAC,
                new CauHinh(0.6, 30, DoDo.JACCARD))).isEmpty();
    }

    @Test
    @DisplayName("Bằng điểm thì sảnh được đặt nhiều hơn đứng trước")
    void bangDiemThiTheoPhoBien() {
        Map<Long, double[]> haiSanhGiongNhau = new LinkedHashMap<>();
        haiSanhGiongNhau.put(S1, new double[]{1, 0});
        haiSanhGiongNhau.put(S2, new double[]{1, 0});
        List<KetQua> kq = BoGoiYKhongGian.xepHang(HO_SO, Set.of(), haiSanhGiongNhau, List.of(),
                new CauHinh(0.6, 30, DoDo.COSINE), Map.of(S1, 3L, S2, 9L));
        assertThat(kq).extracting(KetQua::sanhId).containsExactly(S2, S1);
    }
}
