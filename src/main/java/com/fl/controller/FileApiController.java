package com.fl.controller;

import com.fl.model.FileResponse;
import com.fl.service.FileService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("api")
public class FileApiController {
    @Resource
    FileService fileService;

    @RequestMapping("files")
    List<FileResponse> fileList(@RequestParam("path") String path, @RequestParam("s") Integer start,
                                @RequestParam("c") Integer count) {
        return fileService.fileList(path, start, count);
    }

    @RequestMapping("dirs")
    List<FileResponse> dirList(@RequestParam("path") String path) {
        return fileService.dirList(path);
    }

    Boolean fileRename(@RequestParam("path") String path, @RequestParam("name") String name,
                       @RequestParam("newName") String newName) {
        return fileService.fileRename(path, name, newName);
    }
}
