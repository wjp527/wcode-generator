package com.wjp.maker.template;

import cn.hutool.core.util.StrUtil;
import com.wjp.maker.meta.Meta;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 模板制作工具类
 */
public class TemplateMakerUtils {

    /**
     * 从未分组文件中移除组内的同名文件
     * @param fileInfoList
     * @return
     */
    public static List<Meta.FileConfig.FileInfo> removeGroupFilesFromRoot(List<Meta.FileConfig.FileInfo> fileInfoList) {

        // 一、先获取到所有分组
        List<Meta.FileConfig.FileInfo> groupFileInfoList = fileInfoList.stream()
                .filter(file -> StrUtil.isNotBlank(file.getGroupKey()))
                .collect(Collectors.toList());

        // 二、获取所有分组内的文件列表
        List<Meta.FileConfig.FileInfo> groupInnerFileInfoList = groupFileInfoList.stream()
                .flatMap(file -> file.getFiles().stream())
                .collect(Collectors.toList());


        // 三、获取所有分组内文件输入路径集合
        Set<String> fileInputPathSet = groupInnerFileInfoList.stream()
                .map(Meta.FileConfig.FileInfo::getInputPath)
                .collect(Collectors.toSet());


        // 四、移除所有名称在 set 中的外层文件
        List<Meta.FileConfig.FileInfo> resultFileInfoList = fileInfoList.stream()
                .filter(fileInfo -> !fileInputPathSet.contains(fileInfo.getInputPath()))
                .collect(Collectors.toList());


        return resultFileInfoList;


    }

}
