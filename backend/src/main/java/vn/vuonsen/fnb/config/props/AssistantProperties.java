package vn.vuonsen.fnb.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

/*
 * Cấu hình nhánh gọi mô hình ngôn ngữ ngoài của trợ lý.
 *
 * Khóa API bắt buộc truyền qua biến môi trường hoặc tệp tro-ly-ai.properties, không viết
 * thẳng vào tệp cấu hình vì tệp đó nằm trong git. Máy nào không đặt khóa thì trợ lý tự
 * chạy bằng cơ chế dự phòng trong hệ thống, khách không thấy lỗi gì.
 *
 * Nhờ vậy dự án tải về là chạy được ngay, không ai phải đăng ký khóa API mới xem
 * được trợ lý hoạt động.
 */
@ConfigurationProperties(prefix = "app.assistant")
public record AssistantProperties(Llm llm) {

    // Thiếu hẳn khối cấu hình thì coi như tắt, đỡ phải kiểm tra null ở nơi dùng
    public AssistantProperties {
        if (llm == null) {
            llm = new Llm(false, null, null, null, null, null, 0, 0);
        }
    }

    public record Llm(
            boolean enabled,

            /*
             * Nhà cung cấp mô hình: gemini, ollama hoặc anthropic.
             *
             * Ba lựa chọn khác nhau chủ yếu ở chỗ tốn tiền hay không:
             *   gemini    có hạn mức miễn phí, cần khóa nhưng không cần thẻ ngân hàng
             *   ollama    chạy mô hình ngay trên máy, không cần khóa, không cần mạng
             *   anthropic trả tiền theo lượng dùng
             *
             * Mỗi nhà cung cấp là một lớp cài đặt LlmClient riêng, Spring chọn lớp nào
             * theo đúng giá trị ở đây.
             */
            String provider,

            String apiKey,
            String baseUrl,

            /*
             * Tên mô hình. Để trống thì mỗi nhà cung cấp tự dùng mặc định của mình.
             *
             * Cố ý không đặt một giá trị mặc định chung: tên mô hình của ba nhà cung cấp
             * không giống nhau chút nào, để mặc định chung thì đổi nhà cung cấp mà quên
             * đổi tên mô hình là gọi hỏng ngay.
             */
            String model,

            // Mức công sức suy luận: low, medium, high. Câu tư vấn ngắn chỉ cần low.
            String effort,
            int maxTokens,
            int timeoutSeconds
    ) {
        public static final String GEMINI = "gemini";
        public static final String OLLAMA = "ollama";
        public static final String ANTHROPIC = "anthropic";

        // Thiếu thì coi như dùng Gemini, vì đó là đường miễn phí dễ bắt đầu nhất
        public String providerHieuLuc() {
            return provider == null || provider.isBlank() ? GEMINI : provider.trim().toLowerCase();
        }

        public boolean coKhoa() {
            return apiKey != null && !apiKey.isBlank();
        }

        // Tên mô hình đã khai, hoặc mặc định của nhà cung cấp nếu bỏ trống
        public String modelHoac(String macDinh) {
            return model == null || model.isBlank() ? macDinh : model.trim();
        }

        public String baseUrlHoac(String macDinh) {
            return baseUrl == null || baseUrl.isBlank() ? macDinh : baseUrl.trim();
        }

        public int timeoutAnToan() {
            return Math.max(1, timeoutSeconds);
        }

        public int maxTokensHoac(int macDinh) {
            return maxTokens > 0 ? maxTokens : macDinh;
        }

        /*
         * Chỉ gọi ra ngoài khi vừa bật vừa có khóa.
         *
         * Giữ lại cho những nhà cung cấp đòi khóa. Riêng Ollama chạy ngay trên máy nên
         * không có khóa nào cả; chỗ đó hỏi client bằng sanSang() thay vì hỏi ở đây.
         */
        public boolean dungDuoc() {
            return enabled && coKhoa();
        }
    }
}
