package com.example.service;

import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.entity.ScoringRule;
import com.example.exception.CustomException;
import com.example.mapper.ScoringRuleMapper;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ScoringRuleService {

    @Resource
    private ScoringRuleMapper scoringRuleMapper;

    public void add(ScoringRule scoringRule) {
        requireAdmin();
        validate(scoringRule);
        if (scoringRule.getEnabled() == null) {
            scoringRule.setEnabled(1);
        }
        scoringRuleMapper.insert(scoringRule);
    }

    public void updateById(ScoringRule scoringRule) {
        requireAdmin();
        validate(scoringRule);
        scoringRuleMapper.updateById(scoringRule);
    }

    public void deleteById(Integer id) {
        requireAdmin();
        scoringRuleMapper.deleteById(id);
    }

    public void deleteBatch(List<Integer> ids) {
        requireAdmin();
        for (Integer id : ids) {
            scoringRuleMapper.deleteById(id);
        }
    }

    public ScoringRule selectById(Integer id) {
        return scoringRuleMapper.selectById(id);
    }

    public List<ScoringRule> selectAll(ScoringRule scoringRule) {
        return scoringRuleMapper.selectAll(scoringRule);
    }

    public PageInfo<ScoringRule> selectPage(ScoringRule scoringRule, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return PageInfo.of(scoringRuleMapper.selectAll(scoringRule));
    }

    private void validate(ScoringRule scoringRule) {
        if (scoringRule == null || blank(scoringRule.getInterviewType())
                || blank(scoringRule.getDimension()) || scoringRule.getWeight() == null) {
            throw new CustomException("400", "面试类型、评分维度和权重不能为空");
        }
        if (scoringRule.getWeight().compareTo(BigDecimal.ZERO) < 0
                || scoringRule.getWeight().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new CustomException("400", "评分权重必须在0到100之间");
        }
        scoringRule.setInterviewType(scoringRule.getInterviewType().trim().toUpperCase());
    }

    private boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void requireAdmin() {
        Account currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null || !RoleEnum.ADMIN.name().equals(currentUser.getRole())) {
            throw new CustomException("403", "只有管理员可以维护评分规则");
        }
    }
}
