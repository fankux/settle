package com.fl.dirsync;

import com.fl.dao.FileDao;
import com.fl.dao.SyncPathDao;
import com.fl.entity.FileItem;
import com.fl.entity.PathItem;
import com.fl.service.SyncFlag;
import com.fl.service.FileType;
import com.fl.util.PathUtils;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.stream.Collectors;

@Component
public class SyncTask {
    private static Logger logger = LoggerFactory.getLogger(SyncTask.class);

    @Value("${settle.conf.defaultRootPath}")
    String defaultRootPath;

    @Resource
    FileDao fileDao;
    @Resource
    SyncPathDao syncPathDao;

    private Thread syncThread;
    private boolean syncFlag = false;
    private BlockingDeque<Pair<String, Boolean>> dirs = Queues.newLinkedBlockingDeque();
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

        String flag = syncPathDao.getSyncFlag();
        if (flag.equals(SyncFlag.DONE.name())) {
            syncFlag = false;
            return;
        }

        List<PathItem> items = syncPathDao.selectAll();
        dirs = items.stream().map(pathItem -> ImmutablePair.of(pathItem.getPath(), true)).
                collect(Collectors.toCollection(Queues::newLinkedBlockingDeque));

        if (dirs.isEmpty()) {
            dirs.add(ImmutablePair.of("/", true));
        }

        startSync();
    }

    @PreDestroy
    private void dump() {
        logger.warn("dumping sync dir...");
        List<PathItem> items = dirs.stream().map(pair -> new PathItem(pair.getLeft())).collect(Collectors.toList());
        syncPathDao.deleteAll();
        syncPathDao.insertBatch(items);
    }

    /**
     * 停止刷新线程
     */
    public void stop() {
        syncFlag = false;
        syncThread.interrupt();
        // TODO  join thread
    }

    /**
     * 刷新指定目录
     *
     * @param path
     */
    public void refresh(String path) {
        syncPathDao.setSyncFlag(SyncFlag.DOING.name());
        startSync();
        dirs.addFirst(ImmutablePair.of(path, false));
    }

    /**
     * 刷新指定目录集合
     *
     * @param paths
     */
    public void refreshAll(List<String> paths) {
        syncPathDao.setSyncFlag(SyncFlag.DOING.name());
        startSync();
        for (String path : paths) {
            dirs.addFirst(ImmutablePair.of(path, false));
        }
    }

    private void startSync() {
        if (syncThread != null && syncThread.isAlive()) {
            return;
        }

        syncPathDao.setSyncFlag(SyncFlag.DOING.name());
        syncFlag = true;
        syncThread = new Thread(this::syncAll);
        syncThread.start();
    }

    private void syncAll() {
        while (syncFlag) {
            Pair<String, Boolean> pair;
            try {
                pair = dirs.take();
            } catch (InterruptedException e) {
                logger.warn("take dir interrupt");
                continue;
            }
            syncPath(pair.getLeft(), pair.getRight());
            if (dirs.isEmpty()) {
                syncPathDao.setSyncFlag(SyncFlag.DONE.name());
            }
        }
    }

    /**
     * 索引文件夹递归遍历递归体, DB操作带实务
     *
     * @param path      根目录
     * @param recursion 是否继续遍历子文件夹
     */
    @Transactional
    public void syncPath(String path, boolean recursion) {
        logger.info("fetching {}", path);

        File fp = new File(defaultRootPath + path);
        File[] files = fp.listFiles();
        if (files == null) {
            throw new RuntimeException("list path failed : " + path);
        }

        // clean all items of current path first
        fileDao.deleteByPath(path);

        for (File f : files) {
            String filePath = PathUtils.cleanfix(f.getPath());
            filePath = PathUtils.padPrefixSlash(filePath.replace(defaultRootPath, ""));

            FileItem item = new FileItem();
            if (f.isDirectory()) {
                item.setType(FileType.DIR.code());
                item.setPath(path);
                item.setFileName(f.getName());
                fileDao.insert(item);

                if (recursion) {
                    syncPathDao.insert(new PathItem(filePath));
                    dirs.add(ImmutablePair.of(filePath, true));
                }
                continue;
            }

            int idx = f.getName().lastIndexOf('.');
            if (idx == -1) {
                continue;
            }
            String suffix = f.getName().substring(idx + 1);
            if (!ALLOWED_IMAGE_TYPES.contains(suffix)) {
                continue;
            }

            item.setType(FileType.FILE.code());
            item.setPath(path);
            item.setFileName(f.getName());
            fileDao.insert(item);
        }

        // current path sync finish, delete sync path
        syncPathDao.delete(path);
    }
}
