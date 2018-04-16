package com.fl.dirsync;

import com.fl.dao.FileDao;
import com.fl.dao.SyncPathDao;
import com.fl.entity.FileItem;
import com.fl.entity.PathItem;
import com.fl.service.ImageMeta;
import com.fl.service.ImageService;
import com.fl.service.SyncFlag;
import com.fl.service.FileType;
import com.fl.util.PathUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class SyncTask {
    private static Logger logger = LoggerFactory.getLogger(SyncTask.class);

    @Value("${settle.conf.defaultRootPath}")
    String defaultRootPath;

    @Resource
    ImageService imageService;
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

//        if (dirs.isEmpty()) {
//            dirs.add(ImmutablePair.of("/", true));
//        }
//
//        startSync();
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
        refresh(path, false);
    }

    /**
     * 刷新指定目录, 可指定是否递归
     *
     * @param path      路径
     * @param recursive 是否递归
     */
    public void refresh(String path, boolean recursive) {
        syncPathDao.setSyncFlag(SyncFlag.DOING.name());
        startSync();
        dirs.addFirst(ImmutablePair.of(path, recursive));
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
                syncPath(pair.getLeft(), pair.getRight());
            } catch (InterruptedException e) {
                logger.warn("take dir interrupt");
                continue;
            } catch (Exception e) {
                logger.warn("take dir exception", e);
                continue;
            }
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

        Map<FileItem, FileItem> items = Maps.newHashMap();
        for (File f : files) {
            String filePath = PathUtils.cleanfix(f.getPath());
            filePath = PathUtils.padPrefixSlash(filePath.replace(defaultRootPath, ""));

            FileItem item = new FileItem();
            if (f.isDirectory()) {
                item.setType(FileType.DIR.code());
                item.setMtime(f.lastModified());
                item.setPath(path);
                item.setFileName(f.getName());
                item.setExtraInfo("{}");
                items.put(item, item);

                if (recursion) {
                    syncPathDao.insert(new PathItem(filePath));
                    dirs.add(ImmutablePair.of(filePath, true));
                }
                continue;
            }

            String extension = PathUtils.extension(f.getName());
            if (!ALLOWED_IMAGE_TYPES.contains(extension)) {
                continue;
            }

            // 解析图片信息(图片大小等)
            ImageMeta meta = imageService.fetchImageMeta(f);
            if (meta == null) {
                logger.error("fetching image info failed : {}", path);
                continue;
            }

            item.setType(FileType.FILE.code());
            item.setMtime(f.lastModified());
            item.setPath(path);
            item.setFileName(f.getName());
            item.setExtraInfo(meta.toJsonString());
            items.put(item, item);
        }

        handleInStore(path, items);

        // current path sync finish, delete sync path
        syncPathDao.delete(path);
    }

    private void handleInStore(String path, Map<FileItem, FileItem> items) {
        // 当前新获得的文件夹下的item集合, 对比db中存的, 删掉已经不存在的, 更新或新增新的
        Set<Integer> deleteIds = Sets.newHashSet();
        Set<FileItem> updateItems = Sets.newHashSet();

        Set<FileItem> itemsInStore = fileDao.selectDirItems(path, null, null);
        Iterator<FileItem> iter = itemsInStore.iterator();
        for (; iter.hasNext(); ) {
            FileItem item = iter.next();

            FileItem newItem = items.get(item);
            if (newItem == null) {
                deleteIds.add(item.getId());
                iter.remove();
                items.remove(item);
                continue;
            }

            if (!newItem.getMtime().equals(item.getMtime())) {
                newItem.setId(item.getId());
                updateItems.add(newItem);
                items.remove(item);
            }
        }

        // clean all items of current path first
        // TODO.. 分组执行
        if (!deleteIds.isEmpty()) {
            fileDao.deleteByIds(deleteIds);
        }
        if (!items.isEmpty()) {
            fileDao.insertBatch(items.keySet());
        }
        for (FileItem item : updateItems) {
            fileDao.updateById(item.getId(), item);
        }
    }
}
