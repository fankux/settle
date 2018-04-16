package com.fl.service;

import com.fl.dao.FileDao;
import com.fl.dirsync.SyncTask;
import com.fl.entity.FileItem;
import com.fl.model.FileResponse;
import com.fl.util.PathUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.File;
import java.util.Comparator;
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
        List<String> refreshList = Lists.newArrayList();
        if (StringUtils.isEmpty(path)) {
            path = "/";
            refreshList.add(path);
        }
        Set<FileItem> items = fileDao.selectDirItems(path, start, count);
        // 触发当前打开的子文件夹刷新遍历, 不递归
        refreshList.addAll(items.stream().filter(fileItem -> fileItem.getType().equals(FileType.DIR.code())).
                map(item -> PathUtils.padSuffixSlash(item.getPath()) + item.getFileName()).
                collect(Collectors.toList()));
        syncTask.refreshAll(refreshList);
        return items.stream().map(FileResponse::buildFrom).sorted(Comparator.comparing(FileResponse::getFileName)).
                collect(Collectors.toList());
    }

    public boolean fileRename(String path, String oldName, String newName) {
        File oldFile = new File(path, oldName);
        if (!oldFile.exists()) {
            return false;
        }

        File newFile = new File(path, newName);
        if (!oldFile.renameTo(newFile)) {
            return false;
        }

        FileItem oldItem = new FileItem();
        oldItem.setType(oldFile.isDirectory() ? FileType.DIR.code() : FileType.FILE.code());
        oldItem.setPath(path);
        oldItem.setFileName(oldName);

        FileItem newItem = new FileItem();
        newItem.setType(oldItem.getType());
        newItem.setPath(path);
        newItem.setFileName(oldName);

        fileDao.update(oldItem, newItem);
        if (oldItem.getType().equals(FileType.DIR.code())) {
            // 更新目录的话还需要触发递归遍历更新
            syncTask.refresh(PathUtils.padSuffixSlash(path) + oldName, true);
        }

        return true;
    }

    public void getVideoStream() {

    }
}
