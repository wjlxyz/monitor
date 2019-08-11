package com.mason.project.test.controller;

import com.mason.project.monitor.ErrorCode;
import com.mason.project.monitor.MetricManager;
import com.mason.project.monitor.RequestMetric;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/project/api/v1")
public class TestMonitorController {

    @RequestMapping(path = "/normal")
    public ErrorCode normalController() {

        RequestMetric inRequestMetric = MetricManager.getOrRegisterOutReq("normal");
        RequestMetric.Context context = inRequestMetric.time();
        ErrorCode errorCode = ErrorCode.SUCCESS;
        try {
            return errorCode;
        } catch (Exception e) {
            errorCode = ErrorCode.INTERNAL_ERROR;
            return errorCode;
        } finally {
            context.stop(errorCode);
        }

    }

    @RequestMapping(path = "/internalError")
    public ErrorCode internalErrorController() {
        return ErrorCode.INTERNAL_ERROR;
    }

}
