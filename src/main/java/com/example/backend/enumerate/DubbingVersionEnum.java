package com.example.backend.enumerate;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 配音版本枚举
 */
@Getter
public enum DubbingVersionEnum implements IEnum<Integer> {
    /**
     * 原版
     */
    ORIGINAL(1, "原版"),
    
    /**
     * 配音版
     */
    DUBBED(2, "配音版");

    @JsonValue
    private final Integer code;
    private final String description;

    DubbingVersionEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public Integer getValue() {
        return this.code;
    }

    /**
     * 根据 code 获取枚举
     */
    public static DubbingVersionEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DubbingVersionEnum e : DubbingVersionEnum.values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

    /**
     * 验证 code 是否有效
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }
}
