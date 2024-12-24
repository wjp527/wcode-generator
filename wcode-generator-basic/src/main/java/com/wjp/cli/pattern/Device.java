package com.wjp.cli.pattern;

/**
 * 命令模式 - 接收者【可以理解为电器】-设备类
 */
public class Device {
    private String name;

    public Device(String name) {
        this.name = name;
    }
    public void turnOn() {
        System.out.println(name + "设备打开");
    }

    public void turnOff() {
        System.out.println(name + "设备关闭");
    }
}
