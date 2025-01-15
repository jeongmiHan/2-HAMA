package com.example.hama.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.hama.LoginInterceptor;
import com.example.hama.model.user.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class WebConfig implements WebMvcConfigurer {
   
    @Autowired
       private LoginInterceptor loginInterceptor;
    

       public WebConfig(LoginInterceptor loginInterceptor) {
           this.loginInterceptor = loginInterceptor;
       }

       @Override
       public void addInterceptors(InterceptorRegistry registry) {
           registry.addInterceptor(loginInterceptor)
                   .addPathPatterns("/**") // 모든 경로에 대해 적용
                   .excludePathPatterns(
                           "/",                  // 최상위 경로
                           "/user/register",
                             "/user/login",             // 로그인 경로
                             "/user/logout",            // 로그아웃 경로
                             "/assets/css/**",            // CSS 파일
                             "/assets/js/**",             // JS 파일
                             "/images/**",         // 이미지 파일
                             "/webfonts/**",        // WebJars 경로
                             "/static/**");        // 정적 리소스
       }
}
       
