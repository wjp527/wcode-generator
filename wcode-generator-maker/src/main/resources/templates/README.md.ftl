# ${name}

> ${description}
>
> 作者：${author}
>
> [代码生成器项目](https://github.com/wjp527/wcode-generator/tree/wcode-generator-maker) 制作，感谢您的使用！

可以通过命令行交互式输入的方式动态生成想要的项目代码

## 使用说明

执行项目根目录下的脚本文件：
generator <命令> <选项参数>

示例代码:
<#--generator generate <#list modelConfig.models as modelInfo>-<#if modelInfo.abbr??>${modelInfo.abbr}</#if> </#list>-->


## 参数说明
<#--<#list modelConfig.models as modelInfo>-->
<#--    <#if modelInfo.fieldName??>-->
<#--        ${modelInfo?index+1}: ${modelInfo.fieldName}-->

<#--        类型: ${modelInfo.type}-->

<#--        描述: ${modelInfo.description}-->

<#--        缩写: -${modelInfo.abbr??}-->
<#--    </#if>-->

<#--</#list>-->
