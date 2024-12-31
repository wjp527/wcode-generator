package com.wjp.maker.generator.main;


/**
 * 生成代码生成器
 */
public class MainGenerator extends GenerateTemplate {

    @Override
    protected void buildDist(String outputPath, String sourceCopyDestPath, String shellOutputPath, String jarPath) {
        System.out.println("不用生成 dist 了");
    }
}
