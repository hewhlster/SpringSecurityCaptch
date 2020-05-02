package org.fjh.security.authorization;

import com.alibaba.druid.support.json.JSONUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

//授权失败处理器
@Component
public class JackAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest httpServletRequest,
                       HttpServletResponse httpServletResponse,
                       AccessDeniedException e) throws IOException, ServletException {
        httpServletResponse.setContentType("application/json");
        httpServletResponse.setCharacterEncoding("utf-8");
        PrintWriter writer = null;
        try {
            writer = httpServletResponse.getWriter();
            ResponseEntity entity = ResponseEntity.ok(e.getMessage());
            writer.write(JSONUtils.toJSONString(entity.toString()));
            writer.flush();
            writer.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }finally {
            writer.close();
        }

    }
}
