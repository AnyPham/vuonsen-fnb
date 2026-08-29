package vn.vuonsen.fnb.modules.recommendation;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.vuonsen.fnb.modules.booking.EventType;

import java.util.List;

public interface SpaceEventTypeRepository extends JpaRepository<SpaceEventType, Long> {

    List<SpaceEventType> findByEventType(EventType eventType);
}
