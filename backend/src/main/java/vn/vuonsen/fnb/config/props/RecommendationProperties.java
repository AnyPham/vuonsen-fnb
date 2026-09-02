package vn.vuonsen.fnb.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

/*
 * Trọng số của thuật toán gợi ý, đọc từ mục app.recommendation trong application.yml.
 *
 * Để ở tệp cấu hình chứ không viết cứng trong mã, vì tinh chỉnh trọng số là việc
 * phải làm đi làm lại nhiều lần theo các tình huống thực tế. Đổi số ở đây rồi
 * chạy lại là xong, không phải dịch lại mã nguồn.
 *
 * Tổng năm trọng số nên bằng 100 cho dễ đọc điểm, nhưng không bắt buộc: lớp
 * RecommendationService luôn quy về thang 100 trước khi trả kết quả.
 */
@ConfigurationProperties(prefix = "app.recommendation")
public record RecommendationProperties(

        // Mức phù hợp của không gian với loại sự kiện, lấy từ bảng space_event_types
        int weightEventType,

        // Số khách lấp đầy sức chứa tới đâu
        int weightCapacityFit,

        // Tầm giá của gói tiệc có hợp với loại tiệc không
        int weightPackageTier,

        // Tổng chi phí có nằm trong ngân sách khách khai không
        int weightBudget,

        // Tổ hợp này đã được bao nhiêu khách trước chọn
        int weightPopularity,

        // Khách lấp đầy từ tỉ lệ này trở lên thì coi là vừa vặn, được điểm tối đa
        double capacityFitThreshold,

        // Vượt ngân sách trong khoảng này vẫn được nửa điểm
        double budgetTolerance,

        // Số phương án trả về
        int topN,

        // Mỗi không gian xuất hiện tối đa mấy lần trong kết quả.
        // Đặt 1 để ba phương án là ba không gian khác nhau.
        int maxPerSpace
) {
}
