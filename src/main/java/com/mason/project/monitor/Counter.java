package com.mason.project.monitor;

import com.google.gson.JsonObject;

public class Counter extends com.codahale.metrics.Counter implements StatMetric {
    @Override
    public JsonObject getMetric() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("count", getCount());
        return jsonObject;
    }
}
