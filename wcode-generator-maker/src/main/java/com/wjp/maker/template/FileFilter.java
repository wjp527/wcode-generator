package com.wjp.maker.template;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import com.wjp.maker.template.model.FileFilterConfig;
import com.wjp.maker.template.model.enums.FileFilterRangeEnum;
import com.wjp.maker.template.model.enums.FileFilterRuleEnum;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件过滤器
 */
public class FileFilter {

    /**
     * 单个文件过滤器
     *
     * @param fileFilterConfigList 过滤规则
     * @param file 当前文件
     * @return
     */
    public static boolean doSingleFileFilter(List<FileFilterConfig> fileFilterConfigList, File file) {
        // 获取文件名
        String fileName = file.getName();
        // 获取文件内容
        String fileContent = FileUtil.readUtf8String(file);

        System.out.println("fileFilterConfigList = " + fileFilterConfigList);
        // 所有过滤器校验结束后返回的结果
        boolean result = true;

        // 如果过滤条件为空，则直接返回true
        if (CollectionUtil.isEmpty(fileFilterConfigList)) {
            return true;
        }

        for (FileFilterConfig fileFilterConfig : fileFilterConfigList) {
            // 过滤范围
            String range = fileFilterConfig.getRange();
            // 过滤规则
            String rule = fileFilterConfig.getRule();
            // 过滤值
            String value = fileFilterConfig.getValue();

            System.out.println("range = "+range);
            System.out.println("rule = "+rule);
            System.out.println("value = "+value);
            // 过滤范围枚举
            // fileFilterRangeEnum = FILE_NAME
            FileFilterRangeEnum fileFilterRangeEnum = FileFilterRangeEnum.getEnumByValue(range);
            // 过滤范围不存在，则跳过
            if (fileFilterRangeEnum == null) {
                continue;
            }

            // 要过滤的原内容
            String content = fileName;
            // 这一步的操作，是为了在下面的switch中，不用再判断是fileName 还是 fileContent 了
            switch (fileFilterRangeEnum) {
                case FILE_NAME:
                    content = fileName;
                    break;
                case FILE_CONTENT:
                    content = fileContent;
                    continue;
                default:
                    break;
            }

            FileFilterRuleEnum fileFilterRuleEnum = FileFilterRuleEnum.getEnumByValue(rule);
            // 过滤规则不存在，则跳过
            if (fileFilterRuleEnum == null) {
                continue;
            }

            switch (fileFilterRuleEnum) {
                // 包含
                case CONTAINS:
                    result = content.contains(value);
                    break;
                // 前缀匹配
                case STARTS_WITH:
                    result = content.startsWith(value);
                    break;
                // 后缀匹配
                case ENDS_WITH:
                    result = content.endsWith(value);
                    break;
                // 正则匹配
                case REGEX:
                    result = content.matches(value);
                    break;
                // 等于
                case EQUALS:
                    result = content.equals(value);
                default:
                    break;

            }
            // 如果有一个过滤条件不通过，则返回false
            if (result == false) {
                return false;
            }

        }


        return true;

    }


    /**
     * 对某个或目录进行过滤，返回文件列表
     * @param filePath 文件的绝对路径
     * @param fileFilterConfigList 过滤规则
     * @return
     */
    public static List<File> doFilter(String filePath, List<FileFilterConfig> fileFilterConfigList) {

        filePath = filePath.replace("\\", "/");
        System.out.println("filePath++ " + filePath);
        // 根据路径获取所有文件，并且过滤掉该目录下的所有文件
        List<File> fileList = FileUtil.loopFiles(filePath);

        /**
         * 过滤文件列表
         */
        List<File> collect = fileList.stream()
                .filter(file -> doSingleFileFilter(fileFilterConfigList, file))
                .collect(Collectors.toList());


        System.out.println("collect = " + collect);
        // 返回过滤后的文件列表
        return collect;

    }

}
