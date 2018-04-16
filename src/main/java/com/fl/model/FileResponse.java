package com.fl.model;

import com.fl.entity.FileItem;
import com.fl.util.PathUtils;

public class FileResponse {
    private String fileName;
    private String src;
    private Integer type;
    private String extraInfo;

    public static FileResponse buildFrom(FileItem item) {
        FileResponse response = new FileResponse();
        response.setType(item.getType());
        response.setSrc(PathUtils.padSuffixSlash(item.getPath()) + item.getFileName());
        response.setFileName(item.getFileName());
        response.setExtraInfo(item.getExtraInfo());
        return response;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(String extraInfo) {
        this.extraInfo = extraInfo;
    }
}
