package com.lsiproject.app.propertymanagementmicroservice.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Custom implementation of UserDetails that holds user information
 * extracted directly from the JWT claims (Stateless).
 * It avoids querying a database for user details.
 */
public class UserPrincipal implements UserDetails {

    private final Long idUser;
    private final String walletAddress;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long idUser, String walletAddress, Set<String> roles) {
        this.idUser = idUser;
        this.walletAddress = walletAddress;
        this.authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }

    public Long getIdUser() {
        return idUser;
    }

    public String getWalletAddress() {
        return walletAddress;
    }

    // --- UserDetails Interface Implementation (Required by Spring Security) ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null; // Not applicable for stateless token authentication
    }

    @Override
    public String getUsername() {
        return walletAddress; // Using wallet address as the primary identifier (username)
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

    @Override
    public boolean isEnabled() {
        return true;
    }
}