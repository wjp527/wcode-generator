package com.wjp.maker.template.model;

import com.wjp.maker.meta.Meta;
import lombok.Data;

/**
 * 项目制作配置
 */
@Data
public class TemplateMakerConfig {
    private Long id;

    /**
     * 新的元数据对象
     */
    private Meta meta = new Meta();

    /**
     * 原始项目路径
     */
    private String originProjectPath;

    /**
     * 模板制作过滤器配置
     */
    private TemplateMakerFileConfig fileConfig = new TemplateMakerFileConfig();

    /**
     * 模型信息 + 要搜索的字符串
     */
    private TemplateMakerModelConfig modelConfig = new TemplateMakerModelConfig();

    private TemplateMakerOutputConfig outputConfig = new TemplateMakerOutputConfig();
}
