package vn.vuonsen.fnb.modules.gallery;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GalleryImageRepository extends JpaRepository<GalleryImage, Long> {

    List<GalleryImage> findByActiveTrueOrderBySortOrderAsc();

    List<GalleryImage> findByActiveTrueAndCategoryOrderBySortOrderAsc(String category);
}
