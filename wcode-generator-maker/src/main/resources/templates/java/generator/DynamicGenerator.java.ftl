package ${basePackage}.generator;

import cn.hutool.core.io.FileUtil;
import ${basePackage}.model.DataModel;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.*;

/**
 * 动态文件生成器
 */

public class DynamicGenerator {
    public static void doGenerate(String inputPath, String outputPath, Object model) throws IOException, TemplateException {
        // 创建 FreeMarker 配置对象
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_32);

        // 获取模板文件的父目录
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

        // 关闭输出流
        out.close();
    }

}
