package com.example.backend.entity;
//
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
//import com.fasterxml.jackson.core.json.JsonWriteFeature;
//import com.fasterxml.jackson.databind.util.JSONPObject;
import com.example.backend.constants.MessageKeys;
import com.example.backend.utils.MessageUtils;
import lombok.Data;

@Data
class Pagination<T> {
  private Integer page;
  private long total;
  private  Integer pageSize;
  private T list;

  // 默认构造函数，设置默认值
  public Pagination() {
    this.page = 1; // 默认页数为1
    this.total = 0; // 默认总数为0
    this.pageSize = 10; // 默认页面大小为10
    this.list = null; // 默认列表为空
  }

  public Pagination(T list, long total, int page, int pageSize) {
    this.total = total;
    this.page = page;
    this.pageSize = pageSize;
    this.list = list;
  }
}

@Data
public class RestBean<T> {
  int code;
  T data;
  String message;

  // 带参数的构造函数
  private RestBean (int code, T data, String message) {
    this.code = code;
    this.data = data;
    this.message = message;
  }


  @SuppressWarnings("unchecked")
  public static <T> RestBean<T> success(T data, int page, long total, int pageSize) {
    return (RestBean<T>) new RestBean<>(200, new Pagination<>(data, total, page, pageSize), MessageUtils.getMessage(MessageKeys.Success.GET));
  }

  public static <T> RestBean<T> success(T data, String message) {
    return new RestBean<>(200, data, message);
  }

  public static <T> RestBean<T> error(int code, String message ) {
    return new RestBean<>(code, null, message);
  }


  public String  asJsonString() {
    return JSONObject
      .from(this, JSONWriter.Feature.WriteNulls)
      .toString();
  }

}


// java 17
//public record RestBean<T>(int code, T data, String message) {
//     class RestPage<T> (int page, int total, int pageSize) {
//        this.page = page;
//        this.total = total;
//        this.pageSize = pageSize;
//        this.list = data;
//    }
//
//    public static <T> RestBean<T> successPagination(T data, int page, int total, int pageSize) {
//
//        return new RestBean<>(200, {
//            page = page,
//            total = total,
//            pageSize = pageSize,
//            list = data
//        }, MessageUtils.getMessage(SUCCESS_GET));
//    }
//    public static <T> RestBean<T> success(T data) {
//        return new RestBean<>(200, data, MessageUtils.getMessage(SUCCESS_GET));
//    }
//
//    public static <T> RestBean<T> failure(int code, String message) {
//
//        return new RestBean<>(code, null, message);
//    }
//    public static <T> RestBean<T> failure(int code) {
//
//        return new RestBean<>(code, null, "请求失败");
//    }
//    public String  asJsonString() {
//        return JSONObject
//                .from(this, JSONWriter.Feature.WriteNulls)
//            .toString();
//    }
//}