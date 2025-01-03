package ${basePackage}.cli.command;

import cn.hutool.core.bean.BeanUtil;
import ${basePackage}.generator.MainGenerator;
import ${basePackage}.model.DataModel;
import freemarker.template.TemplateException;
import lombok.Data;
import picocli.CommandLine;

import java.io.IOException;
import java.util.concurrent.Callable;

<#--生成选项-->
<#macro generateOption indent modelInfo>
${indent}@CommandLine.Option(names = {<#if modelInfo.abbr??>"-${modelInfo.abbr}",</#if>"--${modelInfo.fieldName}"}, arity="0..1",<#if modelInfo.description??>description="${modelInfo.description}",</#if>interactive=true, echo=true)
${indent}private ${modelInfo.type} ${modelInfo.fieldName} <#if modelInfo.defaultValue??>=${modelInfo.defaultValue?c}</#if>;
</#macro>

<#macro generateCommand indent modelInfo>
${indent}System.out.println("${modelInfo.groupName}配置: ");
${indent}CommandLine ${modelInfo.groupKey}CommandLine = new CommandLine(${modelInfo.type}Command.class);
${indent}${modelInfo.groupKey}CommandLine.execute(${modelInfo.allArgsStr});
</#macro>

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

    <#list modelConfig.models as modelInfo>
        <#-- 有分组   -->
        <#if modelInfo.groupKey??>
        /**
         * ${modelInfo.groupName}
         */
        static DataModel.${modelInfo.type} ${modelInfo.groupKey} = new DataModel.${modelInfo.type}();

        // 定义一个分组类
        @CommandLine.Command(name = "${modelInfo.groupKey}", description = "${modelInfo.description}", mixinStandardHelpOptions = true)
        @Data
        public static class ${modelInfo.type}Command implements Runnable {
            <#list modelInfo.models as subModelInfo>
                <@generateOption indent="        " modelInfo=subModelInfo />
            </#list>


            @Override
            public void run() {
                // 将命令行参数 赋值给 mainTemplateConfig
                <#list modelInfo.models as subModelInfo>
                ${modelInfo.groupKey}.${subModelInfo.fieldName} = ${subModelInfo.fieldName};
                </#list>
            }
        }

        <#else>
            <#-- 没有分组-->
            <@generateOption indent="    " modelInfo=modelInfo />
        </#if>
    </#list>

    @Override
    public Integer call() throws TemplateException, IOException {

        <#list modelConfig.models as modelInfo>
        <#if modelInfo.groupKey??>
        <#if modelInfo.condition??>
        if(${modelInfo.condition}) {
            <@generateCommand indent="            " modelInfo=modelInfo />
        }
        <#else>
        <@generateCommand indent="        " modelInfo=modelInfo />
        </#if>
        </#if>
        </#list>
        <#-- 填充数据模型对象    -->
        DataModel dataModel = new DataModel();
        // 将 命令行参数 赋值给 mainTemplateConfig
        BeanUtil.copyProperties(this, dataModel);
        <#list modelConfig.models as modelInfo>
        <#if modelInfo.groupKey??>
        dataModel.${modelInfo.groupKey} = ${modelInfo.groupKey};
        </#if>
        </#list>
        System.out.println("dataModel: " + dataModel); // 打印是否初始化 Git 的值
        // 生成代码
        MainGenerator.doGenerate(dataModel);
        return 0;
    }


}
