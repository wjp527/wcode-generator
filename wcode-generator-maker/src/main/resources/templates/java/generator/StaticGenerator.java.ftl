package ${basePackage}.generator;

import cn.hutool.core.io.FileUtil;

import java.io.File;

/**
 * 静态文件生成器
 */
public class StaticGenerator {
    /**
     * 使用hutool复制文件
     * @param inputPath
     * @param outputPath
     */
    public static void copyFilesHuttol(String inputPath, String outputPath) {
        FileUtil.copy(inputPath, outputPath, true);
    }
}
