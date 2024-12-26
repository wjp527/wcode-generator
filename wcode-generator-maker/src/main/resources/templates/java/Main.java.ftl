package ${basePackage};

import ${basePackage}.cli.CommandExecutor;

public class Main {

    public static void main(String[] args) {
        // 根据用户输入的参数，调用对应的命令执行器
//        args = new String[]{"generate", "-l", "-DataModel.java.ftl", "-o"};
        // 动态获取用户输入参数的参数类型
//        args = new String[]{"config"};
        // 获取用户的文件列表
//        args = new String[]{"list"};
        CommandExecutor commandExecutor = new CommandExecutor();
        commandExecutor.doExecute(args);
    }
}
