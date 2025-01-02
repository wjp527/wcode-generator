package com.wjp.maker.template.model;

import com.wjp.maker.meta.Meta;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 文件过滤配置
 */
@Data
public class TemplateMakerFileConfig {
    private List<FileInfoConfig> files;

    @NoArgsConstructor
    @Data
    public static class FileInfoConfig {
        // 文件（目录）路径【绝对路径】
        private String path;
        // 过滤条件
        private List<FileFilterConfig> fileConfigList;
    }

    //{
    //  "files": [
    //    {
    //      "path": "文件（目录）路径",
    //      过滤条件
    //      "filters": [
    //        {
    //          "range": "fileName",
    //          "rule": "regex",
    //          "value": ".*lala.*"
    //        },
    //        {
    //          "range": "fileContent",
    //          "rule": "contains",
    //          "value": "haha"
    //        }
    //      ]
    //    }
    //  ],
    //}

//    // 文件分组配置
//    private FileGroupConfig fileGroupConfig;
//
//    @Data
//    public static class FileGroupConfig {
//        // 条件【用于判断单个文件、多个文件是否需要生成】
//        private String condition;
//        // 分组id
//        private String groupKey;
//        // 分组名称
//        private String groupName;
//    }


}
