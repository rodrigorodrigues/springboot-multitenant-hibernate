package com.example.springbootmultitenanthibernate;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

@Slf4j
public class RequestInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws IOException {
        String requestURI = request.getRequestURI();
        String tenantID = request.getHeader("X-TenantID");
        log.info("RequestURI " + requestURI + " Search for X-TenantID  :: " + tenantID);
        if (tenantID == null) {
            response.getWriter().write("X-TenantID not present in the Request Header");
            response.setStatus(400);
            return false;
        }
        TenantContext.setTenantInfo(tenantID);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           @Nullable ModelAndView modelAndView) throws Exception {
        TenantContext.clear();
    }
}
