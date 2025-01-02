package com.wjp.maker.template.model.enums;

import cn.hutool.core.util.ObjectUtil;

/**
 * 文件过滤范围枚举
 */
public enum FileFilterRangeEnum {

    FILE_NAME("文件名称", "fileName"),
    FILE_CONTENT("文件内容", "fileContent");

    private final String text;
    private final String value;

    FileFilterRangeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public String getValue() {
        return value;
    }

    /**
     * 根据 value 获取枚举
     * @param value
     * @return
     */
    public static FileFilterRangeEnum getEnumByValue(String value) {
        // 空值返回 null
        if(ObjectUtil.isEmpty(value)) {
            return null;
        }

        // 循环遍历枚举
        for (FileFilterRangeEnum anEnum : FileFilterRangeEnum.values()) {
            if(anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }


}
