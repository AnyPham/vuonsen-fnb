package vn.vuonsen.fnb.modules.assistant;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.vuonsen.fnb.modules.assistant.dto.AnswerResponse;
import vn.vuonsen.fnb.modules.assistant.dto.AskRequest;
import vn.vuonsen.fnb.modules.assistant.llm.LuotHoiThoai;

import java.util.List;

/*
 * Cửa vào duy nhất của trợ lý, nối hai nhánh trả lời lại với nhau.
 *
 * Thứ tự ở đây là cố ý: dựng câu trả lời dự phòng trước, rồi mới hỏi mô hình ngôn
 * ngữ. Nghe thì ngược, vì làm vậy tốn thêm mấy câu truy vấn cả khi mô hình sẽ trả
 * lời. Nhưng đổi lại, đến lúc mô hình hỏng thì đã có sẵn câu trả lời trong tay,
 * không phải chạy đi dựng lại giữa lúc đang lỗi. Cơ chế dự phòng chỉ đáng tin khi
 * nó không cần thêm điều kiện gì mới chạy được.
 *
 * Mấy câu truy vấn đó đều là đọc bảng nhỏ và đã có sẵn trong bộ nhớ đệm của tầng
 * dữ liệu, nên cái giá phải trả là không đáng kể.
 *
 * Đề cương yêu cầu "xây dựng cơ chế dự phòng khi không kết nối được dịch vụ AI
 * bên ngoài". Chỗ này chính là cơ chế đó.
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
        AnswerResponse traLoiDuPhong = duPhong.answer(cauHoi);
        return moHinh.traLoi(cauHoi, chuanHoa(lichSu), traLoiDuPhong).orElse(traLoiDuPhong);
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
