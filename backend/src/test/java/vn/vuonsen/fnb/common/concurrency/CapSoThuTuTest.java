package vn.vuonsen.fnb.common.concurrency;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Ba tính chất của bộ cấp số thứ tự, kiểm riêng không cần database.
 *
 * Nguồn "số đã lưu" ở đây cố định là 0, đúng tình huống gây lỗi trước đây: nhiều người cùng
 * hỏi database thì cùng nhận một con số vì chưa ai kịp lưu. Bộ cấp số phải tự phân biệt được
 * họ bằng số đã cấp trong bộ nhớ.
 */
class CapSoThuTuTest {

    private static final LocalDate HOM_NAY = LocalDate.of(2026, 11, 2);

    @Test
    @DisplayName("Năm mươi luồng cùng xin số một lúc: mỗi luồng một số, liền nhau từ 1 tới 50")
    void nhieuLuongKhongTrungSo() throws Exception {
        CapSoThuTu capSo = new CapSoThuTu();
        int soLuong = 50;
        ExecutorService pool = Executors.newFixedThreadPool(soLuong);
        CountDownLatch vachXuatPhat = new CountDownLatch(1);
        Set<Long> daNhan = ConcurrentHashMap.newKeySet();
        try {
            List<Future<?>> dangChay = new ArrayList<>();
            for (int i = 0; i < soLuong; i++) {
                dangChay.add(pool.submit(() -> {
                    vachXuatPhat.await();
                    daNhan.add(capSo.cap("VS", HOM_NAY, () -> 0, so -> false));
                    return null;
                }));
            }
            vachXuatPhat.countDown();
            for (Future<?> f : dangChay) {
                f.get(10, TimeUnit.SECONDS);
            }
        } finally {
            pool.shutdownNow();
        }

        assertThat(daNhan).containsExactlyInAnyOrderElementsOf(LongStream.rangeClosed(1, soLuong).boxed().toList());
    }

    @Test
    @DisplayName("Sang ngày mới thì đếm lại từ số đã lưu của ngày đó, mỗi tiền tố một dãy riêng")
    void sangNgayMoiDemLai() {
        CapSoThuTu capSo = new CapSoThuTu();

        assertThat(capSo.cap("VS", HOM_NAY, () -> 0, so -> false)).isEqualTo(1);
        assertThat(capSo.cap("VS", HOM_NAY, () -> 0, so -> false)).isEqualTo(2);
        assertThat(capSo.cap("DM", HOM_NAY, () -> 0, so -> false))
                .as("tiền tố khác có dãy số riêng").isEqualTo(1);
        assertThat(capSo.cap("VS", HOM_NAY.plusDays(1), () -> 0, so -> false))
                .as("sang ngày mới thì số đã cấp hôm trước không còn tính").isEqualTo(1);
    }

    @Test
    @DisplayName("Số đã có bản ghi mang mã đó thì bỏ qua, cấp số trống kế tiếp")
    void boQuaSoDaDung() {
        CapSoThuTu capSo = new CapSoThuTu();

        assertThat(capSo.cap("TT-B", HOM_NAY, () -> 2, so -> so <= 5)).isEqualTo(6);
    }
}
