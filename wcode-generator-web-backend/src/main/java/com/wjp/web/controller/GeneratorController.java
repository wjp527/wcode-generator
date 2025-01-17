package com.wjp.web.controller;

import cn.hutool.core.codec.Base64Encoder;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.*;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wjp.maker.generator.main.GenerateTemplate;
import com.wjp.maker.generator.main.ZipGenerator;
import com.wjp.maker.meta.MetaValidator;
import com.wjp.web.annotation.AuthCheck;
import com.wjp.web.codegeneratorzmq.CodeGeneratorMessageProducer;
import com.wjp.web.common.BaseResponse;
import com.wjp.web.common.DeleteRequest;
import com.wjp.web.common.ErrorCode;
import com.wjp.web.common.ResultUtils;
import com.wjp.web.constant.UserConstant;
import com.wjp.web.exception.BusinessException;
import com.wjp.web.exception.ThrowUtils;
import com.wjp.web.manager.CacheManager;
import com.wjp.web.manager.CosManager;
import com.wjp.maker.meta.Meta;
import com.wjp.web.model.dto.generator.*;
import com.wjp.web.model.dto.user.UserMatchRequest;
import com.wjp.web.model.entity.Generator;
import com.wjp.web.model.entity.User;
import com.wjp.web.model.enums.FileUploadBizEnum;
import com.wjp.web.model.vo.GeneratorVO;
import com.wjp.web.service.GeneratorService;
import com.wjp.web.service.UserService;
import com.wjp.web.utils.CosDownloadUtils;
import com.wjp.web.utils.FileUtils;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


/**
 * 帖子接口
 *
 * @author <a href="https://github.com/liwjp">程序员鱼皮</a>
 * @from <a href="https://wjp.icu">编程导航知识星球</a>
 */
@RestController
@RequestMapping("/generator")
@Slf4j
public class GeneratorController {

    @Resource
    private GeneratorService generatorService;

    @Resource
    private UserService userService;

    /**
     * 下载文件
     */
    @Autowired
    private CosDownloadUtils cosDownloadUtils;

    /**
     * COS 管理器
     */
    @Resource
    private CosManager cosManager;

    /**
     * 代码生成器消息生产者
     */
    @Resource
    private CodeGeneratorMessageProducer codeGeneratorMessageProducer;


//    @Resource
//    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private CacheManager cacheManager;

    // region 增删改查

