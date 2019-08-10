package com.mason.project.monitor;

import com.codahale.metrics.Clock;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.SlidingTimeWindowArrayReservoir;
import com.google.gson.JsonObject;

import java.util.concurrent.TimeUnit;


public class SlidingTimeWindowCounter implements StatMetric {

    private final long duration;
    private final TimeUnit timeUnit;
    private final Histogram histogram;
    private final double durationFactor;

    public SlidingTimeWindowCounter(long duration, TimeUnit timeUnit) {
        this(duration, timeUnit, Clock.defaultClock());
    }

    /**
     * Counter with duration
     * <p>
     * histogram reset in next duration, total never reset
     * </p>
     *
     * @param duration duration
     * @param timeUnit time
     * @param clock    clock
     */
    public SlidingTimeWindowCounter(long duration, TimeUnit timeUnit, Clock clock) {
        this.duration = duration;
        this.timeUnit = timeUnit;
        durationFactor = 1 / (double) timeUnit.toSeconds(1);
        histogram = new Histogram(new SlidingTimeWindowArrayReservoir(this.duration, this.timeUnit, clock));
    }

    /**
     * increment the counter
     */
    public void increment() {
        histogram.update(1);
    }

    /**
     * get last sliding window count. Notice that this method overhead is significant.
     *
     * @return sliding window count
     */
    public long getCount() {
        return histogram.getSnapshot().size();
    }

    /**
     * get total count of counter
     *
     * @return total count
     */
    public long getTotal() {
        return histogram.getCount();
    }

    /**
     * get throughput in duration
     *
     * @return 1
     */
    public double getThroughputPerSec() {
        return ((double) getCount()) / duration * durationFactor;
    }

    public long getDuration() {
        return duration;
    }

    @Override
    public JsonObject getMetric() {
        JsonObject jsonObject = new JsonObject();
        long count = getCount();
        jsonObject.addProperty("count", getCount());
        jsonObject.addProperty("total", getTotal());
        jsonObject.addProperty("throughput", ((double) count) / duration * durationFactor);
        return jsonObject;
    }
}