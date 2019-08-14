/*
 * projectName: cloud-common
 * date: 2019-01-23
 * copyright(c) 2017-2020 etekcity.com.cn
 */
package com.mason.project.dto;

import com.etekcity.cloud.CloudApplication;
import com.etekcity.cloud.module.ModuleHolder;
import com.etekcity.cloud.stat.HealthStatus;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.Map;

/**
 * @author tadonis
 * @since 1.1.4
 */
@Data
@Slf4j
public class HealthResponse {
    private final static transient String VERSION_FILE = "VERSION.txt";
    private final static transient String VERSION_KEY = "SERVICE.VERSION";
    private final static transient String SERVICE_NAME_KEY = "SERVICE.NAME";

    public final static transient ModuleInfo MODULE_INFO = getModuleInfo();

    private HealthStatus healthStatus;

    // static field is not serialized by gson in default. so, add a non-static field here
    private ModuleInfo moduleInfo;

    private String startTime = CloudApplication.startTime.format(DateTimeFormatter.ISO_DATE_TIME);
    private Long uptimeInSec = Duration.between(LocalDateTime.now(), CloudApplication.startTime).getSeconds();
    private Map<String, HealthStatus> dependencyStatus;

    public HealthResponse(Map<String, HealthStatus> dependencyStatus) {
        this.dependencyStatus = dependencyStatus;
        int bits = 1;
        for (HealthStatus healthStatus: dependencyStatus.values()) {
            bits = bits | healthStatus.getBit();
        }
        healthStatus = HealthStatus.fromBit(bits);
        this.moduleInfo = MODULE_INFO;
    }

    private static class ModuleInfo {
        private Integer Id = ModuleHolder.getModule().getId();
        private String name = ModuleHolder.getModule().getModuleName();
        private String owner = ModuleHolder.getModule().getOwner();

        private String version;
        private String changeLog;

        public ModuleInfo(String version, String changeLog) {
            this.version = version;
            this.changeLog = changeLog;
        }
    }

    public static ModuleInfo getModuleInfo() {
        try {
            Enumeration<URL> urlEnumeration = Thread.currentThread().getContextClassLoader().getResources(VERSION_FILE);
            while (urlEnumeration.hasMoreElements()) {
                URL url = urlEnumeration.nextElement();
                if (isModuleVersionUrl(url)) {
                    return readModuleInfoFromUrl(url);
                }
            }
        } catch (Exception e) {
            log.error("failed to get resources. resource name: {}", VERSION_FILE, e);
            return null;
        }
        return null;
    }

    private static ModuleInfo readModuleInfoFromUrl(URL url) {
        String serviceVersion = null;
        StringBuilder changeLogBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
            while (reader.ready()) {
                String line = reader.readLine();
                if (serviceVersion != null && !line.trim().isEmpty()) {
                    if (line.trim().startsWith(VERSION_KEY)) {
                        break;
                    }
                    changeLogBuilder.append(line);
                    changeLogBuilder.append(System.lineSeparator());
                }

                if (serviceVersion == null && line.trim().startsWith(VERSION_KEY)) {
                    String[] fields = line.trim().split("=", 2);
                    if (fields.length >= 2) {
                        serviceVersion = fields[1];
                        changeLogBuilder.append(line);
                        changeLogBuilder.append(System.lineSeparator());
                    }
                }
            }
        } catch (Exception e) {
            log.error("failed to init module info.", e);
            return null;
        }
        return new ModuleInfo(serviceVersion, changeLogBuilder.toString());
    }

    private static boolean isModuleVersionUrl(URL url) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
            while (reader.ready()) {
                String line = reader.readLine();
                if (line.contains(SERVICE_NAME_KEY)) {
                    String[] fields = line.trim().split("=", 2);
                    if (fields.length >= 2) {
                        if (ModuleHolder.getModule().getModuleName().equalsIgnoreCase(fields[1])) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug(String.format("%s is not target module version file", url.toString()));
            return false;
        }
        return false;
    }

}
