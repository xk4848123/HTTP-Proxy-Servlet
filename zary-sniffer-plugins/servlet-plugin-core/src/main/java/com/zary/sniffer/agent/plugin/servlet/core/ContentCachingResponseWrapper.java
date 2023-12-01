package com.zary.sniffer.agent.plugin.servlet.core;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * HttpServletRequest包装类，增加了内容缓存
 * 1.该类自spring-web 4.1.3 开始新增
 * 2.本项目为兼容Spring-web 4.1.3以下版本，从4.1.9版本源码中拷贝本代码到项目中来
 *
 * @author xulibo
 */
public class ContentCachingResponseWrapper extends HttpServletResponseWrapper {
    private final ResizableByteArrayOutputStream content = new ResizableByteArrayOutputStream(1024);
    private final ServletOutputStream outputStream = new ContentCachingResponseWrapper.ResponseServletOutputStream();
    private PrintWriter writer;
    private int statusCode = 200;
    private Integer contentLength;

    public ContentCachingResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    /**
     * 使用getWriter追加数据有时无效，增加此方法强制追加
     *
     * @param append
     * @throws IOException
     */
    public void appendContent(String append) throws IOException {
        if (null != append) {
            byte[] bytes = append.getBytes(Charset.forName(this.getCharacterEncoding()));
            this.content.write(bytes);
        }
    }

    @Override
    public void setStatus(int sc) {
        super.setStatus(sc);
        this.statusCode = sc;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setStatus(int sc, String sm) {
        super.setStatus(sc, sm);
        this.statusCode = sc;
    }

    @Override
    public void sendError(int sc) throws IOException {
        this.copyBodyToResponse();

        try {
            super.sendError(sc);
        } catch (IllegalStateException var3) {
            super.setStatus(sc);
        }

        this.statusCode = sc;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void sendError(int sc, String msg) throws IOException {
        this.copyBodyToResponse();

        try {
            super.sendError(sc, msg);
        } catch (IllegalStateException var4) {
            super.setStatus(sc, msg);
        }

        this.statusCode = sc;
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        this.copyBodyToResponse();
        super.sendRedirect(location);
    }

    @Override
    public ServletOutputStream getOutputStream() {
        return this.outputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (this.writer == null) {
            String characterEncoding = this.getCharacterEncoding();
            this.writer = characterEncoding != null ? new ContentCachingResponseWrapper.ResponsePrintWriter(characterEncoding) : new ContentCachingResponseWrapper.ResponsePrintWriter("ISO-8859-1");
        }

        return this.writer;
    }

    @Override
    public void setContentLength(int len) {
        if (len > this.content.capacity()) {
            this.content.resize(len);
        }

        this.contentLength = len;
    }

    @Override
    public void setContentLengthLong(long len) {
        if (len > 2147483647L) {
            throw new IllegalArgumentException("Content-Length exceeds ShallowEtagHeaderFilter's maximum (2147483647): " + len);
        } else {
            int lenInt = (int) len;
            if (lenInt > this.content.capacity()) {
                this.content.resize(lenInt);
            }

            this.contentLength = lenInt;
        }
    }

    @Override
    public void setBufferSize(int size) {
        if (size > this.content.capacity()) {
            this.content.resize(size);
        }

    }

    @Override
    public void resetBuffer() {
        this.content.reset();
    }

    @Override
    public void reset() {
        super.reset();
        this.content.reset();
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public byte[] getContentAsByteArray() {
        return this.content.toByteArray();
    }

    private void copyBodyToResponse() throws IOException {
        if (this.content.size() > 0) {
            if (this.contentLength != null) {
                this.getResponse().setContentLength(this.contentLength);
                this.contentLength = null;
            }
            this.getResponse().getOutputStream().write(this.content.toByteArray());
            //StreamUtils.copy(this.content.toByteArray(), this.getResponse().getOutputStream());
            this.content.reset();
        }

    }

    private class ResponsePrintWriter extends PrintWriter {
        public ResponsePrintWriter(String characterEncoding) throws UnsupportedEncodingException {
            super(new OutputStreamWriter(ContentCachingResponseWrapper.this.content, characterEncoding));
        }

        @Override
        public void write(char[] buf, int off, int len) {
            super.write(buf, off, len);
            super.flush();
        }

        @Override
        public void write(String s, int off, int len) {
            super.write(s, off, len);
            super.flush();
        }

        @Override
        public void write(int c) {
            super.write(c);
            super.flush();
        }
    }

    /**
     * ServletOutputStream成员版本历史：
     * - public void print(...)										3.0.1+
     * - public void println(...)									3.0.1+
     * - public abstract boolean canWrite();						3.1-b01+
     * - public abstract void setWriteListener(WriteListener var1);	3.1-b01+
     * 为兼容不同版本，部分重写函数不加@Override注解
     */
    private class ResponseServletOutputStream extends ServletOutputStream {
        private ResponseServletOutputStream() {
        }

        public boolean isReady() {
            return false;
        }

        @Override
        public void write(int b) throws IOException {
            ContentCachingResponseWrapper.this.content.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            ContentCachingResponseWrapper.this.content.write(b, off, len);
        }

        //@Override
        public boolean canWrite() {
            return true;
        }

        //@Override
        public void setWriteListener(WriteListener writeListener) {

        }
    }
}
