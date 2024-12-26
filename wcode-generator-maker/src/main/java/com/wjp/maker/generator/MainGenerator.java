package com.wjp.maker.generator;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.util.StrUtil;
import com.wjp.maker.generator.file.DynamicFileGenerator;
import com.wjp.maker.meta.Meta;
import com.wjp.maker.meta.MetaManager;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

public class MainGenerator {
    public static void main(String[] args) throws TemplateException, IOException, InterruptedException {
        Meta meta = MetaManager.getMetaObject();
        System.out.println(meta.toString());


        // 输出项目根路径
        String projectPath = System.getProperty("user.dir");
        // 生成的模板项目的路径
        String outputPath = projectPath + File.separator + "generated" + File.separator + meta.getName();
        // 创建输出路径
        if(!FileUtil.exist(outputPath)) {
            FileUtil.mkdir(outputPath);
        }

        // 获取resource目录
        ClassPathResource classPathResource = new ClassPathResource("");
        // 获取绝对路径
        String inputResourcePath = classPathResource.getAbsolutePath();


        // Java包基础的路径
        // com.wjp
        String outputBasePackage = meta.getBasePackage();
        // com/wjp
        String outputBasePackagePath = StrUtil.join("/", StrUtil.split(outputBasePackage, "."));
        // 输出路径
        String outputBaseJavaPackagePath = outputPath + File.separator + "src/main/java/" + outputBasePackagePath;


        String inputFilePath;
        String outputFilePath;

        // model.DataModel
        // 动态模板的路径: D:/fullStack/wcode-generator/wcode-generator-maker/target/classes/\templates/java/model/DataModel.java.ftl
        inputFilePath = inputResourcePath + File.separator +  "templates/java/model/DataModel.java.ftl";
        // 通过FreeMarker将动态模板转为静态模板，并放到该位置上: D:\fullStack\wcode-generator\wcode-generator-maker\generated\acm-template-pro-generator\src/main/java/com/wjp\model/DataModel.java
        outputFilePath = outputBaseJavaPackagePath + File.separator +  "model/DataModel.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // 动态生成config子命令
        inputFilePath = inputResourcePath + "templates/java/cli/command/ConfigCommand.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + File.separator +  "cli/command/ConfigCommand.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // 动态生成generate子命令
        inputFilePath = inputResourcePath + "templates/java/cli/command/GenerateCommand.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + File.separator +  "cli/command/GenerateCommand.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // 动态生成list子命令
        inputFilePath = inputResourcePath + "templates/java/cli/command/ListCommand.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + File.separator +  "cli/command/ListCommand.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // 动态生成CommandExecutor
        inputFilePath = inputResourcePath + "templates/java/cli/CommandExecutor.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + File.separator +  "cli/CommandExecutor.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // 动态生成Main
        inputFilePath = inputResourcePath + "templates/java/Main.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + File.separator +  "Main.java";
        // 动态生成项目
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // 动态生成DynamicGenerator.java.ftl
        inputFilePath = inputResourcePath + "templates/java/generator/DynamicGenerator.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + File.separator +  "generator/DynamicGenerator.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // 动态生成 MainGenerator.java.ftl
        inputFilePath = inputResourcePath + "templates/java/generator/MainGenerator.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + File.separator +  "generator/MainGenerator.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // 动态生成 StaticGenerator.java.ftl
        inputFilePath = inputResourcePath + "templates/java/generator/StaticGenerator.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + File.separator +  "generator/StaticGenerator.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);


        // 动态生成 pom.xml.ftl
        inputFilePath = inputResourcePath + "templates/pom.xml.ftl";
        outputFilePath = outputPath + File.separator +  "pom.xml";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // 构建jar包
        JarGenerator.doGenerate(outputPath);

        // 封装脚本
        String shellOutputPath = outputPath + File.separator + "generator";
//        acm-template-pro-generator-1.0-SNAPSHOT-jar-with-dependencies.jar
        String jarName = String.format("%s-%s-jar-with-dependencies.jar", meta.getName(), meta.getVersion());
        String jarPath = "target/" + jarName;
        ScriptGenerator.doGenerate(shellOutputPath, jarPath);



    }
}
