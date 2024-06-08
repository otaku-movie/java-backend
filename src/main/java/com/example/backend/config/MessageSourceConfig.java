package com.example.backend.config;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.core.io.ClassPathResource;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Properties;

@Configuration
public class MessageSourceConfig {

  @Bean
  public MessageSource messageSource() {
    return new YamlMessageSource();
  }

  public static class YamlMessageSource extends AbstractMessageSource {

    private static final String BASENAME = "i18n/messages";
    private static final String DEFAULT_ENCODING = "UTF-8";

    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
      Properties properties = loadYmlByLocale(locale);
      String message = properties.getProperty(code);
      if (message == null) {
        // If message is not found, fall back to default locale
        properties = loadYmlByLocale(Locale.getDefault());
        message = properties.getProperty(code);
      }
      return message != null ? new MessageFormat(message, locale) : null;
    }

    private Properties loadYmlByLocale(Locale locale) {
      String language = locale.getLanguage();
      String country = locale.getCountry();
      String lang = language.split(";")[0];
      String yamlFileName = String.format("%s_%s%s.yml", BASENAME, lang, country.isEmpty() ? "" : "_" + country);
      System.out.println("Loading YAML file: " + yamlFileName);

      ClassPathResource yamlFileResource = new ClassPathResource(yamlFileName);

      YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
      yaml.setResources(yamlFileResource);
      return yaml.getObject();
    }
  }
}
