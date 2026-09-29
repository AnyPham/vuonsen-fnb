package vn.vuonsen.fnb.modules.notification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.vuonsen.fnb.common.entity.BaseEntity;

/*
 * Một lần hệ thống định gửi thư cho khách.
 *
 * Ghi lại cả khi chỉ chạy ở chế độ không gửi thật, để khi khách báo không nhận được email
 * thì còn chỗ tra xem hệ thống đã gửi gì và có lỗi gì không.
 */
@Entity
@Table(name = "email_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailLog extends BaseEntity {

    @Column(nullable = false, length = 160)
    private String recipient;

    @Column(nullable = false, length = 255)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(nullable = false, length = 40)
    private String kind;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "error_detail", length = 500)
    private String errorDetail;
}
