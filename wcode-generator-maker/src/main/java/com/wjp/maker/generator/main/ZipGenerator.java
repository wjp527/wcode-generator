package com.wjp.maker.generator.main;


/**
 * 生成代码生成器压缩包
 */
public class ZipGenerator extends GenerateTemplate {

    /**
     * 生成代码生成器压缩包
     * @param outputPath
     * @param sourceCopyDestPath
     * @param shellOutputPath
     * @param jarPath
     * @return
     */
    @Override
    protected String buildDist(String outputPath, String sourceCopyDestPath, String shellOutputPath, String jarPath) {
        String distPath = super.buildDist(outputPath, sourceCopyDestPath, shellOutputPath, jarPath);
        return super.buildZip(distPath);
    }
}
