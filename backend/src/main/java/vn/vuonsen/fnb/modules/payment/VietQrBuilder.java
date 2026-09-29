package vn.vuonsen.fnb.modules.payment;

import java.math.BigDecimal;
import java.text.Normalizer;

/*
 * Dựng chuỗi mã QR chuyển khoản theo chuẩn VietQR của Napas.
 *
 * Chuẩn này xây trên EMVCo QR: cả chuỗi là một dãy các khối "mã - độ dài - giá trị", trong
 * đó độ dài luôn viết bằng hai chữ số. Khối 38 chứa thông tin ngân hàng thụ hưởng và cũng
 * được gói theo đúng cách đó, tức là khối lồng trong khối.
 *
 * Tự dựng chuỗi thay vì gọi dịch vụ sinh QR bên ngoài vì ba lẽ: không phải phụ thuộc một
 * dịch vụ có thể chết bất cứ lúc nào, không gửi số tài khoản và số tiền của nhà hàng ra
 * ngoài, và quan trọng nhất là phần tính toán nằm trong mã nguồn nên kiểm thử được.
 *
 * Chuỗi trả về đem vẽ thành ảnh QR ở phía giao diện. Khách mở ứng dụng ngân hàng quét là ra
 * sẵn số tài khoản, số tiền và nội dung chuyển khoản, không phải gõ tay nên không gõ nhầm.
 */
public final class VietQrBuilder {

    /** Mã định danh của Napas trong khối thông tin thụ hưởng. */
    private static final String GUID_NAPAS = "A000000727";

    /** Chuyển khoản nhanh tới số tài khoản. */
    private static final String DICH_VU_CHUYEN_KHOAN = "QRIBFTTA";

    private static final String TIEN_VND = "704";
    private static final String MA_QUOC_GIA = "VN";

    /** QR có sẵn số tiền thì chỉ dùng được một lần, khác với QR tĩnh dùng nhiều lần. */
    private static final String KHOI_TAO_MOT_LAN = "12";

    private VietQrBuilder() {
    }

    /**
     * @param binNganHang   mã ngân hàng 6 chữ số do Napas cấp, ví dụ 970422 là MB
     * @param soTaiKhoan    số tài khoản thụ hưởng
     * @param soTien        số tiền, làm tròn về đồng
     * @param noiDung       nội dung chuyển khoản, nên chứa mã đơn để đối soát
     */
    public static String dungChuoi(String binNganHang, String soTaiKhoan,
                                   BigDecimal soTien, String noiDung) {
        String thuHuong = khoi("00", binNganHang) + khoi("01", soTaiKhoan);
        String thongTinNganHang = khoi("00", GUID_NAPAS)
                + khoi("01", thuHuong)
                + khoi("02", DICH_VU_CHUYEN_KHOAN);

        StringBuilder sb = new StringBuilder()
                .append(khoi("00", "01"))
                .append(khoi("01", KHOI_TAO_MOT_LAN))
                .append(khoi("38", thongTinNganHang))
                .append(khoi("53", TIEN_VND))
                .append(khoi("54", soTien.setScale(0, java.math.RoundingMode.HALF_UP).toPlainString()))
                .append(khoi("58", MA_QUOC_GIA));

        String moTa = khongDau(noiDung);
        if (!moTa.isBlank()) {
            sb.append(khoi("62", khoi("08", moTa)));
        }

        // Ô kiểm tra tính trên toàn bộ chuỗi, kể cả mã và độ dài của chính ô đó
        String chuaCoKiemTra = sb + "6304";
        return chuaCoKiemTra + crc16(chuaCoKiemTra);
    }

    /** Một khối: mã hai chữ số, độ dài hai chữ số, rồi tới giá trị. */
    private static String khoi(String ma, String giaTri) {
        return ma + String.format("%02d", giaTri.length()) + giaTri;
    }

    /*
     * Nội dung chuyển khoản bỏ dấu và viết hoa.
     *
     * Ứng dụng ngân hàng phần lớn không nhận tiếng Việt có dấu ở ô nội dung, gửi nguyên dấu
     * thì tới nơi thành ký tự lạ, đối soát không đọc được.
     */
    private static String khongDau(String chuoi) {
        if (chuoi == null) {
            return "";
        }
        String phang = Normalizer.normalize(chuoi, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace('đ', 'd').replace('Đ', 'D');
        return phang.replaceAll("[^A-Za-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim()
                .toUpperCase();
    }

    /*
     * CRC-16/CCITT-FALSE: đa thức 0x1021, giá trị khởi tạo 0xFFFF, không đảo bit.
     * Kết quả viết bằng bốn chữ số hex in hoa, đúng như chuẩn EMVCo quy định.
     */
    private static String crc16(String chuoi) {
        int crc = 0xFFFF;
        for (byte b : chuoi.getBytes(java.nio.charset.StandardCharsets.UTF_8)) {
            crc ^= (b & 0xFF) << 8;
            for (int i = 0; i < 8; i++) {
                crc = (crc & 0x8000) != 0 ? ((crc << 1) ^ 0x1021) : (crc << 1);
                crc &= 0xFFFF;
            }
        }
        return String.format("%04X", crc);
    }
}
