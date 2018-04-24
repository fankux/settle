package com.fl.dao;

import com.fl.entity.FileItem;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface FileDao {
    int selectDirItemCount(@Param("path") String path);

    Set<FileItem> selectDirItems(@Param("path") String path, @Param("start") Integer start,
                                 @Param("count") Integer count);

    Set<FileItem> selectDirs(@Param("path") String path);

    int insert(FileItem item);

    int insertBatch(Set<FileItem> items);

    int delete(FileItem item);

    int deleteByIds(Set<Integer> ids);

    int deleteByPath(@Param("path") String path);

    int update(@Param("old") FileItem oldItem, @Param("new") FileItem newItem);

    int updateById(@Param("id") Integer id, @Param("item") FileItem item);
}
