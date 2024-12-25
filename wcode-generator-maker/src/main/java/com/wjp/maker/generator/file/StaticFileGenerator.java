package com.wjp.maker.generator.file;

import cn.hutool.core.io.FileUtil;

import java.io.File;

/**
 * 静态文件生成器
 */
public class StaticFileGenerator {
    public static void main(String[] args) {
        // 生成最外层的项目根目录: D:\fullStack\wcode-generator
        String projectPath = System.getProperty("user.dir");

        // 输入路径: D:\fullStack\wcode-generator\wcode-generator-demo-projects\acm-template
        // File.separator: 文件分隔符，在不同系统中可能不同，比如在Windows系统中是"\"，在Linux系统中是"/"
        String inputPath = projectPath + File.separator + "wcode-generator-demo-projects" + File.separator + "acm-template";
        // 输出路径
        String outputPath = projectPath;
        // 复制
        copyFilesHuttol(inputPath, outputPath);
    }

    /**
     * 使用hutool复制文件
     * @param inputPath
     * @param outputPath
     */
    public static void copyFilesHuttol(String inputPath, String outputPath) {
        FileUtil.copy(inputPath, outputPath, true);
    }
}
