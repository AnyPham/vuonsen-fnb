package vn.vuonsen.fnb.common.concurrency;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/*
 * Kiểm thử cơ chế thử lại khi đụng độ, không cần database.
 *
 * Lỗi database được giả lập bằng tay nên kết quả luôn lặp lại được, khác với kiểm thử
 * đồng thời thật vốn phụ thuộc vào thời điểm các luồng chạy.
 */
class ThuLaiKhiTrungMaTest {

    private final ThuLaiKhiTrungMa thuLai = new ThuLaiKhiTrungMa();

    private static DataIntegrityViolationException trung(String thongBaoDb) {
        return new DataIntegrityViolationException("vi phạm", new RuntimeException(thongBaoDb));
    }

    @Test
    @DisplayName("Trùng mã đơn thì thử lại, lần sau thành công")
    void trungMaThiThuLai() {
        AtomicInteger soLan = new AtomicInteger();

        String ketQua = thuLai.chay("uk_booking_code", () -> {
            if (soLan.incrementAndGet() < 3) {
                throw trung("Duplicate entry 'VS-20260913-0001' for key 'uk_booking_code'");
            }
            return "VS-20260913-0003";
        });

        assertThat(ketQua).isEqualTo("VS-20260913-0003");
        assertThat(soLan.get()).isEqualTo(3);
    }

    @Test
    @DisplayName("Nhận ra tên ràng buộc viết hoa kiểu H2")
    void nhanRaTenRangBuocVietHoa() {
        var loi = trung("Unique index or primary key violation: \"PUBLIC.UK_BOOKING_CODE_INDEX_2\"");

        assertThat(ThuLaiKhiTrungMa.viPham(loi, "uk_booking_code")).isTrue();
    }

    @Test
    @DisplayName("Chờ khóa quá lâu cũng là xung đột tạm thời, thử lại")
    void loiKhoaTamThoiCungThuLai() {
        AtomicInteger soLan = new AtomicInteger();

        String ketQua = thuLai.chay("uk_booking_code", () -> {
            if (soLan.incrementAndGet() == 1) {
                throw new CannotAcquireLockException("Lock wait timeout exceeded");
            }
            return "ok";
        });

        assertThat(ketQua).isEqualTo("ok");
        assertThat(soLan.get()).isEqualTo(2);
    }

    @Test
    @DisplayName("Vi phạm ràng buộc khác thì không thử lại, báo lỗi ngay")
    void rangBuocKhacKhongThuLai() {
        AtomicInteger soLan = new AtomicInteger();

        assertThatThrownBy(() -> thuLai.chay("uk_booking_code", () -> {
            soLan.incrementAndGet();
            throw trung("Duplicate entry 'a@b.vn' for key 'uk_users_email'");
        })).isInstanceOf(DataIntegrityViolationException.class);

        assertThat(soLan.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("Đụng mãi thì dừng sau số lần giới hạn, không lặp vô hạn")
    void dungSauSoLanGioiHan() {
        AtomicInteger soLan = new AtomicInteger();

        assertThatThrownBy(() -> thuLai.chay("uk_booking_code", () -> {
            soLan.incrementAndGet();
            throw trung("for key 'uk_booking_code'");
        })).isInstanceOf(DataIntegrityViolationException.class);

        assertThat(soLan.get()).isEqualTo(ThuLaiKhiTrungMa.SO_LAN_THU);
    }
}
