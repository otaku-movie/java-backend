package com.example.backend.exception;

import com.example.backend.enumerate.ResponseCode;
import lombok.Getter;

import java.util.Map;

/**
 * 业务异常类
 * 支持状态码和国际化消息
 */
@Getter
public class BusinessException extends RuntimeException {
  private final ResponseCode responseCode;
  private final String messageKey;
  private final Object[] messageArgs;
  private final Map<String, Object> extraData;  // 额外数据，用于传递不可用座位等信息

  /**
   * 构造业务异常
   * 
   * @param responseCode 响应状态码
   * @param messageKey 国际化消息键
   * @param messageArgs 消息参数
   */
  public BusinessException(ResponseCode responseCode, String messageKey, Object... messageArgs) {
    super(messageKey); // 临时消息，实际会通过国际化获取
    this.responseCode = responseCode;
    this.messageKey = messageKey;
    this.messageArgs = messageArgs;
    this.extraData = null;
  }

  /**
   * 构造业务异常（带额外数据）
   * 
   * @param responseCode 响应状态码
   * @param messageKey 国际化消息键
   * @param extraData 额外数据
   * @param messageArgs 消息参数
   */
  public BusinessException(ResponseCode responseCode, String messageKey, Map<String, Object> extraData, Object... messageArgs) {
    super(messageKey);
    this.responseCode = responseCode;
    this.messageKey = messageKey;
    this.messageArgs = messageArgs;
    this.extraData = extraData;
  }

  /**
   * 构造业务异常（带原始异常）
   * 
   * @param responseCode 响应状态码
   * @param messageKey 国际化消息键
   * @param cause 原始异常
   * @param messageArgs 消息参数
   */
  public BusinessException(ResponseCode responseCode, String messageKey, Throwable cause, Object... messageArgs) {
    super(messageKey, cause);
    this.responseCode = responseCode;
    this.messageKey = messageKey;
    this.messageArgs = messageArgs;
    this.extraData = null;
  }
}
