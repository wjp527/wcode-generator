package com.yupi.web.meta;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class Meta {


    private String name;
    private String description;
    private String basePackage;
    private String version;
    private String author;
    private String createTime;
    private FileConfig fileConfig;
    private ModelConfig modelConfig;


    @NoArgsConstructor
    @Data
    public static class FileConfig {
        private String inputRootPath;
        private String outputRootPath;
        private String sourceRootPath;
        private String type;
        private List<FileInfo> files;

        @NoArgsConstructor
        @Data
        public static class FileInfo {
            private String inputPath;
            private String outputPath;
            private String type;
            private String generateType;
            // 条件【用于判断单个文件、多个文件是否需要生成】
            private String condition;
            // 分组id
            private String groupKey;
            // 分组名称
            private String groupName;
            // 分组文件
            private List<FileInfo> files;

        }
    }

    @NoArgsConstructor
    @Data
    public static class ModelConfig {
        private List<ModelInfo> models;

        @NoArgsConstructor
        @Data
        public static class ModelInfo {
            private String fieldName;
            private String type;
            private String description;
            private Object defaultValue;
            private String abbr;
            // 分组id
            private String groupKey;
            // 分组名称
            private String groupName;
            // 条件【用于判断单个文件、多个文件是否需要生成】
            private String condition;
            private List<ModelInfo> models;

            // 中间参数
            // 该分组下所有参数拼接字符串
            private String allArgsStr;
        }
    }
}
