package vn.vuonsen.fnb.modules.partypackage;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PartyPackageRepository extends JpaRepository<PartyPackage, Long> {

    List<PartyPackage> findByActiveTrueOrderBySortOrderAsc();

    Optional<PartyPackage> findByCode(String code);

    // Trang quản trị lấy cả gói đã ngừng bán
    List<PartyPackage> findAllByOrderBySortOrderAsc();
}
