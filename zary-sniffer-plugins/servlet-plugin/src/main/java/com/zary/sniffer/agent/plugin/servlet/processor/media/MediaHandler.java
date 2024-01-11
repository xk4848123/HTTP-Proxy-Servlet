package com.zary.sniffer.agent.plugin.servlet.processor.media;

import com.zary.sniffer.config.Config;
import com.zary.sniffer.util.SystemUtil;
import org.apache.commons.io.FileUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;

public class MediaHandler {
    private static final MediaHandler instance = new MediaHandler();

    public static MediaHandler getInstance() {
        return instance;
    }

    private MediaHandler() {

    }

    public void service(HttpServletRequest servletRequest, HttpServletResponse servletResponse, Config.Route route) throws Exception {
        String basePath = SystemUtil.getExecutePath();
        File file = new File(basePath + File.separator + route.getTarget());
        if (!file.exists()) {
            throw new RemoteException("file not found");
        }
        String fileContent = FileUtils.readFileToString(file, StandardCharsets.UTF_8);

        String contentType = MimeTypeEnum.getContentTypeByFileName(route.getTarget());
        servletResponse.setCharacterEncoding(StandardCharsets.UTF_8.name());
        servletResponse.setContentType(contentType);
        servletResponse.setHeader("Cache-Control", "no-cache");
        servletResponse.setHeader("Cache-Control", "no-store");
        servletResponse.setHeader("Pragma", "no-cache");
        servletResponse.setDateHeader("Expires", 0L);
        servletResponse.getWriter().write(fileContent);
        servletResponse.flushBuffer();
    }

}
