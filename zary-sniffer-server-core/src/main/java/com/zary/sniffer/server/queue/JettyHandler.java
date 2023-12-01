package com.zary.sniffer.server.queue;

import com.zary.sniffer.server.auth.AuthChecker;
import com.zary.sniffer.server.license.LicenseChecker;
import com.zary.sniffer.server.queue.base.MessageProducer;
import com.zary.sniffer.server.ResponseModel;
import com.zary.sniffer.server.util.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class JettyHandler extends AbstractHandler {

    private static final String SEPARATOR = "|";
    private final MessageProducer messageProducer;

    private final LicenseChecker licenseChecker;

    private final String jettyTargetPath;

    private final AuthChecker authChecker;

    public JettyHandler(MessageProducer messageProducer, LicenseChecker licenseChecker, AuthChecker authChecker, String jettyTargetPath) {
        this.messageProducer = messageProducer;
        this.licenseChecker = licenseChecker;
        this.jettyTargetPath = jettyTargetPath;
        this.authChecker = authChecker;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
        ResponseModel model = new ResponseModel();
        httpServletResponse.setContentType("application/json; charset=UTF-8");
        httpServletResponse.setCharacterEncoding("utf-8");

        if (!licenseChecker.isLicenseValid()) {
            model.setStatusCode(HttpStatus.FORBIDDEN_403);
            model.setMessage("license auth fail");
            httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);

        } else if (target != null && !"".equals(target) && target.replace("/", "").equals(jettyTargetPath)) {
            try {
                String token = request.getHeader("token");
                if (authChecker.check(token)) {
                    model.setStatusCode(HttpStatus.FORBIDDEN_403);
                    model.setMessage("token auth fail");
                    httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                } else {
                    String messages = request.getParameter("messages");
                    String type = request.getParameter("type");
                    if (dataToPulsar(messages, type)) {
                        model.setStatusCode(HttpStatus.OK_200);
                        model.setMessage("success");
                        httpServletResponse.setStatus(HttpServletResponse.SC_OK);
                    } else {
                        model.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR_500);
                        model.setMessage("write to pulsar fail");
                        httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }
                }

            } catch (Exception e) {
                httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                model.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR_500);
                model.setMessage(e.getMessage());
            }
        } else {
            model.setStatusCode(HttpStatus.NOT_FOUND_404);
            model.setMessage("not found");
            httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

        httpServletResponse.getWriter().print(JacksonUtil.toJsonStr(model));
        request.setHandled(true);
    }


    private boolean dataToPulsar(String messages, String type) {
        try {
            String[] messageArray = messages.split(",");
            for (String message : messageArray) {
                messageProducer.sendAsync(message + SEPARATOR + type);
            }
            return true;
        } catch (Throwable t) {
            log.error("write to pulsar fail", t);
            return false;
        }
    }
}
