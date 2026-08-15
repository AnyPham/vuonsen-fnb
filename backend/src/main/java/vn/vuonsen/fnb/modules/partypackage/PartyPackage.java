package vn.vuonsen.fnb.modules.partypackage;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderBy;
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

// Gói tiệc trọn gói, tính tiền theo mâm 10 khách
@Entity
@Table(name = "party_packages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartyPackage extends BaseEntity {

    @Column(nullable = false, unique = true, length = 40)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 255)
    private String tagline;

    @Column(name = "price_per_table", nullable = false, precision = 15, scale = 2)
    private BigDecimal pricePerTable;

    @Column(name = "dish_count")
    private Integer dishCount;

    @Column(name = "hours_included")
    private Integer hoursIncluded;

    // Gói được gắn nhãn "Được chọn nhiều nhất"
    @Column(nullable = false)
    @Builder.Default
    private boolean featured = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "package_features", joinColumns = @JoinColumn(name = "package_id"))
    @Column(name = "feature", length = 255)
    @OrderBy("sort_order ASC")
    @Builder.Default
    private List<String> features = new ArrayList<>();
}
