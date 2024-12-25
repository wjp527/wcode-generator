package com.wjp.maker.cli;

import com.wjp.maker.cli.command.ConfigCommand;
import com.wjp.maker.cli.command.GenerateCommand;
import com.wjp.maker.cli.command.ListCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * 命令行执行器【详单与遥控器，实际执行子命令 - 命令对象】
 */
// 命令对象
// 注解 @Command 定义命令对象，name 为命令名，mixinStandardHelpOptions 为是否混合标准帮助选项
@Command(name = "wcode", mixinStandardHelpOptions = true)
public class CommandExecutor implements Runnable {

    // 命令行对象
    private final CommandLine commandLine;

    // 静态代码块，初始化命令对象

    {
        // 注册子命令
        commandLine = new CommandLine(this)
                .addSubcommand(new GenerateCommand())
                .addSubcommand(new ConfigCommand())
                .addSubcommand(new ListCommand());
    }

    /**
     * 执行命令
     */
    @Override
    public void run() {
        // 不输入子命令时，最好给个友好提示
        System.out.println("请输入具体命令，或者输入 --help 查看命令提示");
    }

    /**
     * 执行命令
     * @param args
     * @return
     */
    public Integer doExecute(String[] args) {
        return commandLine.execute(args);
    }
}
