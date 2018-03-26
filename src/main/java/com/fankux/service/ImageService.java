package com.fankux.service;

import com.google.common.io.Closer;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

@Service
public class ImageService {
    private static double SM_SIZE = 400.0;
    private static String THUMBNAIL_PATH = "E:/thumbnail/";

    String fetchFileNamePrefix(String filePath) {
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

    public void fetchImage(String filePath, OutputStream os) {
        fetchFile(filePath, os);
    }

    public boolean fetchThumbnail(String filePath, OutputStream os) {
        String thumbnailFileName = fetchFileNamePrefix(filePath) + "-sm.png";
        return fetchFile(THUMBNAIL_PATH + thumbnailFileName, os);
    }

    public boolean genThumbnail(String filePath, OutputStream os) {
        String thumbnailFileName = fetchFileNamePrefix(filePath) + "-sm.png";

        Closer closer = Closer.create();
        try {
            FileInputStream fis = closer.register(new FileInputStream("E://" + filePath));
            BufferedImage bi = ImageIO.read(fis);
            int h = bi.getHeight();
            int w = bi.getWidth();

            boolean flag = false;
            double ratio = 1.0;
            if (h >= w) {
                if (w > SM_SIZE) {
                    ratio = SM_SIZE / w;
                    flag = true;
                }
            } else {
                if (h > SM_SIZE) {
                    ratio = SM_SIZE / h;
                    flag = true;
                }
            }

            if (flag) {
                Thumbnails.Builder<BufferedImage> builder = Thumbnails.of(bi).outputQuality(1.0).scale(ratio).outputFormat("png");
                builder.toOutputStream(os);

                FileOutputStream fos = new FileOutputStream(THUMBNAIL_PATH + thumbnailFileName);
                builder.toOutputStream(fos);
            }

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
