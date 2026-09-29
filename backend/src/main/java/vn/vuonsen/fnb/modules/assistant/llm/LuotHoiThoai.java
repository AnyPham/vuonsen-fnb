package vn.vuonsen.fnb.modules.assistant.llm;

/*
 * Một lượt trong cuộc trò chuyện, trước câu hỏi hiện tại.
 *
 * Gửi kèm các lượt trước để mô hình hiểu câu hỏi nối tiếp. Khách hỏi "có những gói tiệc
 * nào" rồi hỏi tiếp "gói rẻ nhất bao nhiêu một mâm": thiếu lượt trước thì câu sau không
 * biết đang nói tới gói nào.
 */
public record LuotHoiThoai(boolean cuaKhach, String noiDung) {
}
