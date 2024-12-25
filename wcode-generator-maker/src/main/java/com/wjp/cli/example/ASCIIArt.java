package com.wjp.cli.example;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

// 命令行参数解析
// 实现一个ASCIIArt命令行工具，可以将输入的文字转换为ASCII art。

// 实现步骤：
// 1. 定义一个类，继承Runnable接口，并添加@Command注解，指定命令名称、版本、帮助信息等。
@Command(name = "ASCIIArt", version = "ASCIIArt 1.0", mixinStandardHelpOptions = true)
public class ASCIIArt implements Runnable {
    // 2. 在类中添加@Option注解，指定选项名称、描述、默认值等。
    @Option(names = { "-s", "--font-size" }, description = "Font size")
    int fontSize = 19;

    // 3. 在类中添加@Parameters注解，指定参数名称、描述、默认值等。
    @Parameters(paramLabel = "<word>", defaultValue = "Hello, picocli", 
               description = "Words to be translated into ASCII art.")
    private String[] words = { "Hello,", "picocli" };

    // 4. 实现run()方法，在方法中实现业务逻辑。
    @Override
    public void run() {
        // 自己实现业务逻辑
        System.out.println("fontSize = " + fontSize);
        System.out.println("words = " + String.join(",", words));
    }

    public static void main(String[] args) {
        // 5. 在main()方法中，创建CommandLine对象，并调用execute()方法，传入命令行参数。
        int exitCode = new CommandLine(new ASCIIArt()).execute(args);
        // 6. 调用System.exit()方法，退出程序。
        System.exit(exitCode); 
    }
}
