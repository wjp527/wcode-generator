package com.wjp.cli.example;

import picocli.CommandLine;

@CommandLine.Command(name = "add", description = "增加", mixinStandardHelpOptions = true)
public class AddCommand implements Runnable {
    @Override
    public void run() {
        System.out.println("执行增加命令");
    }
}
