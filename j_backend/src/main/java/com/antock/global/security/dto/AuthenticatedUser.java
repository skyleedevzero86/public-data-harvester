package com.antock.global.security.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Getter
@Builder
public class AuthenticatedUser {

    private Long id;
    private String username;
    private String nickname;
    private Collection<? extends GrantedAuthority> authorities;

    public boolean hasRole(String role) {
        return authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role));
    }
}