package com.mason.project.monitor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.softee.management.annotation.MBean;
import org.softee.management.annotation.ManagedAttribute;
import org.softee.management.helper.MBeanRegistration;


@Slf4j
@MBean(objectName = "mason:name=StatJmx")
public class JmxReporter {

    private static JmxReporter instance = new JmxReporter();
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private JmxReporter() {
        try {
            new MBeanRegistration(this).register();
            log.info("{} register to MetricMBean", this.getClass().getName());
        } catch (Exception e) {
            log.error("Failed to register to StatJmx stats MetricMBean.");
            throw new RuntimeException("Failed to register to StatJmx stats", e);
        }
    }

    public static void init() {

    }

    @ManagedAttribute
    public String getStat() {
        return gson.toJson(MetricManager.getInstance().getMetric());
    }

    public static JmxReporter getInstance() {
        return instance;
    }
}
