package com.fl.entity;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.Date;
import java.util.Objects;

public class FileItem {
    private Integer id;
    private Integer type;
    private String path;
    private String fileName;
    private String extraInfo;
    private Date updateTime;
    private Long mtime;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileItem fileItem = (FileItem) o;
        return Objects.equals(type, fileItem.type) &&
                Objects.equals(path, fileItem.path) &&
                Objects.equals(fileName, fileItem.fileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, path, fileName);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(String extraInfo) {
        this.extraInfo = extraInfo;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Long getMtime() {
        return mtime;
    }

    public void setMtime(Long mtime) {
        this.mtime = mtime;
    }
}
