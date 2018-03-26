package com.fankux.controller;

import com.fankux.service.ImageService;
import com.google.common.collect.Sets;
import com.google.common.io.Closer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
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
    @Value("${conf.defaultRootPath}")
    String defaultRootPath;
    @Value("${conf.thumbnailPath}")
    private String thumbnail_path;

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
            filePath = StringUtils.trimLeadingCharacter(filePath, '/');
            // 日你妈哦URI我怎么去拿绝对路径的值，你瞎几把写就别怪我蛇皮走位了！
            String rootPath = defaultRootPath.substring(0, defaultRootPath.lastIndexOf(":") + 2);
            ServletOutputStream os = closer.register(response.getOutputStream());
            if (raw != null && raw == 1) {
//                logger.info("{} raw fetch", filePath);
                imageService.fetchImage(rootPath + filePath, os);
            } else if (imageService.fetchThumbnail(filePath, os)) {
//                logger.info("{} thumbnail fetch", filePath);
            } else if (imageService.genThumbnail(filePath, os)) {
//                logger.info("{} thumbnail generate", filePath);
            } else {
                imageService.fetchImage(rootPath + filePath, os);
            }
            response.flushBuffer();
            response.setStatus(HttpServletResponse.SC_OK);

        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } finally {
            try {
                closer.close();
            } catch (IOException e) {
                // do nothing
            }
        }
    }
}
