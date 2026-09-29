package vn.vuonsen.fnb.modules.space;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
 * Một ảnh trong thư viện của không gian.
 *
 * Bảng space_images có từ migration V1 nhưng suốt thời gian qua nằm không vì chưa
 * có lớp nào ánh xạ tới. Mỗi không gian mới chỉ hiện được một ảnh đại diện ở cột
 * thumbnail_url, trong khi khách đi xem chỗ đặt tiệc thì muốn nhìn nhiều góc.
 *
 * Viết dạng embeddable thay vì entity riêng vì ảnh không có vòng đời độc lập: xóa
 * không gian thì ảnh đi theo, và không nơi nào cần truy vấn ảnh tách rời khỏi không
 * gian chứa nó. Cách này cũng đồng bộ với danh sách tiện ích ở cùng lớp Space.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SpaceImage {

    @Column(name = "url", nullable = false, length = 500)
    private String url;

    // Chú thích hiện dưới ảnh, ví dụ "Góc sân khấu nhìn từ bàn tiệc"
    @Column(name = "caption", length = 255)
    private String caption;
}
