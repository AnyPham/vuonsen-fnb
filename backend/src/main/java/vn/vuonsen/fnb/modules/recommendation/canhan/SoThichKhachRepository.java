package vn.vuonsen.fnb.modules.recommendation.canhan;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SoThichKhachRepository extends JpaRepository<SoThichKhach, Long> {

    Optional<SoThichKhach> findByUserId(Long userId);
}
