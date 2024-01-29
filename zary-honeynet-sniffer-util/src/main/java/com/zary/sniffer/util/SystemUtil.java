package com.zary.sniffer.util;


import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.security.ProtectionDomain;

public class SystemUtil {
    /**
     * 获取当前执行路径
     *
     * @return classes目录或jar包所在目录
     * @throws Exception
     */
    public static String getExecutePath() throws Exception {
        ProtectionDomain protectionDomain = SystemUtil.class.getProtectionDomain();
        return getExecutePath(protectionDomain);
    }

    public static String getExecutePath(ProtectionDomain protectionDomain) throws URISyntaxException {
        CodeSource codeSource = protectionDomain.getCodeSource();
        URI location = (codeSource == null ? null : codeSource.getLocation().toURI());
        String path = (location == null ? null : location.getSchemeSpecificPart());
        File root = new File(path);
        //本地执行时返回classes路径；打包成jar包时返回jar包所在路径；
        if (path.endsWith("/") || path.endsWith("\\")) return root.getAbsolutePath();
        else return root.getParent();
    }
}