package com.wjp.web.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

// Junit5 才可以这么写
@SpringBootTest
class CosManagerTest {

    @Resource
    private CosManager cosManager;

    /**
     * 删除单个文件
     */
    @Test
    void deleteObject() {
        cosManager.deleteObject("/test/9.png");
    }

    /**
     * 删除多个文件
     */
    @Test
    void deleteObjects() {
        // ✨一定不要在 test/8.png 前面加 /
        cosManager.deleteObjects(Arrays.asList("test/8.png", "test/3.png"));
    }

    /**
     * 删除目录
     */
    @Test
    void deleteDir() {
        // ✨要删除 test文件夹，必须 /test/ 这样写，如果后面不加"/",如果你对象存储中有test1的文件夹，他会把test1给删掉
        cosManager.deleteDir("/test/");
    }
}