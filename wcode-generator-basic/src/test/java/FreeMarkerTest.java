import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FreeMarkerTest {

    @Test
    public void test() throws IOException, TemplateException {
        // new 出 Configuration 对象，参数为 FreeMarker 版本号
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_32);

        // 指定模板文件所在的路径
        configuration.setDirectoryForTemplateLoading(new File("src/main/resources/templates"));

        // // 设置模板文件使用的字符集
        configuration.setDefaultEncoding("utf-8");

        // 设置数字格式化
        configuration.setNumberFormat("0.######");  // now it will print 1000000

        // 创建模板对象，加载指定模板
        Template template = configuration.getTemplate("myweb.html.ftl");


        // 创建数据模型
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("currentYear",2024);
        List<Map<String, Object>> menuItems = new ArrayList<>();

        Map<String, Object> menuItem1 = new HashMap<>();
        menuItem1.put("url", "http://www.baidu.com");
        menuItem1.put("label", "百度");

        Map<String, Object> menuItem2 = new HashMap<>();
        menuItem2.put("url", "http://www.google.com");
        menuItem2.put("label", "谷歌");

        menuItems.add(menuItem1);
        menuItems.add(menuItem2);

        dataModel.put("menuItems", menuItems);


        // 输出文件
        Writer out = new FileWriter("myweb.html");

        // 调用模板对象的 process 方法，将数据模型和输出流传递给模板对象，生成 HTML 文件
        template.process(dataModel, out);

        // 生成文件后别忘了关闭哦
        out.close();





    }
}
