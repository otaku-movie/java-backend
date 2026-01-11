package com.example.backend.utils;

import com.example.backend.entity.Dict;
import com.example.backend.entity.DictItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 字典翻译工具类
 * 用于根据当前语言环境翻译字典和字典项的 name 字段
 */
@Component
public class DictUtils {

    /**
     * 翻译字典的 name 字段
     * 
     * @param dict 字典对象
     * @return 翻译后的 name，如果找不到翻译则返回原始 name
     */
    public static String translateDictName(Dict dict) {
        if (dict == null || dict.getCode() == null) {
            return dict != null ? dict.getName() : null;
        }
        
        String key = "dict." + dict.getCode() + ".name";
        // 使用 getMessage(code, defaultMessage) 方法，如果找不到翻译则返回原始 name
        return MessageUtils.getMessage(key, dict.getName());
    }

    /**
     * 翻译字典项的 name 字段
     * 
     * @param dictItem 字典项对象
     * @param dictCode 字典 code（用于构建翻译 key）
     * @return 翻译后的 name，如果找不到翻译则返回原始 name
     */
    public static String translateDictItemName(DictItem dictItem, String dictCode) {
        if (dictItem == null || dictCode == null || dictItem.getCode() == null) {
            return dictItem != null ? dictItem.getName() : null;
        }
        
        String key = "dict." + dictCode + ".item." + dictItem.getCode();
        // 使用 getMessage(code, defaultMessage) 方法，如果找不到翻译则返回原始 name
        return MessageUtils.getMessage(key, dictItem.getName());
    }

    /**
     * 翻译字典对象（修改原始对象的 name 字段）
     * 
     * @param dict 字典对象
     */
    public static void translateDict(Dict dict) {
        if (dict != null && dict.getCode() != null) {
            dict.setName(translateDictName(dict));
        }
    }

    /**
     * 翻译字典项对象（修改原始对象的 name 字段）
     * 
     * @param dictItem 字典项对象
     * @param dictCode 字典 code
     */
    public static void translateDictItem(DictItem dictItem, String dictCode) {
        if (dictItem != null && dictCode != null) {
            dictItem.setName(translateDictItemName(dictItem, dictCode));
        }
    }

    /**
     * 翻译字典列表
     * 
     * @param dictList 字典列表
     */
    public static void translateDictList(List<Dict> dictList) {
        if (dictList != null) {
            dictList.forEach(DictUtils::translateDict);
        }
    }

    /**
     * 翻译字典项列表
     * 
     * @param dictItemList 字典项列表
     * @param dictCode 字典 code
     */
    public static void translateDictItemList(List<DictItem> dictItemList, String dictCode) {
        if (dictItemList != null && dictCode != null) {
            dictItemList.forEach(item -> translateDictItem(item, dictCode));
        }
    }

    /**
     * 翻译字典 Map（key 为 dict code，value 为 dict item 列表）
     * 
     * @param dictItemMap 字典项 Map
     */
    public static void translateDictItemMap(Map<String, List<DictItem>> dictItemMap) {
        if (dictItemMap != null) {
            dictItemMap.forEach((dictCode, itemList) -> {
                translateDictItemList(itemList, dictCode);
            });
        }
    }
}
