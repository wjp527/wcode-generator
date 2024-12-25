package com.wjp.maker.cli.command;

import cn.hutool.core.bean.BeanUtil;
import com.wjp.maker.generator.file.FileGenerator;
import com.wjp.maker.model.DataModel;
import freemarker.template.TemplateException;
import lombok.Data;
import picocli.CommandLine;

import java.io.IOException;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "generate", mixinStandardHelpOptions = true)
// 交互式输入就 Callable
// 非交互式输入就 Runnable
@Data
public class GenerateCommand implements Callable {

    /**
     * @CommandLine.Option参数介绍 name: 参数名，如-l，--loop name不能写错
     * description: 参数描述
     * arity: 参数的个数 0..1 表示0个或1个
     * interactive: 是否交互式输入，如密码输入
     * echo: 是否输出输入内容，如密码输入
     */
    @CommandLine.Option(names = {"-l", "--loop"}, description = "是否循环", arity = "0..1", interactive = true,echo = true)
    private boolean loop;

    @CommandLine.Option(names = {"-a", "--author"}, description = "作者信息", arity = "0..1", interactive = true,echo = true)
    private String author;

    @CommandLine.Option(names = {"-o", "--outputText"}, description = "输出文本", arity = "0..1", interactive = true,echo = true)
    private String outputText;

    @Override
    public Integer call() throws TemplateException, IOException {
        DataModel dataModel = new DataModel();
        // 将 命令行参数 赋值给 mainTemplateConfig
        BeanUtil.copyProperties(this, dataModel);
        // 生成代码
        FileGenerator.doGenerate(dataModel);
        return 0;
    }


}
