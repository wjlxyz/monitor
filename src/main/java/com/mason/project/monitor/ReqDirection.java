package com.mason.project.monitor;

public enum ReqDirection {
    IN("in"),
    OUT("out");

    private String desc;

    ReqDirection(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
