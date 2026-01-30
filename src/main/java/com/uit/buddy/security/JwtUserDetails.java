package com.uit.buddy.security;

import com.uit.buddy.entity.auth.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
public class JwtUserDetails implements UserDetails {

    private final UUID id;
    private final String email;
    private final String mssv;
    private final String password;
    private final String fullName;
    private final Collection<? extends GrantedAuthority> authorities;

    public JwtUserDetails(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.mssv = user.getMssv();
        this.password = user.getPassword();
        this.fullName = user.getFullName();
        this.authorities = List.of();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
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
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
