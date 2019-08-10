package com.mason.project.monitor;

import com.google.common.cache.AbstractCache;
import com.google.common.cache.CacheStats;
import com.google.gson.JsonObject;

public class CacheMetric implements StatMetric, AbstractCache.StatsCounter {

    private AbstractCache.StatsCounter counter;

    public CacheMetric() {
        this.counter = new AbstractCache.SimpleStatsCounter();
    }

    @Override
    public void recordHits(int i) {
        counter.recordHits(i);
    }

    @Override
    public void recordMisses(int i) {
        counter.recordMisses(i);
    }

    @Override
    public void recordLoadSuccess(long l) {
        counter.recordLoadSuccess(l);
    }

    @Override
    public void recordLoadException(long l) {
        counter.recordLoadException(l);
    }

    @Override
    public void recordEviction() {
        counter.recordEviction();
    }

    @Override
    public CacheStats snapshot() {
        return null;
    }

    @Override
    public JsonObject getMetric() {
        CacheStats cacheStats = counter.snapshot();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("hitCount", cacheStats.hitCount());
        jsonObject.addProperty("missCount", cacheStats.missCount());
        jsonObject.addProperty("loadSuccessCount", cacheStats.loadSuccessCount());
        jsonObject.addProperty("loadExceptionCount", cacheStats.loadExceptionCount());
        jsonObject.addProperty("totalLoadTime", cacheStats.totalLoadTime());
        jsonObject.addProperty("evictionCount", cacheStats.evictionCount());
        return jsonObject;
    }
}
