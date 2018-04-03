package com.fl.service;

public enum SyncFlag {
    INIT(1),
    DOING(2),
    DONE(3);

    public Integer code() {
        return code;
    }

    SyncFlag(Integer code) {
        this.code = code;
    }

    private Integer code;
}
