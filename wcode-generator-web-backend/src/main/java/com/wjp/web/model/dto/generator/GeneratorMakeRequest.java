package com.wjp.web.model.dto.generator;

import com.wjp.maker.meta.Meta;
import lombok.Data;

import java.io.Serializable;

/**
 * 制作代码生成器请求
 *
 * @author wjp
 */
@Data
public class GeneratorMakeRequest implements Serializable {

    /**
     * 元信息
     */
    private Meta meta;

    /**
     * 模板文件压缩路径
     */
    private String zipFilePath;


    private static final long serialVersionUID = 1L;
}