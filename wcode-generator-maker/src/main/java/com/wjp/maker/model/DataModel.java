package com.wjp.maker.model;

import lombok.Data;

/**
 * 静态模版配置
 */
@Data
public class DataModel {

    /**
     * 让我们先明确几个动态生成的需求
     *
     * 1. 在代码开头增加作者 `@Author` 注释 (`增加`代码)
     * 2. 修改程序输出的信息提示 (`替换`代码)
     * 3. 将循环读取输入 改为 单次读取 (`可选`代码)
     */

    /**
     * 作者名称
     */
    private String author = "wjp";

    /**
     * 输出信息
     */
    private String outputText = "sum = ";


    /**
     * 是否循环(开关)
     */
    private boolean loop;

}
