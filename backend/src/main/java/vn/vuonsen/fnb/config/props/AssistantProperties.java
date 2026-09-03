package vn.vuonsen.fnb.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

/*
 * Cấu hình nhánh gọi mô hình ngôn ngữ ngoài của trợ lý.
 *
 * Khóa API bắt buộc truyền qua biến môi trường ASSISTANT_LLM_API_KEY, không viết
 * thẳng vào tệp cấu hình vì tệp đó nằm trong git. Máy nào không đặt biến này thì
 * trợ lý tự chạy bằng cơ chế dự phòng trong hệ thống, khách không thấy lỗi gì.
 *
 * Nhờ vậy dự án tải về là chạy được ngay, không ai phải đăng ký khóa API mới xem
 * được trợ lý hoạt động.
 */
@ConfigurationProperties(prefix = "app.assistant")
public record AssistantProperties(Llm llm) {

    // Thiếu hẳn khối cấu hình thì coi như tắt, đỡ phải kiểm tra null ở nơi dùng
    public AssistantProperties {
        if (llm == null) {
            llm = new Llm(false, null, null, null, 0, 0);
        }
    }

    public record Llm(
            boolean enabled,
            String apiKey,
            String baseUrl,
            String model,
            int maxTokens,
            int timeoutSeconds
    ) {
        /*
         * Chỉ gọi ra ngoài khi vừa bật vừa có khóa. Tách riêng thành một phép kiểm
         * tra để mọi nơi hỏi cùng một câu, tránh chỗ nhớ kiểm tra khóa chỗ quên.
         */
        public boolean dungDuoc() {
            return enabled && apiKey != null && !apiKey.isBlank();
        }
    }
}
