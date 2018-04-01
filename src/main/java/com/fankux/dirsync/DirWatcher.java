package com.fankux.dirsync;

import com.fankux.dao.FileDao;
import com.fankux.util.PathUtils;
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
    2018-04-01 20:51:51.017  INFO 31908 --- [       Thread-6] com.fankux.dirsync.DirWatcher            : wd = 1, rootPath = E:/a1r/
2018-04-01 20:51:51.017  INFO 31908 --- [       Thread-6] com.fankux.dirsync.DirWatcher            : oldName = aaa.txt, newName = bbb.txt
2018-04-01 20:52:04.995  INFO 31908 --- [       Thread-6] com.fankux.dirsync.DirWatcher            : 1 E:/a1r/ bbb.txt Deleted
2018-04-01 20:52:04.995  INFO 31908 --- [       Thread-6] com.fankux.dirsync.DirWatcher            : 1 E:/a1r/ 1\bbb.txt Created
2018-04-01 20:52:04.995  INFO 31908 --- [       Thread-6] com.fankux.dirsync.DirWatcher            : 1 E:/a1r/ 1 Modified
2018-04-01 20:53:07.568  INFO 31908 --- [       Thread-6] com.fankux.dirsync.DirWatcher            : 1 E:/a1r/ 1\新建文本文档.txt Created
2018-04-01 20:53:07.568  INFO 31908 --- [       Thread-6] com.fankux.dirsync.DirWatcher            : 1 E:/a1r/ 1\新建文本文档.txt Modified
2018-04-01 20:53:07.572  INFO 31908 --- [       Thread-6] com.fankux.dirsync.DirWatcher            : 1 E:/a1r/ 1 Modified
2018-04-01 20:53:10.133  INFO 31908 --- [       Thread-6] com.fankux.dirsync.DirWatcher            : wd = 1, rootPath = E:/a1r/
2018-04-01 20:53:10.133  INFO 31908 --- [       Thread-6] com.fankux.dirsync.DirWatcher            : oldName = 1\新建文本文档.txt, newName = 1\ccc.txt
2018-04-01 20:53:10.134  INFO 31908 --- [       Thread-6] com.fankux.dirsync.DirWatcher            : 1 E:/a1r/ 1 Modified
2018-04-01 20:53:14.059  INFO 31908 --- [       Thread-6] com.fankux.dirsync.DirWatcher            : 1 E:/a1r/ 1\ccc.txt Deleted
2018-04-01 20:53:14.154  INFO 31908 --- [       Thread-6] com.fankux.dirsync.DirWatcher            : 1 E:/a1r/ 1 Modified
     */

    private void sync() throws JNotifyException {
        JNotify.addWatch(defaultRootPath, JNotify.FILE_ANY, true, new JNotifyListener() {
            @Override
            public void fileRenamed(int wd, String rootPath, String oldName, String newName) {
                logger.info("wd = " + wd + ", rootPath = " + rootPath);
                logger.info("oldName = " + oldName + ", newName = " + newName);
            }

            @Override
            public void fileModified(int wd, String rootPath, String fileName) {
                logger.info("{} {} {} Modified", wd, rootPath, fileName);
            }

            @Override
            public void fileDeleted(int wd, String rootPath, String fileName) {
                logger.info("{} {} {} Deleted", wd, rootPath, fileName);
            }

            @Override
            public void fileCreated(int wd, String rootPath, String fileName) {
                logger.info("{} {} {} Created", wd, rootPath, fileName);
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

