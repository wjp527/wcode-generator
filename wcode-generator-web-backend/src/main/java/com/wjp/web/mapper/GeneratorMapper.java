package com.wjp.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wjp.web.model.entity.Generator;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author wjp
 * @description 针对表【generator(代码生成器)】的数据库操作Mapper
 * @createDate 2025-01-06 10:05:38
 * @Entity com.wjp.web.model.entity.Generator
 */
public interface GeneratorMapper extends BaseMapper<Generator> {

    /**
     * 查询已删除的生成器
     * @return
     */
    @Select("SELECT id, distPath from generator WHERE isDelete = 1")
    List<Generator> listDeleteGenerator();

}




