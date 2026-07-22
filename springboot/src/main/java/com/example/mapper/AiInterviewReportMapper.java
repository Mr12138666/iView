package com.example.mapper;

import com.example.entity.AiInterviewReport;
import org.apache.ibatis.annotations.Param;

public interface AiInterviewReportMapper {

    int insert(AiInterviewReport report);

    AiInterviewReport selectBySessionId(@Param("sessionId") String sessionId);
}
