package com.fankux.controller;

import com.fankux.model.FileItem;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("api")
public class FileApiController {
    private static Logger logger = LoggerFactory.getLogger(FileStreamController.class);

    private static Set<String> ALLOWED_IMAGE_TYPES = Sets.newHashSet();

    static {
        ALLOWED_IMAGE_TYPES.add("png");
        ALLOWED_IMAGE_TYPES.add("jpg");
        ALLOWED_IMAGE_TYPES.add("jpeg");
        ALLOWED_IMAGE_TYPES.add("gif");
    }

    @Value("${conf.defaultRootPath}")
    String defaultRootPath;

    @RequestMapping("files")
    List<FileItem> fileList(@RequestParam("path") String path, @RequestParam("s") Integer start,
                            @RequestParam("c") Integer count) {
        File fp = new File(defaultRootPath + path);
        File[] files = fp.listFiles();
        if (files == null) {
            return null;
        }

        int dircount = 0;
        List<FileItem> items = Lists.newArrayList();
        for (File f : files) {
            int idx;
            String filePath = f.getPath();
            if ((idx = filePath.indexOf(':')) != -1) {
                filePath = filePath.substring(idx + 1);
            }

            FileItem item = new FileItem();
            if (f.isDirectory()) {
                item.setFileName(f.getName());
                item.setSrc(filePath.replace("\\a1r\\", ""));
                item.setType(2);
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

            item.setType(1);
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
}
