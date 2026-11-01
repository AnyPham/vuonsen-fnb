package vn.vuonsen.fnb.modules.assistant;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.vuonsen.fnb.modules.assistant.dto.AnswerResponse;
import vn.vuonsen.fnb.modules.assistant.dto.AskRequest;
import vn.vuonsen.fnb.modules.assistant.llm.LuotHoiThoai;

import java.util.List;

/*
 * Cửa vào duy nhất của trợ lý, chia lượt hỏi thành hai hướng xử lý.
 *
 * HƯỚNG MỘT — câu nằm trong ngân hàng.
 * Bộ nhận dạng ý định bắt được câu hỏi thuộc một trong các nhóm quen thuộc như không
 * gian, thực đơn, chi phí, đặt cọc. Những câu này có lời đáp dựng sẵn từ cơ sở dữ liệu
 * nên trả thẳng, không gọi mô hình ngôn ngữ lần nào.
 *
 * HƯỚNG HAI — câu ngoài ngân hàng, gồm cả câu so sánh với nơi khác.
 * Ngân hàng không có lời đáp đúng ý, lúc đó mới nhờ mô hình ngôn ngữ diễn đạt. Nhưng
 * mô hình chỉ được đọc phần DỮ LIỆU mà SystemContextBuilder nạp sẵn từ cơ sở dữ liệu,
 * không nối vào cơ sở dữ liệu và không được lấy số liệu từ đâu khác.
 *
 * Vì sao phải chia hai hướng thay vì đưa hết cho mô hình:
 *
 * Thứ nhất, câu trả lời cho cùng một câu hỏi phải giống nhau qua mọi lần hỏi. Khách hỏi
 * giá hôm nay một kiểu, mai một kiểu thì không dùng được. Ngân hàng cho đúng điều đó,
 * mô hình thì không hứa được.
 *
 * Thứ hai, mỗi con số trong lời đáp phải truy được về đúng bảng nào trong cơ sở dữ liệu.
 * Đây là điều phải giải thích trước hội đồng, mà đi qua mô hình thì không chứng minh nổi.
 *
 * Thứ ba, phần lớn lượt hỏi rơi vào ngân hàng, nên không phải gọi dịch vụ ngoài. Nhanh
 * hơn, không tốn hạn mức, và không phụ thuộc đường mạng.
 *
 * Thứ tự dựng câu cũng là cố ý: dựng lời đáp từ ngân hàng trước, rồi mới xét có cần mô
 * hình không. Nghe thì ngược vì tốn thêm mấy câu truy vấn cả khi mô hình sẽ trả lời.
 * Đổi lại, đến lúc mô hình hỏng thì đã có sẵn câu trả lời trong tay, không phải chạy đi
 * dựng lại giữa lúc đang lỗi. Cơ chế dự phòng chỉ đáng tin khi nó không cần thêm điều
 * kiện gì mới chạy được. Mấy câu truy vấn đó đều đọc bảng nhỏ và đã nằm trong bộ nhớ
 * đệm của tầng dữ liệu, nên cái giá phải trả không đáng kể.
 *
 * Đề cương yêu cầu "xây dựng cơ chế dự phòng khi không kết nối được dịch vụ AI bên
 * ngoài". Hướng một chính là cơ chế đó, và nó còn là đường đi mặc định chứ không chỉ
 * là phương án chữa cháy.
 */
@Service
@RequiredArgsConstructor
public class AssistantFacade {

    private static final int SO_LUOT_TOI_DA = 10;
    private static final int DO_DAI_LUOT_TOI_DA = 1500;

    private final AssistantService duPhong;
    private final LlmAssistant moHinh;

    public AnswerResponse answer(String cauHoi) {
        return answer(cauHoi, List.of());
    }

    /*
     * Trả lời kèm các lượt trò chuyện trước. Cơ chế dự phòng chỉ nhìn câu hỏi hiện tại, còn
     * mô hình ngôn ngữ dùng thêm lịch sử để hiểu câu hỏi nối tiếp.
     */
    public AnswerResponse answer(String cauHoi, List<AskRequest.LuotHoi> lichSu) {
        AnswerResponse tuNganHang = duPhong.answer(cauHoi);

        /*
         * Hướng một: câu đã có lời đáp dựng sẵn thì dừng ở đây.
         *
         * Không gọi mô hình kể cả khi mô hình đang bật và sẵn sàng. Để mô hình viết lại
         * một câu vốn đã đúng và đã đủ thì chỉ đổi được cách hành văn, mà đánh mất tính
         * lặp lại được và khiến con số không còn truy nguồn được nữa.
         */
        if (tuNganHang.intent().coSanTrongNganHang()) {
            return tuNganHang.danhDauLayTuNganHang();
        }

        /*
         * Hướng hai: câu ngoài ngân hàng hoặc câu so sánh với nơi khác.
         *
         * Mô hình hỏng, chưa bật hay trả về câu rỗng thì rơi lại lời đáp của ngân hàng,
         * giữ nguyên nguồn DU_PHONG để nhìn vào biết là nhánh dự phòng đã chạy.
         */
        return moHinh.traLoi(cauHoi, chuanHoa(lichSu), tuNganHang).orElse(tuNganHang);
    }

    /*
     * Đổi lịch sử trình duyệt gửi lên thành các lượt hội thoại.
     *
     * Không tin tuyệt đối dữ liệu từ trình duyệt: bỏ lượt rỗng hoặc có vai lạ, chỉ giữ 10 lượt
     * gần nhất và cắt bớt lượt quá dài, để một cuộc trò chuyện kéo dài không đẩy chi phí gọi mô
     * hình lên mãi.
     */
    static List<LuotHoiThoai> chuanHoa(List<AskRequest.LuotHoi> lichSu) {
        if (lichSu == null) {
            return List.of();
        }
        List<LuotHoiThoai> hopLe = lichSu.stream()
                .filter(l -> l != null && l.content() != null && !l.content().isBlank())
                .filter(l -> "user".equals(l.role()) || "assistant".equals(l.role()))
                .map(l -> {
                    String noiDung = l.content().trim();
                    if (noiDung.length() > DO_DAI_LUOT_TOI_DA) {
                        noiDung = noiDung.substring(0, DO_DAI_LUOT_TOI_DA);
                    }
                    return new LuotHoiThoai("user".equals(l.role()), noiDung);
                })
                .toList();
        return List.copyOf(hopLe.subList(Math.max(0, hopLe.size() - SO_LUOT_TOI_DA), hopLe.size()));
    }
}
