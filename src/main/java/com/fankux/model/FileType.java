package com.fankux.model;

public enum FileType {
    FILE(1),
    DIR(2);

    FileType(Integer code) {
        this.code = code;
    }

    public Integer code() {
        return code;
    }

    private Integer code;
}
