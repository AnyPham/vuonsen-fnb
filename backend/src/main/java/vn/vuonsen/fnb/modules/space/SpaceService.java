package vn.vuonsen.fnb.modules.space;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vuonsen.fnb.common.exception.BusinessException;
import vn.vuonsen.fnb.common.exception.ResourceNotFoundException;
import vn.vuonsen.fnb.modules.space.dto.SpaceAdminResponse;
import vn.vuonsen.fnb.modules.space.dto.SpaceRequest;
import vn.vuonsen.fnb.modules.space.dto.SpaceResponse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpaceService {

    private final SpaceRepository spaceRepository;

    public List<SpaceResponse> search(Integer guests, SpaceType type, BigDecimal maxPrice) {
        return spaceRepository.search(guests, type, maxPrice).stream()
                .map(SpaceResponse::from)
                .toList();
    }

    public SpaceResponse getBySlug(String slug) {
        return spaceRepository.findBySlug(slug)
                .map(SpaceResponse::from)
                .orElseThrow(() -> ResourceNotFoundException.of("không gian", slug));
    }

    public Space getEntity(Long id) {
        return spaceRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("không gian", id));
    }

    // ---------- Thao tac quan tri ----------

    // Danh sách cho trang quản trị, gồm cả không gian đã ngừng kinh doanh
    public List<SpaceAdminResponse> listForAdmin() {
        return spaceRepository.findAllByOrderBySortOrderAsc().stream()
                .map(SpaceAdminResponse::from)
                .toList();
    }

    @Transactional
    public SpaceResponse create(SpaceRequest request) {
        if (spaceRepository.findByCode(request.code()).isPresent()) {
            throw new BusinessException("Mã không gian '%s' đã tồn tại".formatted(request.code()));
        }
        Space space = new Space();
        apply(space, request);
        return SpaceResponse.from(spaceRepository.save(space));
    }

    @Transactional
    public SpaceResponse update(Long id, SpaceRequest request) {
        Space space = getEntity(id);
        apply(space, request);
        return SpaceResponse.from(spaceRepository.save(space));
    }

    @Transactional
    public void deactivate(Long id) {
        Space space = getEntity(id);
        space.setActive(false);          // xóa mềm để giữ lịch sử đơn đã đặt
        spaceRepository.save(space);
    }

    private void apply(Space space, SpaceRequest r) {
        if (r.capacityMin() > r.capacityMax()) {
            throw new BusinessException("Sức chứa tối thiểu không được lớn hơn sức chứa tối đa");
        }
        // Thiếu số khách mỗi chòi thì PricingService quay về tính theo buổi,
        // giá báo cho khách sẽ sai mà không ai biết
        if ("HUT".equals(r.feeUnit()) && (r.unitCapacity() == null || r.unitCapacity() <= 0)) {
            throw new BusinessException("Không gian tính phí theo chòi phải khai số khách mỗi chòi");
        }
        space.setCode(r.code());
        space.setName(r.name());
        space.setSlug(r.slug());
        space.setSpaceType(r.spaceType());
        space.setShortDesc(r.shortDesc());
        space.setDescription(r.description());
        space.setCapacityMin(r.capacityMin());
        space.setCapacityMax(r.capacityMax());
        space.setRentalFee(r.rentalFee());
        space.setFeeUnit(r.feeUnit() == null ? "SESSION" : r.feeUnit());
        space.setUnitCapacity(r.unitCapacity());
        space.setThumbnailUrl(r.thumbnailUrl());
        space.setLatitude(r.latitude());
        space.setLongitude(r.longitude());
        space.setActive(r.active() == null || r.active());
        space.setSortOrder(r.sortOrder() == null ? 0 : r.sortOrder());
        space.setAmenities(r.amenities() == null ? new ArrayList<>() : new ArrayList<>(r.amenities()));
    }
}
