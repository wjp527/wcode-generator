package com.wjp.cli.pattern;

public class Client {
    public static void main(String[] args) {
        // 创建接收者对象
        Device tv = new Device("TV");
        Device stereo = new Device("Stereo");

        // 创建具体命令对象，可以绑定不同设备
        TurnOnCommand turnOn = new TurnOnCommand(tv);
        TurnOffCommand turnOff = new TurnOffCommand(stereo);

        // 创建 调用者/请求者【遥控器】
        RemoteControl remote = new RemoteControl();

        // 因为TurnOnCommand、TurnOffCommand是Command接口的实现类，所以可以作为参数传入
        // 执行命令【设置不同功能的按键】
        remote.setCommand(turnOn);
        remote.pressButton();

        remote.setCommand(turnOff);
        remote.pressButton();

    }
}
