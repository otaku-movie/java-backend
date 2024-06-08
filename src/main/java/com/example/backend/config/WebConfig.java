package com.example.backend.config;

import com.example.backend.annotation.Interceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Locale;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  @Autowired
  private Interceptor interceptor;



//  @Bean
//  public LocaleResolver acceptHeaderLocaleResolver() {
//    new I18nConfig();
////    AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
////    resolver.setDefaultLocale(Locale.);
//    return new I18nConfig();
//  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(interceptor);
  }
}
