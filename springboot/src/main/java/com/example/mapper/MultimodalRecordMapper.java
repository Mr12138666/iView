package com.example.mapper;

import com.example.entity.MultimodalRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MultimodalRecordMapper {

    int insert(MultimodalRecord record);

    MultimodalRecord selectById(@Param("id") Long id);

    List<MultimodalRecord> selectBySessionId(@Param("sessionId") String sessionId,
                                             @Param("userId") Integer userId);
}
