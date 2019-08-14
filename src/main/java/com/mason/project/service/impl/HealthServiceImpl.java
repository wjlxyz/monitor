/*
 * projectName: cloud-common
 * date: 2019-01-23
 * copyright(c) 2017-2020 etekcity.com.cn
 */
package com.mason.project.service.impl;

import com.etekcity.cloud.ErrorCode;
import com.etekcity.cloud.monitor.MetricManager;
import com.etekcity.cloud.monitor.RequestMetric;
import com.etekcity.cloud.service.healthcheck.HealthCheckHandler;
import com.etekcity.cloud.service.introspection.dto.AllUrlResponse;
import com.etekcity.cloud.service.introspection.dto.HealthResponse;
import com.etekcity.cloud.service.introspection.dto.UrlInfo;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author tadonis
 * @since 1.1.4
 */
@Slf4j
@Service("IntrospectionService")
public class HealthServiceImpl implements IntrospectionService {

    @Autowired
    private WebApplicationContext applicationContext;

    @Autowired
    private HealthCheckHandler healthCheckHandler;

    @Override
    public String ping() {
        return "pong";
    }

    @Override
    public HealthResponse health() {
        RequestMetric.Context context = MetricManager.getOrRegisterInReq("health").time();
        try {
            return new HealthResponse(healthCheckHandler.check());
        } finally {
            context.success();
        }
    }

    @Override
    public JsonObject getStat() {
        RequestMetric.Context context = MetricManager.getOrRegisterInReq("stat").time();
        try {
            return MetricManager.getInstance().getMetric();
        } finally {
            context.success();
        }
    }

    @Override
    public AllUrlResponse getAllUrl() {
        RequestMetric.Context context = MetricManager.getOrRegisterInReq("allUrl").time();
        AllUrlResponse responseBody = new AllUrlResponse();
        ErrorCode errorCode = ErrorCode.SUCCESS;
        try {
            RequestMappingHandlerMapping mapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
            // 获取url与类和方法的对应信息
            Map<RequestMappingInfo, HandlerMethod> map = mapping.getHandlerMethods();
            for (Map.Entry<RequestMappingInfo, HandlerMethod> m : map.entrySet()) {
                UrlInfo urlInfo = new UrlInfo();
                RequestMappingInfo info = m.getKey();
                HandlerMethod method = m.getValue();
                PatternsRequestCondition p = info.getPatternsCondition();
                List<String> patterns = new ArrayList<>(p.getPatterns());
                urlInfo.setPatterns(patterns);
                // 类名
                urlInfo.setClassName(method.getMethod().getDeclaringClass().getName());
                // 方法名
                urlInfo.setProgramMethodName(method.getMethod().getName());
                RequestMethodsRequestCondition methodsCondition = info.getMethodsCondition();
                List<String> requestMethods = new ArrayList<String>();
                for (RequestMethod requestMethod : methodsCondition.getMethods()) {
                    requestMethods.add(requestMethod.toString());
                }
                urlInfo.setHttpMethodName(requestMethods);

                responseBody.getAllUrl().add(urlInfo);
            }
            return responseBody;
        } catch (Exception e) {
            log.error("get module all url error",e);
            errorCode = ErrorCode.INTERNAL_ERROR;
            return responseBody;
        } finally {
            context.stop(errorCode);
        }
    }


}
