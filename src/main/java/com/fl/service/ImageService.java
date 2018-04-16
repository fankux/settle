package com.fl.service;

import com.fl.dirsync.SyncTask;
import com.fl.util.PathUtils;
import com.google.common.io.Closer;
import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Iterator;

@Service
public class ImageService {
    private static Logger logger = LoggerFactory.getLogger(SyncTask.class);


    @Value("${settle.conf.thumbnailPath}")
    private String thumbnailPath;

    @Value("${settle.conf.defaultRootPath}")
    private String defaultRootPath;

    @Value("${settle.conf.thumbnailImageSize}")
    private Double thumbnailImageSize;

    @PostConstruct
    private void init() {
        thumbnailPath = PathUtils.padSuffixSlash(thumbnailPath);
        defaultRootPath = PathUtils.padSuffixSlash(defaultRootPath);
    }

    private ImageSize getImageSize(File file) {
        String filePath = PathUtils.cleanfix(file.getPath());
        filePath = PathUtils.padPrefixSlash(filePath.replace(defaultRootPath, ""));
        String extension = PathUtils.extension(filePath);

        Iterator<ImageReader> iter = ImageIO.getImageReadersBySuffix(extension);
        if (!iter.hasNext()) {
            return new ImageSize();
        }

        ImageReader reader = iter.next();
        try (Closer closer = Closer.create()) {
            RandomAccessFile raf = closer.register(new RandomAccessFile(file, "r"));

            ImageInputStream stream = new FileImageInputStream(raf);
            reader.setInput(stream);
            int width = reader.getWidth(reader.getMinIndex());
            int height = reader.getHeight(reader.getMinIndex());

            return new ImageSize(width, height);
        } catch (IOException e) {
            logger.error("fetch image size error : ", e);
            return new ImageSize();
        } finally {
            reader.dispose();
        }
    }

    public ImageMeta fetchImageMeta(File file) {
        ImageMeta meta = new ImageMeta();
        meta.setSize(getImageSize(file));
        meta.setType(PathUtils.extension(file.getName()));

        ImageSize thumbnailSize = fetchThumbnailDisplaySize(meta.getWidth(), meta.getHeight());
        meta.setSmSize(thumbnailSize);

        return meta;
    }

    /**
     * 通过路径获得缩略图路径
     *
     * @param path 相对路径
     * @return 相对缩略图路径
     */
    public String fetchThumbnailPath(String path) {
        String thumbnailFileName = fetchFileNamePrefix(path) + "-sm.png";
        return thumbnailPath + thumbnailFileName;
    }

    /**
     * 获得原图
     *
     * @param filePath 相对路径
     * @param os       输出流
     */
    public boolean fetchImage(String filePath, OutputStream os) {
        return fetchFile(defaultRootPath + filePath, os);
    }

    /**
     * 获得缩略图
     *
     * @param filePath 相对路径
     * @param os       输出流
     */
    public boolean fetchThumbnail(String filePath, OutputStream os) {
        return fetchFile(fetchThumbnailPath(filePath), os);
    }

    /**
     * 获得缩略图的显示尺寸, 要保持缩略图的宽最大值恒定
     *
     * @param w 原始宽度
     * @param h 原始高度
     */
    public ImageSize fetchThumbnailDisplaySize(int w, int h) {
        ImageSize size = new ImageSize();

        boolean flag = false;
        if (h >= w) {
            if (w > thumbnailImageSize) {
                size.setWidth(thumbnailImageSize.intValue());
                size.setHeight((int) (thumbnailImageSize * h / w));
                flag = true;
            }
        } else {
            if (h > thumbnailImageSize) {
                size.setWidth(thumbnailImageSize.intValue());
                size.setHeight((int) (thumbnailImageSize * h / w));
                flag = true;
            }
        }

        if (!flag) {
            size.setWidth(w);
            size.setHeight(h);
        }

        return size;
    }

    /**
     * 生成缩略图, 并直接返回缩略图的输出流
     *
     * @param filePath 相对路径
     * @param os       输出流
     * @return OK or FAIL; 原图小于略缩图尺寸, 直接用原图, 返回IGNORE
     */
    public ImageResult genThumbnail(String filePath, OutputStream os) {
        String thumbnailFileName = fetchFileNamePrefix(filePath) + "-sm.png";
        try (Closer closer = Closer.create()) {
            FileInputStream fis = closer.register(new FileInputStream(defaultRootPath + filePath));
            BufferedImage bi = ImageIO.read(fis);
            int h = bi.getHeight();
            int w = bi.getWidth();

            boolean flag = false;
            double ratio = 1.0;
            if (h >= w) {
                if (w > thumbnailImageSize) {
                    ratio = thumbnailImageSize / w;
                    flag = true;
                }
            } else {
                if (h > thumbnailImageSize) {
                    ratio = thumbnailImageSize / h;
                    flag = true;
                }
            }

            if (flag) {
                Thumbnails.Builder<BufferedImage> builder = Thumbnails.of(bi).outputQuality(1.0).scale(ratio).outputFormat("png");
                if (os != null) {
                    builder.toOutputStream(os);
                }

                FileOutputStream fos = new FileOutputStream(thumbnailPath + thumbnailFileName);
                builder.toOutputStream(fos);
                return ImageResult.OK;
            }

            return ImageResult.IGNORE;
        } catch (IOException e) {
            return ImageResult.FAIL;
        }
        // do nothing
    }

    /**
     * 生成缩略图, 并直接返回缩略图的输出流
     *
     * @param filePath 相对路径
     */
    public ImageResult genThumbnail(String filePath) {
        return genThumbnail(filePath, null);
    }

    private String fetchFileNamePrefix(String filePath) {
        int idx;
        String prefix = filePath;
        idx = filePath.lastIndexOf('.');
        if (idx != -1) {
            prefix = filePath.substring(0, idx).replace('/', '_');
        }

        return prefix;
    }

    private boolean fetchFile(String filePath, OutputStream os) {
        try (Closer closer = Closer.create()) {

            RandomAccessFile raf = closer.register(new RandomAccessFile(filePath, "r"));
            MappedByteBuffer mbb = raf.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, raf.length());

            byte[] buff = new byte[(int) raf.length()];
            mbb.get(buff);
            os.write(buff);
            os.flush();

            return true;
        } catch (IOException e) {
            return false;
        }
        // do nothing
    }

}
