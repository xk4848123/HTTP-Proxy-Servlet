package com.zary.sniffer.server.queue;

import com.zary.sniffer.util.JsonUtil;
import com.zary.sniffer.server.ResponseModel;
import com.zary.sniffer.server.queue.base.MessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class AdmxJettyProducer extends AbstractHandler {

    private final MessageProducer messageProducer;

    public AdmxJettyProducer(MessageProducer messageProducer) {
        this.messageProducer = messageProducer;
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
        ResponseModel model = new ResponseModel();
        if (target != null && !"".equals(target) && target.replace("/", "").equals("pulsar")) {
            try {
                String messages = request.getParameter("messages");

                httpServletResponse.setContentType("application/json; charset=UTF-8");
                httpServletResponse.setCharacterEncoding("utf-8");

                if (dataToPulsar(messages)) {
                    model.setStatusCode(HttpStatus.OK_200);
                    httpServletResponse.setStatus(HttpServletResponse.SC_OK);
                    model.setMessage("success");
                } else {
                    httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    model.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR_500);
                    model.setMessage("write to pulsar fail");
                }
            } catch (Exception e) {
                httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                model.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR_500);
                model.setMessage(e.getMessage());
            }
            httpServletResponse.getWriter().print(JsonUtil.toJsonStr(model));
        }
        request.setHandled(true);
    }


    private boolean dataToPulsar(String messages) {
        try {
            messageProducer.sendAsync(messages);
            return true;
        } catch (Throwable t) {
            log.error("write to pulsar fail", t);
            return false;
        }


    }
}
