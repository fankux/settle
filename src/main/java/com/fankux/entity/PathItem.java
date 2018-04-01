package com.fankux.entity;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class PathItem {
    private Integer id;
    private String path;

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    public PathItem() {
    }

    public PathItem(String path) {
        this.path = path;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
