	package com.example.hama.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.hama.LoginInterceptor;


@Configuration
public class WebConfig implements WebMvcConfigurer {
	@Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath = System.getProperty("user.dir") + "/uploads/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath);
    }
	
    private final LoginInterceptor loginInterceptor;

    public WebConfig(LoginInterceptor loginInterceptor) {
        this.loginInterceptor = loginInterceptor;
    }

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
