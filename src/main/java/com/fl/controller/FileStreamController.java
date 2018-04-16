package com.fl.controller;

import com.fl.service.ImageResult;
import com.fl.service.ImageService;
import com.fl.util.PathUtils;
import com.google.common.collect.Sets;
import com.google.common.io.Closer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Set;

@Controller
@RequestMapping("/")
public class FileStreamController {
    private static Logger logger = LoggerFactory.getLogger(FileStreamController.class);

    @Resource
    private ImageService imageService;

    private static Set<String> ALLOWED_IMAGE_TYPES = Sets.newHashSet();

    static {
        ALLOWED_IMAGE_TYPES.add(MediaType.IMAGE_JPEG_VALUE);
        ALLOWED_IMAGE_TYPES.add(MediaType.IMAGE_PNG_VALUE);
        ALLOWED_IMAGE_TYPES.add(MediaType.IMAGE_GIF_VALUE);
    }

    @RequestMapping(value = "img/**", method = RequestMethod.GET)
    void imageOutStream(HttpServletRequest request, HttpServletResponse response,
                        @RequestParam(value = "raw", required = false) Integer raw) {
        Closer closer = Closer.create();
        try {
            String filePath = request.getRequestURI().replace("/img", "");
            filePath = URLDecoder.decode(filePath, Charset.forName("UTF8").name());
            filePath = PathUtils.clearPrefixSlash(filePath);
            ServletOutputStream os = closer.register(response.getOutputStream());

            /*
             * 如果是原图请求, 直接获取原图
             * 然后, 尝试获得缩略图, 存在则返回缩略图
             * 然后, 尝试生成缩略图, 并返回缩略图
             * 最后, 返回原图
             */
            boolean done = false;
            int httpRet = HttpServletResponse.SC_OK;
            if (raw != null && raw == 1) {
                if (!imageService.fetchImage(filePath, os)) {
                    throw new IOException("获取原图失败");
                }
                done = true;
            }

            if (!done) {
                done = imageService.fetchThumbnail(filePath, os);
            }

            if (!done) {
                ImageResult imgRet = imageService.genThumbnail(filePath, os);
                if (imgRet == ImageResult.FAIL) {
                    throw new IOException("生成缩略图失败");
                }
                done = imgRet != ImageResult.IGNORE;
            }

            if (!done) {
                if (!imageService.fetchImage(filePath, os)) {
                    throw new IOException("打开原图失败");
                }
            }
            response.flushBuffer();
            response.setStatus(httpRet);

        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            try {
                closer.close();
            } catch (IOException e) {
                // do nothing
            }
        }
    }
}
