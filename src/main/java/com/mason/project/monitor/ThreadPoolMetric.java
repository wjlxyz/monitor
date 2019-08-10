package com.mason.project.monitor;

import com.google.gson.JsonObject;

import java.lang.ref.SoftReference;
import java.util.concurrent.ThreadPoolExecutor;

public class ThreadPoolMetric implements StatMetric {

    private final SoftReference<ThreadPoolExecutor> threadPoolExecutor;

    public ThreadPoolMetric(ThreadPoolExecutor threadPoolExecutor) {
        this.threadPoolExecutor = new SoftReference<>(threadPoolExecutor);
    }

    public int getPoolSize() {
        ThreadPoolExecutor tpe = threadPoolExecutor.get();
        return tpe == null ? 0 : tpe.getPoolSize();
    }

    public int getActiveCount() {
        ThreadPoolExecutor tpe = threadPoolExecutor.get();
        return tpe == null ? 0 : tpe.getActiveCount();
    }

    public long getTaskCount() {
        ThreadPoolExecutor tpe = threadPoolExecutor.get();
        return tpe == null ? 0 : tpe.getTaskCount();
    }

    public long getCompletedTaskCount() {
        ThreadPoolExecutor tpe = threadPoolExecutor.get();
        return tpe == null ? 0 : tpe.getCompletedTaskCount();
    }

    public long getQueueSize() {
        ThreadPoolExecutor tpe = threadPoolExecutor.get();
        return tpe == null ? 0 : tpe.getQueue().size();
    }

    @Override
    public JsonObject getMetric() {
        ThreadPoolExecutor tpe = threadPoolExecutor.get();
        JsonObject jsonObject = new JsonObject();
        if (tpe != null) {
            jsonObject.addProperty("queueSize", tpe.getQueue().size());
            jsonObject.addProperty("completedTaskCount", tpe.getCompletedTaskCount());
            jsonObject.addProperty("poolSize", tpe.getPoolSize());
            jsonObject.addProperty("activeCount", tpe.getActiveCount());
        } else {
            jsonObject.addProperty("queueSize", -1);
            jsonObject.addProperty("completedTaskCount", -1);
            jsonObject.addProperty("poolSize", -1);
            jsonObject.addProperty("activeCount", -1);
        }

        return jsonObject;
    }
}
