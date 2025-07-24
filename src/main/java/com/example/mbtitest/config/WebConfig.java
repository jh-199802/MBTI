package com.example.mbtitest.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;

/**
 * Spring MVC 웹 설정
 * 정적 리소스 매핑 및 뷰 컨트롤러 설정
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    /**
     * 정적 리소스 핸들러 설정
     * CSS, JS, 이미지 파일들의 경로 매핑
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // CSS 파일 매핑
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/")
                .setCachePeriod(31536000); // 1년 캐시
        
        // JavaScript 파일 매핑
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/")
                .setCachePeriod(31536000);
        
        // 이미지 파일 매핑
        registry.addResourceHandler("/image/**")
                .addResourceLocations("classpath:/static/image/")
                .setCachePeriod(31536000);
        
        // 모든 정적 리소스 매핑 (기본)
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(31536000);
    }
    
    /**
     * 뷰 컨트롤러 설정
     * 컨트롤러 로직이 필요없는 단순 뷰 매핑
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 루트 경로를 메인 페이지로 리다이렉트
        registry.addRedirectViewController("/", "/");
        
        // 기본 정적 HTML 파일들에 대한 접근 차단 (Spring Boot 컨트롤러 우선)
        // /static/html/ 직접 접근 방지
    }
}
