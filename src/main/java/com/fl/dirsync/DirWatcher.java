package com.fl.dirsync;

import com.fl.dao.FileDao;
import com.fl.entity.FileItem;
import com.fl.service.FileType;
import com.fl.util.PathUtils;
import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyException;
import net.contentobjects.jnotify.JNotifyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchService;
import java.util.Arrays;

@Component
public class DirWatcher implements Runnable, Closeable {
    private Logger logger = LoggerFactory.getLogger(DirWatcher.class);

    @Value("${settle.conf.defaultRootPath}")
    String defaultRootPath;

    @Value("${settle.conf.libPath}")
    String libPath;

    @Resource
    FileDao fileDao;

    private boolean syncFlag = true;
    private WatchService watcher;

    @PostConstruct
    private void init() {
        defaultRootPath = PathUtils.padSuffixSlash(defaultRootPath);

        try {
            // 初始化JNI环境
            boolean isAdded = false;
            final Field usrPathsField;
            usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
            usrPathsField.setAccessible(true);
            final String[] paths = (String[]) usrPathsField.get(null);
            for (String path : paths) {
                if (path.equals(libPath)) {
                    isAdded = true;
                    break;
                }
            }
            if (!isAdded) {
                final String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
                newPaths[newPaths.length - 1] = libPath;
                usrPathsField.set(null, newPaths);
            }

            logger.info("java.library.path: {}", System.getProperty("java.library.path"));
            logger.info("usr_paths: {}", Arrays.toString((String[]) usrPathsField.get(null)));
            usrPathsField.setAccessible(false);
            logger.info("jnotify env load ok");
        } catch (Exception e) {
            logger.error("failed to init jnotify, ", e);
            System.exit(-1);
        }

        new Thread(this).start();
    }

    @Override
    public void close() throws IOException {
        syncFlag = false;
        if (watcher != null) {
            watcher.close();
        }
    }

    @Override
    public void run() {
        try {
            sync();
        } catch (JNotifyException e) {
            // do nothing
        }
    }

    /*
        wd = 1, rootPath = E:/a1r/
        oldName = aaa.txt, newName = bbb.txt
        1 E:/a1r/ bbb.txt Deleted
        1 E:/a1r/ 1\bbb.txt Created
        1 E:/a1r/ 1 Modified
        1 E:/a1r/ 1\新建文本文档.txt Created
        1 E:/a1r/ 1\新建文本文档.txt Modified
        1 E:/a1r/ 1 Modified
        wd = 1, rootPath = E:/a1r/
        oldName = 1\新建文本文档.txt, newName = 1\ccc.txt
        1 E:/a1r/ 1 Modified
        1 E:/a1r/ 1\ccc.txt Deleted
        1 E:/a1r/ 1 Modified
     */
    private void sync() throws JNotifyException {
        JNotify.addWatch(defaultRootPath, JNotify.FILE_ANY, true, new JNotifyListener() {
            @Override
            public void fileRenamed(int wd, String rootPath, String oldName, String newName) {
                logger.info("{} {} rename form {} to {} ", wd, rootPath, oldName, newName);

                Path path = Paths.get(rootPath + oldName);
                Integer type = path.toFile().isDirectory() ? FileType.DIR.code() : FileType.FILE.code();

                FileItem old = new FileItem();
                old.setType(type);
                old.setPath(PathUtils.padPrefixSlash(PathUtils.dirname(oldName)));
                old.setFileName(PathUtils.basename(oldName));


                FileItem newItem = new FileItem();
                newItem.setType(type);
                newItem.setPath(PathUtils.padPrefixSlash(PathUtils.dirname(newName)));
                newItem.setFileName(PathUtils.basename(newName));

                logger.info("old : {} ; new : {}", old, newItem);

                fileDao.update(old, newItem);
            }

            @Override
            public void fileModified(int wd, String rootPath, String fileName) {
                logger.info("{} {} {} Modified", wd, rootPath, fileName);
                // do nothing
            }

            @Override
            public void fileDeleted(int wd, String rootPath, String fileName) {
                logger.info("{} {} {} Deleted", wd, rootPath, fileName);

                Path path = Paths.get(rootPath + fileName);
                Integer type = path.toFile().isDirectory() ? FileType.DIR.code() : FileType.FILE.code();

                FileItem item = new FileItem();
                item.setType(type);
                item.setPath(PathUtils.padPrefixSlash(PathUtils.dirname(fileName)));
                item.setFileName(PathUtils.basename(fileName));

                fileDao.delete(item);
            }

            @Override
            public void fileCreated(int wd, String rootPath, String fileName) {
                logger.info("{} {} {} Created", wd, rootPath, fileName);

                Path path = Paths.get(rootPath + fileName);
                Integer type = path.toFile().isDirectory() ? FileType.DIR.code() : FileType.FILE.code();

                FileItem item = new FileItem();
                item.setType(type);
                item.setPath(PathUtils.padPrefixSlash(PathUtils.dirname(fileName)));
                item.setFileName(PathUtils.basename(fileName));

                fileDao.insert(item);
            }
        });
        while (syncFlag) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                // do nothing
            }
        }
    }
}

