package com.zary.sniffer.agent.core.plugin.loader;


import com.zary.sniffer.util.SystemUtil;
import com.zary.sniffer.agent.core.log.LogUtil;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 探针自定义类加载器
 * 为父类classloader提供对plugins插件目录下面的包扫描能力
 */
public class AgentClassLoader extends ClassLoader {
    /**
     * 扫描范围：文件夹
     */
    private List<File> jarDirPaths;
    /**
     * 扫描范围：jar包文件
     */
    private List<File> jarFiles;

    /**
     * 根据参数初始化
     */
    public AgentClassLoader(ClassLoader parent, String[] dirs) {
        super(parent);
        initPaths(dirs);
        initJars();
    }

    /**
     * 初始化文件夹范围
     *
     */
    private void initPaths(String[] dirs) {
        String root = "";
        jarDirPaths = new ArrayList<File>();
        try {
            root = SystemUtil.getExecutePath(AgentClassLoader.class.getProtectionDomain());
            for (String dir : dirs) {
                jarDirPaths.add(new File(root + "/" + dir));
            }
        } catch (Exception e) {
            LogUtil.error("Agent classloader init path failed. root:" + root, e);
        }
    }

    /**
     * 根据文件夹读取jar包列表
     */
    private void initJars() {
        jarFiles = new ArrayList<File>();
        try {
            for (File dir : jarDirPaths) {
                if (dir.exists() && dir.isDirectory()) {
                    String[] jarFileNames = dir.list(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            return name.toLowerCase().endsWith(".jar");
                        }
                    });
                    if (jarFileNames != null){
                        for (String fileName : jarFileNames) {
                            File file = new File(dir, fileName);
                            if (file.exists()) {
                                jarFiles.add(file);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error("Agent classloader init jars failed.", e);
        }
    }

    /**
     * 类查找
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        String path = name.replace('.', '/').concat(".class");
        for (File file : jarFiles) {
            try {
                JarFile jar = new JarFile(file);
                JarEntry entry = jar.getJarEntry(path);
                if (entry != null) {
                    byte[] data = readClassFile(file.getAbsolutePath(), name);
                    return defineClass(name, data, 0, data.length);
                }
            } catch (Exception e) {
                LogUtil.error("read class failed:",e);
            }
        }
        LogUtil.error("Agent classloader find class failed:",name);
        throw new ClassNotFoundException("Agent classloader find class failed:" + name);
    }

    /**
     * 资源查找
     *
     */
    @Override
    protected URL findResource(String name) {
        if (jarFiles != null && jarFiles.size() > 0) {
            for (File file : jarFiles) {
                try {
                    JarFile jar = new JarFile(file);
                    JarEntry entry = jar.getJarEntry(name);
                    if (entry != null) {
                        return new URL("jar:file:" + file.getAbsolutePath() + "!/" + name);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 资源查找
     *
     */
    @Override
    protected Enumeration<URL> findResources(String name) throws IOException {
        List<URL> allResources = new LinkedList<URL>();
        for (File file : jarFiles) {
            JarFile jar = new JarFile(file);
            JarEntry entry = jar.getJarEntry(name);
            if (entry != null) {
                allResources.add(new URL("jar:file:" + file.getAbsolutePath() + "!/" + name));
            }
        }
        final Iterator<URL> iterator = allResources.iterator();
        return new Enumeration<URL>() {
            @Override
            public boolean hasMoreElements() {
                return iterator.hasNext();
            }

            @Override
            public URL nextElement() {
                return iterator.next();
            }
        };
    }


    /**
     * 从jar包中加载一个class
     *
     * @param jarPath   jar包路径，如E:/test/test.jar
     * @param className 类名，如com.aaa.bbb.MyClass
     */
    private byte[] readClassFile(String jarPath, String className) throws Exception {
        className = className.replace('.', '/').concat(".class");
        URL classFileUrl = new URL("jar:file:" + jarPath + "!/" + className);
        byte[] data = null;
        BufferedInputStream is = null;
        ByteArrayOutputStream baos = null;
        try {
            is = new BufferedInputStream(classFileUrl.openStream());
            baos = new ByteArrayOutputStream();
            int ch = 0;
            while ((ch = is.read()) != -1) {
                baos.write(ch);
            }
            data = baos.toByteArray();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignored) {
                }
            }
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException ignored) {
                }
            }
        }
        return data;
    }
}
