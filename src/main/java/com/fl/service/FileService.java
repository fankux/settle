package com.fl.service;

import com.fl.dao.FileDao;
import com.fl.dirsync.SyncTask;
import com.fl.entity.FileItem;
import com.fl.model.FileRequest;
import com.fl.model.FileResponse;
import com.fl.util.PathUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FileService {
    @Resource
    FileDao fileDao;

    @Resource
    SyncTask syncTask;

    @Value("${settle.conf.defaultRootPath}")
    String defaultRootPath;

    private static Set<String> ALLOWED_IMAGE_TYPES = Sets.newHashSet();

    static {
        ALLOWED_IMAGE_TYPES.add("png");
        ALLOWED_IMAGE_TYPES.add("jpg");
        ALLOWED_IMAGE_TYPES.add("jpeg");
        ALLOWED_IMAGE_TYPES.add("gif");
    }

    @PostConstruct
    private void init() {
        defaultRootPath = PathUtils.padSuffixSlash(defaultRootPath);
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
        return items.stream().map(FileResponse::buildFrom).sorted((o1, o2) -> {
            int ret = o2.getType().compareTo(o1.getType());
            if (ret == 0) {
                ret = o1.getFileName().compareTo(o2.getFileName());
            }
            return ret;
        }).collect(Collectors.toList());
    }

    public List<FileResponse> dirList(FileRequest file) {
        Set<FileItem> items = fileDao.selectDirs(file.getPath());
        return items.stream().map(FileResponse::buildFrom).
                sorted(Comparator.comparing(FileResponse::getFileName)).
                collect(Collectors.toList());
    }

    public boolean rename(FileRequest fileRequest, FileRequest newFileRequest) {
        fileRequest.setPath(PathUtils.clearPrefixSlash(fileRequest.getPath()));

        File oldFile = new File(defaultRootPath + fileRequest.getPath(), fileRequest.getName());
        if (!oldFile.exists()) {
            return false;
        }

        newFileRequest.setPath(PathUtils.clearPrefixSlash(newFileRequest.getPath()));
        File newFile = new File(defaultRootPath + newFileRequest.getPath(), newFileRequest.getName());
        if (!oldFile.renameTo(newFile)) {
            return false;
        }

        FileItem oldItem = new FileItem();
        oldItem.setType(oldFile.isDirectory() ? FileType.DIR.code() : FileType.FILE.code());
        oldItem.setPath(fileRequest.getPath());
        oldItem.setFileName(fileRequest.getName());

        FileItem newItem = new FileItem();
        newItem.setType(oldItem.getType());
        newItem.setPath(fileRequest.getPath());
        newItem.setFileName(newFileRequest.getName());

        fileDao.update(oldItem, newItem);
        if (oldItem.getType().equals(FileType.DIR.code())) {
            // 更新目录的话还需要触发递归遍历更新
            syncTask.refresh(PathUtils.padSuffixSlash(fileRequest.getPath()) + fileRequest.getName(), true);
        }

        return true;
    }

    public boolean delete(FileRequest fileRequest) {
        fileRequest.setPath(PathUtils.clearPrefixSlash(fileRequest.getPath()));
        File file = new File(defaultRootPath + fileRequest.getPath(), fileRequest.getName());
        if (!file.exists()) {
            return true;
        }

        try {
            if (file.isDirectory()) {
                FileUtils.deleteDirectory(file);

                fileDao.deleteByPath(fileRequest.getPath());
            } else {
                file.delete();

                FileItem item = new FileItem();
                item.setPath(fileRequest.getPath());
                item.setFileName(fileRequest.getName());
                item.setType(FileType.FILE.code());
                fileDao.delete(item);
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean deleteBatch(Set<FileRequest> files) {
        boolean ret = true;
        for (FileRequest file : files) {
            if (!delete(file)) {
                ret = false;
            }
        }
        return ret;
    }

    public boolean move(FileRequest fileRequest, FileRequest newFileRequest) {
        fileRequest.setPath(PathUtils.clearPrefixSlash(fileRequest.getPath()));

        File file = new File(defaultRootPath + fileRequest.getPath(), fileRequest.getName());
        if (!file.exists()) {
            return false;
        }

        newFileRequest.setPath(PathUtils.clearPrefixSlash(newFileRequest.getPath()));
        File newFile = new File(defaultRootPath + newFileRequest.getPath());

        try {
            if (file.isDirectory()) {
                FileUtils.moveDirectory(file, newFile);
            } else {
                FileUtils.moveFileToDirectory(file, newFile, false);
            }

            FileItem oldItem = new FileItem();
            oldItem.setType(file.isDirectory() ? FileType.DIR.code() : FileType.FILE.code());
            oldItem.setPath(fileRequest.getPath());
            oldItem.setFileName(fileRequest.getName());

            FileItem newItem = new FileItem();
            newItem.setType(oldItem.getType());
            newItem.setPath(fileRequest.getPath());
            newItem.setFileName(newFileRequest.getName());
            newItem.setMtime(file.lastModified());
            fileDao.update(oldItem, newItem);
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public boolean upload(String path, String name, CommonsMultipartFile upload) {
        String ext = PathUtils.extension(upload.getOriginalFilename()).toLowerCase();
        if (!ALLOWED_IMAGE_TYPES.contains(ext)) {
            return false;
        }

        path = defaultRootPath + PathUtils.clearPrefixSlash(path);
        File file = new File(path);
        if (!file.exists()) {
            return false;
        }
        file = new File(path, name);
        if (file.exists()) {
            return false;
        }

        try {
            upload.transferTo(file);

            FileItem item = new FileItem();
            item.setType(FileType.FILE.code());
            item.setPath(file.getPath());
            item.setFileName(file.getName());
            item.setMtime(file.lastModified());
            fileDao.insert(item);
        } catch (IOException e) {
            return false;
        }

        return true;
    }

}
