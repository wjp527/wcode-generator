package com.wjp.cli.example;

import picocli.CommandLine;

@CommandLine.Command(name = "delete", description = "删除", mixinStandardHelpOptions = true)
public class DeleteCommand implements Runnable {
    public void run() {
        System.out.println("执行删除命令");
    }
}
