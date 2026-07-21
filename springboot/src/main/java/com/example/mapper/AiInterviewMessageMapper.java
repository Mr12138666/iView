package com.example.mapper;

import com.example.entity.AiInterviewMessage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AiInterviewMessageMapper {

    int insert(AiInterviewMessage message);

    List<AiInterviewMessage> selectBySessionId(@Param("sessionId") String sessionId);

    Integer selectMaxOrder(@Param("sessionId") String sessionId);
}
