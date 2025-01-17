package com.wjp.maker.generator;

import java.io.*;

public class JarGenerator {
    public static void main(String[] args) throws IOException, InterruptedException {
        doGenerate("D:\\fullStack\\wcode-generator\\wcode-generator-maker\\generated");
    }
    public static void doGenerate(String projectDir) throws IOException, InterruptedException {
        // 调用 Process 类 执行 Maven 打包命令
        String winMavenCommand = "mvn.cmd clean package -DskipTests=true";
        String OtherMavenCommand = "mvn clean package -DskipTests=true";
        String mavenCommand = OtherMavenCommand;


        ProcessBuilder processBuilder = new ProcessBuilder(mavenCommand.split(" "));
        processBuilder.directory(new File(projectDir));

        // 启动命令
        Process process = processBuilder.start();

        // 读取命令的输出
        InputStream inputStream = process.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while((line = bufferedReader.readLine()) != null) {
            System.out.println(line);
        }

        int exitCode = process.waitFor();
        System.out.println("命令执行结束,退出码: " + exitCode);


    }
}
