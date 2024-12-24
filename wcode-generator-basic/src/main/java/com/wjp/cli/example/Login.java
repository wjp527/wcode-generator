package com.wjp.cli.example;

import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

// 解释代码
// 1. 定义一个类 Login，实现 Callable 接口
public class Login implements Callable<Integer> {
    // 2. 定义两个选项：user 和 password
    @Option(names = {"-u", "--user"}, description = "User name")
    String user;

    // interactive: 交互式输入(true: 交互式输入，false: 非交互式输入)
    // arity: 0..1 表示参数可以有 0 个或 1 个
    @Option(names = {"-p" ,"--password"},arity = "0..1", description = "Passphrase", interactive = true)
    String password;

    @Option(names = {"-cp", "--checkPassword"}, arity = "0..1",description = "Check Password", interactive = true)
    String checkPassword;

    // 3. 实现 call 方法，打印 password 值
    public Integer call() throws Exception {
        System.out.println("password = " + password);
        System.out.println("checkPassword = " + checkPassword);
        return 0;
    }

    // 5. 运行 main 方法，打印输出
    public static void main(String[] args) {
        // 4. 定义 main 方法，创建 CommandLine 对象，调用 execute 方法，传入参数
        new CommandLine(new Login()).execute("-u", "user123", "-p", "xxx", "-cp","123");
    }
}
