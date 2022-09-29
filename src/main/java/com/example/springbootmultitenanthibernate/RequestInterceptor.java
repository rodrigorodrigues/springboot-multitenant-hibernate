package com.example.springbootmultitenanthibernate;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

@Slf4j
@AllArgsConstructor
public class RequestInterceptor implements HandlerInterceptor {
    private final MultipleDataSourcesProperties multipleDataSourcesProperties;

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
        String datasourceID = request.getHeader("X-DatasourceID");
        log.info("RequestURI " + requestURI + " Search for X-DatasourceID  :: " + datasourceID);
        Database database = multipleDataSourcesProperties.getDefaultDatabase();
        if (StringUtils.hasLength(datasourceID)) {
            database = Database.valueOf(datasourceID);
        }
        TenantContext.setDatabaseInfo(database);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           @Nullable ModelAndView modelAndView) throws Exception {
        TenantContext.clear();
    }
}