    /**
     * 创建
     *
     * @param generatorAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addGenerator(@RequestBody GeneratorAddRequest generatorAddRequest, HttpServletRequest request) {
        if (generatorAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Generator generator = new Generator();
        BeanUtils.copyProperties(generatorAddRequest, generator);

        // 标签
        List<String> tags = generatorAddRequest.getTags();
        if (tags != null) {
            generator.setTags(JSONUtil.toJsonStr(tags));
        }

        // 文件配置
        Meta.FileConfig fileConfig = generatorAddRequest.getFileConfig();
        if (fileConfig != null) {
            // 转为 json 字符串，存入数据库
            generator.setFileConfig(JSONUtil.toJsonStr(fileConfig));
        }

        // 模型配置
        Meta.ModelConfig modelConfig = generatorAddRequest.getModelConfig();
        if (modelConfig != null) {
            // 转为 json 字符串，存入数据库
            generator.setModelConfig(JSONUtil.toJsonStr(modelConfig));
        }

        generatorService.validGenerator(generator, true);
        User loginUser = userService.getLoginUser(request);
        generator.setUserId(loginUser.getId());
        generator.setDownLoadNum(0L);
        generator.setStatus(0);
        boolean result = generatorService.save(generator);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newGeneratorId = generator.getId();
        return ResultUtils.success(newGeneratorId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteGenerator(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Generator oldGenerator = generatorService.getById(id);
        ThrowUtils.throwIf(oldGenerator == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldGenerator.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = generatorService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param generatorUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateGenerator(@RequestBody GeneratorUpdateRequest generatorUpdateRequest) {
        if (generatorUpdateRequest == null || generatorUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Generator generator = new Generator();
        BeanUtils.copyProperties(generatorUpdateRequest, generator);
        // 标签
        List<String> tags = generatorUpdateRequest.getTags();
        if (tags != null) {
            generator.setTags(JSONUtil.toJsonStr(tags));
        }

        // 文件配置
        Meta.FileConfig fileConfig = generatorUpdateRequest.getFileConfig();
        if (fileConfig != null) {
            // 转为 json 字符串，存入数据库
            generator.setFileConfig(JSONUtil.toJsonStr(fileConfig));
        }

        // 模型配置
        Meta.ModelConfig modelConfig = generatorUpdateRequest.getModelConfig();
        if (modelConfig != null) {
            // 转为 json 字符串，存入数据库
            generator.setModelConfig(JSONUtil.toJsonStr(modelConfig));
        }
        // 参数校验
        generatorService.validGenerator(generator, false);
        long id = generatorUpdateRequest.getId();
        // 判断是否存在
        Generator oldGenerator = generatorService.getById(id);
        ThrowUtils.throwIf(oldGenerator == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = generatorService.updateById(generator);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<GeneratorVO> getGeneratorVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Generator generator = generatorService.getById(id);
        if (generator == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        GeneratorVO generatorVO = generatorService.getGeneratorVO(generator, request);

        return ResultUtils.success(generatorVO);
    }

    /**
     * 分页获取列表（仅管理员）
     *
     * @param generatorQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Generator>> listGeneratorByPage(@RequestBody GeneratorQueryRequest generatorQueryRequest) {
        long current = generatorQueryRequest.getCurrent();
        long size = generatorQueryRequest.getPageSize();
        Page<Generator> generatorPage = generatorService.page(new Page<>(current, size),
                generatorService.getQueryWrapper(generatorQueryRequest));
        return ResultUtils.success(generatorPage);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param generatorQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo/fast")
    public BaseResponse<Page<GeneratorVO>> listGeneratorVOByPageFast(@RequestBody GeneratorQueryRequest generatorQueryRequest,
                                                                 HttpServletRequest request) {
        long current = generatorQueryRequest.getCurrent();
        long size = generatorQueryRequest.getPageSize();


        // 获取缓存的 key
        String cacheKey = getPageCacheKey(generatorQueryRequest);
        // 获取操作对象【Caffeine 缓存】
        Object cacheValue = cacheManager.get(cacheKey);
        // 有缓存
        if(cacheValue != null) {
            return ResultUtils.success((Page<GeneratorVO>) cacheValue);
        }

        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        QueryWrapper<Generator> queryWrapper = generatorService.getQueryWrapper(generatorQueryRequest);
        // 优化，只返回必要的参数，如果要进入详情，那就调用接口，这样做的原因是: 用户访问的多了，页面加载的就会越慢，影响用户体验，优化数据库查询，禁止 直接用 * 查询字段
        queryWrapper.select(
                "id","name","description","tags","picture","status","userId","createTime","updateTime"
        );
        Page<Generator> generatorPage = generatorService.page(new Page<>(current, size),
                queryWrapper);

        Page<GeneratorVO> generatorVOPage = generatorService.getGeneratorVOPage(generatorPage, request);

//        这种方法也是可以的，她说最方便，最快捷的，但是用了多级缓存之后，就不需要他了，【Caffeine + Redis】
//        // 只返回必要的参数，如果要进入详情，那就调用接口，这样做的原因是: 用户访问的多了，页面加载的就会越慢，影响用户体验
//        generatorVOPage.getRecords().forEach(item -> {
//            item.setFileConfig(null);
//            item.setModelConfig(null);
//        });


        // 写入缓存【Caffeine 缓存】
        // 缓存过期时间设置为 100 分钟 ✨✨✨
        cacheManager.put(cacheKey, generatorVOPage);

        return ResultUtils.success(generatorVOPage);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param generatorQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<GeneratorVO>> listGeneratorVOByPage(@RequestBody GeneratorQueryRequest generatorQueryRequest,
                                                                 HttpServletRequest request) {
        long current = generatorQueryRequest.getCurrent();
        long size = generatorQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);

        // 计时
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Page<Generator> generatorPage = generatorService.page(new Page<>(current, size),
                generatorService.getQueryWrapper(generatorQueryRequest));
        stopWatch.stop();
        log.info("查询生成器耗时：{} ms", stopWatch.getTotalTimeMillis());

        // 计时
        stopWatch = new StopWatch();
        stopWatch.start();
        Page<GeneratorVO> generatorVOPage = generatorService.getGeneratorVOPage(generatorPage, request);
        stopWatch.stop();
        log.info("查询关联信息耗时：{} ms", stopWatch.getTotalTimeMillis());
        return ResultUtils.success(generatorVOPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param generatorQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<GeneratorVO>> listMyGeneratorVOByPage(@RequestBody GeneratorQueryRequest generatorQueryRequest,
                                                                   HttpServletRequest request) {
        if (generatorQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        generatorQueryRequest.setUserId(loginUser.getId());
        long current = generatorQueryRequest.getCurrent();
        long size = generatorQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Generator> generatorPage = generatorService.page(new Page<>(current, size),
                generatorService.getQueryWrapper(generatorQueryRequest));
        return ResultUtils.success(generatorService.getGeneratorVOPage(generatorPage, request));
    }

    // endregion


    /**
     * 编辑（用户）
     *
     * @param generatorEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editGenerator(@RequestBody GeneratorEditRequest generatorEditRequest, HttpServletRequest request) {
        if (generatorEditRequest == null || generatorEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Generator generator = new Generator();
        BeanUtils.copyProperties(generatorEditRequest, generator);
        // 标签
        List<String> tags = generatorEditRequest.getTags();
        if (tags != null) {
            generator.setTags(JSONUtil.toJsonStr(tags));
        }

        // 文件配置
        Meta.FileConfig fileConfig = generatorEditRequest.getFileConfig();
        if (fileConfig != null) {
            // 转为 json 字符串，存入数据库
            generator.setFileConfig(JSONUtil.toJsonStr(fileConfig));
        }

        // 模型配置
        Meta.ModelConfig modelConfig = generatorEditRequest.getModelConfig();
        if (modelConfig != null) {
            // 转为 json 字符串，存入数据库
            generator.setModelConfig(JSONUtil.toJsonStr(modelConfig));
        }
        // 参数校验
        generatorService.validGenerator(generator, false);
        User loginUser = userService.getLoginUser(request);
        long id = generatorEditRequest.getId();
        // 判断是否存在
        Generator oldGenerator = generatorService.getById(id);
        ThrowUtils.throwIf(oldGenerator == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldGenerator.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = generatorService.updateById(generator);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 下载 代码生成器
     *
     * @param id
     * @return
     */
    @GetMapping("/download")
    public void downloadGeneratorById(long id, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Generator generator = generatorService.getById(id);
        if (generator == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        String filepath = generator.getDistPath();
        if (StrUtil.isBlank(filepath)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "产物包不存在");
        }

        // 追踪事件
        log.info("用户 {} 下载了 {}", loginUser, filepath);


        // 设置响应头
        response.setContentType("application/octet-stream;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + filepath);

        // 有限从缓存读取
        String zipFilePath = FileUtils.getCacheFilePath(id, filepath);
        // 如果有缓存，则直接读取缓存
        if(FileUtil.exist(zipFilePath)) {
            // 写入响应
            Files.copy(Paths.get(zipFilePath), response.getOutputStream());
            return ;
        } else {
            // 没有缓存
            // 取COS中 下载文件
            cosManager.downloadFile(filepath, response);
        }

    }


    /**
     * 使用代码生成器
     *
     * @param generatorUseRequest
     * @param request
     * @param response
     * @return
     */
    @PostMapping("/use")
    public void useGenerator(@RequestBody GeneratorUseRequest generatorUseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 1.获取用户输入的请求参数
        Long id = generatorUseRequest.getId();
        Map<String, Object> dataModel = generatorUseRequest.getDataModel();

        // 1-1.需要用户登录
        User loginUser = userService.getLoginUser(request);
        log.info("userId = {} 使用了生成器 id = {}", loginUser != null ? loginUser.getId() : null, id);
        if (loginUser == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);  // 设置为401
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "请先登录");
        }

