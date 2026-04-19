package com.example.backend.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.config.RequestContextHolder;
import com.example.backend.entity.Cinema;
import com.example.backend.entity.User;
import com.example.backend.entity.UserCinema;
import com.example.backend.enumerate.DataScope;
import com.example.backend.mapper.CinemaMapper;
import com.example.backend.mapper.UserCinemaMapper;
import com.example.backend.mapper.UserMapper;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 根据后台登录用户 {@link User#dataScope} 写入 {@link RequestContextHolder} RLS 键，供 {@link
 * com.example.backend.config.RlsMybatisInterceptor} 设置 PostgreSQL 会话变量。
 *
 * <p><b>数据范围语义（与菜单/按钮权限无关）</b>
 *
 * <ul>
 *   <li><b>platform（平台）</b>：{@code org_id}、{@code cinema_ids} 置空，{@code level=platform}。库内 RLS 策略在
 *       platform 分支下放行；{@link com.example.backend.config.DataScopeSelectInnerInterceptor} 对 SELECT 不做
 *       IN 收窄。即在<b>行级数据范围</b>上可看到当前库内策略允许的全量业务数据（仍受接口权限、业务校验等约束）。
 *   <li><b>chain（院线）</b>：仅能访问<b>本账号绑定品牌（{@code users.brand_id}）下全部影院</b>及其关联业务数据（订单、场次、影厅等依赖
 *       {@code cinema_id} 或 RLS 策略与这些影院关联）。实现上写入 {@code org_id=brandId}，并将该品牌下所有 {@code cinema.id} 拼入
 *       {@code cinema_ids}，供 {@code movie_order} 等表按「影院 id 列表」策略过滤；{@code cinema} 表本身还按 {@code brand_id =
 *       app.current_org_id} 约束。看不到其它品牌的数据。若 {@code brand_id} 为空或品牌下无影院，管理后台登录会拒绝（见
 *       {@code AdminUserController#login}）；若因历史数据仍进入请求上下文，此处保持 {@code chain} 且 {@code org_id}/{@code
 *       cinema_ids} 为空，RLS 通常放行 0 行，不再回退为 {@code platform} 以免误放大权限。
 *   <li><b>cinema（影院）</b>：仅能访问 {@code user_cinema} 勾选的若干影院及其关联数据；{@code cinema_ids} 为上述影院 id，{@code org_id}
 *       与之一致（供策略使用）。
 * </ul>
 */
@Service
public class AdminDataScopeRlsService {

  private static final Set<String> VALID_LEVELS =
      Set.of(
          DataScope.PLATFORM.getCode(),
          DataScope.CHAIN.getCode(),
          DataScope.CINEMA.getCode());

  @Autowired private UserMapper userMapper;
  @Autowired private CinemaMapper cinemaMapper;
  @Autowired private UserCinemaMapper userCinemaMapper;

  /** 为当前 Sa-Token 登录用户（后台 users 表）应用数据范围；用户不存在时回退为 platform。 */
  public void applyForLoggedInAdminUser() {
    int userId = StpUtil.getLoginIdAsInt();
    User user = userMapper.selectById(userId);
    if (user == null) {
      setPlatformContext(String.valueOf(userId));
      return;
    }
    applyForUser(user);
  }

  public void applyForUser(User user) {
    String level = DataScope.normalize(user.getDataScope());
    if (!VALID_LEVELS.contains(level)) {
      level = DataScope.PLATFORM.getCode();
    }
    String orgId = "";
    String cinemaIds = "";
    switch (level) {
      case "chain":
        if (user.getBrandId() != null) {
          orgId = String.valueOf(user.getBrandId());
          cinemaIds =
              cinemaMapper
                  .selectList(
                      new LambdaQueryWrapper<Cinema>().eq(Cinema::getBrandId, user.getBrandId()))
                  .stream()
                  .map(c -> String.valueOf(c.getId()))
                  .collect(Collectors.joining(","));
        } else {
          // 正常登录已拦截；此处保持 chain + 空 org/cinema，避免误按 platform 放大可见范围
        }
        break;
      case "cinema":
        List<UserCinema> ucs =
            userCinemaMapper.selectList(
                new LambdaQueryWrapper<UserCinema>().eq(UserCinema::getUserId, user.getId()));
        cinemaIds =
            ucs.stream()
                .map(uc -> String.valueOf(uc.getCinemaId()))
                .collect(Collectors.joining(","));
        orgId = cinemaIds;
        break;
      default:
        level = DataScope.PLATFORM.getCode();
        cinemaIds = "";
    }
    RequestContextHolder.setRls("level", level);
    RequestContextHolder.setRls("org_id", orgId);
    RequestContextHolder.setRls("cinema_ids", cinemaIds != null ? cinemaIds : "");
    RequestContextHolder.setRls("user_id", String.valueOf(user.getId()));
  }

  private void setPlatformContext(String userId) {
    RequestContextHolder.setRls("level", DataScope.PLATFORM.getCode());
    RequestContextHolder.setRls("org_id", "");
    RequestContextHolder.setRls("cinema_ids", "");
    RequestContextHolder.setRls("user_id", userId);
  }
}
