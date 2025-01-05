package com.wjp.maker.template;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.wjp.maker.meta.Meta;
import com.wjp.maker.meta.enums.FileGenerateTypeEnum;
import com.wjp.maker.meta.enums.FileTypeEnum;
import com.wjp.maker.template.model.TemplateMakerConfig;
import com.wjp.maker.template.model.TemplateMakerFileConfig;
import com.wjp.maker.template.model.TemplateMakerModelConfig;
import com.wjp.maker.template.model.TemplateMakerOutputConfig;

import java.io.File;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 模板制作工具
 */
public class TemplateMaker {
    /**
     * 制作模板
     *
     * @param templateMakerConfig
     * @return
     */
    public static long makeTemplate(TemplateMakerConfig templateMakerConfig) {
        Long id = templateMakerConfig.getId();
        Meta newMeta = templateMakerConfig.getMeta();
        String originProjectPath = templateMakerConfig.getOriginProjectPath();
        TemplateMakerFileConfig templateMakerFileConfig = templateMakerConfig.getFileConfig();
        TemplateMakerModelConfig templateMakerModelConfig = templateMakerConfig.getModelConfig();
        TemplateMakerOutputConfig outputConfig = templateMakerConfig.getOutputConfig();

        return makeTemplate(newMeta, originProjectPath, templateMakerFileConfig, templateMakerModelConfig, outputConfig, id);
    }


    /**
     * 判断id是否存在，如果不存在，则生成id，如果存在，则直接返回id
     *
     * @param newMeta                   新的元数据对象
     * @param originProjectPath         原始项目路径
     * @param templateMakerFileConfig   模板制作过滤器配置
     * @param templateMakerModelConfig  模型信息 + 要搜索的字符串
     * @param templateMakerOutputConfig 输出配置
     * @param id                        要生成的id
     * @return
     */
    public static long makeTemplate(Meta newMeta, String originProjectPath, TemplateMakerFileConfig templateMakerFileConfig,
                                    TemplateMakerModelConfig templateMakerModelConfig,
                                    TemplateMakerOutputConfig templateMakerOutputConfig,
                                    Long id) {
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

        // 持久化项目路径，自动读取工作空间下的第一个目录(项目根目录)即可
        // FileUtil.loopFiles(new File(tempDirPath), 1, null): 在tempDirPath目录下的第一个目录进行遍历
        String sourceRootPath = FileUtil.loopFiles(new File(templatePath), 1, null)
                // 转为流
                .stream()
                // 过滤出目录
                .filter(File::isDirectory)
                // 取第一个
                .findFirst()
                // 抛出异常
                .orElseThrow(RuntimeException::new)
                // 获取绝对路径
                .getAbsolutePath();


        // ✨将 目录中 "\\" 替换为 "/"【注意: windows系统】
        // D:/fullStack/wcode-generator/wcode-generator-maker/.temp/1/springboot-init
        sourceRootPath = sourceRootPath.replaceAll("\\\\", "/");


        // 二、生成文件模板
        // 制作文件模板
        List<Meta.FileConfig.FileInfo> newFileInfoList = makeFileTemplates(templateMakerFileConfig, templateMakerModelConfig, sourceRootPath);
        // 获取模型信息
        List<Meta.ModelConfig.ModelInfo> newModelInfoList = getModelInfoList(templateMakerModelConfig);

        // 三、生成配置文件
        String metaOutputPath = templatePath + File.separator + "meta.json";

        // 如果有这个mate.json文件，那就说明这不是第一次创建，所以是可以在之前的文件中进行累加操作的
        if (FileUtil.exist(metaOutputPath)) {
            // 字符串 转为 json 对象
            newMeta = JSONUtil.toBean(FileUtil.readUtf8String(metaOutputPath), Meta.class);
            // 1. 追加配置参数
            List<Meta.FileConfig.FileInfo> fileInfoList = newMeta.getFileConfig().getFiles();
            // 多个文件进行追加到一起【fileConfig.files】
            fileInfoList.addAll(newFileInfoList);
            // 2.追加模型参数
            List<Meta.ModelConfig.ModelInfo> modelInfoList = newMeta.getModelConfig().getModels();
            modelInfoList.addAll(newModelInfoList);

            // 配置去重
            // 文件去重
            newMeta.getFileConfig().setFiles(distinctFiles(fileInfoList));
            // 模型去重
            newMeta.getModelConfig().setModels(distinctModels(modelInfoList));

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
            modelInfoList.addAll(newModelInfoList);


        }

        // 2.额外的输出配置
        if (templateMakerOutputConfig != null) {
            // 文件外层和分组去重
            if (templateMakerOutputConfig.isRemoveGroupFilesFromRoot()) {
                List<Meta.FileConfig.FileInfo> fileInfoList = newMeta.getFileConfig().getFiles();
                newMeta.getFileConfig().setFiles(TemplateMakerUtils.removeGroupFilesFromRoot(fileInfoList));
            }
        }


        // 3. 写入新的meta.json文件
        FileUtil.writeUtf8String(JSONUtil.toJsonPrettyStr(newMeta), metaOutputPath);

        return id;
    }

