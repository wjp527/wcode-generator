package com.wjp.cli.pattern;

/**
 * 命令模式 - 请求者 - 遥控器
 */
public class RemoteControl {
    // 创建遥控器对象
    private Command command;

    // 设置按键的命令【它可以接受一个命令对象】
    public void setCommand(Command command) {
        this.command = command;
    }

    // 按下按键
    public void pressButton() {
        command.execute();
    }

}
