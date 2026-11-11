package vn.vuonsen.fnb.modules.recommendation.canhan;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.vuonsen.fnb.modules.recommendation.canhan.BoGoiYMon.KetQua;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/*
 * Ví dụ tính tay cho lọc cộng tác theo món.
 *
 *   Khách đã gọi món M1. Tập khách đã gọi từng món:
 *     M1 = {A, B, C}   M2 = {A, B}   M3 = {C}   M4 = {}
 *   sim(M2, M1) = 2 / √(2·3) ≈ 0,8165   sim(M3, M1) = 1 / √3 ≈ 0,5774   sim(M4, M1) = 0
 *   Hồ sơ khẩu vị (1, 0); vector món M2 = (0, 1), M3 = (1, 0), M4 = (1, 1)
 *   α = 0,5:  M2 ≈ 0,4082   M3 ≈ 0,7887   M4 ≈ 0,3536
 */
class BoGoiYMonTest {

    private static final Map<Long, double[]> MON = new LinkedHashMap<>();

    static {
        MON.put(1L, new double[]{1, 0});
        MON.put(2L, new double[]{0, 1});
        MON.put(3L, new double[]{1, 0});
        MON.put(4L, new double[]{1, 1});
    }

    private static final Map<Long, Set<Long>> KHACH_THEO_MON = Map.of(
            1L, Set.of(101L, 102L, 103L),
            2L, Set.of(101L, 102L),
            3L, Set.of(103L));

    private static KetQua cua(List<KetQua> kq, long mon) {
        return kq.stream().filter(k -> k.monId() == mon).findFirst().orElseThrow();
    }

    @Test
    @DisplayName("Ví dụ tính tay: khách gọi M1 thì M3 đứng đầu, M2 nhờ hay gọi cùng M1 xếp thứ hai")
    void viDuTinhTay() {
        List<KetQua> kq = BoGoiYMon.xepHang(new double[]{1, 0}, Set.of(1L), MON, KHACH_THEO_MON, 0.5, Map.of());

        assertThat(kq).extracting(KetQua::monId).containsExactly(3L, 2L, 4L);
        assertThat(cua(kq, 2L).diemCongTac()).isCloseTo(2 / Math.sqrt(6), within(1e-9));
        assertThat(cua(kq, 3L).diemCongTac()).isCloseTo(1 / Math.sqrt(3), within(1e-9));
        assertThat(cua(kq, 4L).diemCongTac()).isZero();
        assertThat(cua(kq, 3L).diem()).isCloseTo(0.5 + 0.5 / Math.sqrt(3), within(1e-9));
        assertThat(cua(kq, 2L).monGiongNhat()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Món khách đã gọi không được gợi ý lại")
    void boMonDaGoi() {
        List<KetQua> kq = BoGoiYMon.xepHang(new double[]{1, 0}, Set.of(1L), MON, KHACH_THEO_MON, 0.5, Map.of());
        assertThat(kq).extracting(KetQua::monId).doesNotContain(1L);
    }

    @Test
    @DisplayName("Chưa gọi món nào, chỉ có sở thích tự khai, thì xếp theo nội dung")
    void chuaGoiMonThiTheoNoiDung() {
        List<KetQua> kq = BoGoiYMon.xepHang(new double[]{1, 0}, Set.of(), MON, KHACH_THEO_MON, 0.5, Map.of());
        kq.forEach(k -> assertThat(k.diem()).isCloseTo(k.diemNoiDung(), within(1e-12)));
    }
}
