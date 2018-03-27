package com.fankux.controller;

import com.fankux.model.FileItem;
import com.fankux.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("api")
public class FileApiController {
    private static Logger logger = LoggerFactory.getLogger(FileStreamController.class);

    @Resource
    FileService fileService;

    @RequestMapping("files")
    List<FileItem> fileList(@RequestParam("path") String path, @RequestParam("s") Integer start,
                            @RequestParam("c") Integer count) {
        return fileService.fileList(path, start, count);
    }
}
