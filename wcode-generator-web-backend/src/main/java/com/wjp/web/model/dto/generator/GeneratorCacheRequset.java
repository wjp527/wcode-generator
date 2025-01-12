package com.wjp.web.model.dto.generator;

import com.wjp.maker.meta.Meta;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 缓存代码生成器请求
 *
 * @author <a href="https://github.com/liwjp">程序员鱼皮</a>
 * @from <a href="https://wjp.icu">编程导航知识星球</a>
 */
@Data
public class GeneratorCacheRequset implements Serializable {

    /**
     * 生成器的 id
     */
    private Long id;


    private static final long serialVersionUID = 1L;
}