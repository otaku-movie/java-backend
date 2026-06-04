package com.example.backend.config;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Flyway 行为配置。
 *
 * <p>dev 环境允许 V 脚本在本地频繁修改（rebase、cherry-pick、切分支等场景），
 * 数据库已记录的 checksum 会经常与本地不一致，导致 Spring Boot 启动时
 * Flyway 校验失败。这里在启动前先执行 repair，把 schema history
 * 里的 checksum 同步成当前文件的值，避免每次手动 mvn flyway:repair。</p>
 *
 * <p>prod / test 等其它 profile 仍走默认严格校验，避免线上误改脚本被忽略。</p>
 */
@Configuration
public class FlywayConfig {

  @Bean
  @Profile("dev")
  public FlywayMigrationStrategy devFlywayMigrationStrategy() {
    return flyway -> {
      flyway.repair();
      flyway.migrate();
    };
  }
}
