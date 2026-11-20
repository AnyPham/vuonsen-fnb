package vn.vuonsen.fnb.modules.assistant;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.ArrayList;
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
            /*
             * So sánh đứng đầu bảng.
             *
             * Câu "giá ở đây có rẻ hơn nhà hàng khác không" nếu để các nhóm sau xét trước
             * thì rơi vào nhóm chi phí, rồi khách nhận lại bảng giá của chính Vườn Sen mà
             * không ai trả lời câu họ thật sự hỏi. Nhóm này phải chặn trước.
             *
             * Từ khóa chọn loại có tham chiếu ra bên ngoài như "noi khac", "nha hang khac".
             * Cố ý không lấy "re hon" hay "dat hon" đứng một mình, vì câu hỏi nối tiếp
             * "còn gói nào rẻ hơn" là hỏi trong phạm vi Vườn Sen, ngân hàng đáp được.
             */
            Map.entry(Intent.SO_SANH, List.of(
                    "so sanh", "so voi", "noi khac", "cho khac", "quan khac", "nha hang khac",
                    "tiem khac", "doi thu", "ben ngoai", "thi truong", "dat hon o dau",
                    "re hon o dau", "hon nha hang", "hon cho khac", "hon noi khac")),

            /*
             * Hoàn cọc phải đứng trước đặt cọc.
             *
             * Hai nhóm dùng chung chữ "cọc", mà câu "hủy thì có được hoàn cọc không" vừa
             * chứa từ khóa nhóm này vừa chứa nhóm kia. Khách hỏi về hủy thì điều họ cần
             * biết là mất bao nhiêu, không phải phải đóng bao nhiêu.
             */
            // "hoan lai", "huy thi", "neu huy" bổ sung theo KAN-69: câu "hủy thì hoàn lại tiền
            // đặt cọc không" trước đây chỉ khớp "dat coc" và bị trả lời về mức đặt cọc
            Map.entry(Intent.HOAN_COC, List.of(
                    "hoan coc", "hoan tien", "hoan lai", "huy don", "huy tiec", "huy dat",
                    "huy thi", "neu huy", "doi y", "co lay lai", "lay lai coc", "mat coc",
                    "tra lai tien", "khong to chuc nua")),

            Map.entry(Intent.DAT_COC, List.of(
                    "dat coc", "tien coc", "coc bao nhieu", "phai coc", "giu cho")),

            /*
             * Thanh toán đứng trước chi phí: "thanh toán bằng cách nào" hỏi về phương
             * thức trả tiền, không phải hỏi giá bao nhiêu.
             */
            Map.entry(Intent.THANH_TOAN, List.of(
                    "thanh toan", "chuyen khoan", "quet ma", "ma qr", "vietqr", "vnpay",
                    "tra tien", "the ngan hang", "tra bang gi", "hinh thuc thanh toan")),

            /*
             * Sảnh nhỏ đứng trước cả không gian lẫn thực đơn.
             *
             * Câu "nhóm 20 người tự chọn món được không" chứa chữ của nhóm thực đơn, còn
             * "sảnh nhỏ" chứa chữ "sanh" của nhóm không gian. Cả hai đều trả lời trượt
             * điều khách hỏi, nên nhóm này phải xét trước.
             */
            Map.entry(Intent.SANH_NHO, List.of(
                    "sanh nho", "tu chon mon", "chon mon le", "thuc don tu chon", "nhom nho",
                    "khong lay goi", "khong can goi tiec", "an theo phan", "tu chon thuc don")),

            Map.entry(Intent.KHUYEN_MAI, List.of(
                    "khuyen mai", "giam gia", "uu dai", "dat som", "mien phi thue", "dip le", "ngay le")),

            // "gia goi", "gia thue"... bổ sung theo KAN-68: "giá gói tiệc bao nhiêu" có chữ "giá"
            // và "bao nhiêu" đứng cách nhau nên không khớp "gia bao nhieu", rơi xuống nhóm gói tiệc
            Map.entry(Intent.CHI_PHI, List.of(
                    "bao nhieu tien", "gia bao nhieu", "chi phi", "bao nhieu mot mam",
                    "gia ca", "tinh gia", "bang gia", "het bao nhieu", "thue bao nhieu",
                    "gia goi", "gia mot mam", "gia moi mam", "gia thue", "gia sanh", "gia phong")),

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
                    "may gio", "duong di", "ban do")),

            /*
             * Lời chào đứng cuối bảng (KAN-67).
             *
             * Khách hay mở đầu bằng "Chào shop, ..." rồi mới hỏi. Để nhóm này ở trên thì cả câu
             * bị hiểu là lời chào và câu hỏi thật bị bỏ qua. Đứng cuối thì chỉ câu chào suông,
             * không chứa từ khóa nào khác, mới được trả lời bằng lời chào.
             */
            Map.entry(Intent.CHAO_HOI, List.of(
                    "xin chao", "chao ban", "chao shop", "hello", "alo", "hi", "chao"))
    );

    /*
     * Từ nối cho thấy khách hỏi hai việc trong một câu, ví dụ "không gian và món ăn".
     * Chỉ gộp chủ đề khi có từ nối, để câu đơn có chữ "phòng" hay "sảnh" đi kèm không bị
     * trả lời lan sang chủ đề khác.
     */
    private static final List<String> TU_NOI = List.of("va", "voi", "cung nhu", "kem", "con");

    /* Số chủ đề tối đa trong một câu trả lời gộp, nhiều hơn thì câu trả lời quá dài. */
    private static final int SO_CHU_DE_GOP_TOI_DA = 2;

    public Intent detect(String cauHoi) {
        List<Intent> ds = tatCaChuDe(cauHoi);
        return ds.isEmpty() ? Intent.KHONG_HIEU : ds.get(0);
    }

    /*
     * Các chủ đề cần trả lời của một câu hỏi gộp (KAN-70).
     *
     * Trả về danh sách rỗng khi câu chỉ hỏi một chủ đề. Không gộp khi có chủ đề sảnh nhỏ,
     * vì sảnh nhỏ vốn đã trả lời cả không gian lẫn thực đơn tự chọn.
     */
    public List<Intent> chuDeGop(String cauHoi) {
        String s = " " + boDau(cauHoi) + " ";
        if (TU_NOI.stream().noneMatch(t -> s.contains(" " + t + " "))) {
            return List.of();
        }
        List<Intent> ds = tatCaChuDe(cauHoi).stream()
                .filter(i -> i != Intent.CHAO_HOI && i.coSanTrongNganHang())
                .limit(SO_CHU_DE_GOP_TOI_DA)
                .toList();
        if (ds.size() < 2 || ds.contains(Intent.SANH_NHO)) {
            return List.of();
        }
        return ds;
    }

    /*
     * Mọi chủ đề có từ khóa xuất hiện trong câu, theo thứ tự ưu tiên của bảng.
     *
     * Đệm hai đầu bằng dấu cách để so khớp theo từ trọn vẹn. Nếu so kiểu chứa chuỗi thì
     * từ khóa "hi" sẽ khớp vào giữa chữ "chi phí", khiến câu hỏi về chi phí bị hiểu nhầm
     * thành lời chào.
     */
    private List<Intent> tatCaChuDe(String cauHoi) {
        String s = " " + boDau(cauHoi) + " ";
        List<Intent> kq = new ArrayList<>();
        for (var nhom : TU_KHOA) {
            if (nhom.getValue().stream().anyMatch(tu -> s.contains(" " + tu + " "))) {
                kq.add(nhom.getKey());
            }
        }
        return kq;
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
