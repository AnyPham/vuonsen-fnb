package vn.vuonsen.fnb.modules.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Kiểm thử chuỗi mã VietQR.
 *
 * Chuỗi này khách quét bằng ứng dụng ngân hàng thật, sai một ký tự là ứng dụng báo mã hỏng
 * hoặc tệ hơn là ra sai số tiền. Không thể thử bằng cách quét tay mỗi lần sửa mã nguồn, nên
 * phải kiểm từng phần: cấu trúc khối, ô kiểm tra CRC, và cách xử lý nội dung có dấu.
 */
class VietQrBuilderTest {

    private static final String BIN_MB = "970422";
    private static final String SO_TAI_KHOAN = "0000000000";

    @Test
    @DisplayName("Chuỗi mở đầu bằng khối phiên bản và khối dùng một lần")
    void moDauDungChuan() {
        String qr = VietQrBuilder.dungChuoi(BIN_MB, SO_TAI_KHOAN, new BigDecimal("500000"), "VS-1");

        // 00 độ dài 02 giá trị 01, rồi 01 độ dài 02 giá trị 12
        assertThat(qr).startsWith("000201" + "010212");
    }

    @Test
    @DisplayName("Khối thụ hưởng chứa mã Napas, mã ngân hàng và số tài khoản")
    void khoiThuHuongDayDu() {
        String qr = VietQrBuilder.dungChuoi(BIN_MB, "113366668888", new BigDecimal("1000"), "VS-1");

        assertThat(qr).contains("A000000727");
        assertThat(qr).contains("0006" + BIN_MB);
        assertThat(qr).contains("0112" + "113366668888");
        assertThat(qr).contains("QRIBFTTA");
    }

    @Test
    @DisplayName("Số tiền và đơn vị tiền tệ ghi đúng khối 54 và 53")
    void soTienVaDonViTien() {
        String qr = VietQrBuilder.dungChuoi(BIN_MB, SO_TAI_KHOAN, new BigDecimal("32756400"), "VS-1");

        assertThat(qr).contains("5303704");          // 53, dài 03, giá trị 704 là VND
        assertThat(qr).contains("5408" + "32756400"); // 54, dài 08
        assertThat(qr).contains("5802VN");
    }

    @Test
    @DisplayName("Số tiền lẻ được làm tròn về đồng vì mã QR không nhận số thập phân")
    void lamTronVeDong() {
        String qr = VietQrBuilder.dungChuoi(BIN_MB, SO_TAI_KHOAN, new BigDecimal("1000.6"), "VS-1");

        assertThat(qr).contains("5404" + "1001");
        assertThat(qr).doesNotContain(".");
    }

    @Test
    @DisplayName("Nội dung chuyển khoản bỏ dấu và viết hoa")
    void noiDungBoDau() {
        String qr = VietQrBuilder.dungChuoi(BIN_MB, SO_TAI_KHOAN, new BigDecimal("1000"),
                "Đặt tiệc cưới VS-20260927-0001");

        assertThat(qr).contains("DAT TIEC CUOI VS 20260927 0001");
        assertThat(qr).doesNotContain("Đặt");
    }

    @Test
    @DisplayName("Ô kiểm tra CRC nằm cuối chuỗi, đủ bốn chữ số hex in hoa")
    void crcCuoiChuoi() {
        String qr = VietQrBuilder.dungChuoi(BIN_MB, SO_TAI_KHOAN, new BigDecimal("500000"), "VS-1");

        assertThat(qr).matches(".*6304[0-9A-F]{4}$");
    }

    /*
     * Ô kiểm tra phải tính trên cả bốn ký tự "6304" đứng ngay trước nó, đây là chỗ hay làm
     * sai nhất khi tự dựng mã. Tính lại bằng một cách viết khác rồi so, sai chỗ nào lộ ngay.
     */
    @Test
    @DisplayName("CRC tính trên toàn bộ chuỗi kể cả mã và độ dài của chính ô kiểm tra")
    void crcTinhDungPhamVi() {
        String qr = VietQrBuilder.dungChuoi(BIN_MB, SO_TAI_KHOAN, new BigDecimal("500000"), "VS-1");

        String phanThan = qr.substring(0, qr.length() - 4);
        String crcTrongChuoi = qr.substring(qr.length() - 4);

        assertThat(phanThan).endsWith("6304");
        assertThat(crcTrongChuoi).isEqualTo(tinhLaiCrc(phanThan));
    }

    @Test
    @DisplayName("Đổi số tiền thì ô kiểm tra cũng đổi theo")
    void doiSoTienThiDoiCrc() {
        String mot = VietQrBuilder.dungChuoi(BIN_MB, SO_TAI_KHOAN, new BigDecimal("500000"), "VS-1");
        String hai = VietQrBuilder.dungChuoi(BIN_MB, SO_TAI_KHOAN, new BigDecimal("600000"), "VS-1");

        assertThat(mot.substring(mot.length() - 4)).isNotEqualTo(hai.substring(hai.length() - 4));
    }

    // Bản tính CRC-16/CCITT-FALSE viết theo cách khác để đối chứng
    private String tinhLaiCrc(String chuoi) {
        int crc = 0xFFFF;
        for (char c : chuoi.toCharArray()) {
            for (int bit = 0; bit < 8; bit++) {
                boolean thamSo = ((c >> (7 - bit)) & 1) == 1;
                boolean caoNhat = ((crc >> 15) & 1) == 1;
                crc <<= 1;
                if (caoNhat ^ thamSo) {
                    crc ^= 0x1021;
                }
                crc &= 0xFFFF;
            }
        }
        return String.format("%04X", crc);
    }
}
