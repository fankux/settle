package com.fl.controller;

import com.fl.model.FileRequest;
import com.fl.model.FileResponse;
import com.fl.service.FileService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

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
    List<FileResponse> dirList(@RequestBody FileRequest file) {
        return fileService.dirList(file);
    }

    Boolean fileRename(@RequestBody FileRequest file, @RequestBody FileRequest newFile) {
        return fileService.rename(file, newFile);
    }

    @RequestMapping("file/delete")
    Boolean fileDelete(@RequestBody FileRequest file) {
        return fileService.delete(file);
    }

    @RequestMapping("files/delete")
    Boolean fileDeleteBatch(@RequestBody Set<FileRequest> files) {
        return fileService.deleteBatch(files);
    }

    @RequestMapping("file/move")
    Boolean fileMove(@RequestBody FileRequest file, @RequestBody FileRequest newFile) {
        return fileService.move(file, newFile);
    }

    @RequestMapping("file/upload")
    Boolean fileUpload(@RequestParam("path") String path, @RequestParam("name") String name,
                       @RequestParam("file") CommonsMultipartFile file) {
        return fileService.upload(path, name, file);
    }
}
