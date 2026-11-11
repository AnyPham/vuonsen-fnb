package vn.vuonsen.fnb.modules.recommendation.canhan;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/*
 * Kiểm các độ đo tương đồng bằng ví dụ tính tay. Các con số ở đây là đúng ví dụ in trong tiểu
 * luận, mục 2.2.1, nên ví dụ trong tiểu luận luôn khớp với mã nguồn.
 */
class DoTuongDongTest {

    private static final double SAI_SO = 1e-9;

    @Test
    @DisplayName("Cosine: a = (2, 0, 1), b = (1, 0, 1) cho 3 / √10 ≈ 0,9487")
    void cosineTinhTay() {
        // a · b = 2·1 + 0·0 + 1·1 = 3 ; ‖a‖ = √5 ; ‖b‖ = √2
        double kq = DoTuongDong.cosine(new double[]{2, 0, 1}, new double[]{1, 0, 1});
        assertThat(kq).isCloseTo(3 / Math.sqrt(10), within(SAI_SO));
        assertThat(kq).isCloseTo(0.9487, within(1e-4));
    }

    @Test
    @DisplayName("Cosine chỉ đo hướng: nhân một vector với 10 thì độ tương đồng không đổi")
    void cosineKhongPhuThuocDoDai() {
        double[] a = {2, 0, 1};
        double[] b = {1, 0, 1};
        double[] aGapMuoi = {20, 0, 10};
        assertThat(DoTuongDong.cosine(aGapMuoi, b)).isCloseTo(DoTuongDong.cosine(a, b), within(SAI_SO));
    }

    @Test
    @DisplayName("Vector bằng không là chưa có thông tin: trả 0 thay vì để thư viện ném lỗi")
    void vectorKhongTra0() {
        assertThat(DoTuongDong.cosine(new double[]{0, 0, 0}, new double[]{1, 2, 3})).isZero();
        assertThat(DoTuongDong.pearson(new double[]{0, 0, 0}, new double[]{1, 2, 3})).isZero();
    }

    @Test
    @DisplayName("Pearson: trừ trung bình rồi mới tính cosine, ví dụ trên ra √3 / 2 ≈ 0,866")
    void pearsonTinhTay() {
        // a − trung bình = (1, −1, 0) ; b − trung bình = (1/3, −2/3, 1/3)
        // tích = 1 ; độ dài √2 và √(2/3) ; 1 / √(4/3) = √3 / 2
        double kq = DoTuongDong.pearson(new double[]{2, 0, 1}, new double[]{1, 0, 1});
        assertThat(kq).isCloseTo(Math.sqrt(3) / 2, within(SAI_SO));
    }

    @Test
    @DisplayName("Pearson của vector hằng không xác định, quy về 0")
    void pearsonVectorHang() {
        assertThat(DoTuongDong.pearson(new double[]{1, 1, 1}, new double[]{1, 2, 3})).isZero();
    }

    @Test
    @DisplayName("Jaccard: {1, 2, 3} và {2, 3, 4} chung 2 trên tổng 4 phần tử, ra 0,5")
    void jaccardTinhTay() {
        assertThat(DoTuongDong.jaccard(Set.of(1L, 2L, 3L), Set.of(2L, 3L, 4L))).isCloseTo(0.5, within(SAI_SO));
    }

    @Test
    @DisplayName("Jaccard của tập rỗng là 0: hai khách chưa đặt gì không có căn cứ để coi là giống nhau")
    void jaccardTapRong() {
        assertThat(DoTuongDong.jaccard(Set.of(), Set.of())).isZero();
        assertThat(DoTuongDong.jaccard(Set.of(1L), Set.of())).isZero();
    }

    @Test
    @DisplayName("Cosine trên tập bằng đúng cosine của Commons Math trên vector có hoặc không")
    void cosineTapHopKhopThuVien() {
        // Hai món, mỗi món là tập khách đã gọi. Dựng lại thành hai vector 0/1 dài bằng số khách
        Set<Long> monA = Set.of(1L, 2L, 3L, 5L);
        Set<Long> monB = Set.of(2L, 3L, 4L);
        double[] vA = new double[6];
        double[] vB = new double[6];
        monA.forEach(k -> vA[k.intValue()] = 1);
        monB.forEach(k -> vB[k.intValue()] = 1);

        double thuVien = new ArrayRealVector(vA).cosine(new ArrayRealVector(vB));
        assertThat(DoTuongDong.cosineTapHop(monA, monB)).isCloseTo(thuVien, within(SAI_SO));
        assertThat(thuVien).isCloseTo(2 / Math.sqrt(12), within(SAI_SO));
    }

    @Test
    @DisplayName("Chuẩn hóa theo nhóm: cosine tổng bằng trung bình có trọng số cosine từng nhóm")
    void cosineTongLaTrungBinhCoTrongSo() {
        KhungVector khung = DacTrungKhongGian.KHUNG;
        double[] trongSo = {0.4, 0.3, 0.1, 0.2};
        Random r = new Random(7);
        for (int lan = 0; lan < 50; lan++) {
            double[] a = new double[khung.soChieu()];
            double[] b = new double[khung.soChieu()];
            for (int i = 0; i < a.length; i++) {
                a[i] = r.nextDouble();
                b[i] = r.nextDouble();
            }
            double[] na = khung.chuanHoa(a, trongSo);
            double[] nb = khung.chuanHoa(b, trongSo);

            double tongTrongSo = 0;
            List<KhungVector.Nhom> cacNhom = khung.cacNhom();
            for (int i = 0; i < cacNhom.size(); i++) {
                tongTrongSo += trongSo[i] * khung.cosineNhom(a, b, cacNhom.get(i));
            }
            assertThat(DoTuongDong.cosine(na, nb)).isCloseTo(tongTrongSo, within(1e-9));
        }
    }
}
