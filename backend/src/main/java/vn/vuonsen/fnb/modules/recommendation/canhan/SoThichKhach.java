package vn.vuonsen.fnb.modules.recommendation.canhan;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.vuonsen.fnb.common.entity.BaseEntity;
import vn.vuonsen.fnb.modules.booking.EventType;
import vn.vuonsen.fnb.modules.recommendation.canhan.SoThichGoiY.KieuKhongGian;
import vn.vuonsen.fnb.modules.recommendation.canhan.SoThichGoiY.TamGia;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

/*
 * Sở thích khách tự khai, bảng so_thich_khach (migration V31).
 *
 * Giữ user_id dạng số chứ không nối quan hệ tới User, giống bảng luot_xem: hệ gợi ý chỉ cần
 * biết sở thích của ai, không cần nạp cả tài khoản. Ràng buộc khóa ngoại vẫn nằm ở cơ sở dữ
 * liệu nên xóa tài khoản là xóa luôn sở thích.
 *
 * Danh sách loại tiệc lưu thành một chuỗi ngăn cách dấu phẩy. Chỉ có năm giá trị và không
 * bao giờ truy vấn theo từng loại, nên không đáng dựng thêm một bảng nối.
 */
@Entity
@Table(name = "so_thich_khach")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SoThichKhach extends BaseEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "loai_tiec", length = 120)
    private String loaiTiec;

    @Column(name = "so_khach")
    private Integer soKhach;

    @Enumerated(EnumType.STRING)
    @Column(name = "kieu_khong_gian", length = 20)
    private KieuKhongGian kieuKhongGian;

    @Enumerated(EnumType.STRING)
    @Column(name = "tam_gia", length = 20)
    private TamGia tamGia;

    @Column(name = "an_chay", nullable = false)
    @Builder.Default
    private boolean anChay = false;

    public Set<EventType> cacLoaiTiec() {
        if (loaiTiec == null || loaiTiec.isBlank()) {
            return EnumSet.noneOf(EventType.class);
        }
        return Arrays.stream(loaiTiec.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(EventType::valueOf)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(EventType.class)));
    }

    public void datLoaiTiec(Set<EventType> loai) {
        this.loaiTiec = loai == null || loai.isEmpty()
                ? null
                : loai.stream().sorted().map(Enum::name).collect(Collectors.joining(","));
    }

    public SoThichGoiY choGoiY() {
        return new SoThichGoiY(cacLoaiTiec(), soKhach, kieuKhongGian, tamGia, anChay);
    }
}
