package com.example.mapper;

import com.example.entity.ScoringRule;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ScoringRuleMapper {

    int insert(ScoringRule scoringRule);

    void updateById(ScoringRule scoringRule);

    void deleteById(Integer id);

    @Select("select * from scoring_rule where id = #{id}")
    ScoringRule selectById(Integer id);

    List<ScoringRule> selectAll(ScoringRule scoringRule);
}
