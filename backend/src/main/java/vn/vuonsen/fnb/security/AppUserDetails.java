package vn.vuonsen.fnb.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import vn.vuonsen.fnb.modules.user.User;

import java.util.Collection;
import java.util.List;

// Bọc entity User lại để Spring Security dùng được
@Getter
public class AppUserDetails implements UserDetails {

    private final Long userId;
    private final String email;
    private final String fullName;
    private final String password;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    public AppUserDetails(User user) {
        this.userId = user.getId();
        this.email = user.getEmail();
        this.fullName = user.getFullName();
        this.password = user.getPasswordHash();
        this.enabled = user.isEnabled();
        this.authorities = List.of(new SimpleGrantedAuthority(user.getRole().authority()));
    }

    // Đăng nhập bằng email
    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
