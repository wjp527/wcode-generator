package com.wjp.generator;

import com.wjp.model.MainTemplateConfig;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

public class MainGenerator {
    public static void main(String[] args) throws TemplateException, IOException {

        // 创建数据模型

        MainTemplateConfig mainTemplateConfig = new MainTemplateConfig();
        mainTemplateConfig.setAuthor("wjp");
        mainTemplateConfig.setOutputText("sum");
        mainTemplateConfig.setLoop(true);
        doGenerate(mainTemplateConfig);
    }

    public static void doGenerate(Object model) throws TemplateException, IOException {
        // 1. 生成静态文件
        // 生成最外层的项目根目录: D:\fullStack\wcode-generator
        String projectPath = System.getProperty("user.dir");

        File parentFile = new File(projectPath).getParentFile();

        // 输入路径【静态模板文件路径】: D:\fullStack\wcode-generator\wcode-generator-demo-projects\acm-template
        // File.separator: 文件分隔符，在不同系统中可能不同，比如在Windows系统中是"\"，在Linux系统中是"/"

        String inputPath = new File(parentFile, "wcode-generator-demo-projects/acm-template").getAbsolutePath();
        // 输出路径【静态模版生成文件路径】
        String outputPath = projectPath;

        // 复制
        StaticGenerator.copyFilesHuttol(inputPath, outputPath);

        // 2. 生成动态文件
        // 这里是项目根目录: D:\fullStack\wcode-generator,而我们需要进入的是 wcode-generator-basic 这个项目里
        // 输入路径【动态模板文件路径】: D:\fullStack\wcode-generator\wcode-generator-basic\src/main/resources/templates/MainTemplate.java.ftl
        String dynamicInputPath = projectPath + File.separator + "src/main/resources/templates/MainTemplate.java.ftl";

        // 输出路径【动态模版生成文件路径】: D:\fullStack\wcode-generator\wcode-generator-basic\MainTemplate.java
        String dynamicOutputPath = projectPath + File.separator + "acm-template/src/com/yupi/acm/MainTemplate.java";


        DynamicGenerator.doGenerate(dynamicInputPath, dynamicOutputPath, model);
    }
}
