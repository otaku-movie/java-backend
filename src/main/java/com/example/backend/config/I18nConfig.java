package com.example.backend.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;
import java.util.Optional;

@Configuration
public class I18nConfig {
  @Bean
  public LocaleResolver localeResolver() {
    return new I18nLocaleResolver();
  }

  static class I18nLocaleResolver implements LocaleResolver {
    @Override
    public Locale resolveLocale(HttpServletRequest request) {
      String acceptLanguage = request.getHeader("Accept-Language");
      if (acceptLanguage == null || acceptLanguage.isEmpty()) {
        return Locale.getDefault();
      }

      // Split the header by comma to get the most preferred language
      String[] languages = acceptLanguage.split(",");
      if (languages.length > 0) {
        String[] langCountry = languages[0].split("-|_");
        if (langCountry.length > 1) {
          return new Locale(langCountry[0], langCountry[1]);
        } else if (langCountry.length == 1) {
          return new Locale(langCountry[0]);
        }
      }

      return Locale.getDefault();
    }

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
      // This method is intentionally left empty as we are not setting locale dynamically
    }
  }
}
