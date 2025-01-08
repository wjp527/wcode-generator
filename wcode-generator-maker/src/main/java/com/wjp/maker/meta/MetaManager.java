package com.wjp.maker.meta;


import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONUtil;



/**
 * 配置文件管理器
 */
public class MetaManager {

    // ✨ volatile关键字，保证多线程环境下，meta对象是线程安全的
    private static volatile Meta meta;

    /**
     * ✨双检锁单例模式
     * 解决 单例模式在初始化代码出现重复执行多次的问题
     *
     * @return
     */
    public static Meta getMetaObject() {
        // 之所以在这里套了层if判断语句，是因为,如果meta是为空，它进入到了synchronized代码块，然后在判断meta是否为空，不为空，会return出去，
        // 但是synchronized代码块执行完，其他线程进入到if判断语句，此时meta不为空，也会return出去，导致重复执行初始化代码，也会消耗内存，为什么不在它的外层多做一次判断呢？
        if (meta == null) {
            synchronized (MetaManager.class) {
                if (meta == null) {
                    meta = initMeta();

                }
            }
        }
        return meta;
    }

    // ✨读取配置文件【resource/meta.json】
    private static Meta initMeta() {
        // 读取配置文件【resource/meta.json】
        String metaJson = ResourceUtil.readUtf8Str("meta.json");
//        String metaJson = ResourceUtil.readUtf8Str("springboot-init-meta.json");
        // 反序列化为Meta对象
        Meta newMeta = JSONUtil.toBean(metaJson, Meta.class);
        // 校验配置文件、处理默认值
        MetaValidator.doValidAndFill(newMeta);
        return newMeta;
    }

}
