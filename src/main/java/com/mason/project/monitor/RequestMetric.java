package com.mason.project.monitor;

import com.codahale.metrics.*;
import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;


public class RequestMetric implements StatMetric {

    private static Set<ErrorCode> DB_ERROR_CODES = new HashSet<>();
    private static Set<ErrorCode> INTERNAL_ERROR_CODES = new HashSet<>();
    private double durationFactor = 1 / (double) TimeUnit.MICROSECONDS.toNanos(1);
    private double throughputFactor;

    static {
        for (ErrorCode errorCode : ErrorCode.values()) {
            if (errorCode.name().startsWith("INTERNAL_DB")) {
                DB_ERROR_CODES.add(errorCode);
            } else if (errorCode.name().startsWith("INTERNAL_")) {
                INTERNAL_ERROR_CODES.add(errorCode);
            }
        }
    }

    /**
     * histogram need this
     */
    private final long duration;

    /**
     * for histogram need this
     */
    private final TimeUnit timeUnit;

    private final ReqDirection reqDirection;

    /**
     * clock for timer
     */
    private final Clock clock;

    /**
     * records success request-responses. Note that
     * 1. biz error should be treated as success
     * 2. one response is returned from depend module, request should be treated as success.
     */
    private final Timer successTimer;

    /**
     * records internal errors.
     */
    private final Histogram internalErrorHistogram;

    /**
     * records db errors.
     */
    private final Histogram dbErrorHistogram;

    /**
     * Notice: duration convert to nano should not exceed {@link Long#MAX_VALUE}
     *
     * @param duration     specifies for how long you want to maintain this counter, histogram
     * @param timeUnit     time
     * @param reqDirection req
     */
    public RequestMetric(long duration, TimeUnit timeUnit, ReqDirection reqDirection) {
        this(duration, timeUnit, reqDirection, Clock.defaultClock());
    }

    public RequestMetric(long duration, TimeUnit timeUnit, ReqDirection reqDirection, Clock clock) {
        Preconditions.checkArgument(duration > 0, "duration should be positive");
        Preconditions.checkArgument(timeUnit != null, "timeUnit should not be null");

        this.duration = duration;
        this.timeUnit = timeUnit;
        this.clock = clock;
        this.reqDirection = reqDirection;
        this.successTimer = new Timer(new SlidingTimeWindowArrayReservoir(this.duration, this.timeUnit, this.clock));
        this.internalErrorHistogram =
                new Histogram(new SlidingTimeWindowArrayReservoir(this.duration, this.timeUnit, this.clock));
        this.dbErrorHistogram =
                new Histogram(new SlidingTimeWindowArrayReservoir(this.duration, this.timeUnit, this.clock));

        throughputFactor = 1 / (double) timeUnit.toSeconds(duration);
    }

    /**
     * add request used time
     *
     * @param timeNs    request time in Nano second
     * @param errorCode errorCode
     */
    public void addRequest(long timeNs, ErrorCode errorCode) {
        // biz error wont be added to errorCount
        boolean isDbError = DB_ERROR_CODES.contains(errorCode);
        boolean isInternalError = INTERNAL_ERROR_CODES.contains(errorCode);
        if (isDbError) {
            dbErrorHistogram.update(1);
        }
        if (isInternalError) {
            internalErrorHistogram.update(1);
        }
        // biz error. biz error treated as success
        if (!isDbError && !isInternalError) {
            addSuccessRequest(timeNs);
        }
    }

    public void addSuccessRequest(long timeNs) {
        successTimer.update(timeNs, TimeUnit.NANOSECONDS);
    }

    public ReqDirection getReqDirection() {
        return reqDirection;
    }


    /**
     * Returns a new {@link Timer.Context}.
     *
     * @return a new {@link Timer.Context}
     * @see Timer.Context
     */
    public RequestMetric.Context time() {
        return new RequestMetric.Context(this, clock);
    }

    /**
     * A timing context.
     *
     * @see RequestMetric#time()
     */
    public static class Context implements AutoCloseable {
        private final RequestMetric requestMetric;
        private final Clock clock;
        private final long startTime;

        private Context(RequestMetric requestMetric, Clock clock) {
            this.requestMetric = requestMetric;
            this.clock = clock;
            this.startTime = clock.getTick();
        }

        /**
         * Updates the RequestMetric with the difference between current and start time. Call to this method will
         * not reset the start time. Multiple calls result in multiple updates.
         *
         * @return the elapsed time in nanoseconds
         */
        public long success() {
            final long elapsed = clock.getTick() - startTime;
            requestMetric.addSuccessRequest(elapsed);
            return elapsed;
        }

        /**
         * Updates the RequestMetric with the difference between current and start time. Call to this method will
         * not reset the start time. Multiple calls result in multiple updates.
         *
         * @param errorCode errorCode
         * @return the elapsed time in nanoseconds
         */
        public long stop(ErrorCode errorCode) {
            final long elapsed = clock.getTick() - startTime;
            if (errorCode == ErrorCode.SUCCESS) {
                requestMetric.addSuccessRequest(elapsed);
            } else {
                requestMetric.addRequest(elapsed, errorCode);
            }
            return elapsed;
        }

        /**
         * Equivalent to calling {@link #success()}.
         */
        @Override
        public void close() {
            success();
        }
    }

    /**
     * @return JsonObject
     */
    @Override
    public JsonObject getMetric() {
        Snapshot timerSnapshot = successTimer.getSnapshot();
        Snapshot errorSnapshot = internalErrorHistogram.getSnapshot();
        Snapshot dbErrorSnapshot = dbErrorHistogram.getSnapshot();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("m1Rate", successTimer.getOneMinuteRate());
        jsonObject.addProperty("throughput", timerSnapshot.size() * throughputFactor);
        jsonObject.addProperty("averageTimeInUs", timerSnapshot.getMean() * durationFactor);
        jsonObject.addProperty("maxLatencyInUs", timerSnapshot.getMax() * durationFactor);
        jsonObject.addProperty("95p", timerSnapshot.get95thPercentile() * durationFactor);
        jsonObject.addProperty("99p", timerSnapshot.get99thPercentile() * durationFactor);
        jsonObject.addProperty("999P", timerSnapshot.get999thPercentile() * durationFactor);
        jsonObject.addProperty("successCount", timerSnapshot.size());
        jsonObject.addProperty("internalErrorCount", errorSnapshot.size());
        jsonObject.addProperty("dbErrorCount", dbErrorSnapshot.size());

        // fix bug here
        int totalSize = timerSnapshot.size() + errorSnapshot.size() + dbErrorSnapshot.size();
        jsonObject.addProperty("errorRate", totalSize == 0 ? 0 :
                (errorSnapshot.size() + dbErrorSnapshot.size()) / ((double) totalSize));
        jsonObject.addProperty("successTotal", successTimer.getCount());
        jsonObject.addProperty("InternalErrorTotal", internalErrorHistogram.getCount());
        jsonObject.addProperty("dbErrorTotal", dbErrorHistogram.getCount());
        return jsonObject;
    }
}