        // 获取到 某一个 生成器信息
        Generator generator = generatorService.getById(id);
        if (generator == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        // 2.生成器的存储路径
        // 获取到存在 cos 中的 dist 路径
        String distPath = generator.getDistPath();
        if (StrUtil.isBlank(distPath)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "产物包不存在");
        }

        // 3.从对象存储下载生成器的压缩包

        // 3-1.定义独立的工作空间
        // 项目根目录
        String projectPath = System.getProperty("user.dir");
        // 临时的项目目录
        String tempDirPath = String.format("%s/.temp/use/%s", projectPath, id);
        // 临时的压缩包目录
        String zipFilePath = tempDirPath + "/dist.zip";

        // 如果不存在，则创建文件
        if (!FileUtil.exist(zipFilePath)) {
            FileUtil.touch(zipFilePath);
        }

        // 计时
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            // 下载文件
            cosManager.download(distPath, zipFilePath);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成器下载失败");
        }
        stopWatch.stop();
        log.info("下载文件耗时：{} ms", stopWatch.getTotalTimeMillis());

        // 3-2.解压压缩包，得到脚本文件
        // 计时
        stopWatch = new StopWatch();
        stopWatch.start();
        File unzipDistDir = ZipUtil.unzip(zipFilePath);
        stopWatch.stop();
        log.info("解压文件耗时：{} ms", stopWatch.getTotalTimeMillis());

        // 3-3.将用户输入的参数写到 json 文件中
        // 计时
        stopWatch = new StopWatch();
        stopWatch.start();
        String dataModelFilePath = tempDirPath + "/dataModel.json";
        // 将 json 转为 字符串
        String jsonStr = JSONUtil.toJsonStr(dataModel);
        // 写入到 dataModel.json 文件中
        FileUtil.writeUtf8String(jsonStr, dataModelFilePath);
        stopWatch.stop();
        log.info("写入文件耗时：{} ms", stopWatch.getTotalTimeMillis());

        // 4.执行脚本
        // 找到脚本文件所在路径
        // ✨要注意，如果不是 windows 系统，找 generator 文件而不是 bat
        File scriptFile = FileUtil.loopFiles(unzipDistDir, 2, null)
                // 流的方式
                .stream()
                // 进行过滤出 generator.bat 文件
                .filter(file -> file.isFile()
//                        && "generator.bat".equals(file.getName()))
                        && "generator".equals(file.getName()))
                // 找到第一个文件
                .findFirst()
                // 如果找不到，则抛出异常
                .orElseThrow(RuntimeException::new);

        // 4-1.添加可执行权限【Linux / Mac】
        try {
            Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rwxrwxrwx");
            Files.setPosixFilePermissions(scriptFile.toPath(), permissions);
        } catch (Exception e) {

        }

        // 5.构造命令
        File scriptDir = scriptFile.getParentFile();
