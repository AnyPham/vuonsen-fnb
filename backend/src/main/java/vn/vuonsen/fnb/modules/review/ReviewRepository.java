package vn.vuonsen.fnb.modules.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByApprovedTrueOrderByCreatedAtDesc(Pageable pageable);

    Page<Review> findByApprovedOrderByCreatedAtDesc(boolean approved, Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.approved = true")
    Double averageRating();
}
