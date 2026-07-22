package com.example.service;

import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.entity.QuestionBank;
import com.example.exception.CustomException;
import com.example.mapper.QuestionBankMapper;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionBankService {

    @Resource
    private QuestionBankMapper questionBankMapper;

    public void add(QuestionBank questionBank) {
        requireAdmin();
        validate(questionBank);
        if (questionBank.getEnabled() == null) {
            questionBank.setEnabled(1);
        }
        questionBankMapper.insert(questionBank);
    }

    public void updateById(QuestionBank questionBank) {
        requireAdmin();
        validate(questionBank);
        questionBankMapper.updateById(questionBank);
    }

    public void deleteById(Integer id) {
        requireAdmin();
        questionBankMapper.deleteById(id);
    }

    public void deleteBatch(List<Integer> ids) {
        requireAdmin();
        for (Integer id : ids) {
            questionBankMapper.deleteById(id);
        }
    }

    public QuestionBank selectById(Integer id) {
        return questionBankMapper.selectById(id);
    }

    public List<QuestionBank> selectAll(QuestionBank questionBank) {
        return questionBankMapper.selectAll(questionBank);
    }

    public PageInfo<QuestionBank> selectPage(QuestionBank questionBank, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return PageInfo.of(questionBankMapper.selectAll(questionBank));
    }

    private void validate(QuestionBank questionBank) {
        if (questionBank == null || blank(questionBank.getTitle()) || blank(questionBank.getInterviewType())) {
            throw new CustomException("400", "题库名称和面试类型不能为空");
        }
        questionBank.setInterviewType(questionBank.getInterviewType().trim().toUpperCase());
        if (!blank(questionBank.getDifficulty())) {
            questionBank.setDifficulty(questionBank.getDifficulty().trim().toUpperCase());
        }
    }

    private boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void requireAdmin() {
        Account currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null || !RoleEnum.ADMIN.name().equals(currentUser.getRole())) {
            throw new CustomException("403", "只有管理员可以维护题库");
        }
    }
}
