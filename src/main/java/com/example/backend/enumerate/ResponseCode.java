package com.example.backend.enumerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 响应状态码枚举
 * 
 * <p>状态码设计规则：</p>
 * <ul>
 *   <li>原有状态码保持不变（SUCCESS=1, ERROR=0, PARAMETER_ERROR=-1, LOGIN_EXPIRED=401, NOT_PERMISSION=403, REPEAT=-2）</li>
 *   <li>新增状态码使用新的编号体系，按业务模块组织：</li>
 *   <li>  2xxx: 参数/验证类错误</li>
 *   <li>  3xxx: 业务逻辑类错误（按模块划分）</li>
 *   <li>     - 3001-3099: 通用业务错误</li>
 *   <li>     - 3100-3199: 订单相关业务错误</li>
 *   <li>     - 3200-3299: 选座相关业务错误</li>
 *   <li>     - 3300-3399: 支付相关业务错误</li>
 *   <li>     - 3400-3499: 场次相关业务错误</li>
 *   <li>     - 3500-3599: 票种相关业务错误</li>
 *   <li>     - 3600-3699: 信用卡相关业务错误</li>
 *   <li>  5xxx: 资源类错误（按模块划分）</li>
 *   <li>     - 5001-5099: 通用资源错误</li>
 *   <li>     - 5100-5199: 订单资源错误</li>
 *   <li>     - 5200-5299: 选座资源错误</li>
 *   <li>     - 5300-5399: 支付资源错误</li>
 *   <li>     - 5400-5499: 场次资源错误</li>
 *   <li>     - 5500-5599: 票种资源错误</li>
 *   <li>     - 5600-5699: 信用卡资源错误</li>
 *   <li>  6xxx: 系统类错误</li>
 * </ul>
 */
@Getter
@RequiredArgsConstructor
public enum ResponseCode {
  // ==================== 原有状态码（保持不变） ====================
  /** 操作成功 */
  SUCCESS(1),
  /** 通用错误 */
  ERROR(0),
  /** 参数错误 */
  PARAMETER_ERROR(-1),
  /** 登录已过期 */
  LOGIN_EXPIRED(401),
  /** 没有权限 */
  NOT_PERMISSION(403),
  /** 数据已存在 */
  REPEAT(-2),
  
  // ==================== 参数/验证类错误 (2xxx) ====================
  /** 参数缺失 */
  PARAMETER_MISSING(2002),
  /** 参数格式错误 */
  PARAMETER_FORMAT_ERROR(2003),
  /** 参数验证失败 */
  PARAMETER_VALIDATION_ERROR(2004),
  
  // ==================== 业务逻辑类错误 (3xxx) ====================
  
  // --- 通用业务错误 (3001-3099) ---
  /** 通用业务错误 */
  BUSINESS_ERROR(3001),
  /** 数据冲突 */
  DATA_CONFLICT(3002),
  /** 操作不允许 */
  OPERATION_NOT_ALLOWED(3003),
  /** 状态不正确 */
  INVALID_STATE(3004),
  /** 资源已被占用 */
  RESOURCE_OCCUPIED(3005),
  /** 操作超时 */
  OPERATION_TIMEOUT(3006),
  /** 重复操作 */
  DUPLICATE_OPERATION(3007),
  /** 请求过于频繁，限流 */
  RATE_LIMIT_EXCEEDED(3008),
  
  // --- 订单相关业务错误 (3100-3199) ---
  /** 订单正在创建中 */
  ORDER_CREATING(3101),
  /** 订单已支付，无法重复支付 */
  ORDER_ALREADY_PAID(3102),
  /** 订单已取消，无法操作 */
  ORDER_ALREADY_CANCELED(3103),
  /** 订单已超时 */
  ORDER_TIMEOUT(3104),
  /** 订单状态不允许此操作 */
  ORDER_STATE_INVALID(3105),
  /** 订单创建失败 */
  ORDER_CREATE_FAILED(3106),
  
