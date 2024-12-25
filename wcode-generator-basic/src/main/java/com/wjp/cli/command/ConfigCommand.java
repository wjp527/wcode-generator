package com.wjp.cli.command;

import cn.hutool.core.util.ReflectUtil;
import com.wjp.model.MainTemplateConfig;
import picocli.CommandLine;

import java.lang.reflect.Field;

/**
 * 配置子命令
 */
// 交互式输入就 Callable
// 非交互式输入就 Runnable
@CommandLine.Command(name = "config", description = "查看配置信息")
public class ConfigCommand implements Runnable{
    @Override
    public void run() {
        // 获取 MainTemplateConfig.class 类的所有字段
        Field[] fields = ReflectUtil.getFields(MainTemplateConfig.class);
        for (Field field : fields) {
            System.out.println("字段名称" + field.getName());
            System.out.println("字段类型" + field.getType());
        }
    }
}
