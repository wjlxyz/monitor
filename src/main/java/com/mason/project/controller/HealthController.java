/*
 * projectName: cloud-common
 * date: 2019-01-24
 * copyright(c) 2017-2020 etekcity.com.cn
 */
package com.mason.project.controller;

import com.etekcity.cloud.service.introspection.IntrospectionService;
import com.etekcity.cloud.service.introspection.dto.AllUrlResponse;
import com.etekcity.cloud.service.introspection.dto.HealthResponse;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author tadonis
 * @since 1.1.4
 */
@RestController
@RequestMapping("/introspect")
@Slf4j
public class HealthController {

    private final IntrospectionService introspectionService;

    @Autowired
    public HealthController(IntrospectionService introspectionService) {
        this.introspectionService = introspectionService;
    }

    @GetMapping(path = "ping", produces = MediaType.TEXT_PLAIN_VALUE)
    public String ping() {
        return introspectionService.ping();
    }

    @GetMapping(path = "health", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public HealthResponse getHealth() {
        return introspectionService.health();
    }

    @GetMapping(path = "stat", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public JsonObject getStat() {
        return introspectionService.getStat();
    }

}
