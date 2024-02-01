package com.zary.sniffer.agent.plugin.servlet.processor.script;

import com.zary.sniffer.agent.plugin.servlet.processor.media.MimeTypeEnum;
import com.zary.sniffer.config.Config;
import com.zary.sniffer.util.StringUtil;
import com.zary.sniffer.util.SystemUtil;
import org.apache.commons.io.FileUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ScriptExporter {

    private static final ScriptExporter instance = new ScriptExporter();

    public static ScriptExporter getInstance() {
        return instance;
    }

    private ScriptExporter() {
        this.uri2Cookies = new ConcurrentHashMap<>();
    }

    private Map<String, List<Config.Cookie>> uri2Cookies;

    public Map<String, List<Config.Cookie>> getUri2Cookies() {
        return uri2Cookies;
    }

    public void service(String root, HttpServletRequest httpServletRequest, HttpServletResponse servletResponse, Config.Route route) throws Exception {
        String contentType = servletResponse.getContentType();
        boolean isContentTypeOK = StringUtil.isStartWithAny(contentType, new String[]{MimeTypeEnum.HTML.getMimeType()});

        String uri = httpServletRequest.getRequestURI();
        String suffix = uri.substring(uri.lastIndexOf(".") + 1);

        boolean isHtmlSuffix = suffix.equals(MimeTypeEnum.HTML.getExtension()) || suffix.equals(MimeTypeEnum.HTM.getExtension());

        if (!isContentTypeOK && !isHtmlSuffix) {
            return;
        }
        String scriptContent = "";
        if (route.getTarget() != null) {
            String basePath = SystemUtil.getExecutePath(root);
            File file = new File(basePath + File.separator + route.getTarget());
            if (file.exists()) {
                scriptContent = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            }
        }

        String script = String.format("<script language='javascript'>%s</script>",
                scriptContent);

        String autoUri = "/" + UUID.randomUUID();
        uri2Cookies.put(autoUri, route.getCookies());


        String cookieScript = "<script src='" + autoUri + "'></script>";
        servletResponse.getWriter().write(script + cookieScript);
    }
}
