package com.mason.project.monitor;

import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


@Slf4j
public class MetricManager implements StatMetric {
    public static long DURATION = 300;
    public static TimeUnit TIME_UNIT = TimeUnit.SECONDS;

    public static MetricManager getInstance() {
        return registry;
    }

    private static MetricManager registry = new MetricManager();

    private static RequestMetricBuilder inReqBuilder = new RequestMetricBuilder(DURATION, TIME_UNIT, ReqDirection.IN);
    private static RequestMetricBuilder outReqBuilder = new RequestMetricBuilder(DURATION, TIME_UNIT, ReqDirection.OUT);
    private static StwCounterBuilder stwCounterBuilder = new StwCounterBuilder(DURATION, TIME_UNIT);

    private MetricManager() {
        JmxReporter.init();
    }

    private ConcurrentHashMap<String, StatMetric> metricMap = new ConcurrentHashMap<>();

    private Object lock = new Object();

    public static void setduration(long duration, TimeUnit timeUnit) {
        DURATION = duration;
        TIME_UNIT = timeUnit;

        // recreate builders
        inReqBuilder = new RequestMetricBuilder(DURATION, TIME_UNIT, ReqDirection.IN);
        outReqBuilder = new RequestMetricBuilder(DURATION, TIME_UNIT, ReqDirection.OUT);
        stwCounterBuilder = new StwCounterBuilder(DURATION, TIME_UNIT);
    }

    public <T extends StatMetric> T getOrRegister(String name, Builder<T> builder) {
        final StatMetric metric = metricMap.get(name);
        if (metric == null) {
            synchronized (lock) {
                if (!metricMap.containsKey(name)) {
                    try {
                        return register(name, builder.build());
                    } catch (MetricAlreadyExistsException e) {
                        return (T) metricMap.get(name);
                    }
                } else {
                    return (T) metricMap.get(name);
                }
            }
        }
        return (T) metric;
    }

    public <T extends StatMetric> T register(String name, T metric) throws IllegalArgumentException {
        if (metricMap.putIfAbsent(name, metric) != null) {
            throw new MetricAlreadyExistsException("A metric named " + name + " already exists");
        }
        log.debug("success register metric: {}, type: {}", name, metric.getClass().getSimpleName());
        return metric;
    }

    /**
     * remove statMetric.
     *
     * @param name metric name
     * @param <T>  statMetric type
     * @return metric if exists or null if not exist such metric
     */
    public <T extends StatMetric> T remove(String name) {
        return (T) metricMap.remove(name);
    }

    /**
     * clear all metrics
     */
    public void reset() {
        metricMap.clear();
    }

    /**
     * Returns the previous simple thread pool monitor associated with the specified name or
     * register new pool monitor if there was no mapping for the name.
     *
     * @param name               metric name
     * @param threadPoolExecutor thread pool
     * @return ThreadPoolMetric
     */
    public static ThreadPoolMetric registerThreadPoolMetric(String name, ThreadPoolExecutor threadPoolExecutor) {
        return getInstance().getOrRegister(name, new ThreadPoolMetricBuilder(threadPoolExecutor));
    }

    /**
     * Returns the previous IN Request Metric associated with the specified name,
     * register new IN Request Metric if there was no mapping for the name
     *
     * @param name metric name. set it to method name on usage.
     * @return RequestMetric for name.
     */
    public static RequestMetric getOrRegisterInReq(String name) {
        return getInstance().getOrRegister(name, inReqBuilder);
    }


    /**
     * Returns the previous OUT Request Metric associated with the specified name,
     * register new OUT Request Metric if there was no mapping for the name.
     * using MetricManager
     *
     * @param name metric name. set it to method name on usage.
     * @return RequestMetric for name.
     */
    public static RequestMetric getOrRegisterOutReq(String name) {
        return getInstance().getOrRegister(name, outReqBuilder);
    }

    /**
     * Returns the previous simple counter associated with the specified name,
     * register new simple counter if there was no mapping for the name
     *
     * @param name metric name
     * @return Counter
     */
    public static Counter getOrRegisterCounter(String name) {
        return getInstance().getOrRegister(name, Builder.COUNTER);
    }

    /**
     * Returns the previous SlidingTimeWindow counter associated with the specified name,
     * register new SlidingTimeWindow counter if there was no mapping for the name
     *
     * @param name metric name
     * @return SlidingTimeWindowCounter
     */
    public static SlidingTimeWindowCounter getOrRegisterStwCounter(String name) {
        return getInstance().getOrRegister(name, stwCounterBuilder);
    }

    /**
     * Returns the previous simple counter associated with the specified name,
     * register new simple counter if there was no mapping for the name
     *
     * @param name metric name
     * @return SlidingTimeWindowCounter
     */
    public static CacheMetric getOrRegisterCacheStat(String name) {
        return getInstance().getOrRegister(name, Builder.CACHE_STATS_COUNTER);
    }

    @Override
    public JsonObject getMetric() {
        JsonObject jsonObject = new JsonObject();
        for (Map.Entry<String, StatMetric> entry : metricMap.entrySet()) {
            StatMetric statMetric = entry.getValue();
            if (statMetric instanceof RequestMetric) {
                RequestMetric requestMetric = (RequestMetric) statMetric;
                jsonObject.add(String.format("%s.%s", requestMetric.getReqDirection().getDesc(), entry.getKey()),
                        requestMetric.getMetric());
            } else {
                jsonObject.add(entry.getKey(), statMetric.getMetric());
            }
        }
        return jsonObject;
    }

    public interface Builder<T extends StatMetric> {
        T build();

        Builder<Counter> COUNTER = Counter::new;

        Builder<CacheMetric> CACHE_STATS_COUNTER = CacheMetric::new;
    }

    public static class StwCounterBuilder implements Builder<SlidingTimeWindowCounter> {
        /**
         * histogram need this
         */
        private final long duration;

        /**
         * for histogram need this
         */
        private final TimeUnit timeUnit;

        public StwCounterBuilder(long duration, TimeUnit timeUnit) {
            this.duration = duration;
            this.timeUnit = timeUnit;
        }

        public SlidingTimeWindowCounter build() {
            return new SlidingTimeWindowCounter(duration, timeUnit);
        }
    }

    public static class ThreadPoolMetricBuilder implements Builder<ThreadPoolMetric> {
        private ThreadPoolExecutor threadPoolExecutor;

        public ThreadPoolMetricBuilder(ThreadPoolExecutor threadPoolExecutor) {
            this.threadPoolExecutor = threadPoolExecutor;
        }

        public ThreadPoolMetric build() {
            return new ThreadPoolMetric(threadPoolExecutor);
        }
    }

    public static class RequestMetricBuilder implements Builder<RequestMetric> {
        /**
         * histogram need this
         */
        private final long duration;

        /**
         * for histogram need this
         */
        private final TimeUnit timeUnit;

        private final ReqDirection reqDirection;

        public RequestMetricBuilder(long duration, TimeUnit timeUnit, ReqDirection reqDirection) {
            this.duration = duration;
            this.timeUnit = timeUnit;
            this.reqDirection = reqDirection;
        }

        public RequestMetric build() {
            return new RequestMetric(duration, timeUnit, reqDirection);
        }
    }

}