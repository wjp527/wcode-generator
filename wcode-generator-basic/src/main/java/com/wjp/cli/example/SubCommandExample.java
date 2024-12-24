package com.wjp.cli.example;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "main", mixinStandardHelpOptions = true, subcommands = {AddCommand.class, DeleteCommand.class, QueryCommand.class})
public class SubCommandExample implements Runnable {

    @Override
    public void run() {
        System.out.println("执行主命令");
    }


    public static void main(String[] args) {
        // 执行主命令
//        String[] myArgs = new String[] { };
        // 查看主命令的帮助手册
        String[] myArgs = new String[]{"--help"};
        // 执行增加命令
//        String[] myArgs = new String[] { "add" };
        // 执行增加命令的帮助手册
//        String[] myArgs = new String[] { "add", "--help" };
        // 执行不存在的命令，会报错
//        String[] myArgs = new String[] { "update" };
        int exitCode = new CommandLine(new SubCommandExample()).execute(myArgs);
        System.exit(exitCode);
    }
}
