package com.wjp.maker.template.model;

import lombok.Data;

/**
 * 输出配置
 * 控制分组去重
 */
@Data
public class TemplateMakerOutputConfig {

    // 从未分组的文件中移除组内的同名文件
    private boolean removeGroupFilesFromRoot = true;

}
