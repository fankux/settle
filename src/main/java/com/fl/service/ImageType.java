package com.fl.service;

public enum ImageType {
    JPG(1),
    PNG(2),
    GIF(3);

    ImageType(Integer code) {
        this.code = code;
    }

    public Integer code() {
        return code;
    }

    private Integer code;
}
