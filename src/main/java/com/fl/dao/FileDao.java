package com.fl.dao;

import com.fl.entity.FileItem;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileDao {
    List<FileItem> selectDirItems(@Param("path") String path, @Param("start") Integer start,
                                  @Param("count") Integer count);

    int insert(FileItem item);

    int delete(FileItem item);

    int deleteByPath(@Param("path") String path);

    int update(@Param("old") FileItem oldItem, @Param("new") FileItem newItem);
}
