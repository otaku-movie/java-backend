package com.example.backend.service;

import com.example.backend.entity.MovieTicketType;

/**
 * movie_ticket_type 票种语义字段常量（与爬虫 import:prices 对齐）。
 */
public final class TicketTypeSemantics {

  private TicketTypeSemantics() {}

  public static final String PRICE_KIND_BASE = "base";
  public static final String PRICE_KIND_PROMO = "promo";
  /** 特殊人群/套票等，不参与场次列表公众优惠价。 */
  public static final String PRICE_KIND_SPECIAL = "special";

  public static final String AUDIENCE_ADULT = "adult";
  public static final String AUDIENCE_MEMBER = "member";

  public static boolean isMemberRequired(MovieTicketType t) {
    if (t == null) {
      return false;
    }
    if (Boolean.TRUE.equals(t.getMemberRequired())) {
      return true;
    }
    if (AUDIENCE_MEMBER.equalsIgnoreCase(nullToEmpty(t.getAudienceCategory()))) {
      return true;
    }
    String code = t.getServiceDayCode();
    return code != null && code.endsWith("_member");
  }

  /** 常设基准票种（非会员、price_kind=base 或历史无标记时的 schedule 为空）。 */
  public static boolean isBaseKind(MovieTicketType t) {
    if (t == null) {
      return false;
    }
    if (isMemberRequired(t)) {
      return false;
    }
    String kind = t.getPriceKind();
    if (PRICE_KIND_BASE.equalsIgnoreCase(kind)) {
      return true;
    }
    if (kind == null || kind.isBlank()) {
      return t.getScheduleType() == null;
    }
    return false;
  }

  /** 公众参考价候选：常设 + 成人档（无 audience 时兼容旧数据）。 */
  public static boolean isAdultReferenceCandidate(MovieTicketType t) {
    if (!isBaseKind(t)) {
      return false;
    }
    String cat = t.getAudienceCategory();
    if (cat == null || cat.isBlank()) {
      return true;
    }
    return AUDIENCE_ADULT.equalsIgnoreCase(cat);
  }

  /** 非会员折扣/服务日票种（不含特殊人群价）。 */
  public static boolean isPublicPromo(MovieTicketType t) {
    if (t == null || isMemberRequired(t)) {
      return false;
    }
    String kind = nullToEmpty(t.getPriceKind());
    if (PRICE_KIND_SPECIAL.equalsIgnoreCase(kind)) {
      return false;
    }
    if (PRICE_KIND_PROMO.equalsIgnoreCase(kind)) {
      return true;
    }
    return t.getPriceKind() == null && t.getScheduleType() != null;
  }

  /**
   * 场次列表「¥X 起」用的公众优惠价：成人 + 真实服务日，排除障害者/シニア/套票等。
   */
  public static boolean isAdultPublicPromo(MovieTicketType t) {
    if (!isPublicPromo(t)) {
      return false;
    }
    if (!isAdultAudience(t)) {
      return false;
    }
    if (isRestrictedAudience(t)) {
      return false;
    }
    return !isNonListPromoServiceDay(t.getServiceDayCode());
  }

  private static boolean isAdultAudience(MovieTicketType t) {
    String cat = t.getAudienceCategory();
    if (cat == null || cat.isBlank()) {
      return true;
    }
    return AUDIENCE_ADULT.equalsIgnoreCase(cat);
  }

  private static boolean isRestrictedAudience(MovieTicketType t) {
    return switch (nullToEmpty(t.getAudienceCategory())) {
      case "disability", "senior", "child_3plus", "junior_school", "high_school", "college" -> true;
      default -> false;
    };
  }

  /**
   * 日期条服务日标签：水曜・会员木曜等（按日历匹配，不含レイト等时段优惠、障害者割引）。
   */
  public static boolean isCalendarServiceDay(MovieTicketType t) {
    if (t == null || isTimeSlotPromo(t)) {
      return false;
    }
    if (isNonListPromoServiceDay(t.getServiceDayCode())) {
      return false;
    }
    if (PRICE_KIND_SPECIAL.equalsIgnoreCase(nullToEmpty(t.getPriceKind()))) {
      return false;
    }
    if (isMemberRequired(t)) {
      return hasCalendarMarker(t);
    }
    return isAdultPublicPromo(t);
  }

  private static boolean isTimeSlotPromo(MovieTicketType t) {
    Integer st = t.getScheduleType();
    return st != null && st == 3;
  }

  private static boolean hasCalendarMarker(MovieTicketType t) {
    if (t.getServiceDayCode() != null && !t.getServiceDayCode().isBlank()) {
      return true;
    }
    Integer st = t.getScheduleType();
    return st != null && st != 3;
  }

  /** 条件复杂、非「服务日」的 code，不应进入列表预览最低价。 */
  private static boolean isNonListPromoServiceDay(String code) {
    if (code == null || code.isBlank()) {
      return false;
    }
    return switch (code.toLowerCase()) {
      case "other", "couple_50", "pair_50" -> true;
      default -> false;
    };
  }

  private static String nullToEmpty(String s) {
    return s == null ? "" : s;
  }
}
