package com.wjp.web.model.dto.generator;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 使用代码生成器请求
 *
 * @author wjp
 */
@Data
public class GeneratorToLeadRequest implements Serializable {

    /**
     * 导入的模版文件路径
     */
    public String key;

    /**
     * 模版的类型【模型配置 / 文件配置】
     */

    public String type;


    private static final long serialVersionUID = 1L;
}