    /**
     * 获取模型配置
     * 我觉得他就是对模型有分组的用list集合整合在一起，没有的直接抛出，不用list整合
     *
     * @param templateMakerModelConfig
     * @return
     */
    private static List<Meta.ModelConfig.ModelInfo> getModelInfoList(TemplateMakerModelConfig templateMakerModelConfig) {
        // 本次新增的模型列表
        List<Meta.ModelConfig.ModelInfo> newModelInfoList = new ArrayList<>();
        // 非空校验
        if (templateMakerModelConfig == null) {
            return newModelInfoList;
        }

        // 处理模型信息
        List<TemplateMakerModelConfig.ModelInfoConfig> models = templateMakerModelConfig.getModels();
        if (CollUtil.isEmpty(models)) {
            return newModelInfoList;
        }


        List<Meta.ModelConfig.ModelInfo> inputModelInfoList = models.stream()
                .map(modelInfoConfig -> {
                    Meta.ModelConfig.ModelInfo modelInfo = new Meta.ModelConfig.ModelInfo();
                    BeanUtil.copyProperties(modelInfoConfig, modelInfo);
                    return modelInfo;
                }).collect(Collectors.toList());


        // 如果是模型分组
        TemplateMakerModelConfig.ModelGroupConfig modelGroupConfig = templateMakerModelConfig.getModelGroupConfig();
        if (modelGroupConfig != null) {
            // 复制变量
            Meta.ModelConfig.ModelInfo groupModelInfo = new Meta.ModelConfig.ModelInfo();
            BeanUtil.copyProperties(modelGroupConfig, groupModelInfo);

            // 模型全放到一个分组内
            groupModelInfo.setModels(inputModelInfoList);
            newModelInfoList = new ArrayList<>();
            newModelInfoList.add(groupModelInfo);
        } else {
            // 不分组，添加所有的模型信息到列表
            newModelInfoList.addAll(inputModelInfoList);
        }
        return newModelInfoList;
    }

    /**
     * 生成多个文件
     *
     * @param templateMakerFileConfig
     * @param templateMakerModelConfig
     * @param sourceRootPath
     * @return
     */
    private static List<Meta.FileConfig.FileInfo> makeFileTemplates(TemplateMakerFileConfig templateMakerFileConfig,
                                                                    TemplateMakerModelConfig templateMakerModelConfig,
                                                                    String sourceRootPath) {
        List<Meta.FileConfig.FileInfo> newFileInfoList = new ArrayList<>();

        // 非空校验
        if (templateMakerFileConfig == null) {
            return newFileInfoList;
        }
        List<TemplateMakerFileConfig.FileInfoConfig> fileInfoConfigList = templateMakerFileConfig.getFiles();
        if (CollUtil.isEmpty(fileInfoConfigList)) {
            return newFileInfoList;
        }


        for (TemplateMakerFileConfig.FileInfoConfig fileInfoConfig : fileInfoConfigList) {
            String inputFilePath = fileInfoConfig.getPath();
            // 获取文件输入路径【绝对路径】
            String inputFileAbsolutePath = sourceRootPath + File.separator + inputFilePath;

            // ✨一定要传入绝对路径
            // 得到过滤后的文件列表
            List<File> files = FileFilter.doFilter(inputFileAbsolutePath, fileInfoConfig.getFilterConfigList());
            // 不处理已经生成的 FTL 模板文件
            files = files.stream()
                    .filter(item -> !item.getAbsolutePath().endsWith(".ftl"))
                    .collect(Collectors.toList());

            for (File file : files) {
                // 制作文件模版
                Meta.FileConfig.FileInfo fileInfo = makeFileTemplate(templateMakerModelConfig, sourceRootPath, file, fileInfoConfig);
                newFileInfoList.add(fileInfo);
            }

        }


        // 如果是文件组
        TemplateMakerFileConfig.FileGroupConfig fileGroupConfig = templateMakerFileConfig.getFileGroupConfig();

        if (fileGroupConfig != null) {
            String condition = fileGroupConfig.getCondition();
            String groupKey = fileGroupConfig.getGroupKey();
            String groupName = fileGroupConfig.getGroupName();

            // 组里面的每一个对象信息
            Meta.FileConfig.FileInfo groupFileInfo = new Meta.FileConfig.FileInfo();
            groupFileInfo.setCondition(condition);
            groupFileInfo.setGroupKey(groupKey);
            groupFileInfo.setGroupName(groupName);
            groupFileInfo.setType(FileTypeEnum.GROUP.getValue());

            // 文件全部放到一个组内
            groupFileInfo.setFiles(newFileInfoList);
            newFileInfoList = new ArrayList<>();
            newFileInfoList.add(groupFileInfo);

        }
        return newFileInfoList;
    }

