package com.example.hama.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.hama.LoginInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;

    public WebConfig(LoginInterceptor loginInterceptor) {
        this.loginInterceptor = loginInterceptor;
    }

    // ✅ 기존의 "/uploads/**" 관리 (독립된 메서드로 유지)
    public void addResourceHandlersForUploads(ResourceHandlerRegistry registry) {
        String uploadPath = System.getProperty("user.dir") + "/uploads/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath);
    }

    // ✅ 기존의 "/uploadPath/**" 관리 (독립된 메서드로 유지)
    public void addResourceHandlersForUploadPath(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploadPath/**") // URL 패턴
                .addResourceLocations("file:///c:/upload/"); // 실제 파일 경로
    }

    // ✅ 리뷰와 보드 업로드 경로를 추가 (독립된 메서드)
    public void addResourceHandlersForReviewAndBoard(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/review/**")
                .addResourceLocations("file:///C:/upload/review/");

        registry.addResourceHandler("/uploads/board/**")
                .addResourceLocations("file:///C:/upload/board/");
    }

    // ✅ 하나의 `addResourceHandlers` 메서드에서 세 개의 독립적인 핸들러를 호출
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        addResourceHandlersForUploads(registry);
        addResourceHandlersForUploadPath(registry);
        addResourceHandlersForReviewAndBoard(registry);
    }

    // ✅ 인터셉터 설정 유지
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**") // 모든 경로에 대해 인터셉터 적용
                .excludePathPatterns(
                    "/", "/user/register", "/user/login", "/user/logout",
                    "/user/find-id", "/user/reset-password", // 공개 URL
                    "/assets/**", "/images/**", "/webfonts/**", "/static/**",
                    "/api/user/validate-password", "/api/user/check-id",
                    "/api/user/check-name", "/api/email/verify-email",
                    "/api/email/verify-code", "/api/user/find-id", "/user/reset-password", "/api/user/reset-password",
                    "/admin/**" // 관리자 페이지도 제외
                );
    }
}
