package com.zary.sniffer.util;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class FileUtil {

    public static boolean isExist(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }


    public static String readLines(String filePath, String encoding) throws IOException {
        File file = new File(filePath);
        StringBuilder sb = new StringBuilder();
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            fis = new FileInputStream(file);
            isr = new InputStreamReader(fis, encoding);
            br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null) {
                if (!line.trim().equals("")) {
                    sb.append(line);
                }
            }
            br.close();
            isr.close();
            fis.close();
        } catch (IOException e) {
            throw e;
        } finally {
            if (br != null) {
                br.close();
            }
            if (isr != null) {
                isr.close();
            }
            if (fis != null) {
                fis.close();
            }
        }
        return sb.toString();
    }

    /**
     * 获取文件字节大小
     *
     * @param filePath
     * @return
     */
    public static long getSize(String filePath) {
        if (!isExist(filePath)) {
            return 0;
        }
        File file = new File(filePath);
        return file.length();
    }

    /**
     * 创建文件
     *
     * @param filePath
     * @return
     */
    public static boolean create(String filePath) throws IOException {
        if (StringUtil.isEmpty(filePath)) {
            throw new IllegalArgumentException("filePath empty");
        }
        //windows风格反斜杠替换
        filePath = filePath.replaceAll("\\\\", "/");
        //文件已存在
        if (isExist(filePath)) {
            return Boolean.TRUE;
        }
        //文件路径创建
        String dirPath = filePath.substring(0, filePath.lastIndexOf("/"));
        if (!DirectoryUtil.isExsit(dirPath)) {
            DirectoryUtil.create(dirPath);
        }
        //文件创建
        File file = new File(filePath);
        file.createNewFile();

        return Boolean.TRUE;
    }

    /**
     * 写入内容
     *
     * @param filePath
     * @param content
     * @return
     */
    public static boolean write(String filePath, String content, String encoding) throws IOException {
        if (!isExist(filePath)) {
            create(filePath);
        }
        FileOutputStream fileOut = null;
        BufferedOutputStream bufferOut = null;
        OutputStreamWriter writerOut = null;
        try {
            fileOut = new FileOutputStream(new File(filePath), false);
            bufferOut = new BufferedOutputStream(fileOut);
            writerOut = new OutputStreamWriter(bufferOut, encoding);
            writerOut.write(content);
            writerOut.flush();
        } catch (IOException e) {
            throw e;
        } finally {
            disposeWriter(writerOut, bufferOut, fileOut);
        }
        return Boolean.TRUE;
    }

    /**
     * 写入内容
     *
     * @param filePath
     * @param content
     * @return
     */
    public static boolean write(String filePath, String content) throws IOException {
        return write(filePath, content, StandardCharsets.UTF_8.name());
    }

    /**
     * 追加内容
     *
     * @param filePath
     * @param content
     * @return
     */
    public static boolean append(String filePath, String content, String encoding) throws IOException {
        if (!isExist(filePath)) {
            create(filePath);
        }
        FileOutputStream fileOut = null;
        BufferedOutputStream bufferOut = null;
        OutputStreamWriter writerOut = null;
        try {
            fileOut = new FileOutputStream(new File(filePath), true);
            bufferOut = new BufferedOutputStream(fileOut);
            writerOut = new OutputStreamWriter(bufferOut, encoding);
            writerOut.append(content);
            writerOut.flush();
        } catch (IOException e) {
            throw e;
        } finally {
            disposeWriter(writerOut, bufferOut, fileOut);
        }
        return Boolean.TRUE;
    }

    /**
     * 获取写入writer(使用后手工关闭)
     *
     * @param filePath
     * @param append
     * @param charset
     * @return
     * @throws FileNotFoundException
     */
    public static OutputStreamWriter getStreamWriter(String filePath, boolean append, Charset charset) throws FileNotFoundException {
        FileOutputStream fileOut = new FileOutputStream(new File(filePath), append);
        BufferedOutputStream bufferOut = new BufferedOutputStream(fileOut);
        OutputStreamWriter writerOut = new OutputStreamWriter(bufferOut, charset);
        return writerOut;
    }

    /**
     * 追加内容
     *
     * @param filePath
     * @param content
     * @return
     */
    public static boolean append(String filePath, String content) throws IOException {
        return append(filePath, content, StandardCharsets.UTF_8.name());
    }

    /**
     * 【警示】清空文件，慎用！！！
     *
     * @param filePath
     * @return
     */
    public static boolean clear(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            file.createNewFile();
        }
        write(filePath, StringUtil.EMPTY);
        return Boolean.TRUE;
    }

    /**
     * 移动文件
     *
     * @param source
     * @param target
     * @return
     */
    public static boolean moveTo(String source, String target) throws IOException {
        boolean res = copyTo(source, target);
        delete(source);
        return res;
    }

    /**
     * 拷贝文件
     *
     * @param source
     * @param target
     * @return
     */
    public static boolean copyTo(String source, String target) throws IOException {
        if (!isExist(source)) {
            throw new IllegalArgumentException("source not exsits.");
        }
        create(target);
        File fsource = new File(source);
        File ftarget = new File(target);
        FileInputStream in = null;
        FileOutputStream out = null;
        int bufferSize = 2097152;
        try {
            int length = bufferSize;
            in = new FileInputStream(fsource);
            out = new FileOutputStream(ftarget);
            FileChannel inC = in.getChannel();
            FileChannel outC = out.getChannel();
            ByteBuffer b = null;
            while (true) {
                if (inC.position() == inC.size()) {
                    inC.close();
                    outC.close();
                    break;
                }
                if ((inC.size() - inC.position()) < length) {
                    length = (int) (inC.size() - inC.position());
                } else {
                    length = bufferSize;
                }
                b = ByteBuffer.allocateDirect(length);
                inC.read(b);
                b.flip();
                outC.write(b);
                outC.force(false);
            }
            return Boolean.TRUE;
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                in.close();
                out.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * 【警示】删除文件，慎用！！！
     *
     * @param filePath
     * @return
     */
    public static boolean delete(String filePath) {
        if (isExist(filePath)) {
            File file = new File(filePath);
            file.delete();
        }
        return Boolean.TRUE;
    }

    /**
     * 资源释放
     *
     * @param writer
     * @param buffer
     * @param file
     */
    private static void disposeWriter(OutputStreamWriter writer, BufferedOutputStream buffer, FileOutputStream file) {
        try {
            if (writer != null) {
                writer.close();
            }
            if (buffer != null) {
                buffer.close();
            }
            if (file != null) {
                file.close();
            }
        } catch (Exception e) {
            //...
        }
    }

    /**
     * 去除UTF8字符串可能存在的BOM头
     *
     * @param input
     * @return
     */
    public static String removeBomUtf8(String input) {
        try {
            Charset utf8 = Charset.forName("utf-8");
            //0xEF 0xBB 0xBF
            byte[] utf8_bom_heads = {-17, -69, -65};
            byte[] bytes = input.getBytes(utf8);
            if (bytes.length >= 3) {
                if (bytes[0] == utf8_bom_heads[0] && bytes[1] == utf8_bom_heads[1] && bytes[2] == utf8_bom_heads[2]) {
                    byte[] res = new byte[bytes.length - 3];
                    for (int i = 3; i < bytes.length; i++) {
                        res[i - 3] = bytes[i];
                    }
                    return new String(res, 0, res.length, utf8);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return input;
    }

}
