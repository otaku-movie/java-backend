package com.example.backend;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.tomcat.util.descriptor.LocalResolver;
import org.springframework.cglib.core.Local;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

@Configuration
public class I18nConfig implements LocaleResolver {
  @Override
  public Locale resolveLocale(HttpServletRequest request) {
    String language = request.getHeader("accept-language");
    Locale locale = Locale.getDefault();

    return  locale;
  }

  @Override
  public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {

  }
}