    /**
     * 制作文件模版
     *
     * @param templateMakerModelConfig
     * @param sourceRootPath
     * @param inputFile
     * @param fileInfoConfig
     * @return
     */
    public static Meta.FileConfig.FileInfo makeFileTemplate(TemplateMakerModelConfig templateMakerModelConfig,
                                                            String sourceRootPath,
                                                            File inputFile,
                                                            TemplateMakerFileConfig.FileInfoConfig fileInfoConfig) {
        // 获取输入文件的绝对路径
        String fileInputAbsolutePath = inputFile.getAbsolutePath();
        // 将 目录中 "\\" 替换为 "/"【注意: windows系统】
        fileInputAbsolutePath = fileInputAbsolutePath.replaceAll("\\\\", "/");
        // 输出文件位置
        String fileOutputAbsolutePath = fileInputAbsolutePath + ".ftl";

        // 增加一个挖坑的ftl模板文件位置【空文件现在】
        // 得到相对路径
        String fileInputPath = fileInputAbsolutePath.replace(sourceRootPath + "/", "");
        // 二、使用字符串替换，生成模板文件
        String fileOutputPath = fileInputPath + ".ftl";


        String fileContent;

        // 如果有这个模板文件，那就是之前创建过了，直接读取文件内容，并在此基础上进行挖坑
        boolean hasTemplateFile = FileUtil.exist(fileOutputAbsolutePath);
        if (hasTemplateFile) {
            // fileOutputAbsolutePath: 带ftl后缀的文件路径
            fileContent = FileUtil.readUtf8String(fileOutputAbsolutePath);
        } else {
            // 读取文件内容【格式为UTF-8 格式的】
            fileContent = FileUtil.readUtf8String(fileInputAbsolutePath);
        }

        // 支持多个模型，对于同一个文件的内容，便利模型进行多轮替换
        String replacement;
        // 记录最新替换后的内容
        String newFileContent = fileContent;
        TemplateMakerModelConfig.ModelGroupConfig modelGroupConfig = templateMakerModelConfig.getModelGroupConfig();

        List<TemplateMakerModelConfig.ModelInfoConfig> models = templateMakerModelConfig.getModels();

        for (TemplateMakerModelConfig.ModelInfoConfig modelInfoConfig : models) {
            // 模型信息中的字段名
            String fieldName = modelInfoConfig.getFieldName();
            // 模型信息中的替换值
            String replaceText = modelInfoConfig.getReplaceText();
            // 不是分组
            if (modelGroupConfig == null) {
                // 并将其替换为 FreeMarker 模板中的占位符，以便后续可以通过 FreeMarker 引擎动态生成代码。
                // 置换物
                replacement = String.format("${%s}", fieldName);
            } else {
                String groupKey = modelGroupConfig.getGroupKey();
                // 有分组信息
                replacement = String.format("${%s.%s}", groupKey, fieldName);

            }
            // 替换 实现累加的效果
            // replaceText: 要被替换的文本
            // replacement: 要替换的文本
            // replaceText = jdbc:mysql://localhost:3306/my_db   replacement = ${mysql.url}
            // 在ftl文件中是需要 ${mysql.url} 这样的占位符，在这里进行替换set
            newFileContent = StrUtil.replace(newFileContent, replaceText, replacement);
        }

        Meta.FileConfig.FileInfo fileInfo = new Meta.FileConfig.FileInfo();
        fileInfo.setInputPath(fileOutputPath);
        fileInfo.setOutputPath(fileInputPath);
        fileInfo.setType(FileTypeEnum.FILE.getValue());
        // 控制单个文件是否生成
        fileInfo.setCondition(fileInfoConfig.getCondition());
        // 动态生成
        fileInfo.setGenerateType(FileGenerateTypeEnum.DYNAMIC.getValue());

        // 是否更改了文件内容
        boolean contentEquals = newFileContent.equals(fileContent);

        // 如果之前不存在模板文件，并且这次替换并没有修改文件的内容，才是静态生成
        if (!hasTemplateFile) {
            // 和原文件内容一直，没有挖坑，生成静态文件
            if (contentEquals) {
                // 静态生成的文件，输入输出路径都保持一致
                fileInfo.setInputPath(fileInputPath);
                fileInfo.setGenerateType(FileGenerateTypeEnum.STATIC.getValue());
            } else {
                // 写入模板内容
                FileUtil.writeUtf8String(newFileContent, fileOutputAbsolutePath);
            }
        } else if (!contentEquals) {
            // 写入模板内容
            FileUtil.writeUtf8String(newFileContent, fileOutputAbsolutePath);
        }


        return fileInfo;
    }

