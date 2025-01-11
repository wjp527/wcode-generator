package com.wjp.web.manager;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.*;
import com.qcloud.cos.transfer.Download;
import com.qcloud.cos.transfer.TransferManager;
import com.qcloud.cos.utils.IOUtils;
import com.wjp.web.common.ErrorCode;
import com.wjp.web.config.CosClientConfig;
import com.wjp.web.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Cos 对象存储操作
 *
 * @author <a href="https://github.com/liwjp">程序员鱼皮</a>
 * @from <a href="https://wjp.icu">编程导航知识星球</a>
 */
@Component
@Slf4j
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    // 复用对象
    private TransferManager transferManager;

    // bean 加载完成后执行
    @PostConstruct
    public void init() {
        // 执行初始化逻辑
        System.out.println("Bean initialized!");
        // 多线程并发上传下载
        ExecutorService threadPool = Executors.newFixedThreadPool(32);
        transferManager = new TransferManager(cosClient, threadPool);
    }



    /**
     * 上传对象
     *
     * @param key           唯一键
     * @param localFilePath 本地文件路径
     * @return
     */
    public PutObjectResult putObject(String key, String localFilePath) {
        // PutObjectRequest: 是对象存储服务 SDK 提供的一个类，用于封装上传文件的请求参数。
        // cosClientConfig.getBucket(): 获取目标存储桶（Bucket）的名称。存储桶是对象存储服务中的一个逻辑分组，用于管理对象（文件）。cosClientConfig 是一个配置类或对象，用于管理存储桶的相关信息。
        // key: 上传后的文件路径（即目标文件名），决定了文件在对象存储中的存储位置。
        // new File(localFilePath): 通过 localFilePath 构造一个 Java 文件对象，指向本地的文件路径。
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                new File(localFilePath));
        // putObject(putObjectRequest): 客户端的 putObject 方法，用于上传文件到对象存储。
        // 参数是 putObjectRequest，即刚刚创建的请求对象，包含了存储桶名称、文件路径、和文件内容。
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 上传对象
     *
     * @param key  唯一键
     * @param file 文件
     * @return
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 下载对象 【流式下载】
     * @param key 对象唯一键
     * @return
     */
    public COSObject getObject(String key) {
        // 下载对象
        // 1. 创建 GetObjectRequest 对象，设置要下载的对象所在的 bucket 和 key
        // bucket是什么 是cos的存储桶，key是对象在桶中的唯一标识
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);

        // 2. 通过 COSClient 对象调用 getObject 方法，获取 COSObject 对象，COSObject 对象包含了对象内容和元数据信息
        COSObject cosObject = cosClient.getObject(getObjectRequest);
        return cosObject;
    }


    /**
     * 下载文件【流式下载】
     * @param filepath
     */
    public void downloadFile(String filepath, HttpServletResponse response) throws IOException {
        COSObjectInputStream cosObjectInput = null;
        // 有两种下载文件的方式:
        // 1. 直接下载到本地服务器上: 服务器需要进行处理这个文件，前端不需要
        // 2. 直接通过流进行下载文件: 直接传给前端 ✔
        try {
            COSObject cosObject = getObject(filepath);
            cosObjectInput = cosObject.getObjectContent();
            // 处理下载到的流
            byte[] bytes = IOUtils.toByteArray(cosObjectInput);
            // 设置响应头
            // 流式响应
            // application/octet-stream: 二进制流数据。对于大多数浏览器，这种类型会触发下载行为
            response.setContentType("application/octet-stream;charset=UTF-8");
            // 设置HTTP响应的文件名
            // setHeader(): 用于设置HTTP响应头部字段
            // Content-Disposition:
            // - HTTP头字段，用于指定响应的呈现方式
            // - attachment: 响应一个附件，客户端会下载文件
            // - filename= : 制定文件的下载名称
            //   - filepath: 是下载到客户端显示的名称
            response.setHeader("Content-Disposition", "attachment; filename=" + filepath);
            // 写入响应
            response.getOutputStream().write(bytes);
            // 刷新缓冲区
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("file download error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载失败");
        } finally {
            if (cosObjectInput != null) {
                // 用完流之后一定要调用 close()
                cosObjectInput.close();
            }
        }
    }


    /**
     * 下载对象到本地文件
     *
     * @param key 从哪开始下载
     * @param localFilePath 下载到本地哪个位置
     * @return
     * @throws InterruptedException
     */
    public Download download(String key, String localFilePath) throws InterruptedException {
        File downloadFile = new File(localFilePath);
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        Download download = transferManager.download(getObjectRequest, downloadFile);
        // 同步等待下载完成
        download.waitForCompletion();
        return download;
    }




}
