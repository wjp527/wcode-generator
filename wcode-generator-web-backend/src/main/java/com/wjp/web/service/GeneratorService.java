package com.wjp.web.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wjp.web.model.dto.generator.GeneratorQueryRequest;
import com.wjp.web.model.entity.Generator;
import com.wjp.web.model.entity.User;
import com.wjp.web.model.vo.GeneratorVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 帖子服务
 *
 * @author <a href="https://github.com/liwjp">程序员鱼皮</a>
 * @from <a href="https://wjp.icu">编程导航知识星球</a>
 */
public interface GeneratorService extends IService<Generator> {

    /**
     * 校验
     *
     * @param generator
     * @param add
     */
    void validGenerator(Generator generator, boolean add);

    /**
     * 获取查询条件
     *
     * @param generatorQueryRequest
     * @return
     */
    QueryWrapper<Generator> getQueryWrapper(GeneratorQueryRequest generatorQueryRequest);


    /**
     * 获取帖子封装
     *
     * @param generator
     * @param request
     * @return
     */
    GeneratorVO getGeneratorVO(Generator generator, HttpServletRequest request);

    /**
     * 分页获取帖子封装
     *
     * @param generatorPage
     * @param request
     * @return
     */
    Page<GeneratorVO> getGeneratorVOPage(Page<Generator> generatorPage, HttpServletRequest request);



    /**
     * 匹配用户
     * @param num
     * @param loginUser
     * @return
     */
    List<Generator> matchGenerators(long num, User loginUser);

    /**
     * 获取当前用户(脱敏信息)
     * @param originGenerator
     */
    Generator getSafetyGenerator(Generator originGenerator);
}
