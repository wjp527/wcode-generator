package com.wjp.maker.generator.main;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.util.StrUtil;
import com.wjp.maker.generator.JarGenerator;
import com.wjp.maker.generator.ScriptGenerator;
import com.wjp.maker.generator.file.DynamicFileGenerator;
import com.wjp.maker.meta.Meta;
import com.wjp.maker.meta.MetaManager;
import com.wjp.maker.model.DataModel;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

public class GenerateTemplate {
    public void doGenerate() throws TemplateException, IOException, InterruptedException {
        Meta meta = MetaManager.getMetaObject();
        DataModel model = new DataModel();
        System.out.println(meta.toString());


        // 输出项目根路径
        String projectPath = System.getProperty("user.dir");
        // 生成的模板项目的路径
        String outputPath = projectPath + File.separator + "generated" + File.separator + meta.getName();
        // 创建输出路径
        if (!FileUtil.exist(outputPath)) {
            FileUtil.mkdir(outputPath);
        }

        // 1. 从原始模版路径 复制到 生成的代码包 中
        String sourceCopyDestPath = copySource(meta, outputPath);

        // 2.生成代码
        generateCode(meta, model, outputPath);

        // 3.构建jar包
        String jarPath = buildJar(outputPath, meta);

        // 4.封装脚本
        String shellOutputPath = buildScript(outputPath, jarPath);


        // 5.生成精简版项目代码
        buildDist(outputPath, sourceCopyDestPath,shellOutputPath, jarPath);

    }

    protected void buildDist(String outputPath, String sourceCopyDestPath,String shellOutputPath, String jarPath) {
        String distOutputPath = outputPath + "-dist";
//        String distOutputPath = outputPath + "-dist";

        // 生成jar文件
        String targetAbsolutePath = distOutputPath + File.separator + "target";
        // 创建文件夹
        FileUtil.mkdir(targetAbsolutePath);
        String jarAbsolutePath = outputPath + File.separator + jarPath;
        // 复制jar文件到目标文件夹
        FileUtil.copy(jarAbsolutePath, targetAbsolutePath, true);

        // 复制脚本文件
        FileUtil.copy(shellOutputPath + ".bat", distOutputPath, true);

        // 复制源模版文件
        FileUtil.copy(sourceCopyDestPath, distOutputPath, true);
    }

    protected String buildScript(String outputPath, String jarPath) {
        String shellOutputPath = outputPath + File.separator + "generator";

        ScriptGenerator.doGenerate(shellOutputPath, jarPath);
        return shellOutputPath;
    }


    protected String buildJar(String outputPath, Meta meta) throws IOException, InterruptedException {
        // 构建jar包
        JarGenerator.doGenerate(outputPath);

        // acm-template-pro-generator-1.0-SNAPSHOT-jar-with-dependencies.jar
        String jarName = String.format("%s-%s-jar-with-dependencies.jar", meta.getName(), meta.getVersion());
        String jarPath = "target/" + jarName;
        return jarPath;
    }

    protected void generateCode(Meta meta, DataModel model, String outputPath) throws IOException, TemplateException {
        // 获取resource目录
        ClassPathResource classPathResource = new ClassPathResource("");
        // 获取绝对路径
        String inputResourcePath = classPathResource.getAbsolutePath();


        // Java包基础的路径
        // com.wjp
        String outputBasePackage = meta.getBasePackage();
        // com/wjp
        String outputBasePackagePath = StrUtil.join("/", StrUtil.split(outputBasePackage, "."));
        // 最终通过模版生成文件的路径
        String outputBaseJavaPackagePath = outputPath + File.separator + "src/main/java/" + outputBasePackagePath;


        String inputFilePath;
        String outputFilePath;

        // model.DataModel
        // 动态模板的路径: D:/fullStack/wcode-generator/wcode-generator-maker/target/classes/\templates/java/model/DataModel.java.ftl
        inputFilePath = inputResourcePath + File.separator + "templates/java/model/DataModel.java.ftl";
        // 通过FreeMarker将动态模板转为静态模板，并放到该位置上: D:\fullStack\wcode-generator\wcode-generator-maker\generated\acm-template-pro-generator\src/main/java/com/wjp\model/DataModel.java
        outputFilePath = outputBaseJavaPackagePath + File.separator + "model/DataModel.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // 动态生成config子命令
        inputFilePath = inputResourcePath + "templates/java/cli/command/ConfigCommand.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + File.separator + "cli/command/ConfigCommand.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);


        // 动态生成 MainGenerator.java.ftl
        inputFilePath = inputResourcePath + "templates/java/generator/MainGenerator.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + File.separator + "generator/MainGenerator.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // 动态生成generate子命令

        inputFilePath = inputResourcePath + "templates/java/cli/command/GenerateCommand.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + File.separator + "cli/command/GenerateCommand.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // 动态生成list子命令
        inputFilePath = inputResourcePath + "templates/java/cli/command/ListCommand.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + File.separator + "cli/command/ListCommand.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // 动态生成CommandExecutor
        inputFilePath = inputResourcePath + "templates/java/cli/CommandExecutor.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + File.separator + "cli/CommandExecutor.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // 动态生成Main
        inputFilePath = inputResourcePath + "templates/java/Main.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + File.separator + "Main.java";
        // 动态生成项目
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // 动态生成DynamicGenerator.java.ftl
        inputFilePath = inputResourcePath + "templates/java/generator/DynamicGenerator.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + File.separator + "generator/DynamicGenerator.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);



        // 动态生成 StaticGenerator.java.ftl
        inputFilePath = inputResourcePath + "templates/java/generator/StaticGenerator.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + File.separator + "generator/StaticGenerator.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);


        // 动态生成 pom.xml.ftl
        inputFilePath = inputResourcePath + "templates/pom.xml.ftl";
        outputFilePath = outputPath + File.separator + "pom.xml";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // 动态生成 .gitignore
        inputFilePath = inputResourcePath + "templates/.gitignore.ftl";
        outputFilePath = outputPath + File.separator + ".gitignore";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, model);


        // 动态生成 README.md文档
        inputFilePath = inputResourcePath + "templates/README.md.ftl";
        outputFilePath = outputPath + File.separator + "README.md";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);
    }

    protected String copySource(Meta meta, String outputPath) {
        // 从原始模版路径 复制到 生成的代码包 中
        String sourceRootPath = meta.getFileConfig().getSourceRootPath();
        String sourceCopyDestPath = outputPath + File.separator + ".source";
        // 复制
        // 原始路径，目标路径，是否覆盖
        FileUtil.copy(sourceRootPath, sourceCopyDestPath, false);
        return sourceCopyDestPath;
    }
}

