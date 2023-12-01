package com.zary.sniffer.agent.plugin.servlet.core;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.*;

/**
 * HttpServletRequest包装类，增加了内容缓存
 * 1.该类自spring-web 4.1.3 开始新增
 * 2.本项目为兼容Spring-web 4.1.3以下版本，从4.1.9版本源码中拷贝本代码到项目中来
 *
 * @author xulibo
 */
public class ContentCachingRequestWrapper extends HttpServletRequestWrapper {
    private static final String FORM_CONTENT_TYPE = "application/x-www-form-urlencoded";
    private static final String METHOD_POST = "POST";
    private final ByteArrayOutputStream cachedContent;
    private ServletInputStream inputStream;
    private BufferedReader reader;

    public ContentCachingRequestWrapper(HttpServletRequest request) {
        super(request);
        int contentLength = request.getContentLength();
        this.cachedContent = new ByteArrayOutputStream(contentLength >= 0 ? contentLength : 1024);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (this.inputStream == null) {
            this.inputStream = new ContentCachingRequestWrapper.ContentCachingInputStream(this.getRequest().getInputStream());
        }

        return this.inputStream;
    }

    @Override
    public String getCharacterEncoding() {
        String enc = super.getCharacterEncoding();
        return enc != null ? enc : "ISO-8859-1";
    }

    @Override
    public BufferedReader getReader() throws IOException {
        if (this.reader == null) {
            this.reader = new BufferedReader(new InputStreamReader(this.getInputStream(), this.getCharacterEncoding()));
        }

        return this.reader;
    }

    @Override
    public String getParameter(String name) {
        if (this.cachedContent.size() == 0 && this.isFormPost()) {
            this.writeRequestParametersToCachedContent();
        }

        return super.getParameter(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        if (this.cachedContent.size() == 0 && this.isFormPost()) {
            this.writeRequestParametersToCachedContent();
        }

        return super.getParameterMap();
    }

    @Override
    public Enumeration<String> getParameterNames() {
        if (this.cachedContent.size() == 0 && this.isFormPost()) {
            this.writeRequestParametersToCachedContent();
        }

        return super.getParameterNames();
    }

    @Override
    public String[] getParameterValues(String name) {
        if (this.cachedContent.size() == 0 && this.isFormPost()) {
            this.writeRequestParametersToCachedContent();
        }

        return super.getParameterValues(name);
    }

    private boolean isFormPost() {
        String contentType = this.getContentType();
        return contentType != null && contentType.contains("application/x-www-form-urlencoded") && "POST".equalsIgnoreCase(this.getMethod());
    }

    private void writeRequestParametersToCachedContent() {
        try {
            if (this.cachedContent.size() == 0) {
                String requestEncoding = this.getCharacterEncoding();
                Map<String, String[]> form = super.getParameterMap();
                Iterator nameIterator = form.keySet().iterator();

                while (nameIterator.hasNext()) {
                    String name = (String) nameIterator.next();
                    List<Object> values = Arrays.asList((Object[]) form.get(name));
                    Iterator valueIterator = values.iterator();

                    while (valueIterator.hasNext()) {
                        String value = (String) valueIterator.next();
                        this.cachedContent.write(URLEncoder.encode(name, requestEncoding).getBytes());
                        if (value != null) {
                            this.cachedContent.write(61);
                            this.cachedContent.write(URLEncoder.encode(value, requestEncoding).getBytes());
                            if (valueIterator.hasNext()) {
                                this.cachedContent.write(38);
                            }
                        }
                    }

                    if (nameIterator.hasNext()) {
                        this.cachedContent.write(38);
                    }
                }
            }

        } catch (IOException var8) {
            throw new IllegalStateException("Failed to write request parameters to cached content", var8);
        }
    }

    public byte[] getContentAsByteArray() {
        return this.cachedContent.toByteArray();
    }

    /**
     * ServletInputStream成员版本历史：
     * - public int readLine(byte[] b, int off, int len)			3.0.1+
     * - public abstract boolean isFinished();						3.1-b01+
     * - public abstract boolean isReady();							3.1-b01+
     * - public abstract void setReadListener(ReadListener var1);	3.1-b01+
     * 为兼容不同版本，部分重写函数不加@Override注解
     */
    private class ContentCachingInputStream extends ServletInputStream {
        private final ServletInputStream is;

        public ContentCachingInputStream(ServletInputStream is) {
            this.is = is;
        }

        @Override
        public int read() throws IOException {
            int ch = this.is.read();
            if (ch != -1) {
                ContentCachingRequestWrapper.this.cachedContent.write(ch);
            }

            return ch;
        }

        //@Override
        public boolean isFinished() {
            return false;
        }

        //@Override
        public boolean isReady() {
            return false;
        }

        //@Override
        public void setReadListener(ReadListener readListener) {

        }
    }
}
