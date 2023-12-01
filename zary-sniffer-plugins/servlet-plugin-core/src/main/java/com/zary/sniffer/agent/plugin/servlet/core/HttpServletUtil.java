package com.zary.sniffer.agent.plugin.servlet.core;

import com.zary.sniffer.config.ConfigCache;
import com.zary.sniffer.core.model.WebRequestInfo;
import com.zary.sniffer.tracing.TracerManager;
import com.zary.sniffer.util.StringUtil;

import com.zary.sniffer.config.PluginConsts;

import javax.servlet.DispatcherType;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class HttpServletUtil {

    public static void fillWebRequestInfo(WebRequestInfo info, HttpServletRequest request) {
        if (request != null) {
            info.setReq_url(request.getRequestURL().toString());
            info.setReq_method(request.getMethod());
            info.setReq_agent(request.getHeader("user-agent"));
            info.setReq_ip(getClientIp(request));
            info.setReq_size(request.getContentLength());

            Cookie[] cookies = request.getCookies();
            info.setReq_cookie(getCookieString(cookies));
            Map<String, String[]> params = request.getParameterMap();
            info.setReq_params(getRequestParamString(params));
            info.setFingerprint(getRequestFingerprint(request));
            info.setSession_id(getRequestSessionId(request));
        }
    }


    private static String getCookieValue(Cookie[] cookies, String key) {
        if (null == cookies || cookies.length <= 0 || null == key || key.length() <= 0) {
            return "";
        }
        for (Cookie cookie : cookies) {
            String ckey = cookie.getName();
            String cvalue = cookie.getValue();
            if (ckey.equalsIgnoreCase(key)) {
                return cvalue;
            }
        }
        return "";
    }

    private static String getParamsValue(Map<String, String[]> params, String key) {
        if (null == params || params.size() <= 0 || null == key || key.length() <= 0) {
            return "";
        }
        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            String pkey = entry.getKey();
            String[] pvalue = entry.getValue();
            if (pkey.equalsIgnoreCase(key)) {
                return StringUtil.join(pvalue, ",");
            }
        }
        return "";
    }

    private static String getHeaderValue(HttpServletRequest request, String key) {
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String itemKey = headerNames.nextElement();
                if (null != itemKey && itemKey.equalsIgnoreCase(key)) {
                    String value = request.getHeader(key);
                    if (null != value && value.length() > 0) {
                        return value;
                    }
                }
            }
        }
        return "";
    }

    /**
     * 获取客户端IP，兼容Nginx反向代理
     * Nginx需添加配置
     * proxy_set_header X-Real-IP $remote_addr;
     *
     * @param request
     * @return
     */
    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (!StringUtil.isEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
            //多次反向代理后会有多个ip值，第一个ip才是真实ip
            int index = ip.indexOf(",");
            if (index != -1) {
                return ip.substring(0, index);
            } else {
                return ip;
            }
        }
        ip = request.getHeader("X-Real-IP");
        if (!StringUtil.isEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }

    /**
     * 获取Cookie值字符串
     *
     * @param cookies
     * @return
     */
    public static String getCookieString(Cookie[] cookies) {
        if (cookies == null || cookies.length <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                sb.append(cookie.getName());
                sb.append("=");
                sb.append(cookie.getValue());
                sb.append("&");
            }
        }
        String res = sb.toString();
        if (res.endsWith("&")) {
            res = res.substring(0, res.length() - 1);
        }
        return res;
    }

    /**
     * 获取请求参数字符串
     *
     * @param params
     * @return
     */
    public static String getRequestParamString(Map<String, String[]> params) {
        StringBuilder sb = new StringBuilder();
        if (params != null && params.size() > 0) {
            for (Map.Entry<String, String[]> entry : params.entrySet()) {
                String key = entry.getKey();
                String[] value = entry.getValue();
                sb.append(key);
                sb.append("=");
                sb.append(StringUtil.join(value));
                sb.append("&");
            }
        }
        String res = sb.toString();
        if (res.endsWith("&")) {
            res = res.substring(0, res.length() - 1);
        }
        return res;
    }

    /**
     * 从请求中查找并获取指纹，位置包括：参数、Cookie、请求头
     *
     * @param request
     * @return
     */
    public static String getRequestFingerprint(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        String value = "";
        //cookies
        value = getCookieValue(request.getCookies(), PluginConsts.KEY_CLIENT_FINGERPRINT);
        if (!StringUtil.isEmpty(value)) {
            return value;
        }
        //headers
        value = getHeaderValue(request, PluginConsts.KEY_CLIENT_FINGERPRINT);
        if (!StringUtil.isEmpty(value)) {
            return value;
        }
        //params
        value = getParamsValue(request.getParameterMap(), PluginConsts.KEY_CLIENT_FINGERPRINT);
        if (!StringUtil.isEmpty(value)) {
            return value;
        }
        return "";
    }

    /**
     * 请求中是否包含指纹
     *
     * @param request
     * @return
     */
    public static boolean hasRequestFingerprint(HttpServletRequest request) {
        String token = getRequestFingerprint(request);
        return (token != null && token.length() > 0);
    }

    /**
     * 从请求中查找并获取sessionid
     * 对于单机程序，采集的就是http本身的sessionid，
     * 对于存在转发的请求，探针会通过插桩httpclient，使多个串行的请求始终保持第一个获取到的sessionid，以此保证请求相互联系
     *
     * @param request
     * @return
     */
    public static String getRequestSessionId(HttpServletRequest request) {
        String value = "";
        //cookies
        value = getCookieValue(request.getCookies(), PluginConsts.KEY_CLIENT_SESSION_ID);
        if (!StringUtil.isEmpty(value)) {
            return value;
        }
        //headers
        value = getHeaderValue(request, PluginConsts.KEY_CLIENT_SESSION_ID);
        if (!StringUtil.isEmpty(value)) {
            return value;
        }
        //params
        value = getParamsValue(request.getParameterMap(), PluginConsts.KEY_CLIENT_SESSION_ID);
        if (!StringUtil.isEmpty(value)) {
            return value;
        }
        //没找到返回原始sessionid
        return request.getRequestedSessionId();
    }

    /**
     * 是否为responseWrapper对象
     *
     * @param obj
     * @return
     */
    public static boolean isWrapperResponse(Object obj) {
        return obj instanceof ContentCachingResponseWrapper;
    }

    /**
     * 是否为requestWrapper对象
     *
     * @param obj
     * @return
     */
    public static boolean isWrapperRequest(Object obj) {
        return obj instanceof ContentCachingRequestWrapper;
    }

    /**
     * 声明一个Cookie到线程中
     *
     * @param key
     * @param value
     * @deprecated cookie、header只能在response没有writer内容之前设置，通过threadData设置就没有意义了
     */
    @Deprecated
    public static void writeThreadCookie(String key, String value) {
        HashMap<String, String> map = new HashMap<>();
        if (TracerManager.getCurTracer().hasOtherThreadData(PluginConsts.KEY_CACHE_COOKIE_APPEND)) {
            map = TracerManager.getCurTracer().acquireOtherThreadData(PluginConsts.KEY_CACHE_COOKIE_APPEND);
        }
        map.put(key, value);
        TracerManager.getCurTracer().fillOtherThreadData(PluginConsts.KEY_CACHE_COOKIE_APPEND, map);
    }

    /**
     * 声明一个Header到线程中
     *
     * @param key
     * @param value
     * @deprecated cookie、header只能在response没有writer内容之前设置，通过threadData设置就没有意义了
     */
    @Deprecated
    public static void writeThreadHeader(String key, String value) {
        HashMap<String, String> map = new HashMap<String, String>();
        if (TracerManager.getCurTracer().hasOtherThreadData(PluginConsts.KEY_CACHE_HEADER_APPEND)) {
            map = (HashMap<String, String>) TracerManager.getCurTracer().acquireOtherThreadData(PluginConsts.KEY_CACHE_HEADER_APPEND);
        }
        map.put(key, value);
        TracerManager.getCurTracer().fillOtherThreadData(PluginConsts.KEY_CACHE_HEADER_APPEND, map);
    }

    /**
     * 将当前线程中声明的Cookie写入response
     *
     * @param response
     * @deprecated cookie、header只能在response没有writer内容之前设置，通过threadData设置就没有意义了
     */
    @Deprecated
    public static void flushThreadCookie(HttpServletResponse response) {
        try {
            Object cookies = TracerManager.getCurTracer().acquireOtherThreadData(PluginConsts.KEY_CACHE_COOKIE_APPEND);
            if (cookies != null && (cookies instanceof HashMap)) {
                HashMap<String, String> map = (HashMap<String, String>) cookies;
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    response.addCookie(new Cookie(entry.getKey(), entry.getValue()));
                }
                TracerManager.getCurTracer().removeOtherThreadData(PluginConsts.KEY_CACHE_COOKIE_APPEND);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setResponseCookie(HttpServletResponse response, String key, String value) {
        response.addCookie(new Cookie(key, value));
    }

    public static void setResponseHeader(HttpServletResponse response, String key, String value) {
        response.setHeader(key, value);
    }

    /**
     * 将当前线程中声明的Header写入response
     *
     * @param response
     * @deprecated cookie、header只能在response没有writer内容之前设置，通过threadData设置就没有意义了
     */
    @Deprecated
    public static void flushThreadHeader(HttpServletResponse response) {
        try {
            Object headers = TracerManager.getCurTracer().acquireOtherThreadData(PluginConsts.KEY_CACHE_HEADER_APPEND);
            if (headers != null && (headers instanceof HashMap)) {
                HashMap<String, String> map = (HashMap<String, String>) headers;
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    response.setHeader(entry.getKey(), entry.getValue());
                }
                TracerManager.getCurTracer().removeOtherThreadData(PluginConsts.KEY_CACHE_HEADER_APPEND);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 探针拦截插件应该忽略的请求
     *
     * @param request
     * @return
     */
    public static boolean isIgnorePluginRequest(HttpServletRequest request) {
        return !isDispatcherRequest(request) || isStaticRequest(request.getRequestURI()) || isAutoPassRequest(request);
    }

    /**
     * 请求是不是匹配探针auto_pass白名单
     *
     * @param request
     * @return
     */
    public static boolean isAutoPassRequest(HttpServletRequest request) {
        return ConfigCache.get().isAutoPass(request.getRequestURI());
    }

    /**
     * 请求类型是否是 DispatcherType.REQUEST
     * REQUEST：默认值，浏览器直接请求资源
     * FORWARD：转发访问资源 : RequestDispatcher.forward();
     * INCLUDE：包含访问资源 : RequestDispatcher.include();
     * ERROR：错误跳转资源 : 被声明式异常处理机制调用的时候
     *
     * @param request
     * @return
     */
    public static boolean isDispatcherRequest(HttpServletRequest request) {
        return (request.getDispatcherType() == DispatcherType.REQUEST);
    }

    /**
     * 根据请求url判断是否请求的是静态资源
     *
     * @param url
     * @return
     */
    public static boolean isStaticRequest(String url) {
        try {
            if (StringUtil.isEmpty(url)) {
                return Boolean.FALSE;
            }
            if (url.indexOf("?") >= 0) {
                url = url.split("\\?")[0];
            }
            if (url.indexOf(";") >= 0) {
                //http://xxx/abc.css;jsessionid=xxx
                url = url.split(";")[0];
            }
            if (url.indexOf(".") < 0) {
                return Boolean.FALSE;
            }
            String suffix = url.substring(url.lastIndexOf(".")).toLowerCase();
            return PluginConsts.HTTP_STATIC_FILES.contains(suffix);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Boolean.FALSE;
    }

    /**
     * 根据content类型判断是不是响应静态资源
     *
     * @param contentType
     * @return
     */
    public static boolean isStaticResponse(String contentType) {
        return !StringUtil.isStartWithAny(contentType, PluginConsts.CONTENT_TYPE_SCRIPT);
    }

    /**
     * 自动输出脚本：通过wrapper方式，适用于servlet方式，需要判断响应内容，防止json等内容被误触发
     *
     * @param wrapper
     * @throws IOException
     */
    public static void flushAdmScript(ContentCachingResponseWrapper wrapper) throws IOException {
        //1.auto script配置：是否自动追加脚本
        if (!ConfigCache.get().isAuto_script()) {
            return;
        }
        //2.contentType：当前请求响应的类型是否是text/html
        String contentType = wrapper.getContentType() + "";
        boolean isContentTypeOK = StringUtil.isStartWithAny(contentType, PluginConsts.CONTENT_TYPE_SCRIPT);
        if (!isContentTypeOK) {
            return;
        }
        //3.content：包含</body>且不包含脚本id(避免已经输出过了重复)
        String content = new String(wrapper.getContentAsByteArray(), wrapper.getCharacterEncoding()).toLowerCase();
        boolean isContentOK = content.contains("</body>") && !content.contains(PluginConsts.KEY_CLIENT_SCRIPT_ID);
        if (!isContentOK) {
            return;
        }
        //4.追加脚本
        String script = String.format("<script id='%s' language='javascript'>%s</script>",
                PluginConsts.KEY_CLIENT_SCRIPT_ID, ConfigCache.get().getScript());
        wrapper.appendContent(script);
    }

    /**
     * 自动输出脚本：通过response.getWriter方式，适用于springmvc在view阶段输出的场景，不需要判断响应内容
     *
     * @param request
     * @param response
     */
    public static void flushAdmScript(HttpServletRequest request, HttpServletResponse response) {
        //1.auto script配置：是否自动追加脚本
        if (!ConfigCache.get().isAuto_script()) {
            return;
        }
        //2.当前请求已经存在指纹
        String finger = getRequestFingerprint(request);
        if (StringUtil.isNotEmpty(finger)) {
            return;
        }
        //4.contentType：当前请求响应的类型是否是text/html
        String contentType = response.getContentType() + "";
        boolean isContentTypeOK = StringUtil.isStartWithAny(contentType, PluginConsts.CONTENT_TYPE_SCRIPT);
        if (!isContentTypeOK) {
            return;
        }
        //5.尝试写脚本
        PrintWriter writer = null;
        try {
            writer = response.getWriter();
            String script = String.format("<script id='%s' language='javascript'>%s</script>",
                    PluginConsts.KEY_CLIENT_SCRIPT_ID, ConfigCache.get().getScript());
            writer.write(script);
            writer.flush();
        } catch (Exception e) {
            //可能 getOutputStream() has already been called for this response ...
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (Exception e) {
            }
        }
    }

    /**
     * 写入一个标记，通知当前线程需要回写脚本
     *
     * @deprecated 取消该标记，View.render直接回写
     */
    @Deprecated
    public static void writeAdmScriptOpenFlag() {
        TracerManager.getCurTracer().fillOtherThreadData(PluginConsts.KEY_IS_NEED_WRITE_SCRIPT, true);
    }
}
