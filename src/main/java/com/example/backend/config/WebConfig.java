package com.example.backend.config;

import com.example.backend.annotation.Interceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {
  @Autowired
  private Interceptor interceptor;

  /**
   * HTTP 请求信息拦截器
   * 用于在 ThreadLocal 中存储请求信息，以便 SQL 拦截器使用
   * 同时记录接口耗时
   */
  private static class RequestInfoInterceptor implements HandlerInterceptor {
    /**
     * 慢接口阈值（毫秒），超过此时间的接口会记录为 WARN 级别
     */
    private static final long SLOW_API_THRESHOLD = 100; // 100毫秒

    /**
     * 格式化耗时：超过1000毫秒显示为秒，否则显示毫秒
     */
    private static String formatDuration(long durationMs) {
      if (durationMs >= 1000) {
        double seconds = durationMs / 1000.0;
        return String.format("%.2fs", seconds);
      }
      return durationMs + "ms";
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
      RequestContextHolder.RequestInfo requestInfo = new RequestContextHolder.RequestInfo();
      requestInfo.setMethod(request.getMethod());
      requestInfo.setUri(request.getRequestURI());
      requestInfo.setQueryString(request.getQueryString());
      requestInfo.setApiPath(request.getRequestURI());
      requestInfo.setStartTime(System.currentTimeMillis());
      RequestContextHolder.setRequestInfo(requestInfo);
      return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
      RequestContextHolder.RequestInfo requestInfo = RequestContextHolder.getRequestInfo();
      if (requestInfo != null && requestInfo.getStartTime() != null) {
        long duration = System.currentTimeMillis() - requestInfo.getStartTime();
        String apiInfo = String.format("%s %s", requestInfo.getMethod(), requestInfo.getUri());
        
        // 格式化耗时
        String durationStr = formatDuration(duration);
        
        // 如果执行时间超过阈值，记录为 WARN 级别
        if (duration >= SLOW_API_THRESHOLD) {
          // 使用多次 log 调用实现真正的多行显示
          log.warn("═══════════════════════════════════════════════════════════════════════");
          log.warn("【慢接口警告】耗时: {} | 接口: {}", durationStr, apiInfo);
          log.warn("状态码: {}", response.getStatus());
          log.warn("═══════════════════════════════════════════════════════════════════════");
        } else {
          log.info("【接口执行】{} | 耗时: {} | 状态码: {}", apiInfo, durationStr, response.getStatus());
        }
      }
      
      // 清除 ThreadLocal，避免内存泄漏
      RequestContextHolder.clear();
    }
  }



//  @Bean
//  public LocaleResolver acceptHeaderLocaleResolver() {
//    new I18nConfig();
////    AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
////    resolver.setDefaultLocale(Locale.);
//    return new I18nConfig();
//  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    // 先添加请求信息拦截器（优先级更高，最先执行）
    registry.addInterceptor(new RequestInfoInterceptor()).order(-1);
    // 添加权限拦截器
    registry.addInterceptor(interceptor);
  }
}
