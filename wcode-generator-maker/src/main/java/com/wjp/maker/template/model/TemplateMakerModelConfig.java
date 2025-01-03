package com.wjp.maker.template.model;

import com.wjp.maker.meta.Meta;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 文件过滤配置
 */
@Data
public class TemplateMakerModelConfig {
    private List<ModelInfoConfig> models;

    @NoArgsConstructor
    @Data
    public static class ModelInfoConfig {

        private String fieldName;

        private String type;

        private String description;

        private Object defaultValue;

        private String abbr;

        // 用于替换哪些文本
        private String replaceText;
    }

    //{
    //  "models": [
    //    {
    //      "path": "文件（目录）路径",
    //      过滤条件
    //      "filters": [
    //        {
    //          "range": "modelName",
    //          "rule": "regex",
    //          "value": ".*lala.*"
    //        },
    //        {
    //          "range": "modelContent",
    //          "rule": "contains",
    //          "value": "haha"
    //        }
    //      ]
    //    }
    //  ],
    //}

    // 文件分组配置
    private ModelGroupConfig modelGroupConfig;

    /**
     * 模型分组配置
     */
    @Data
    public static class ModelGroupConfig {
        // 条件【用于判断单个文件、多个文件是否需要生成】
        private String condition;
        // 分组id
        private String groupKey;
        // 分组名称
        private String groupName;

        private String type;

        private String description;
    }


}
