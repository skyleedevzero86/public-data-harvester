package com.antock.global.security;

import com.antock.api.member.application.service.MemberDomainService;
import com.antock.api.member.domain.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberDomainService memberDomainService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("사용자 인증 정보 로드 시도 - username: {}", username);

        Member member = memberDomainService.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("사용자를 찾을 수 없음 - username: {}", username);
                    return new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
                });

        if (!member.isActive()) {
            log.warn("비활성 사용자 로그인 시도 - username: {}, status: {}", username, member.getStatus());
            throw new UsernameNotFoundException("비활성 사용자입니다: " + username);
        }

        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + member.getRole().name())
        );

        log.debug("사용자 인증 정보 로드 완료 - username: {}, role: {}", username, member.getRole());

        return User.builder()
                .username(member.getUsername())
                .password(member.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(member.isLocked())
                .credentialsExpired(false)
                .disabled(!member.isActive())
                .build();
    }
}