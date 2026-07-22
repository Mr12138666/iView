package com.example.service;

import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.entity.QuestionBankQuestion;
import com.example.exception.CustomException;
import com.example.mapper.QuestionBankQuestionMapper;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionBankQuestionService {

    @Resource
    private QuestionBankQuestionMapper relationMapper;

    public void add(QuestionBankQuestion relation) {
        requireAdmin();
        validate(relation);
        if (relationMapper.selectOne(relation.getQuestionBankId(), relation.getQuestionId()) != null) {
            throw new CustomException("400", "该题目已关联到当前题库");
        }
        if (relation.getSortOrder() == null) {
            relation.setSortOrder(0);
        }
        relationMapper.insert(relation);
    }

    public void updateById(QuestionBankQuestion relation) {
        requireAdmin();
        validate(relation);
        relationMapper.updateById(relation);
    }

    public void deleteById(Integer id) {
        requireAdmin();
        relationMapper.deleteById(id);
    }

    public void deleteBatch(List<Integer> ids) {
        requireAdmin();
        for (Integer id : ids) {
            relationMapper.deleteById(id);
        }
    }

    public QuestionBankQuestion selectById(Integer id) {
        return relationMapper.selectById(id);
    }

    public List<QuestionBankQuestion> selectAll(QuestionBankQuestion relation) {
        return relationMapper.selectAll(relation);
    }

    public PageInfo<QuestionBankQuestion> selectPage(QuestionBankQuestion relation, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return PageInfo.of(relationMapper.selectAll(relation));
    }

    private void validate(QuestionBankQuestion relation) {
        if (relation == null || relation.getQuestionBankId() == null || relation.getQuestionId() == null) {
            throw new CustomException("400", "题库和题目不能为空");
        }
    }

    private void requireAdmin() {
        Account currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null || !RoleEnum.ADMIN.name().equals(currentUser.getRole())) {
            throw new CustomException("403", "只有管理员可以维护题库题目关联");
        }
    }
}
