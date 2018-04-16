package com.fl.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class ImageMeta extends MetaBase {
    private Integer type;
    private Integer width;
    private Integer height;
    private Integer smWidth;
    private Integer smHeight;

    public ImageMeta() {
        this.fileType = FileType.IMG.code();
    }

    public String toJsonString() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "";
        }
    }

    public void setSize(ImageSize size) {
        width = size.getWidth();
        height = size.getHeight();
    }

    public void setSmSize(ImageSize size) {
        smWidth = size.getWidth();
        smHeight = size.getHeight();
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    public void setType(String suffix) {
        switch (suffix) {
            case "jpg":
            case "jpeg":
                this.type = ImageType.JPG.code();
                break;
            case "png":
                this.type = ImageType.PNG.code();
                break;
            case "gif":
                this.type = ImageType.GIF.code();
                break;
        }
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getType() {
        return type;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getSmWidth() {
        return smWidth;
    }

    public void setSmWidth(Integer smWidth) {
        this.smWidth = smWidth;
    }

    public Integer getSmHeight() {
        return smHeight;
    }

    public void setSmHeight(Integer smHeight) {
        this.smHeight = smHeight;
    }
}
