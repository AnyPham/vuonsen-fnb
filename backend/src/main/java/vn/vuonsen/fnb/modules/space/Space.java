package vn.vuonsen.fnb.modules.space;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.vuonsen.fnb.common.entity.BaseEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// Không gian sự kiện cho thuê
@Entity
@Table(name = "spaces")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Space extends BaseEntity {

    @Column(nullable = false, unique = true, length = 40)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, unique = true, length = 140)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(name = "space_type", nullable = false, length = 30)
    private SpaceType spaceType;

    @Column(name = "short_desc", length = 500)
    private String shortDesc;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "capacity_min", nullable = false)
    private Integer capacityMin;

    @Column(name = "capacity_max", nullable = false)
    private Integer capacityMax;

    // Phí thuê một buổi, hoặc một chòi tùy theo feeUnit
    @Column(name = "rental_fee", nullable = false, precision = 15, scale = 2)
    private BigDecimal rentalFee;

    // SESSION = tính theo buổi, HUT = tính theo chòi, DAY = trọn ngày
    @Column(name = "fee_unit", nullable = false, length = 20)
    @Builder.Default
    private String feeUnit = "SESSION";

    // Số khách mỗi chòi, chỉ dùng khi feeUnit = HUT
    @Column(name = "unit_capacity")
    private Integer unitCapacity;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    // Tọa độ để hiện lên Google Maps
    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "space_amenities", joinColumns = @JoinColumn(name = "space_id"))
    @Column(name = "amenity", length = 120)
    @Builder.Default
    private List<String> amenities = new ArrayList<>();
}
