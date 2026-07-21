package com.example.mapper;

import com.example.entity.InterviewQuestion;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface InterviewQuestionMapper {

    int insert(InterviewQuestion question);

    void updateById(InterviewQuestion question);

    void deleteById(Integer id);

    @Select("select * from interview_question where id = #{id}")
    InterviewQuestion selectById(Integer id);

    List<InterviewQuestion> selectAll(InterviewQuestion question);
}
