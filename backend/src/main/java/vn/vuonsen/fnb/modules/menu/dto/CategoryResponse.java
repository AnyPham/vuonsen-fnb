package vn.vuonsen.fnb.modules.menu.dto;

import vn.vuonsen.fnb.modules.menu.DishCategory;

public record CategoryResponse(Long id, String code, String name, Integer sortOrder) {

    public static CategoryResponse from(DishCategory c) {
        return new CategoryResponse(c.getId(), c.getCode(), c.getName(), c.getSortOrder());
    }
}
