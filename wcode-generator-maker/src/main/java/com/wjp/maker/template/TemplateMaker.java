package com.wjp.maker.template;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.wjp.maker.meta.Meta;
import com.wjp.maker.meta.enums.FileGenerateTypeEnum;
import com.wjp.maker.meta.enums.FileTypeEnum;
import com.wjp.maker.template.model.FileFilterConfig;
import com.wjp.maker.template.model.TemplateMakerFileConfig;
import com.wjp.maker.template.model.enums.FileFilterRangeEnum;
import com.wjp.maker.template.model.enums.FileFilterRuleEnum;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 模板制作工具
 */
public class TemplateMaker {

    /**
     * 判断id是否存在，如果不存在，则生成id，如果存在，则直接返回id
     *
     * @param newMeta                   新的元数据对象
     * @param originProjectPath         原始项目路径
     * @param templateMakerFileConfig 模板制作过滤器配置
     * @param modelInfo                 模型信息
     * @param searchStr                 要搜索的字符串
     * @param id                        要生成的id
     * @return
     */
    private static long makeTemplate(Meta newMeta, String originProjectPath, TemplateMakerFileConfig templateMakerFileConfig, Meta.ModelConfig.ModelInfo modelInfo, String searchStr, Long id) {
        // 没有 id 则生成
        if (id == null) {
            // 生成雪花算法
            id = IdUtil.getSnowflakeNextId();
        }

        // 指定原始项目路径
        String projectPath = System.getProperty("user.dir");


        // 复制目录
        // 生成模板的临时文件夹目录
        String tempDirPath = projectPath + File.separator + ".temp";
        // 生成临时文件夹下的文件夹名
        // D:/fullStack/wcode-generator/wcode-generator-maker/.temp/1
        String templatePath = tempDirPath + File.separator + id;

        // 判断是否有这个文件夹
        // 如果没有，就进行创建文件夹并复制
        if (!FileUtil.exist(templatePath)) {
            FileUtil.mkdir(templatePath);
            // 复制文件
            FileUtil.copy(originProjectPath, templatePath, true);
        }


        // 2. 输入文件信息
        // 要挖坑的项目根目录的位置
        // FileUtil.getLastPathEle(Paths.get(originProjectPath)).toString(): 获取最后一个路径元素
        // D:\fullStack\wcode-generator\wcode-generator-maker\.temp\1874438033919512576\springboot-init
        String sourceRootPath = templatePath + File.separator + FileUtil.getLastPathEle(Paths.get(originProjectPath)).toString();


        // ✨将 目录中 "\\" 替换为 "/"【注意: windows系统】
        // D:/fullStack/wcode-generator/wcode-generator-maker/.temp/1/springboot-init
        sourceRootPath = sourceRootPath.replaceAll("\\\\", "/");


        // 循环变量，输入的文件 【支持多选多个文件】
        // inputFilePathList = [/src/main/java/com/yupi/springbootinit/common, /src/main/java/com/yupi/springbootinit/controller]

        // 获取文件配置信息
        List<TemplateMakerFileConfig.FileInfoConfig> fileInfoConfigList = templateMakerFileConfig.getFiles();
        List<Meta.FileConfig.FileInfo> newFileInfoList = new ArrayList<>();
        for (TemplateMakerFileConfig.FileInfoConfig fileInfoConfig : fileInfoConfigList) {
            String inputFilePath = fileInfoConfig.getPath();
            // 获取文件输入路径【绝对路径】
            String inputFileAbsolutePath = sourceRootPath + File.separator + inputFilePath;
            // ✨一定要传入绝对路径
            // 得到过滤后的文件列表
            List<File> files = FileFilter.doFilter(inputFileAbsolutePath, fileInfoConfig.getFileConfigList());
            for (File file : files) {
                // 制作文件模版
                Meta.FileConfig.FileInfo fileInfo = makeFileTemplate(modelInfo, searchStr, sourceRootPath, file);
                newFileInfoList.add(fileInfo);
            }

        }


        // 如果是文件组
//        TemplateMakerFileConfig.FileGroupConfig fileGroupConfig = templateMakerFileConfig.getFileGroupConfig();

//        if(fileGroupConfig != null) {
//            String condition = fileGroupConfig.getCondition();
//            String groupKey = fileGroupConfig.getGroupKey();
//            String groupName = fileGroupConfig.getGroupName();

//            Meta.FileConfig.FileInfo groupFileInfo = new Meta.FileConfig.FileInfo();
//            groupFileInfo.setCondition(condition);
//            groupFileInfo.setGroupKey(groupKey);
//            groupFileInfo.setGroupName(groupName);
//
//            // 文件全部放到一个组内
//            groupFileInfo.setFiles(newFileInfoList);
//            newFileInfoList = new ArrayList<>();
//            newFileInfoList.add(groupFileInfo);
//
//
//        }

        // 三、生成配置文件
        String metaOutputPath = sourceRootPath + File.separator + "meta.json";

        // 如果有这个mate.json文件，那就说明这不是第一次创建，所以是可以在之前的文件中进行累加操作的
        if (FileUtil.exist(metaOutputPath)) {
            // 字符串 转为 json 对象
            Meta oldMeta = JSONUtil.toBean(FileUtil.readUtf8String(metaOutputPath), Meta.class);
            // 1. 追加配置参数
            List<Meta.FileConfig.FileInfo> fileInfoList = oldMeta.getFileConfig().getFiles();
            // 多个文件进行追加到一起【fileConfig.files】
            fileInfoList.addAll(newFileInfoList);
            // 2.追加模型参数
            List<Meta.ModelConfig.ModelInfo> modelInfoList = oldMeta.getModelConfig().getModels();
            modelInfoList.add(modelInfo);

            // 配置去重
            // 文件去重
            oldMeta.getFileConfig().setFiles(distinctFiles(fileInfoList));
            // 模型去重
            oldMeta.getModelConfig().setModels(distinctModels(modelInfoList));

            // 3. 写入新的meta.json文件
            FileUtil.writeUtf8String(JSONUtil.toJsonPrettyStr(oldMeta), metaOutputPath);
        } else {
            // 创建文件的配置对象
            Meta.FileConfig fileConfig = new Meta.FileConfig();
            newMeta.setFileConfig(fileConfig);
            fileConfig.setSourceRootPath(sourceRootPath);
            List<Meta.FileConfig.FileInfo> fileInfoList = new ArrayList<>();
            fileConfig.setFiles(fileInfoList);
            fileInfoList.addAll(newFileInfoList);

            Meta.ModelConfig modelConfig = new Meta.ModelConfig();
            newMeta.setModelConfig(modelConfig);
            List<Meta.ModelConfig.ModelInfo> modelInfoList = new ArrayList<>();

//            先 setModels(modelInfoList)，再 add(modelInfo) 的主要目的是为了防止
//            modelConfig.models 为 null，
//            确保你在 modelConfig.getModels() 时能够成功获取到你添加的数据。
            modelConfig.setModels(modelInfoList);
            modelInfoList.add(modelInfo);


            // 2. 将配置对象转为json格式输出
            FileUtil.writeUtf8String(JSONUtil.toJsonPrettyStr(newMeta), metaOutputPath);
        }

        return id;
    }

