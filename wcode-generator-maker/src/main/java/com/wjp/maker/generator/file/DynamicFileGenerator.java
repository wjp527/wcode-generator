package com.wjp.maker.generator.file;

import cn.hutool.core.io.FileUtil;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.*;

/**
 * 动态文件生成器
 */

public class DynamicFileGenerator {

    /**
     * 使用相对路径生成文件
     *
     * @param relativeInputPath 模板文件相对输入路径
     * @param outputPath 输出路径
     * @param model 数据模型
     * @throws IOException
     * @throws TemplateException
     */
    public static void doGenerate(String relativeInputPath, String outputPath, Object model) throws IOException, TemplateException {
        // new 出 Configuration 对象，参数为 FreeMarker 版本号
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_32);;

        // 获取模板文件所属包和模板名称
        int lastSplitIndex = relativeInputPath.lastIndexOf("/");
        String basePackagePath = relativeInputPath.substring(0, lastSplitIndex);
        String templateName = relativeInputPath.substring(lastSplitIndex + 1);

        // 通过类加载器读取模板
        ClassTemplateLoader templateLoader = new ClassTemplateLoader(DynamicFileGenerator.class, basePackagePath);
        configuration.setTemplateLoader(templateLoader);

        // 设置模板文件使用的字符集
        configuration.setDefaultEncoding("utf-8");

        // 创建模板对象，加载指定模板
        Template template = configuration.getTemplate(templateName);

        // 文件不存在则创建文件和父目录
        if (!FileUtil.exist(outputPath)) {
            FileUtil.touch(outputPath);
        }

        // 生成
        Writer out = new FileWriter(outputPath);
        template.process(model, out);

        // 生成文件后别忘了关闭哦
        out.close();
    }

    /**
     * 动态生成文件
     * @param inputPath 模板文件路径
     * @param outputPath 输出文件路径
     * @param model 数据模型
     * @throws IOException
     * @throws TemplateException
     */
    public static void doGenerateByPath(String inputPath, String outputPath, Object model) throws IOException, TemplateException {
        // 创建 FreeMarker 配置对象
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_32);

        // 获取模板文件所在的父目录
        File templateDir = new File(inputPath).getParentFile();
        System.out.println("Template directory: " + templateDir);

        // 设置模板文件所在的路径
        configuration.setDirectoryForTemplateLoading(templateDir);

        // 设置 FreeMarker 编码为 UTF-8
        configuration.setDefaultEncoding("UTF-8");

        // 设置数字格式化
        configuration.setNumberFormat("0.######");

        // 获取模板文件名
        String templateName = new File(inputPath).getName();
        System.out.println("Loading template: " + templateName);

        // 加载模板文件
        Template template = configuration.getTemplate(templateName);

        // 如果文件不存在，则创建
        if(!FileUtil.exist(outputPath)) {
            // touch:创建文件
            // mkdir:创建目录
            FileUtil.touch(outputPath);
        }

        // 输出文件，确保文件使用 UTF-8 编码
        Writer out = new OutputStreamWriter(new FileOutputStream(outputPath), "UTF-8");

        // 调用模板对象的 process 方法生成文件
        template.process(model, out);
        // 输出out路径
        System.out.println("outputPath: " + outputPath);

        // 关闭输出流
        out.close();
    }

}
