package com.fankux.service;

import com.fankux.dao.FileDao;
import com.fankux.dirsync.SyncTask;
import com.fankux.entity.FileItem;
import com.fankux.model.FileResponse;
import com.fankux.util.PathUtils;
import com.google.common.collect.Sets;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FileService {
    @Resource
    FileDao fileDao;

    @Resource
    SyncTask syncTask;

    private static Set<String> ALLOWED_IMAGE_TYPES = Sets.newHashSet();

    static {
        ALLOWED_IMAGE_TYPES.add("png");
        ALLOWED_IMAGE_TYPES.add("jpg");
        ALLOWED_IMAGE_TYPES.add("jpeg");
        ALLOWED_IMAGE_TYPES.add("gif");
    }

    public List<FileResponse> fileList(String path, Integer start, Integer count) {
        if (StringUtils.isEmpty(path)) {
            path = "/";
        }
        List<FileItem> items = fileDao.selectDirItems(path, start, count);
        // 触发当前打开的子文件夹刷新遍历, 不递归
        syncTask.refreshAll(items.stream().filter(fileItem -> fileItem.getType().equals(FileType.DIR.code())).
                map(item -> PathUtils.padSuffixSlash(item.getPath()) + item.getFileName()).
                collect(Collectors.toList()));
        return items.stream().map(FileResponse::buildFrom).collect(Collectors.toList());
    }

    public void getVideoStream() {

    }
}
