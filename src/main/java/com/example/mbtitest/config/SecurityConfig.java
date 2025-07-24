package com.example.mbtitest.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final SessionAuthenticationFilter sessionAuthenticationFilter;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 커스텀 세션 인증 필터 추가
            .addFilterBefore(sessionAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(authz -> authz
                // 정적 리소스는 모두 허용
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                
                // 공개 페이지들
                .requestMatchers("/", "/main", "/test", "/result/**", "/about", "/contact", "/privacy", "/terms").permitAll()
                .requestMatchers("/mbti-type/**", "/statistics/**", "/stats/**").permitAll()
                
                // API 엔드포인트들
                .requestMatchers("/api/test/**", "/api/share/**", "/api/view/**").permitAll()
                .requestMatchers("/user/api/register", "/user/api/login", "/user/api/check-**").permitAll()
                .requestMatchers("/user/api/current").permitAll()
                
                // 사용자 인증 관련 페이지들
                .requestMatchers("/user/register", "/user/login").permitAll()
                
                // 에러 페이지들
                .requestMatchers("/error/**").permitAll()
                
                // 커뮤니티 관련 - 로그인 필요
                .requestMatchers("/comments/**", "/community/**").authenticated()
                .requestMatchers("/api/comments/**").authenticated()
                
                // 사용자 프로필 관련 - 로그인 필요
                .requestMatchers("/user/profile", "/user/settings").authenticated()
                
                // 나머지 모든 요청은 허용 (개발 단계)
                .anyRequest().permitAll()
            )
            .csrf(csrf -> csrf.disable())  // CSRF 비활성화 (개발용)
            .formLogin(form -> form.disable())  // 기본 로그인 폼 비활성화
            .httpBasic(basic -> basic.disable())  // HTTP Basic 인증 비활성화
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    // 인증이 필요한 페이지에 접근했을 때
                    String requestURI = request.getRequestURI();
                    if (requestURI.startsWith("/api/")) {
                        // API 요청인 경우 JSON 응답
                        response.setStatus(401);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"success\":false,\"message\":\"로그인이 필요합니다.\",\"redirectUrl\":\"/user/login\"}");
                    } else {
                        // 일반 페이지 요청인 경우 로그인 페이지로 리다이렉트
                        response.sendRedirect("/user/login?redirectUrl=" + requestURI);
                    }
                })
            );
        
        return http.build();
    }
}
