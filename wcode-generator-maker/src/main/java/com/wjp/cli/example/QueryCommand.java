package com.wjp.cli.example;

import picocli.CommandLine;

@CommandLine.Command(name = "query", description = "查询", mixinStandardHelpOptions = true)
public class QueryCommand implements Runnable {
    public void run() {
        System.out.println("执行查询命令");
    }
}