//        // 注意，如果是 mac / linux 系统，要用 "./generator"
//        // D:/fullStack/wcode-generator/wcode-generator-web-backend/.temp/use/7/dist/generator.bat
//        String scriptAbsolutePath = scriptFile.getAbsolutePath().replace("\\", "/");
//        // 通过自动执行 json-generate 命令，自动将 dataModel.json 文件中的数据，自动生成代码
//        String[] commands = new String[] {scriptAbsolutePath, "json-generate", "--file=" + dataModelFilePath};

        // 构造命令
        // 注意，如果是 mac / linux 系统，要用 "./generator"
        String scriptAbsolutePath = scriptFile.getAbsolutePath();
        String[] commands = new String[]{scriptAbsolutePath, "json-generate", "--file=" + dataModelFilePath};

        // 这里一定要拆分！
        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        // D:\fullStack\wcode-generator\wcode-generator-web-backend\.temp\\use\7\dist\.gitignore
        processBuilder.directory(scriptDir);

        try {
            // 计时
            stopWatch = new StopWatch();
            stopWatch.start();
            // 启动进程
            Process process = processBuilder.start();

            // 读取命令的输出
            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                // 输出脚本的执行日志
                System.out.println("输出: " + line);
            }

            // 等待命令执行完成
            int exitCode = process.waitFor();
            System.out.println("命令执行结束，退出码：" + exitCode);
            stopWatch.stop();
            log.info("执行脚本文件耗时：{} ms", stopWatch.getTotalTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "执行生成器脚本错误");
        }


        // 6.压缩得到的生成结果，返回给前端
        String generatedPath = scriptDir.getAbsolutePath() + "/generated";
        String resultPath = tempDirPath + "/result.zip";
        // 压缩生成的结果 调用脚本生成最终可执行的代码压缩包
        // 计时
        stopWatch = new StopWatch();
        stopWatch.start();
        File resultFile = ZipUtil.zip(generatedPath, resultPath);
        stopWatch.stop();
        log.info("压缩文件耗时：{} ms", stopWatch.getTotalTimeMillis());

        // 设置响应头
        response.setContentType("application/octet-stream;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + resultFile.getName());
        // 输出文件到响应流
        try {
            // 输出文件到响应流
            Files.copy(resultFile.toPath(), response.getOutputStream());
        } catch (IOException e) {
            // 捕获异常，防止继续响应
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件传输失败");
        }

        // 7.清理文件【异步】
        CompletableFuture.runAsync(() -> {
            FileUtil.del(tempDirPath);
        });
    }


    /**
     * 制作代码生成器
     *
     * @param generatorMakeRequest
     * @param request
     * @param response
     */
    @PostMapping("/make")
    public void makeGenerator(@RequestBody GeneratorMakeRequest generatorMakeRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 1) 输入参数
        Meta meta = generatorMakeRequest.getMeta();
        String zipFilePath = generatorMakeRequest.getZipFilePath();

        // 需要用户登录
        User loginUser = userService.getLoginUser(request);
        log.info("userId = {} 在线制作生成器", loginUser.getId());

        if(loginUser == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "请先登录");
        }

        // 2) 创建独立的工作空间，下载压缩包到本地
        String projectPath = System.getProperty("user.dir");
        // 生成随机id
        String id = IdUtil.getSnowflakeNextId() + RandomUtil.randomString(6);
        // 生成临时文件目录
        String tempDirPath = String.format("%s/.temp/make/%s", projectPath, id);
        // 生成本地压缩包目录
        String localZipFilePath = tempDirPath + "/project.zip";

        // 如果不存在，则创建文件目录
        if (!FileUtil.exist(localZipFilePath)) {
            FileUtil.touch(localZipFilePath);
        }

        // 下载文件
        // 计时
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            cosManager.download(zipFilePath, localZipFilePath);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "压缩包下载失败");
        }
        stopWatch.stop();
        log.info("下载文件耗时：{} ms", stopWatch.getTotalTimeMillis());

        // 3）解压，得到项目模板文件
        // 计时
        stopWatch = new StopWatch();
        stopWatch.start();
        File unzipDistDir = ZipUtil.unzip(localZipFilePath);
        stopWatch.stop();
        log.info("解压文件耗时：{} ms", stopWatch.getTotalTimeMillis());

        // 4）构造 meta 对象和生成器的输出路径
        // 获取绝对路径
        String sourceRootPath = unzipDistDir.getAbsolutePath();
        // 设置 meta 对象的 sourceRootPath 属性
        meta.getFileConfig().setSourceRootPath(sourceRootPath);
        // 校验和处理默认值
        MetaValidator.doValidAndFill(meta);
        String outputPath = tempDirPath + "/generated/" +meta.getName() ;

        // 5）调用 maker 方法制作生成器
        // 计时
        stopWatch = new StopWatch();
        stopWatch.start();
        GenerateTemplate generateTemplate = new ZipGenerator();
        try {
            generateTemplate.doGenerate(meta, outputPath);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "制作失败");
        }
        stopWatch.stop();
        log.info("制作代码生成器耗时：{} ms", stopWatch.getTotalTimeMillis());

        // 6）下载制作好的生成器压缩包
        // 计时
        stopWatch = new StopWatch();
        stopWatch.start();
        String suffix = "-dist.zip";
        String zipFileName = meta.getName() + suffix;
        // 生成器压缩包的绝对路径
        String distZipFilePath = outputPath + suffix;

        // 设置响应头
        response.setContentType("application/octet-stream;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + zipFileName);
        Files.copy(Paths.get(distZipFilePath), response.getOutputStream());
        stopWatch.stop();
        log.info("下载代码生成器压缩包耗时：{} ms", stopWatch.getTotalTimeMillis());

        // 7）清理工作空间的文件
        CompletableFuture.runAsync(() -> {
            FileUtil.del(tempDirPath);
        });
    }


    /**
     * 制作代码生成器【异步 RabbitMQ】
     *
     * @param generatorMakeRequest
     * @param request
     * @param response
     */
    @PostMapping("/make/async")
    public void makeAsyncGenerator(@RequestBody GeneratorMakeRequest generatorMakeRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 1) 输入参数
        Meta meta = generatorMakeRequest.getMeta();
        String zipFilePath = generatorMakeRequest.getZipFilePath();

        // 需要用户登录
        User loginUser = userService.getLoginUser(request);
        log.info("userId = {} 在线制作生成器", loginUser.getId());

        if(loginUser == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "请先登录");
        }

        // 2) 创建独立的工作空间，下载压缩包到本地
        String projectPath = System.getProperty("user.dir");
        // 生成随机id
        String id = IdUtil.getSnowflakeNextId() + RandomUtil.randomString(6);
        // 生成临时文件目录
        String tempDirPath = String.format("%s/.temp/make/%s", projectPath, id);
        // 生成本地压缩包目录
        String localZipFilePath = tempDirPath + "/project.zip";

        // 如果不存在，则创建文件目录
        if (!FileUtil.exist(localZipFilePath)) {
            FileUtil.touch(localZipFilePath);
        }

        // 下载文件
        // 计时
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            cosManager.download(zipFilePath, localZipFilePath);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "压缩包下载失败");
        }
        stopWatch.stop();
        log.info("下载文件耗时：{} ms", stopWatch.getTotalTimeMillis());

        // 3）解压，得到项目模板文件
        // 计时
        stopWatch = new StopWatch();
        stopWatch.start();
        File unzipDistDir = ZipUtil.unzip(localZipFilePath);
        stopWatch.stop();
        log.info("解压文件耗时：{} ms", stopWatch.getTotalTimeMillis());

        // 4）构造 meta 对象和生成器的输出路径
        // 获取绝对路径
        String sourceRootPath = unzipDistDir.getAbsolutePath();
        // 设置 meta 对象的 sourceRootPath 属性
        meta.getFileConfig().setSourceRootPath(sourceRootPath);
        // 校验和处理默认值
        MetaValidator.doValidAndFill(meta);
        String outputPath = tempDirPath + "/generated/" +meta.getName() ;

        // 5）调用 maker 方法制作生成器
        Map<String, Object> message = new HashMap<>();
        message.put("meta", meta);
        message.put("outputPath", outputPath);
        // 发送消息
        codeGeneratorMessageProducer.sendMessage(message.toString());

        // 计时
        stopWatch = new StopWatch();
        stopWatch.start();
        GenerateTemplate generateTemplate = new ZipGenerator();
        try {
            generateTemplate.doGenerate(meta, outputPath);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "制作失败");
        }
        stopWatch.stop();
        log.info("制作代码生成器耗时：{} ms", stopWatch.getTotalTimeMillis());

        // 6）下载制作好的生成器压缩包
        // 计时
        stopWatch = new StopWatch();
        stopWatch.start();
        String suffix = "-dist.zip";
        String zipFileName = meta.getName() + suffix;
        // 生成器压缩包的绝对路径
        String distZipFilePath = outputPath + suffix;

        // 设置响应头
        response.setContentType("application/octet-stream;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + zipFileName);
        Files.copy(Paths.get(distZipFilePath), response.getOutputStream());
        stopWatch.stop();
        log.info("下载代码生成器压缩包耗时：{} ms", stopWatch.getTotalTimeMillis());

        // 7）清理工作空间的文件
        CompletableFuture.runAsync(() -> {
            FileUtil.del(tempDirPath);
        });
    }


    @PostMapping("/toLead")
    public BaseResponse<Meta> toLead(@RequestBody GeneratorToLeadRequest generatorToLeadRequest, HttpServletRequest request) {

        // 1.校验参数
        // 判断参数是否为空
        String key = generatorToLeadRequest.getKey();
        // type: modelConfig / fileConfig
        String type = generatorToLeadRequest.getType();

        if(key == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "key【路径】不能为空");
        }

        // key路径是否在cos中存在 && type 是否在FileUploadBizEnum枚举中定义
        FileUploadBizEnum configEnum = FileUploadBizEnum.getEnumByValue(type);
        if(configEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "type【类型】不正确");
        }

        // 2.判断用户是否登录
         User loginUser = userService.getLoginUser(request);
        if(loginUser == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "请先登录");
        }

        // 3.读取cos中的文件
        String projectPath = System.getProperty("user.dir");
        // 生成随机id
        String id = IdUtil.getSnowflakeNextId() + RandomUtil.randomString(6);
        // 生成临时文件目录
        String tempDirPath = String.format("%s/.temp/to_lead/%s", projectPath, id);
        // 生成模型配置文件目录
        String modelFilePath = tempDirPath + "/model.json";

        // 如果不存在，则创建文件目录
        if (!FileUtil.exist(modelFilePath)) {
            FileUtil.touch(modelFilePath);
        }

        // 下载文件
        try {
            cosManager.download(key, modelFilePath);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "压缩包下载失败");
        }




        // 进行Hutool工具类进行读取
        String Str = FileUtil.readUtf8String(modelFilePath);
        Meta Config = JSONUtil.toBean(Str, Meta.class);

        // 根据type，进行动态的对该文件进行filter筛选
        Meta ConfigFilter = new Meta();
        Map<String, Object> filterMap = new HashMap<>();
        switch (configEnum) {
            case GENERATOR_TO_LEAD_BY_MODEL_TEMPLATE:
                // 读取模板文件
                Meta.ModelConfig modelConfig = Config.getModelConfig();
                List<Meta.ModelConfig.ModelInfo> models = modelConfig.getModels();
                ConfigFilter.setModelConfig(modelConfig);
                break;
                case GENERATOR_TO_LEAD_BY_FILE_TEMPLATE:
                // 读取模板文件
                Meta.FileConfig fileConfig = Config.getFileConfig();
                List<Meta.FileConfig.FileInfo> fileInfoList = fileConfig.getFiles();
                ConfigFilter.setFileConfig(fileConfig);
                break;
            default:
                break;
        }



        // 7.清理文件【异步】
        CompletableFuture.runAsync(() -> {
            FileUtil.del(tempDirPath);
        });

        // 返回结果
        return ResultUtils.success(ConfigFilter);
    }


    /**
     * 缓存代码生成器
     * @param generatorCacheRequset
     * @param request
     * @param response
     * @throws IOException
     */
    @PostMapping("/cache")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public void cacheGeneratorById(@RequestBody GeneratorCacheRequset generatorCacheRequset, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if(generatorCacheRequset == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        long id = generatorCacheRequset.getId();
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Generator generator = generatorService.getById(id);
        if (generator == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        String distPath = generator.getDistPath();
        if (StrUtil.isBlank(distPath)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "产物包不存在");
        }

        // 追踪事件
        log.info("用户 {} 下载了 {}", loginUser, distPath);

        // 获取缓存文件路径
        String zipFilePath = FileUtils.getCacheFilePath(id, distPath);

        try {
            // 下载文件
            // distPath: 从哪开始下载
            // zipFilePath: 下载到哪个路径
            cosManager.download(distPath, zipFilePath);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "产物包下载失败");
        }

    }

    /**
     * 获取分页缓存 key
     * @param generatorQueryRequest
     * @return
     */
    public static String getPageCacheKey(GeneratorQueryRequest generatorQueryRequest) {
        String jsonStr = JSONUtil.toJsonStr(generatorQueryRequest);
        // 请求参数编码
        String base64 = Base64Encoder.encode(jsonStr);
        // 缓存key
        String key = "generator:page:" + base64;
        return key;
    }


    /**
     * 匹配用户
     * @param userMatchRequest
     * @param request
     * @return
     */
    @PostMapping("/match")
    public BaseResponse<List<Generator>> matchGenerators(@RequestBody UserMatchRequest userMatchRequest, HttpServletRequest request) {
        long num = userMatchRequest.getNum();
        if(num <= 0 || num >= 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "页数不对");
        }
        User loginUser = userService.getLoginUser(request);
        List<Generator> userList = generatorService.matchGenerators(num,loginUser);
        return ResultUtils.success(userList);
    }

}
