package vn.vuonsen.fnb.modules.assistant;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.List;
import java.util.Map;

/*
 * Nhận diện ý định của câu hỏi bằng cách so khớp từ khóa.
 *
 * Vì sao không dùng mô hình ngôn ngữ ở bước này: đề cương giới hạn trợ lý hoạt
 * động trong phạm vi dữ liệu của hệ thống, và yêu cầu có cơ chế dự phòng khi
 * không kết nối được dịch vụ bên ngoài. Cách so khớp từ khóa chạy độc lập,
 * không tốn phí, không phụ thuộc mạng, và giải thích được vì sao ra kết quả đó.
 *
 * Câu hỏi được bỏ dấu trước khi so, nên khách gõ "sanh nao chua duoc 300 khach"
 * không dấu vẫn hiểu được. Đây là cách gõ rất phổ biến khi nhắn nhanh.
 */
@Component
public class IntentDetector {

    /*
     * Thứ tự trong danh sách này là thứ tự ưu tiên khi một câu chứa từ khóa của
     * nhiều nhóm. Ví dụ "giá gói tiệc bao nhiêu" chứa cả từ của nhóm chi phí lẫn
     * nhóm gói tiệc, ưu tiên chi phí vì đó mới là điều khách muốn biết.
     */
    private static final List<Map.Entry<Intent, List<String>>> TU_KHOA = List.of(
            Map.entry(Intent.CHAO_HOI, List.of(
                    "xin chao", "chao ban", "chao shop", "hello", "alo", "hi", "chao")),

            Map.entry(Intent.DAT_COC, List.of(
                    "dat coc", "tien coc", "coc bao nhieu", "phai coc", "giu cho")),

            Map.entry(Intent.KHUYEN_MAI, List.of(
                    "khuyen mai", "giam gia", "uu dai", "dat som", "mien phi thue")),

            Map.entry(Intent.CHI_PHI, List.of(
                    "bao nhieu tien", "gia bao nhieu", "chi phi", "bao nhieu mot mam",
                    "gia ca", "tinh gia", "bang gia", "het bao nhieu", "thue bao nhieu")),

            Map.entry(Intent.QUY_TRINH_DAT, List.of(
                    "dat tiec nhu the nao", "dat truoc bao lau", "quy trinh", "thu tuc",
                    "cach dat", "lam sao de dat", "bao truoc", "dat nhu the nao")),

            Map.entry(Intent.TRA_CUU_DON, List.of(
                    "tra cuu", "kiem tra don", "ma don", "don cua toi", "xem don")),

            Map.entry(Intent.KHONG_GIAN, List.of(
                    "khong gian", "sanh", "phong", "choi", "san vuon", "suc chua",
                    "chua duoc", "chua noi", "chua het", "bao nhieu khach",
                    "ngoai troi", "trong nha", "hoi nghi")),

            Map.entry(Intent.GOI_TIEC, List.of(
                    "goi tiec", "goi dich vu", "combo", "goi nao", "tron goi")),

            // Không để từ "lau" đứng một mình vì trùng với "bao lâu" trong câu hỏi
            // về thời gian báo trước
            Map.entry(Intent.THUC_DON, List.of(
                    "thuc don", "mon an", "co mon gi", "mon gi", "an gi", "mon lau",
                    "lau nuong", "nuong", "khai vi", "trang mieng", "do uong", "ban chay")),

            Map.entry(Intent.LIEN_HE, List.of(
                    "dia chi", "o dau", "so dien thoai", "lien he", "gio mo cua",
                    "may gio", "duong di", "ban do"))
    );

    public Intent detect(String cauHoi) {
        // Đệm hai đầu bằng dấu cách để so khớp theo từ trọn vẹn.
        // Nếu so kiểu chứa chuỗi thì từ khóa "hi" sẽ khớp vào giữa chữ "chi phí",
        // khiến câu hỏi về chi phí bị hiểu nhầm thành lời chào.
        String s = " " + boDau(cauHoi) + " ";
        for (var nhom : TU_KHOA) {
            for (String tu : nhom.getValue()) {
                if (s.contains(" " + tu + " ")) {
                    return nhom.getKey();
                }
            }
        }
        return Intent.KHONG_HIEU;
    }

    /*
     * Lấy số đầu tiên trong câu, dùng cho câu hỏi kiểu "sảnh nào chứa 300 khách".
     * Trả về 0 nếu câu không có số nào.
     */
    public int soTrongCau(String cauHoi) {
        StringBuilder so = new StringBuilder();
        for (char c : cauHoi.toCharArray()) {
            if (Character.isDigit(c)) {
                so.append(c);
            } else if (so.length() > 0) {
                break;
            }
        }
        if (so.length() == 0 || so.length() > 6) {
            return 0;
        }
        return Integer.parseInt(so.toString());
    }

    /*
     * Chuẩn hóa câu hỏi: bỏ dấu tiếng Việt, đưa về chữ thường, đổi mọi ký tự không
     * phải chữ và số thành dấu cách rồi gộp các dấu cách liền nhau.
     *
     * Chữ đ phải xử lý riêng vì không tách được thành chữ cái và dấu.
     * Bỏ dấu câu là bắt buộc, nếu không thì "300 khách?" sẽ không khớp với từ khóa
     * "khach" khi so theo từ trọn vẹn.
     */
    String boDau(String text) {
        if (text == null) {
            return "";
        }
        String s = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return s.replace('đ', 'd').replace('Đ', 'D')
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", " ")
                .trim();
    }
}
