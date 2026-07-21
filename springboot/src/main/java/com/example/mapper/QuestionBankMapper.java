package com.example.mapper;

import com.example.entity.QuestionBank;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface QuestionBankMapper {

    int insert(QuestionBank questionBank);

    void updateById(QuestionBank questionBank);

    void deleteById(Integer id);

    @Select("select * from question_bank where id = #{id}")
    QuestionBank selectById(Integer id);

    List<QuestionBank> selectAll(QuestionBank questionBank);
}
