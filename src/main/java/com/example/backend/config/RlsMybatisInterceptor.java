package com.example.backend.config;

import com.example.backend.enumerate.DataScope;
import java.sql.Connection;
import java.sql.Statement;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Component;

/**
 * MyBatis 执行层拦截器：在每条 SQL 执行前向当前 JDBC {@link Connection} 写入 PostgreSQL 自定义会话变量
 *（GUC），供库内 <b>RLS（行级安全）策略</b>通过 {@code current_setting('app.xxx', true)} 读取。
 *
 * <p><b>与谁配合</b>
 *
 * <ul>
 *   <li>{@link RequestContextHolder}：由 {@link DataScopeInterceptor}、{@link NonAdminRlsInterceptor} 等根据登录用户
 *       写入 {@code level}、{@code org_id}、{@code cinema_ids}、{@code user_id}；
 *   <li>若 {@code level} 为空，本拦截器直接放行，不执行任何 SET（避免影响未接入数据范围的请求）。
 * </ul>
 *
 * <p><b>SET 与 SET LOCAL</b>
 *
 * <ul>
 *   <li>连接为 {@code autoCommit=true}（很多连接池默认取到的逻辑）时，使用 {@code SET}，SQL 结束后在 {@code finally}
 *       里对曾写入的变量执行 {@code RESET}，避免变量泄漏到同连接的下一次请求；
 *   <li>在事务中（{@code autoCommit=false}）时使用 {@code SET LOCAL}，变量仅对本事务有效，事务结束自动失效，无需
 *       RESET。
 * </ul>
 *
 * <p><b>安全说明</b>：{@code org_id}、{@code cinema_ids} 仅允许数字与逗号组成的列表，防止拼接进 SQL 的特殊字符注入。
 */
@Slf4j
@Component
@Intercepts({
  @Signature(
      type = Executor.class,
      method = "query",
      args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
  @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
public class RlsMybatisInterceptor implements Interceptor {

  /** 院线/影院维度下逗号分隔的 id 列表，允许为空或纯数字序列，如 {@code 1,2,3}。 */
  private static boolean isValidCinemaIdsList(String s) {
    return s == null || s.isEmpty() || s.matches("^(\\d+,)*\\d+$");
  }

  /** 仅允许纯数字，用于 {@code app.current_user_id}。 */
  private static boolean isDigits(String s) {
    return s != null && s.matches("^\\d+$");
  }

  /**
   * 在 Mapper 的 {@code query}/{@code update} 真正执行前设置会话变量，执行后视 {@code autoCommit} 决定是否 RESET。
   */
  @Override
  public Object intercept(Invocation invocation) throws Throwable {
    String level = RequestContextHolder.getRls("level");
    if (level == null || level.isEmpty()) {
      return invocation.proceed();
    }
    if (!level.equals(DataScope.PLATFORM.getCode())
        && !level.equals(DataScope.CHAIN.getCode())
        && !level.equals(DataScope.CINEMA.getCode())) {
      return invocation.proceed();
    }

    Executor executor = (Executor) invocation.getTarget();
    Connection conn = executor.getTransaction().getConnection();
    boolean autoCommit = conn.getAutoCommit();
    // 非事务：SET 持久到连接上，必须在同连接上 RESET；事务内：SET LOCAL 仅当前事务可见
    String setKw = autoCommit ? "SET" : "SET LOCAL";

    boolean setOrg = false;
    boolean setCinemas = false;
    boolean setUser = false;

    try (Statement stmt = conn.createStatement()) {
      stmt.execute(setKw + " app.current_level = '" + level + "'");
      String orgId = RequestContextHolder.getRls("org_id");
      if (orgId != null && !orgId.isEmpty() && isValidCinemaIdsList(orgId)) {
        stmt.execute(setKw + " app.current_org_id = '" + orgId + "'");
        setOrg = true;
      }
      String cinemaIds = RequestContextHolder.getRls("cinema_ids");
      if (cinemaIds != null && !cinemaIds.isEmpty()) {
        if (!isValidCinemaIdsList(cinemaIds)) {
          throw new IllegalStateException("Invalid RLS cinema_ids");
        }
        stmt.execute(setKw + " app.current_cinema_ids = '" + cinemaIds + "'");
        setCinemas = true;
      }
      String userId = RequestContextHolder.getRls("user_id");
      if (userId != null && isDigits(userId)) {
        stmt.execute(setKw + " app.current_user_id = '" + userId + "'");
        setUser = true;
      }
    }

    try {
      return invocation.proceed();
    } finally {
      // SET LOCAL 随事务结束自动清除；仅对本轮使用了 SET 的连接做 RESET，避免污染连接池复用的连接
      if (autoCommit) {
        resetGuc(conn, "app.current_level");
        if (setOrg) {
          resetGuc(conn, "app.current_org_id");
        }
        if (setCinemas) {
          resetGuc(conn, "app.current_cinema_ids");
        }
        if (setUser) {
          resetGuc(conn, "app.current_user_id");
        }
      }
    }
  }

  /** 将自定义 GUC 恢复为默认值，RESET 失败仅打 debug，避免干扰业务异常路径。 */
  private static void resetGuc(Connection conn, String name) {
    try (Statement stmt = conn.createStatement()) {
      stmt.execute("RESET " + name);
    } catch (Exception e) {
      log.debug("RESET {}: {}", name, e.getMessage());
    }
  }

  @Override
  public Object plugin(Object target) {
    return Plugin.wrap(target, this);
  }
}
