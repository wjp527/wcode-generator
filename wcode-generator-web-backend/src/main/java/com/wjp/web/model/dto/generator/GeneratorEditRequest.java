package com.wjp.web.model.dto.generator;

import com.wjp.maker.meta.Meta;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 编辑请求
 * 这个Query 和 Update 区别就在于，
 * Query 是 普通用户修改的
 * Update 是 管理员进行修改信息的
 *
 * @author <a href="https://github.com/liwjp">程序员鱼皮</a>
 * @from <a href="https://wjp.icu">编程导航知识星球</a>
 */
@Data
public class GeneratorEditRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 基础包
     */
    private String basePackage;

    /**
     * 版本
     */
    private String version;

    /**
     * 作者
     */
    private String author;

    /**
     * 标签列表（json 数组）
     */
    private List<String> tags;

    /**
     * 图片
     */
    private String picture;

    /**
     * 文件配置（json字符串）
     */
    private Meta.FileConfig fileConfig;

    /**
     * 模型配置（json字符串）
     */
    private Meta.ModelConfig modelConfig;

    /**
     * 代码生成器产物路径
     */
    private String distPath;

    /**
     * 下载数
     */
//    private Long downLoadNum;

    // 这里 状态 ，普通用户是修改不了的，得是管理员才能进行修改
    /**
     * 状态
     */
//    private Integer status;

    private static final long serialVersionUID = 1L;
}