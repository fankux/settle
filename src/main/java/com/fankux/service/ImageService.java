package com.fankux.service;

import com.fankux.util.PathUtils;
import com.google.common.io.Closer;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

@Service
public class ImageService {

    @Value("${settle.conf.thumbnailPath}")
    private String thumbnailPath;

    @Value("${settle.conf.defaultRootPath}")
    private String defaultRootPath;

    @Value("${settle.conf.thumbnailImageSize}")
    private Double thumbnailImageSize;

    @PostConstruct
    private void init() {
        defaultRootPath = PathUtils.padSuffixSlash(defaultRootPath);
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
        Closer closer = Closer.create();
        try {

            RandomAccessFile raf = closer.register(new RandomAccessFile(filePath, "r"));
            MappedByteBuffer mbb = raf.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, raf.length());

            byte[] buff = new byte[(int) raf.length()];
            mbb.get(buff);
            os.write(buff);
            os.flush();

            return true;
        } catch (IOException e) {
            return false;
        } finally {
            try {
                closer.close();
            } catch (IOException e) {
                // do nothing
            }
        }
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
        String thumbnailFileName = fetchFileNamePrefix(filePath) + "-sm.png";
        thumbnailPath = PathUtils.padSuffixSlash(thumbnailPath);
        return fetchFile(thumbnailPath + thumbnailFileName, os);
    }

    /**
     * 生成缩略图, 并直接返回缩略图的输出流
     *
     * @param filePath 相对路径
     * @param os       输出流
     */
    public boolean genThumbnail(String filePath, OutputStream os) {
        String thumbnailFileName = fetchFileNamePrefix(filePath) + "-sm.png";
        Closer closer = Closer.create();
        try {
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
                builder.toOutputStream(os);

                thumbnailPath = PathUtils.padSuffixSlash(thumbnailPath);
                FileOutputStream fos = new FileOutputStream(thumbnailPath + thumbnailFileName);
                builder.toOutputStream(fos);
            }
            // 原图小于略缩图尺寸, 直接用原图
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            try {
                closer.close();
            } catch (IOException e) {
                // do nothing
            }
        }
    }
}
