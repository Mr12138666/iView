package com.example.mapper;

import com.example.entity.AiInterviewSession;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AiInterviewSessionMapper {

    int insert(AiInterviewSession session);

    AiInterviewSession selectById(@Param("id") String id);

    List<AiInterviewSession> selectByUserId(@Param("userId") Integer userId, @Param("status") String status);

    int updateToRunning(@Param("id") String id, @Param("version") Integer version);

    int updateQuestionNo(@Param("id") String id, @Param("questionNo") Integer questionNo);

    int updateToEvaluating(@Param("id") String id, @Param("version") Integer version);

    int updateToRunningFromEvaluating(@Param("id") String id);

    int updateToFinished(@Param("id") String id);

    int updateToCancelled(@Param("id") String id, @Param("version") Integer version);
}
