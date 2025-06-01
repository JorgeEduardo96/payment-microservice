package br.com.orderservice.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class GatewayHeaderInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String fromGateway = request.getHeader("X-From-Gateway");

        if (!"true".equals(fromGateway)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.getWriter().write("Access denied: must come through API Gateway.");
            return false;
        }
        return true;
    }
}