    /**
     * 文件去重
     *
     * @param fileInfoList
     * @return
     */
    public static List<Meta.FileConfig.FileInfo> distinctFiles(List<Meta.FileConfig.FileInfo> fileInfoList) {

        // 1. 将所有分组配置 (fileInfo) 分为有分组 和 无分组的


        // 先处理有分组的文件
        Map<String, List<Meta.FileConfig.FileInfo>> groupKeyFileInfoListMap = fileInfoList.stream()
                // { "groupKey": "a", "files": [1,2]},{ "groupKey": "a", "files": [2,3]},{ "groupKey": "b", "files": [3,4]}
                .filter(fileInfo -> StrUtil.isNotBlank(fileInfo.getGroupKey()))
                // 目的: 将同组的文件合并到一起
                // { "groupKey": "a", "files: [[1,2], [2,3]]},{ "groupKey": "b", "files: [[3,4]]}
                .collect(
                        // 快速分组
                        Collectors.groupingBy(fileInfo -> fileInfo.getGroupKey())
                );


        // 2. 队友有分组的文件配置，如果有相同的分组，同分组的文件会进行合并(merge)，不同分组课同时保留

        // 同组内进配置合并
        // { "groupKey": "a", "files: [[ 1, 2 ], [ 2, 3 ]]}
        // { "groupKey": "a", "files: [ 1, 2, 2, 3 ]}
        // { "groupKey": "a", "files: [ 1, 2, 3 ]}

        // 合并后的对象 map
        Map<String, Meta.FileConfig.FileInfo> groupKeyMergedFileInfoMap = new HashMap<>();

        // entrySet: 常用于遍历 Map 中的所有键值对
        // groupKeyFileInfoListMap 理解为: { "groupKey": "a", "files: [[ 1, 2 ], [ 2, 3 ]]}
        for (Map.Entry<String, List<Meta.FileConfig.FileInfo>> entry : groupKeyFileInfoListMap.entrySet()) {
            // 所有的文件列表 = [[1,2], [2,3]]
            // tempFileInfoList 理解为: [[ 1, 2 ], [ 2, 3 ]]
            List<Meta.FileConfig.FileInfo> tempFileInfoList = entry.getValue();
            // newFileInfoList 理解为: [1,2,3]
            List<Meta.FileConfig.FileInfo> newFileInfoList = new ArrayList<>(tempFileInfoList.stream()
                    // 打平/扁平化 [[1,2],[2,3]] =>  [1,2,3]
                    // flatMap: 将一个对象 转为 多个对象
                    // Map: 一个对象 映射为 1个对象
                    .flatMap(fileInfo -> fileInfo.getFiles().stream())
                    .collect(
                            // 按照输入路径进行去重
                            Collectors.toMap(
                                    fileInfo -> fileInfo.getOutputPath(), // 键的生成逻辑
                                    fileInfo -> fileInfo,               // 值的生成逻辑
                                    (existingValue, newValue) -> newValue // 键冲突时的处理逻辑
                            )

                            // 简写:
                            // Collectors.toMap(Meta.FileConfig.FileInfo::getOutputPath, o -> o, (e, r) -> r)

                    ).values());

            // 同组配置信息的覆盖
            // 取最后一个文件配置信息
            Meta.FileConfig.FileInfo newFileInfo = CollUtil.getLast(tempFileInfoList);
            newFileInfo.setFiles(newFileInfoList);
            String groupKey = entry.getKey();
            groupKeyMergedFileInfoMap.put(groupKey, newFileInfo);

        }


        // 3. 创建新的文件配置列表(结果列表)，先将 合并后的分组 添加到结果列表
        ArrayList<Meta.FileConfig.FileInfo> resultList = new ArrayList<>(groupKeyMergedFileInfoMap.values());

        // 4. 再将 无分组的文件 配置列表添加到结果列表
        // 没有分组 去重
        ArrayList<Meta.FileConfig.FileInfo> newFileInfoList = new ArrayList<>(fileInfoList.stream()
                // 没分组
                .filter(fileInfo -> StrUtil.isBlank(fileInfo.getGroupKey()))
                .collect(
                        // 按照输入路径去重
                        // o: 旧值
                        // r: 新值
                        Collectors.toMap(Meta.FileConfig.FileInfo::getOutputPath, o -> o, (e, r) -> r)
                ).values());


        resultList.addAll(newFileInfoList);

        return resultList;
    }

