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
}
