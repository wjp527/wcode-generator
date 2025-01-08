package ${basePackage}.cli.command;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import ${basePackage}.generator.MainGenerator;
import ${basePackage}.model.DataModel;
import freemarker.template.TemplateException;
import lombok.Data;
import picocli.CommandLine;

import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * 读取 json 文件生成代码
 */
@CommandLine.Command(name = "json-generate",  description = "读取 json 文件生成代码",mixinStandardHelpOptions = true)
// 交互式输入就 Callable
// 非交互式输入就 Runnable
@Data
public class JsonGenerateCommand implements Callable {
    /**
     * @CommandLine.Option参数介绍 name: 参数名，如-l，--loop name不能写错
     * description: 参数描述
     * arity: 参数的个数 0..1 表示0个或1个
     * interactive: 是否交互式输入，如密码输入
     * echo: 是否输出输入内容，如密码输入
     */

    @CommandLine.Option(names = {"-f","--file"}, arity="0..1",description="json 文件路径",interactive=true, echo=true)
    private String filePath;
    /**
     * 核心模块
     */
    static DataModel.MainTemplate mainTemplate = new DataModel.MainTemplate();



    /**
     * 整个命令执行的入口
     *
     * @return
     * @throws TemplateException
     * @throws IOException
     */
    @Override
    public Integer call() throws TemplateException, IOException {
        // 读取 json 文件，转为数据模型
        String JsonStr = FileUtil.readUtf8String(filePath);
        DataModel dataModel = JSONUtil.toBean(JsonStr, DataModel.class);
        System.out.println("dataModel: " + dataModel); // 打印是否初始化 Git 的值
        // 生成代码
        MainGenerator.doGenerate(dataModel);
        return 0;
    }


}
