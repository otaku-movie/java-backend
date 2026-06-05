package com.example.backend;

import cn.dev33.satoken.SaManager;
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
import org.springframework.scheduling.annotation.EnableAsync;
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
@EnableAsync
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
		var log = org.slf4j.LoggerFactory.getLogger(BackendApplication.class);
		var successLog = org.slf4j.LoggerFactory.getLogger("com.example.backend.SUCCESS");
		log.info("Active Profiles: {}", Arrays.toString(profiles));
		var cfg = SaManager.getConfig();
		successLog.info(
			"启动成功，Sa-Token: tokenName={}, timeout={}s, concurrent={}, share={}",
			cfg.getTokenName(),
			cfg.getTimeout(),
			cfg.getIsConcurrent(),
			cfg.getIsShare());
	}
}
