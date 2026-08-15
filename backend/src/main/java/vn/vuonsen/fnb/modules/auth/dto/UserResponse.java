package vn.vuonsen.fnb.modules.auth.dto;

import vn.vuonsen.fnb.modules.user.User;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        String address,
        String role
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getAddress(),
                user.getRole().name());
    }
}
