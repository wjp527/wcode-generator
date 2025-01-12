package com.wjp.web.utils;

public class FileUtils {
    /**
     * 获取缓存文件路径
     * @param id
     * @param disPath
     * @return
     */
    public static String  getCacheFilePath(long id, String disPath) {
        String projectPath = System.getProperty("user.dir");
        String tempDirPath = String.format("%s/.temp/cache/%s", projectPath, id);
        String zipFilePath = tempDirPath + "/" + disPath;
        return zipFilePath;
    }
}
