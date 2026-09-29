package vn.vuonsen.fnb.modules.dishorder.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import vn.vuonsen.fnb.modules.dishorder.FulfillmentType;

import java.time.LocalDateTime;
import java.util.List;

/*
 * Đơn đặt món khách gửi lên.
 *
 * Chỉ nhận mã món và số lượng, không nhận giá. Giá lấy từ cơ sở dữ liệu ở phía máy
 * chủ, vì nếu tin con số khách gửi lên thì ai cũng sửa được thành 1 đồng.
 */
public record DishOrderRequest(
        @NotNull(message = "Vui lòng chọn hình thức nhận món")
        FulfillmentType fulfillmentType,

        @NotBlank(message = "Vui lòng nhập tên người đặt")
        @Size(max = 120) String customerName,

        @NotBlank(message = "Vui lòng nhập số điện thoại")
        @Pattern(regexp = "0\\d{9,10}", message = "Số điện thoại phải gồm 10 hoặc 11 chữ số và bắt đầu bằng 0")
        String customerPhone,

        @Email(message = "Email không đúng định dạng")
        @Size(max = 160) String customerEmail,

        // Bắt buộc với đơn giao tận nhà, kiểm tra ở tầng nghiệp vụ vì phụ thuộc
        // vào hình thức nhận món chứ không phải lúc nào cũng bắt buộc
        @Size(max = 400) String deliveryAddress,

        // Bắt buộc với đơn ăn tại chỗ
        @Min(value = 1, message = "Số khách phải lớn hơn 0")
        Integer guestCount,

        @NotNull(message = "Vui lòng chọn thời điểm nhận món")
        LocalDateTime serveAt,

        @Size(max = 600) String note,

        @NotEmpty(message = "Vui lòng chọn ít nhất một món")
        @Valid List<ItemRequest> items
) {

    public record ItemRequest(
            @NotNull(message = "Thiếu mã món") Long dishId,
            @NotNull @Min(value = 1, message = "Số lượng phải lớn hơn 0") Integer quantity
    ) {
    }
}
