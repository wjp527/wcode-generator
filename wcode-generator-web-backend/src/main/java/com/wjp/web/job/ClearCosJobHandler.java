package com.wjp.web.job;

import cn.hutool.core.util.StrUtil;
import com.wjp.web.manager.CosManager;
import com.wjp.web.mapper.GeneratorMapper;
import com.wjp.web.model.entity.Generator;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 清理cos任务处理器 任务调度
 * @author wjp
 */
@Component
@Slf4j
public class ClearCosJobHandler {

    @Resource
    private CosManager cosManager;

    @Resource
    private GeneratorMapper generatorMapper;

    /**
     * 1、简单任务示例（Bean模式）
     */
    @XxlJob("clearCosJobHandler")
    public void clearCosJobHandler() throws Exception {
        log.info("clearCosJobHandler start");

        // 编写业务逻辑
        // 1.包括用户上传的模版制作文件(generator_make_template/
        cosManager.deleteDir("/generator_make_template/");
        // 模型配置文件 json
        cosManager.deleteDir("/generator_to_lead_by_model_template/");
        // 文件配置文件 json
        cosManager.deleteDir("/generator_to_lead_by_file_template/");

        // 2.删除数据库表中 generator表中已经被删除的dist文件
        List<Generator> generatorList = generatorMapper.listDeleteGenerator();
        // 获取dist文件路径【集合】
        List<String> collect = generatorList.stream()
                // 获取到distPath
                .map(Generator::getDistPath)
                // 过滤掉空字符串
                .filter(StrUtil::isNotBlank)
                // 去掉"/"
                .map(distPath -> distPath.substring(1))
                // 整合数据
                .collect(Collectors.toList());

        if(collect.size() == 0) {

        } else {
            cosManager.deleteObjects(collect);
        }

        log.info("clearCosJobHandler end");
    }
}
