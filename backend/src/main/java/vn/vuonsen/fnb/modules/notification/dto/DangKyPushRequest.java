package vn.vuonsen.fnb.modules.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/* Dữ liệu trình duyệt trả về sau khi khách đồng ý nhận thông báo. */
public record DangKyPushRequest(
        @NotBlank @Size(max = 512) String endpoint,
        @NotBlank String p256dh,
        @NotBlank String auth,
        String ngonNgu) {
}
