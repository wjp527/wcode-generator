package com.wjp.web.model.vo;

import cn.hutool.json.JSONUtil;
import com.wjp.maker.meta.Meta;
import com.wjp.web.model.entity.Generator;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 帖子视图
 *
 * @author <a href="https://github.com/liwjp">程序员鱼皮</a>
 * @from <a href="https://wjp.icu">编程导航知识星球</a>
 */
@Data
public class GeneratorVO implements Serializable {

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
    // todo
    private String distPath;

    /**
     * 下载数
     */
    private Long downLoadNum;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建人信息
     */
    private UserVO user;


    /**
     * 包装类转对象 [对象 -> json字符串]
     *
     * @param generatorVO
     * @return
     */
    public static Generator voToObj(GeneratorVO generatorVO) {
        if (generatorVO == null) {
            return null;
        }
        Generator generator = new Generator();
        BeanUtils.copyProperties(generatorVO, generator);
        // tags标签
        List<String> tagList = generatorVO.getTags();
        generator.setTags(JSONUtil.toJsonStr(tagList));

        // 文件配置
        Meta.FileConfig fileConfig = generatorVO.getFileConfig();
        String fileConfigStr = JSONUtil.toJsonStr(fileConfig);
        generator.setFileConfig(fileConfigStr);

        // 模型配置
        Meta.ModelConfig modelConfig = generatorVO.getModelConfig();
        String modelConfigStr = JSONUtil.toJsonStr(modelConfig);
        generator.setModelConfig(modelConfigStr);

        return generator;
    }

    /**
     * 对象转包装类 [json字符串 -> 对象]
     *
     * @param generator
     * @return
     */
    public static GeneratorVO objToVo(Generator generator) {
        if (generator == null) {
            return null;
        }
        GeneratorVO generatorVO = new GeneratorVO();
        BeanUtils.copyProperties(generator, generatorVO);
        generatorVO.setTags(JSONUtil.toList(generator.getTags(), String.class));

        // 文件配置
        generatorVO.setFileConfig(JSONUtil.toBean(generator.getFileConfig(), Meta.FileConfig.class));
        // 模型配置
        generatorVO.setModelConfig(JSONUtil.toBean(generator.getModelConfig(), Meta.ModelConfig.class));

        return generatorVO;
    }
}
