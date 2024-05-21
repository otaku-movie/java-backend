package com.example.backend.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.io.PrintWriter;

@EnableWebSecurity // 开启 web security
@Configuration
public class SecurityConfiguration {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return  http
            .authorizeHttpRequests(auth -> {
//                允许所有人访问
                auth.anyRequest().permitAll();
            })
            .cors(conf -> {
                System.out.println(conf);
                System.out.println("hello world");
                CorsConfiguration cors = new CorsConfiguration();
                cors.addAllowedOrigin("*");
                cors.addAllowedHeader("*");
                cors.addExposedHeader("*");
                cors.addAllowedMethod("*");

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", cors);
                conf.configurationSource(source);
            })
            .exceptionHandling(conf -> {
//                conf.accessDeniedHandler(this::loginSuccess);
//                conf.authenticationEntryPoint(this::loginSuccess);

            })
            .csrf(AbstractHttpConfigurer::disable)
            .build();
    }

    void loginError(HttpServletRequest request, HttpServletResponse response) {


    }

    void loginSuccess(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("application/json;charset=utf-8");
        PrintWriter writer = null;
        try {
            writer = response.getWriter();
//            writer.write(RestBean.success(authentication.getName()).asJsonString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
