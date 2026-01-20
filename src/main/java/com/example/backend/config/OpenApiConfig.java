package com.example.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 配置类
 * 用于生成 Swagger/OpenAPI 文档，支持 Apifox 导入
 */
@Configuration
public class OpenApiConfig {

    /**
     * OpenAPI 信息配置
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("电影票务系统 API 文档")
                        .description("电影票务系统后端 API 接口文档，支持选座、订单、支付等功能")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("开发团队")
                                .email("support@example.com")
                        )
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")
                        )
                )
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("本地开发环境"),
                        new Server().url("http://192.168.3.47:8080").description("测试环境"),
                        new Server().url("https://api.example.com").description("生产环境")
                ))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Bearer Authentication", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("token")
                        )
                );
    }

    /**
     * 通用 API 分组（/api/*）
     */
    @Bean
    public GroupedOpenApi commonApi() {
        return GroupedOpenApi.builder()
                .group("通用接口")
                .pathsToMatch("/api/**")
                .pathsToExclude("/api/admin/**", "/api/app/**")
                .build();
    }

    /**
     * 管理员 API 分组（/api/admin/*）
     */
    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("管理员接口")
                .pathsToMatch("/api/admin/**")
                .build();
    }

    /**
     * App API 分组（/api/app/*）
     */
    @Bean
    public GroupedOpenApi appApi() {
        return GroupedOpenApi.builder()
                .group("App 接口")
                .pathsToMatch("/api/app/**")
                .build();
    }

    /**
     * 所有 API 分组
     */
    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("所有接口")
                .pathsToMatch("/api/**")
                .build();
    }
}
