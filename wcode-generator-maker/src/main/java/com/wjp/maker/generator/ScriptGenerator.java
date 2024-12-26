package com.wjp.maker.generator;

import cn.hutool.core.io.FileUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

public class ScriptGenerator {
    public static void doGenerate(String outputPath, String jarPath) {
        // Linux 脚本
        // #!/bin/bash
        //# 设置 Java 的文件编码为 UTF-8，确保 Java 程序能够正确处理中文
        //java -Dfile.encoding=UTF-8 -jar target/wcode-generator-basic-1.0-SNAPSHOT-jar-with-dependencies.jar "$@"

        StringBuilder sb = new StringBuilder();


        // 检测操作系统
        String osName = System.getProperty("os.name").toLowerCase();

        if(osName.contains("linux") || osName.contains("mac")) {
            // Linux 脚本
            sb.append("#!/bin/bash").append("\n");
            sb.append(String.format("java -jar %s \"$@\"", jarPath)).append("\n");
            FileUtil.writeBytes(sb.toString().getBytes(StandardCharsets.UTF_8), outputPath);
            // 添加可执行权限
            Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rwxrwxrwx");

            // 这里trycatch的原因是，window是无法设置权限的，所以这里用trycatch来捕获异常
            // 设置文件的最高权限
            try {
                Files.setPosixFilePermissions(Paths.get(outputPath), permissions);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // 生成bat文件
            FileUtil.writeBytes(sb.toString().getBytes(StandardCharsets.UTF_8), outputPath + ".bat");
        } else {
            // Windows 脚本
            //@echo off
            //java -jar target/yuzi-generator-basic-1.0-SNAPSHOT-jar-with-dependencies.jar %*
            sb.append("@echo off").append("\n");
            sb.append(String.format("java -jar %s %%*", jarPath)).append("\n");
            // 生成bat文件
            FileUtil.writeBytes(sb.toString().getBytes(StandardCharsets.UTF_8), outputPath + ".bat");
        }

    }
}