    /**
     * 制作文件模版
     *
     * @param modelInfo
     * @param searchStr
     * @param sourceRootPath
     * @param inputFile
     * @return
     */
    private static Meta.FileConfig.FileInfo makeFileTemplate(Meta.ModelConfig.ModelInfo modelInfo, String searchStr, String sourceRootPath, File inputFile) {
        // 获取输入文件的绝对路径
        String fileInputAbsolutePath = inputFile.getAbsolutePath();
        // 将 目录中 "\\" 替换为 "/"【注意: windows系统】
        fileInputAbsolutePath = fileInputAbsolutePath.replaceAll("\\\\", "/");
        // 输出文件位置
        String fileOutputAbsolutePath = fileInputAbsolutePath + ".ftl";

        // 增加一个挖坑的ftl模板文件位置【空文件现在】
        // 得到相对路径
        String fileInputPath = fileInputAbsolutePath.replace(sourceRootPath + "/", "");
        String fileOutputPath = fileInputPath + ".ftl";


        // 二、使用字符串替换，生成模板文件


        String fileContent;

        // 如果有这个模板文件，那就是之前创建过了，直接读取文件内容，并在此基础上进行挖坑
        if (FileUtil.exist(fileOutputAbsolutePath)) {
            // fileOutputAbsolutePath: 带ftl后缀的文件路径
            fileContent = FileUtil.readUtf8String(fileOutputAbsolutePath);
        } else {
            // 读取文件内容【格式为UTF-8 格式的】
            fileContent = FileUtil.readUtf8String(fileInputAbsolutePath);
        }

        System.out.println("fileContent = " + fileContent);
        // 下面这两行代码的主要目的是在文件内容中找到指定的字符串（如类名、方法名等），
        // 并将其替换为 FreeMarker 模板中的占位符，以便后续可以通过 FreeMarker 引擎动态生成代码。
        // 置换物
        String replacement = String.format("${%s}", modelInfo.getFieldName());
        // 替换
        String newFileContent = StrUtil.replace(fileContent, searchStr, replacement);

        Meta.FileConfig.FileInfo fileInfo = new Meta.FileConfig.FileInfo();
        fileInfo.setInputPath(fileInputPath);
        fileInfo.setType(FileTypeEnum.FILE.getValue());

        // 和原文件内容一直，没有挖坑，生成静态文件
        if (newFileContent.equals(fileContent)) {
            // 静态生成的文件，输入输出路径都保持一致
            fileInfo.setOutputPath(fileInputPath);
            fileInfo.setGenerateType(FileGenerateTypeEnum.STATIC.getValue());
        } else {
            fileInfo.setOutputPath(fileOutputPath);
            fileInfo.setGenerateType(FileGenerateTypeEnum.DYNAMIC.getValue());
            // 写入模板内容
            FileUtil.writeUtf8String(newFileContent, fileOutputAbsolutePath);
        }

        return fileInfo;
    }

