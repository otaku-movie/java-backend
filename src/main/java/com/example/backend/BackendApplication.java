package com.example.backend;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.stp.StpUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import java.util.Arrays;


@SpringBootApplication(
	exclude = {
		// 去掉 security 的登录页面
		SecurityAutoConfiguration.class
	}
)
@EnableAspectJAutoProxy(proxyTargetClass = true)
@MapperScan("com.example.backend.mapper")
public class BackendApplication {

	@Autowired
	private Environment environment;

	@Value("${spring.profiles.active:default}")
	private static String env;

	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = SpringApplication.run(BackendApplication.class, args);

		// 通过 Environment 取激活的 Profile
		String[] profiles = ctx.getEnvironment().getActiveProfiles();
		System.out.println("Active Profiles: " + Arrays.toString(profiles));

		System.out.println("启动成功，Sa-Token 配置如下：" + SaManager.getConfig());
	}
}
