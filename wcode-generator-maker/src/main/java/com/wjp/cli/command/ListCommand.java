package com.wjp.cli.command;

import cn.hutool.core.io.FileUtil;
import picocli.CommandLine;

import java.io.File;
import java.util.List;

/**
 * 列出所有模板
 */
@CommandLine.Command(name = "list", mixinStandardHelpOptions = true)
// 交互式输入就 Callable
// 非交互式输入就 Runnable
public class ListCommand implements Runnable{

    @Override
    public void run() {
        // 获取项目根目录: D:\fullStack\wcode-generator\wcode-generator-maker
        String projectPath = System.getProperty("user.dir");

        // 获取整个项目的根目录 就是 D:\fullStack\wcode-generator
        File parentFile = new File(projectPath).getParentFile();

        // 输入路径
        String inputPath = new File(parentFile, "wcode-generator-demo-projects/acm-template").getAbsolutePath();

        // 获取所有文件
        List<File> files = FileUtil.loopFiles(inputPath);
        for (File file : files) {
            System.out.println(file);
        }
    }
}
