package com.wjp.maker;

//import com.wjp.maker.cli.CommandExecutor;

import com.wjp.maker.generator.main.GenerateTemplate;
import com.wjp.maker.generator.main.MainGenerator;
import com.wjp.maker.generator.main.ZipGenerator;
import freemarker.template.TemplateException;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws TemplateException, IOException, InterruptedException {
        // 创建数据模型
//        GenerateTemplate generateTemplate = new MainGenerator();
        GenerateTemplate generateTemplate = new ZipGenerator();
        // 生成代码
        generateTemplate.doGenerate();
    }

//    public static void main(String[] args) {
        // 根据用户输入的参数，调用对应的命令执行器
//        args = new String[]{"generate", "-l", "-DataModel.java.ftl", "-o"};
        // 动态获取用户输入参数的参数类型
//        args = new String[]{"config"};
        // 获取用户的文件列表
//        args = new String[]{"list"};
//        CommandExecutor commandExecutor = new CommandExecutor();
//        commandExecutor.doExecute(args);
//    }
}
