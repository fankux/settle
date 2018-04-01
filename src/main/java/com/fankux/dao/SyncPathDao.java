package com.fankux.dao;

import com.fankux.entity.PathItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SyncPathDao {
    List<PathItem> selectAll();

    void insert(PathItem pathItem);

    void insertBatch(List<PathItem> pathItems);

    void delete(@Param("path") String path);

    void deleteAll();

    String getSyncFlag();

    void setSyncFlag(@Param("value") String value);
}
