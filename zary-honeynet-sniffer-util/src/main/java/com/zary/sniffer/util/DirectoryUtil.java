package com.zary.sniffer.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * com.zx.lib.utils
 *
 * @author xulibo
 * @version 2017/9/20
 */
public class DirectoryUtil {
    /**
     * 是否存在
     *
     * @param dirPath
     * @return
     */
    public static boolean isExsit(String dirPath) {
        File file = new File(dirPath);
        if (file.exists() && file.isDirectory()) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * 获取总大小(单线程递归)
     *
     * @param dirPath
     * @return
     */
    public static long getSize(String dirPath) {
        File file = new File(dirPath);
        if (!file.exists()) {
            return 0;
        }
        if (file.isFile()) {
            return file.length();
        }
        long totalSize = 0;
        final File[] children = file.listFiles();
        if (children != null) {
            for (final File child : children) {
                totalSize += getSize(child.getAbsolutePath());
            }
        }
        return totalSize;
    }

    /**
     * 创建文件夹
     *
     * @param dirPath 不能包含文件，否则文件名也会被创建为目录
     * @return
     */
    public static boolean create(String dirPath) {
        File file = new File(dirPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        if (isExsit(dirPath)) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * 获取文件夹文件列表
     *
     * @param dirPath
     * @param fileOnly 指示是否只列出文件而忽略文件夹
     * @return
     */
    public static List<File> getFiles(String dirPath, boolean fileOnly) {
        List<File> list = null;
        if (isExsit(dirPath)) {
            list = new ArrayList<File>();
            File file = new File(dirPath);
            File[] childs = file.listFiles();
            if (childs != null) {
                for (File child : childs) {
                    if (fileOnly) {
                        if (child.isDirectory()) {
                            continue;
                        }
                    }
                    list.add(child);
                }
            }
        }
        return list;
    }

    /**
     * @param dirPath 文件目录
     * @param fileOnly 指示是否只列出文件而忽略文件夹
     * @param comparator 自定义文件排序
     * @return
     */
    public static List<File> getFiles(String dirPath, boolean fileOnly, Comparator<File> comparator) {
        List<File> list = getFiles(dirPath, fileOnly);
        //list.sort(comparator);
        return list;
    }

    /**
     * 递归获取文件夹下所有文件
     */
    public static List<File> getFiles(String dirPath, List<File> files) {
        File file = new File(dirPath);
        if (file.isDirectory()) {
            File[] fileList = file.listFiles();
            for (File subFile : fileList) {
                if (subFile.isDirectory()) {
                    getFiles(subFile.getAbsolutePath(), files);
                } else {
                    files.add(subFile);
                }
            }
        }
        return files;
    }



    /**
     * 【警示】清空文件夹，慎用！！！
     *
     * @param dirPath
     * @return
     */
    public static boolean clear(String dirPath) {
        File file = new File(dirPath);
        return deleteChilds(file, Boolean.FALSE);
    }

    /**
     * 【警示】删除文件夹，慎用！！！
     *
     * @param dirPath
     * @return
     */
    public static boolean delete(String dirPath) {
        File file = new File(dirPath);
        return deleteChilds(file, Boolean.TRUE);
    }

    /**
     * 清空文件夹内容
     *
     * @param dir        文件夹File
     * @param deleteSelf 指示是否删除自身
     * @return
     */
    private static boolean deleteChilds(File dir, boolean deleteSelf) {
        if (!dir.exists()) {
            return Boolean.TRUE;
        }
        File[] childs = dir.listFiles();
        if (childs != null && childs.length > 0) {
            for (File child : childs) {
                if (child.isFile()) {
                    child.delete();
                } else if (child.isDirectory()) {
                    deleteChilds(child, Boolean.TRUE);
                }
            }
        }
        if (deleteSelf) {
            dir.delete();
        }
        return Boolean.TRUE;
    }
}
