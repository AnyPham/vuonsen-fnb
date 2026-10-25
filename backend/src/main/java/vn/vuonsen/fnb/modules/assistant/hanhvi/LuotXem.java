package vn.vuonsen.fnb.modules.assistant.hanhvi;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/*
 * Một lần khách mở trang chi tiết của một thứ gì đó.
 *
 * Đây là đơn vị nhỏ nhất của kênh thu thập ngầm. Cố ý ghi rất ít: ai, xem gì, lúc nào.
 * Không ghi địa chỉ IP, không ghi chuỗi nhận dạng trình duyệt, không ghi thời gian ở lại
 * trang. Ba thứ đó đều là dữ liệu cá nhân mà bài toán gợi ý không cần tới.
 */
@Entity
@Table(name = "luot_xem")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LuotXem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* NULL khi khách chưa đăng nhập. Bản ghi đó chỉ dùng để đếm mức phổ biến chung. */
    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai_doi_tuong", nullable = false, length = 20)
    private LoaiDoiTuong loaiDoiTuong;

    @Column(name = "doi_tuong_id", nullable = false)
    private Long doiTuongId;

    @Column(name = "xem_luc", nullable = false)
    private LocalDateTime xemLuc;

    public static LuotXem ghi(Long userId, LoaiDoiTuong loai, Long doiTuongId) {
        return new LuotXem(null, userId, loai, doiTuongId, LocalDateTime.now());
    }

    public enum LoaiDoiTuong {
        KHONG_GIAN,
        MON_AN,
        GOI_TIEC
    }
}
