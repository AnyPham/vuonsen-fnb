package vn.vuonsen.fnb.modules.menu;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
 * Một ảnh trong thư viện của món ăn.
 *
 * Cùng hình dạng với SpaceImage bên module không gian nhưng cố ý để riêng, không
 * gom thành lớp dùng chung. Hai module không nên phụ thuộc nhau chỉ vì tình cờ có
 * hai trường giống tên: mai này ảnh món cần thêm thứ mà ảnh không gian không cần
 * thì tách ra lại mệt hơn là để riêng từ đầu.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DishImage {

    @Column(name = "url", nullable = false, length = 500)
    private String url;

    // Chú thích, ví dụ "Phần dọn cho 4 người" hoặc "Rau ăn kèm"
    @Column(name = "caption", length = 255)
    private String caption;
}
