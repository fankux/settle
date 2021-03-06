package com.fl.service;

public enum FileType {
    FILE(1),
    DIR(2),
    IMG(3),
    VIDEO(4);

    FileType(Integer code) {
        this.code = code;
    }

    public Integer code() {
        return code;
    }

    private Integer code;
}
