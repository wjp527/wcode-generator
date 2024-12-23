package com.wjp.generator;

import com.wjp.model.MainTemplateConfig;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;


public class DynamicGenerator {
    public static void main(String[] args) throws IOException, TemplateException {

        // 这里是项目根目录: D:\fullStack\wcode-generator,而我们需要进入的是 wcode-generator-basic 这个项目里

        // 项目路径: D:\fullStack\wcode-generator\wcode-generator-basic
        String projectPath = System.getProperty("user.dir") + File.separator + "wcode-generator-basic";

        // 输入路径【模板文件路径】: D:\fullStack\wcode-generator\wcode-generator-basic\src/main/resources/templates/MainTemplate.java.ftl
        String inputPath = projectPath + File.separator + "src/main/resources/templates/MainTemplate.java.ftl";

        // 输出路径【模版生成文件路径】: D:\fullStack\wcode-generator\wcode-generator-basic\MainTemplate.java
        String outputPath = projectPath + File.separator + "MainTemplate.java";


        // 创建数据模型

        MainTemplateConfig mainTemplateConfig = new MainTemplateConfig();
//        mainTemplateConfig.setAuthor("wjp");
//        mainTemplateConfig.setOutputText("sum");
        mainTemplateConfig.setLoop(true);

        doGenerate(inputPath, outputPath, mainTemplateConfig);
    }

    public static void doGenerate(String inputPath, String outputPath, Object model) throws IOException, TemplateException {
        // new 出 Configuration 对象，参数为 FreeMarker 版本号
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_32);
        // 获取实际文件的父目录
        File templateDir = new File(inputPath).getParentFile();
        System.out.println("templateDir = " + templateDir);
        // ✨指定模板文件所在的路径
        configuration.setDirectoryForTemplateLoading(templateDir);

        // // 设置模板文件使用的字符集
        configuration.setDefaultEncoding("utf-8");

        // 设置数字格式化
        configuration.setNumberFormat("0.######");  // now it will print 1000000

        // 创建模板对象，加载指定模板
        String templateName = new File(inputPath).getName();
        // 从指定的模板目录加载一个模板文件
        // templateName: 要加载的模版名
        Template template = configuration.getTemplate(templateName);


        // 输出文件
        Writer out = new FileWriter(outputPath);

        // 调用模板对象的 process 方法，将数据模型和输出流传递给模板对象，生成 HTML 文件
        template.process(model, out);

        // 生成文件后别忘了关闭哦
        out.close();


    }
}
