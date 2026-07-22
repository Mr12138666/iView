package com.example.mapper;

import com.example.entity.QuestionBankQuestion;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface QuestionBankQuestionMapper {

    int insert(QuestionBankQuestion relation);

    void updateById(QuestionBankQuestion relation);

    void deleteById(Integer id);

    QuestionBankQuestion selectById(Integer id);

    QuestionBankQuestion selectOne(@Param("questionBankId") Integer questionBankId,
                                   @Param("questionId") Integer questionId);

    List<QuestionBankQuestion> selectAll(QuestionBankQuestion relation);
}