    /**
     * 模型去重
     *
     * @param modelInfoList
     * @return
     */
    public static List<Meta.ModelConfig.ModelInfo> distinctModels(List<Meta.ModelConfig.ModelInfo> modelInfoList) {

        // 1. 将所有分组配置 (modelInfo) 分为有分组 和 无分组的


        // 先处理有分组的模型
        Map<String, List<Meta.ModelConfig.ModelInfo>> groupKeyModelInfoListMap = modelInfoList.stream()
                // { "groupKey": "a", "models": [1,2]},{ "groupKey": "a", "models": [2,3]},{ "groupKey": "b", "models": [3,4]}
                .filter(modelInfo -> StrUtil.isNotBlank(modelInfo.getGroupKey()))
                // 目的: 将同组的模型合并到一起
                // { "groupKey": "a", "models: [[1,2], [2,3]]},{ "groupKey": "b", "models: [[3,4]]}
                .collect(
                        // 快速分组
                        Collectors.groupingBy(modelInfo -> modelInfo.getGroupKey())
                );


        // 2. 队友有分组的模型配置，如果有相同的分组，同分组的模型会进行合并(merge)，不同分组课同时保留

        // 同组内进配置合并
        // { "groupKey": "a", "models: [[ 1, 2 ], [ 2, 3 ]]}
        // { "groupKey": "a", "models: [ 1, 2, 2, 3 ]}
        // { "groupKey": "a", "models: [ 1, 2, 3 ]}

        // 合并后的对象 map
        Map<String, Meta.ModelConfig.ModelInfo> groupKeyMergedModelInfoMap = new HashMap<>();

        // entrySet: 常用于遍历 Map 中的所有键值对
        // groupKeyModelInfoListMap 理解为: { "groupKey": "a", "models: [[ 1, 2 ], [ 2, 3 ]]}
        for (Map.Entry<String, List<Meta.ModelConfig.ModelInfo>> entry : groupKeyModelInfoListMap.entrySet()) {
            // 所有的模型列表 = [[1,2], [2,3]]
            // tempModelInfoList 理解为: [[ 1, 2 ], [ 2, 3 ]]
            List<Meta.ModelConfig.ModelInfo> tempModelInfoList = entry.getValue();
            // newModelInfoList 理解为: [1,2,3]
            List<Meta.ModelConfig.ModelInfo> newModelInfoList = new ArrayList<>(tempModelInfoList.stream()
                    // 打平/扁平化 [[1,2],[2,3]] =>  [1,2,3]
                    // flatMap: 将一个对象 转为 多个对象
                    // Map: 一个对象 映射为 1个对象
                    .flatMap(modelInfo -> modelInfo.getModels().stream())
                    .collect(
                            // 按照输入路径进行去重
                            Collectors.toMap(Meta.ModelConfig.ModelInfo::getFieldName, o -> o, (e, r) -> r)

                    ).values());

            // 同组配置信息的覆盖
            // 取最后一个模型配置信息
            Meta.ModelConfig.ModelInfo newModelInfo = CollUtil.getLast(tempModelInfoList);
            newModelInfo.setModels(newModelInfoList);
            String groupKey = entry.getKey();
            groupKeyMergedModelInfoMap.put(groupKey, newModelInfo);

        }


        // 3. 创建新的模型配置列表(结果列表)，先将 合并后的分组 添加到结果列表
        ArrayList<Meta.ModelConfig.ModelInfo> resultList = new ArrayList<>(groupKeyMergedModelInfoMap.values());

        // 4. 再将 无分组的模型 配置列表添加到结果列表
        // 没有分组 去重
        ArrayList<Meta.ModelConfig.ModelInfo> newModelInfoList = new ArrayList<>(modelInfoList.stream()
                // 没分组
                .filter(modelInfo -> StrUtil.isBlank(modelInfo.getGroupKey()))
                .collect(
                        // 按照输入路径去重
                        Collectors.toMap(Meta.ModelConfig.ModelInfo::getFieldName, o -> o, (e, r) -> r)
                ).values());


        resultList.addAll(newModelInfoList);

        return resultList;
    }

}
