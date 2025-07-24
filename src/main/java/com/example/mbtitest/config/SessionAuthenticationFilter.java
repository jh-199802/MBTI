package com.example.mbtitest.config;

import com.example.mbtitest.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Slf4j
@Component
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        try {
            HttpSession session = request.getSession(false);
            
            if (session != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                User currentUser = (User) session.getAttribute("currentUser");
                
                if (currentUser != null && currentUser.isActive()) {
                    // 세션에서 사용자 정보를 가져와서 Spring Security Context에 설정
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                            currentUser.getUsername(), 
                            null, 
                            new ArrayList<>() // 권한은 필요에 따라 추가
                        );
                    
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    log.debug("세션에서 사용자 인증 정보 설정: {}", currentUser.getUsername());
                }
            }
            
        } catch (Exception e) {
            log.error("세션 인증 처리 중 오류 발생", e);
            SecurityContextHolder.clearContext();
        }
        
        filterChain.doFilter(request, response);
    }
}