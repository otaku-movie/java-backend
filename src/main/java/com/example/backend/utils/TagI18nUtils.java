package com.example.backend.utils;

import com.example.backend.entity.MovieShowTimeTag;
import com.example.backend.entity.MovieTag;
import com.example.backend.response.movie.Tags;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.List;

/**
 * 标签国际化工具：把库里以日文存储的标签名，按当前请求语言替换成对应译名。
 *
 * 译名存在数据库列上（name = 日文、name_zh = 中文、name_en = 英文），不走 i18n YAML
 * （这类标签会随爬虫增长，写死在代码里不合适）。接口对外仍只返回单个 {@code name} 字段，
 * 由这里按 Accept-Language 选好对应语言的值；译名为空时回退日文原名，保证：
 *   - 新爬到、还没配译名的标签 → 原样显示日文；
 *   - App 端无需感知多语言列，响应结构与之前完全一致。
 */
public final class TagI18nUtils {

  private TagI18nUtils() {}

  /**
   * 按当前请求语言在「日文 / 中文 / 英文」三个值中挑一个；选中的为空则回退日文原名。
   */
  public static String pickByLocale(String ja, String zh, String en) {
    String lang = LocaleContextHolder.getLocale().getLanguage();
    if ("zh".equals(lang)) {
      return isBlank(zh) ? ja : zh;
    }
    if ("en".equals(lang)) {
      return isBlank(en) ? ja : en;
    }
    // 日文及其它未覆盖语言：返回日文原名
    return ja;
  }

  /** 就地翻译上映场次标签列表（response 仍只用 name 字段）。 */
  public static void translateShowTimeTags(List<MovieShowTimeTag> list) {
    if (list == null) return;
    for (MovieShowTimeTag tag : list) {
      if (tag != null) {
        tag.setName(pickByLocale(tag.getName(), tag.getNameZh(), tag.getNameEn()));
      }
    }
  }

  /** 就地翻译电影标签列表（response.movie.Tags，仍只用 name 字段）。 */
  public static void translateMovieTags(List<Tags> list) {
    if (list == null) return;
    for (Tags tag : list) {
      if (tag != null) {
        tag.setName(pickByLocale(tag.getName(), tag.getNameZh(), tag.getNameEn()));
      }
    }
  }

  /** 取电影标签实体按当前语言的显示名（用于只需要名字字符串的场景）。 */
  public static String translateMovieTagName(MovieTag tag) {
    if (tag == null) return null;
    return pickByLocale(tag.getName(), tag.getNameZh(), tag.getNameEn());
  }

  private static boolean isBlank(String s) {
    return s == null || s.trim().isEmpty();
  }
}
