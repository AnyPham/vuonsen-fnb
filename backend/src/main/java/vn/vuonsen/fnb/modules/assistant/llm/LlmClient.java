package vn.vuonsen.fnb.modules.assistant.llm;

import java.util.List;

/*
 * Cổng gọi mô hình ngôn ngữ ngoài.
 *
 * Tách thành giao diện để phần trợ lý không phụ thuộc vào nhà cung cấp cụ thể:
 * đổi sang dịch vụ khác chỉ cần viết một lớp cài đặt mới, không đụng tới cách
 * dựng ngữ cảnh hay cơ chế dự phòng. Kiểm thử cũng thay được bằng bản giả, không
 * cần gọi mạng thật.
 */
public interface LlmClient {

    /*
     * Gửi chỉ dẫn hệ thống, các lượt trò chuyện trước và câu hỏi mới của khách, trả về
     * câu trả lời dạng chữ. lichSu xếp cũ trước mới sau, có thể rỗng.
     *
     * Gọi hỏng thì ném ngoại lệ chứ không tự nuốt lỗi, để nơi gọi quyết định có
     * chuyển sang cơ chế dự phòng hay không.
     */
    String hoi(String chiDanHeThong, List<LuotHoiThoai> lichSu, String cauHoi);

    // Tên dịch vụ, dùng khi ghi nhật ký để biết đang gọi bên nào
    String tenDichVu();

    /*
     * Đã đủ điều kiện để gọi chưa.
     *
     * Mỗi nhà cung cấp một yêu cầu khác nhau nên để chính nó tự trả lời, thay vì nơi gọi
     * đoán hộ. Gemini và Anthropic đòi khóa API; Ollama chạy ngay trên máy nên không có
     * khóa nào cả, luôn coi là sẵn sàng và để lỗi kết nối tự lộ ra lúc gọi.
     *
     * Mặc định là sẵn sàng, để bản giả trong kiểm thử không phải cài thêm gì.
     */
    default boolean sanSang() {
        return true;
    }

    /*
     * Câu hướng dẫn ghi ra nhật ký khi chưa sẵn sàng, nói rõ phải làm gì để bật lên.
     * Đọc nhật ký khởi động là biết thiếu cái gì, khỏi phải dò trong mã nguồn.
     */
    default String huongDanBat() {
        return "chưa đủ điều kiện gọi";
    }
}
