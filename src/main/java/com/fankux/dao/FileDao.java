package com.fankux.dao;

import com.fankux.model.FileItem;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileDao {
    List<FileItem> selectDirItems(@Param("path") String path, @Param("start") Integer start,
                                  @Param("count") Integer count);

    int insert(FileItem item);
}
