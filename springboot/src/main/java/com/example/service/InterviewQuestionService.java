package com.example.service;

import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.entity.InterviewQuestion;
import com.example.exception.CustomException;
import com.example.mapper.InterviewQuestionMapper;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InterviewQuestionService {

    @Resource
    private InterviewQuestionMapper questionMapper;

    public void add(InterviewQuestion question) {
        requireAdmin();
        validate(question);
        if (question.getEnabled() == null) {
            question.setEnabled(1);
        }
        questionMapper.insert(question);
    }

    public void updateById(InterviewQuestion question) {
        requireAdmin();
        validate(question);
        questionMapper.updateById(question);
    }

    public void deleteById(Integer id) {
        requireAdmin();
        questionMapper.deleteById(id);
    }

    public void deleteBatch(List<Integer> ids) {
        requireAdmin();
        for (Integer id : ids) {
            questionMapper.deleteById(id);
        }
    }

    public InterviewQuestion selectById(Integer id) {
        return questionMapper.selectById(id);
    }

    public List<InterviewQuestion> selectAll(InterviewQuestion question) {
        return questionMapper.selectAll(question);
    }

    public PageInfo<InterviewQuestion> selectPage(InterviewQuestion question, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return PageInfo.of(questionMapper.selectAll(question));
    }

    private void validate(InterviewQuestion question) {
        if (question == null || blank(question.getTitle()) || blank(question.getContent())
                || blank(question.getInterviewType()) || blank(question.getDifficulty())) {
            throw new CustomException("400", "题目标题、内容、面试类型和难度不能为空");
        }
        question.setInterviewType(question.getInterviewType().trim().toUpperCase());
        question.setDifficulty(question.getDifficulty().trim().toUpperCase());
    }

    private boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void requireAdmin() {
        Account currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null || !RoleEnum.ADMIN.name().equals(currentUser.getRole())) {
            throw new CustomException("403", "只有管理员可以维护题目");
        }
    }
}
