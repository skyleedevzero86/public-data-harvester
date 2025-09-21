package com.antock.global.config;

import com.antock.global.security.JwtTokenProvider;
import com.antock.global.security.filter.JwtAuthenticationFilter;
import com.antock.global.security.handler.JwtAccessDeniedHandler;
import com.antock.global.security.handler.JwtAuthenticationEntryPoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtTokenProvider jwtTokenProvider;
        private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
        private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
        private final UserDetailsService userDetailsService;
        private final ObjectMapper objectMapper;

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                                .authorizeHttpRequests(authz -> authz
                                                .requestMatchers(
                                                                "/",
                                                                "/members/login",
                                                                "/members/join",
                                                                "/members/password/find",
                                                                "/members/password/reset",
                                                                "/api/v1/members/login",
                                                                "/api/v1/members/join",
                                                                "/api/v1/members/password/find",
                                                                "/api/v1/members/password/reset",
                                                                "/api/v1/members/password/reset/validate",

                                                                "/api/v1/corp/**",
                                                                "/api/v1/region-stats/**",
                                                                "/api/v1/files/**",
                                                                "/api/v1/coseller/**",
                                                                "/corp/**",
                                                                "/coseller/**",
                                                                "/file/**",
                                                                "/dashboard/**",
                                                                "/region/**",
                                                                "/main/**",
                                                                "/static/**",
                                                                "/webjars/**",
                                                                "/WEB-INF/views/**",
                                                                "/error",
                                                                "/web/files/**",
                                                                "/css/**",
                                                                "/js/**",
                                                                "/images/**",
                                                                "/favicon.ico")
                                                .permitAll()
                                                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                                                .requestMatchers("/coseller/save").authenticated()
                                                .requestMatchers("/members/profile", "/members/admin/**",
                                                                "/members/password/change")
                                                .authenticated()
                                                .anyRequest().authenticated())

                                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)

                                .exceptionHandling(exceptionHandling -> exceptionHandling
                                                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                                                .accessDeniedHandler(jwtAccessDeniedHandler))

                                .headers(headers -> headers
                                                .contentSecurityPolicy(csp -> csp
                                                                .policyDirectives("default-src 'self'; " +
                                                                                "script-src 'self' 'unsafe-inline' 'unsafe-eval' https://cdn.jsdelivr.net; "
                                                                                +
                                                                                "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; "
                                                                                +
                                                                                "img-src 'self' data: https:; " +
                                                                                "font-src 'self' https://cdn.jsdelivr.net; "
                                                                                +
                                                                                "connect-src 'self' https://cdn.jsdelivr.net; "
                                                                                +
                                                                                "object-src 'none'; " +
                                                                                "frame-src 'self'; " +
                                                                                "worker-src 'self'; " +
                                                                                "child-src 'self'; " +
                                                                                "frame-ancestors 'self'; " +
                                                                                "form-action 'self'; " +
                                                                                "base-uri 'self'; " +
                                                                                "manifest-src 'self'; " +
                                                                                "upgrade-insecure-requests; " +
                                                                                "block-all-mixed-content"))

                                                .frameOptions(frameOptions -> frameOptions.deny())
                                                .httpStrictTransportSecurity(hsts -> hsts
                                                                .maxAgeInSeconds(31536000)
                                                                .preload(true)));

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();

                configuration.setAllowedOriginPatterns(List.of("http://localhost:*", "https://localhost:*"));
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(Arrays.asList("*"));
                configuration.setAllowCredentials(true);
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }

        @Bean
        public JwtAuthenticationFilter jwtAuthenticationFilter() {
                return new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService, objectMapper);
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
                return authConfig.getAuthenticationManager();
        }
}