  // --- 选座相关业务错误 (3200-3299) ---
  /** 选座冲突（座位已被其他用户选择） */
  SEAT_CONFLICT(3201),
  /** 选座请求正在处理中 */
  SEAT_PROCESSING(3202),
  /** 座位已被其他用户选择或锁定 */
  SEAT_OCCUPIED(3203),
  /** 选座保存失败 */
  SEAT_SAVE_FAILED(3204),
  /** 当前没有有效的选座记录 */
  SEAT_NOT_SELECTED(3205),
  /** 座位已失效或未选中 */
  SEAT_INVALID_OR_NOT_SELECTED(3206),
  /** 座位坐标不匹配 */
  SEAT_COORDINATE_MISMATCH(3207),
  
  // --- 支付相关业务错误 (3300-3399) ---
  /** 支付方式不支持 */
  PAYMENT_METHOD_NOT_SUPPORTED(3301),
  /** 支付金额不匹配 */
  PAYMENT_AMOUNT_MISMATCH(3302),
  /** 支付失败 */
  PAYMENT_FAILED(3303),
  /** 订单正在支付中，请勿重复提交 */
  PAYMENT_IN_PROGRESS(3304),
  
  // --- 场次相关业务错误 (3400-3499) ---
  /** 场次已开始，无法操作 */
  SHOWTIME_ALREADY_STARTED(3401),
  /** 场次已结束 */
  SHOWTIME_ALREADY_ENDED(3402),
  /** 场次已售罄 */
  SHOWTIME_SOLD_OUT(3403),
  
  // --- 票种相关业务错误 (3500-3599) ---
  /** 票种已停用 */
  TICKET_TYPE_DISABLED(3501),
  /** 票种库存不足 */
  TICKET_TYPE_OUT_OF_STOCK(3502),
  
  // --- 信用卡相关业务错误 (3600-3699) ---
  /** 信用卡信息无效 */
  CREDIT_CARD_INVALID(3601),
  /** 信用卡已过期 */
  CREDIT_CARD_EXPIRED(3602),
  /** 信用卡余额不足 */
  CREDIT_CARD_INSUFFICIENT_BALANCE(3603),
  
  // ==================== 资源类错误 (5xxx) ====================
  
  // --- 通用资源错误 (5001-5099) ---
  /** 资源不存在 */
  RESOURCE_NOT_FOUND(5001),
  /** 资源已存在 */
  RESOURCE_ALREADY_EXISTS(5002),
  /** 用户不存在 */
  USER_NOT_FOUND(5003),
  
  // --- 订单资源错误 (5100-5199) ---
  /** 订单不存在 */
  ORDER_NOT_FOUND(5101),
  /** 订单已存在（重复创建） */
  ORDER_ALREADY_EXISTS(5102),
  
  // --- 选座资源错误 (5200-5299) ---
  /** 座位不存在 */
  SEAT_NOT_FOUND(5201),
  /** 座位已存在 */
  SEAT_ALREADY_EXISTS(5202),
  /** 选座记录不存在 */
  SELECT_SEAT_NOT_FOUND(5203),
  
  // --- 支付资源错误 (5300-5399) ---
  /** 支付方式不存在 */
  PAYMENT_METHOD_NOT_FOUND(5301),
  /** 支付记录不存在 */
  PAYMENT_RECORD_NOT_FOUND(5302),
  
  // --- 场次资源错误 (5400-5499) ---
  /** 场次不存在 */
  SHOWTIME_NOT_FOUND(5401),
  /** 场次已存在 */
  SHOWTIME_ALREADY_EXISTS(5402),
  
  // --- 票种资源错误 (5500-5599) ---
  /** 票种不存在 */
  TICKET_TYPE_NOT_FOUND(5501),
  /** 票种已存在 */
  TICKET_TYPE_ALREADY_EXISTS(5502),
  
  // --- 信用卡资源错误 (5600-5699) ---
  /** 信用卡不存在 */
  CREDIT_CARD_NOT_FOUND(5601),
  /** 信用卡已存在 */
  CREDIT_CARD_ALREADY_EXISTS(5602),
  
  // ==================== 系统类错误 (6xxx) ====================
  /** 系统错误 */
  SYSTEM_ERROR(6001),
  /** 数据库错误 */
  DATABASE_ERROR(6002),
  /** 外部服务错误 */
  EXTERNAL_SERVICE_ERROR(6003),
  /** 网络错误 */
  NETWORK_ERROR(6004);

  private final int code;
}