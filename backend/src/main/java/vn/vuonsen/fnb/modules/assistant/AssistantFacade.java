package vn.vuonsen.fnb.modules.assistant;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.vuonsen.fnb.modules.assistant.dto.AnswerResponse;

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

    private final AssistantService duPhong;
    private final LlmAssistant moHinh;

    public AnswerResponse answer(String cauHoi) {
        AnswerResponse traLoiDuPhong = duPhong.answer(cauHoi);
        return moHinh.traLoi(cauHoi, traLoiDuPhong).orElse(traLoiDuPhong);
    }
}