    public static void main(String[] args) {
        // 一、输入信息
        // 1. 项目的基本信息
        String name = "springboot-init-generator";
        String description = "ACM 示例模板生成器";
        Meta meta = new Meta();
        meta.setName(name);
        meta.setDescription(description);
        // 获取这个项目的根目录
        String projectPath = System.getProperty("user.dir");
        // 项目的原始目录
        String originProjectPath = FileUtil.getAbsolutePath(new File(projectPath).getParentFile()) + File.separator + "wcode-generator-demo-projects/springboot-init";
        // 要挖坑的文件位置
        String fileInputPath1 = "/src/main/java/com/yupi/springbootinit/common";
        String fileInputPath2 = "/src/main/java/com/yupi/springbootinit/controller";
        List<String> inputFilePathList = Arrays.asList(fileInputPath1, fileInputPath2);
        // 3.输入模型参数信息【要进行挖坑的地方】
        Meta.ModelConfig.ModelInfo modelInfo = new Meta.ModelConfig.ModelInfo();
        // 第一次执行
        modelInfo.setFieldName("outputText");
        modelInfo.setType("String");
        modelInfo.setDefaultValue("sum = ");
        String searchStr = "BaseResponse";

        // 第二次执行
//        // 对找到的字符串进行挖坑 ${className}
//        modelInfo.setFieldName("className");
//        // 挖坑的类型
//        modelInfo.setType("String");
//        // 在模板文件中找到这个字符串
//        String searchStr = "MainTemplate";


        // 文件过滤配置信息
        TemplateMakerFileConfig.FileInfoConfig fileInfoConfig1 = new TemplateMakerFileConfig.FileInfoConfig();
        fileInfoConfig1.setPath(fileInputPath1);

        List<FileFilterConfig> fileFilterConfigList = new ArrayList<>();
        // 过滤规则：文件名包含 Base
        // 范围: 是 文件名
        // 规则: 包含
        // 值: Base
        FileFilterConfig fileFilterConfig1 = FileFilterConfig.builder()
                .range(FileFilterRangeEnum.FILE_NAME.getValue())
                .rule(FileFilterRuleEnum.CONTAINS.getValue())
                .value("Base").build();

        fileFilterConfigList.add(fileFilterConfig1);
        fileInfoConfig1.setFileConfigList(fileFilterConfigList);

        TemplateMakerFileConfig.FileInfoConfig fileInfoConfig2 = new TemplateMakerFileConfig.FileInfoConfig();
        fileInfoConfig2.setPath(fileInputPath2);

        List<TemplateMakerFileConfig.FileInfoConfig> fileInfoConfigList = Arrays.asList(fileInfoConfig1, fileInfoConfig2);
        TemplateMakerFileConfig templateMakerFileConfig = new TemplateMakerFileConfig();
        templateMakerFileConfig.setFiles(fileInfoConfigList);



        // 分组配置
//        TemplateMakerFileConfig.FileGroupConfig fileGroupConfig = new TemplateMakerFileConfig.FileGroupConfig();
//        fileGroupConfig.setCondition("outputText");
//        fileGroupConfig.setGroupKey("test");
//        fileGroupConfig.setGroupName("测试分组");
//        templateMakerFileConfig.setFileGroupConfig(fileGroupConfig);


        long id = makeTemplate(meta, originProjectPath, templateMakerFileConfig, modelInfo, searchStr, 1874438033919512576L);
        System.out.println("id = " + id);
    }

    /**
     * 文件去重
     *
     * @param fileInfoList
     * @return
     */
    private static List<Meta.FileConfig.FileInfo> distinctFiles(List<Meta.FileConfig.FileInfo> fileInfoList) {
        // 去重
        ArrayList<Meta.FileConfig.FileInfo> newFileInfoList = new ArrayList<>(fileInfoList.stream()
                .collect(
                        // 按照输入路径去重
                        Collectors.toMap(Meta.FileConfig.FileInfo::getInputPath, o -> o, (e, r) -> r)
                ).values());

        return newFileInfoList;
    }

    /**
     * 模型去重
     *
     * @param modelInfoList
     * @return
     */
    private static List<Meta.ModelConfig.ModelInfo> distinctModels(List<Meta.ModelConfig.ModelInfo> modelInfoList) {
        // 去重
        ArrayList<Meta.ModelConfig.ModelInfo> newModelInfoList = new ArrayList<>(modelInfoList.stream()
                .collect(
                        // 按照输入路径去重
                        Collectors.toMap(Meta.ModelConfig.ModelInfo::getFieldName, o -> o, (e, r) -> r)
                ).values());

        return newModelInfoList;
    }

}
