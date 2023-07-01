package account.business.configs.security;

import account.business.models.users.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class UserDetailsImpl implements UserDetails {
    private final String email;
    private final String password;
    private final List<GrantedAuthority> rolesAndAuthorities;
    private final boolean isActive;

    public UserDetailsImpl(UserEntity userEntity) {
        email = userEntity.getEmail().toLowerCase();
        password = userEntity.getPassword();
        List<GrantedAuthority> tempAuthorities = userEntity.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        rolesAndAuthorities = Collections.unmodifiableList(tempAuthorities);
        isActive = userEntity.isActive();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return rolesAndAuthorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

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
        return isActive;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}