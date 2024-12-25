package com.wjp.cli.pattern;

/**
 * 命令模式：关闭设备命令【遥控器上的按键】
 */
public class TurnOffCommand implements Command {
    // 创建一个对象
    private Device device;

    /**
     * 构造函数 - 关闭设备之前，必须要与该设备进行绑定，这样才能执行关闭设备操作
     * @param device
     */
    public TurnOffCommand(Device device) {
        this.device = device;
    }

    /**
     * 执行关闭设备操作
     */
    @Override
    public void execute() {
        device.turnOff();
    }
}
