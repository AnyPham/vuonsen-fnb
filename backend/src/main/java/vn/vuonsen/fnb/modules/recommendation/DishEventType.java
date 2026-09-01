package vn.vuonsen.fnb.modules.recommendation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.vuonsen.fnb.modules.booking.EventType;
import vn.vuonsen.fnb.modules.menu.Dish;

// Mức phù hợp của một món ăn với một loại sự kiện, chấm từ 1 đến 5.
// Ví dụ lẩu mắm hợp họp mặt gia đình (5 điểm) nhưng ít hợp tiệc cưới (2 điểm).
@Entity
@Table(name = "dish_event_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DishEventType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dish_id", nullable = false)
    private Dish dish;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 40)
    private EventType eventType;

    @Column(nullable = false)
    @Builder.Default
    private Integer suitability = 3;
}
