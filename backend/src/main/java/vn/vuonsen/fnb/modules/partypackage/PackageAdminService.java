package vn.vuonsen.fnb.modules.partypackage;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vuonsen.fnb.common.exception.BusinessException;
import vn.vuonsen.fnb.common.exception.ResourceNotFoundException;
import vn.vuonsen.fnb.modules.partypackage.dto.PackageAdminResponse;
import vn.vuonsen.fnb.modules.partypackage.dto.PackageRequest;

import java.util.ArrayList;
import java.util.List;

// Thêm, sửa, ngừng bán gói tiệc từ trang quản trị
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PackageAdminService {

    private final PartyPackageRepository packageRepository;

    public List<PackageAdminResponse> listAll() {
        return packageRepository.findAllByOrderBySortOrderAsc().stream()
                .map(PackageAdminResponse::from)
                .toList();
    }

    @Transactional
    public PackageAdminResponse create(PackageRequest request) {
        if (packageRepository.findByCode(request.code()).isPresent()) {
            throw new BusinessException("Mã gói tiệc '%s' đã tồn tại".formatted(request.code()));
        }
        PartyPackage pkg = new PartyPackage();
        apply(pkg, request);
        return PackageAdminResponse.from(packageRepository.save(pkg));
    }

    @Transactional
    public PackageAdminResponse update(Long id, PackageRequest request) {
        PartyPackage pkg = getEntity(id);

        // Đổi mã sang mã của gói khác thì báo lỗi, mã phải là duy nhất
        packageRepository.findByCode(request.code())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new BusinessException("Mã gói tiệc '%s' đã tồn tại".formatted(request.code()));
                });

        apply(pkg, request);
        return PackageAdminResponse.from(packageRepository.save(pkg));
    }

    // Ngừng bán chứ không xóa hẳn, vì các đơn cũ vẫn trỏ tới gói này
    @Transactional
    public void deactivate(Long id) {
        PartyPackage pkg = getEntity(id);
        pkg.setActive(false);
        packageRepository.save(pkg);
    }

    private PartyPackage getEntity(Long id) {
        return packageRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("gói tiệc", id));
    }

    private void apply(PartyPackage pkg, PackageRequest r) {
        pkg.setCode(r.code());
        pkg.setName(r.name());
        pkg.setTagline(r.tagline());
        pkg.setPricePerTable(r.pricePerTable());
        pkg.setDishCount(r.dishCount());
        pkg.setHoursIncluded(r.hoursIncluded());
        pkg.setFeatured(r.featured() != null && r.featured());
        pkg.setActive(r.active() == null || r.active());
        pkg.setSortOrder(r.sortOrder() == null ? 0 : r.sortOrder());

        // Bỏ các dòng để trống khi nhân viên nhập thiếu
        List<String> features = new ArrayList<>();
        if (r.features() != null) {
            r.features().stream()
                    .filter(f -> f != null && !f.isBlank())
                    .map(String::trim)
                    .forEach(features::add);
        }
        pkg.setFeatures(features);
    }
}
