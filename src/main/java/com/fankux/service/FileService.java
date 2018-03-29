package com.fankux.service;

import cc.eguid.FFmpegCommandManager.FFmpegManager;
import cc.eguid.FFmpegCommandManager.FFmpegManagerImpl;

import cc.eguid.FFmpegCommandManager.entity.TaskEntity;
import com.fankux.model.FileItem;
import com.fankux.model.FileType;
import com.fankux.util.PathUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;

@Service
public class FileService {
    private static Set<String> ALLOWED_IMAGE_TYPES = Sets.newHashSet();

    static {
        ALLOWED_IMAGE_TYPES.add("png");
        ALLOWED_IMAGE_TYPES.add("jpg");
        ALLOWED_IMAGE_TYPES.add("jpeg");
        ALLOWED_IMAGE_TYPES.add("gif");
    }

    @Value("${settle.conf.defaultRootPath}")
    String defaultRootPath;

    public List<FileItem> fileList(String path, Integer start, Integer count) {
        defaultRootPath = PathUtils.padSuffixSlash(defaultRootPath);
        File fp = new File(defaultRootPath + path);
        File[] files = fp.listFiles();
        if (files == null) {
            return null;
        }

        int dircount = 0;
        List<FileItem> items = Lists.newArrayList();
        for (File f : files) {
            int idx;
            String filePath = PathUtils.cleanfix(f.getPath());
            filePath = PathUtils.padPrefixSlash(filePath.replace(defaultRootPath, ""));
            FileItem item = new FileItem();
            if (f.isDirectory()) {
                item.setFileName(f.getName());
                item.setSrc(filePath);
                item.setType(FileType.DIR.code());
                items.add(item);
                ++dircount;
                continue;
            }

            idx = f.getName().lastIndexOf('.');
            if (idx == -1) {
                continue;
            }
            String suffix = f.getName().substring(idx + 1);
            if (!ALLOWED_IMAGE_TYPES.contains(suffix)) {
                continue;
            }

            item.setType(FileType.FILE.code());
            item.setSrc(filePath);
            item.setFileName(f.getName());

            items.add(item);
        }
        if (dircount >= (items.size() / 2)) {
            return items;
        }
        if (start != 0 && start >= items.size()) {
            return Lists.newArrayList();
        }
        return items.subList(start, Math.min(start + count, items.size()));
    }


    public void getVideoStream() {

    }
}
