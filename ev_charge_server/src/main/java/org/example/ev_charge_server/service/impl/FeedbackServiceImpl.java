package org.example.ev_charge_server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.ev_charge_server.comm.Result;
import org.example.ev_charge_server.entity.Feedback;
import org.example.ev_charge_server.service.FeedbackService;
import org.example.ev_charge_server.mapper.FeedbackMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Given
* @description 针对表【feedback(故障反馈表)】的数据库操作Service实现
* @createDate 2026-04-22 14:37:27
*/
@Service
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback>
    implements FeedbackService{

    @Override
    public List<Feedback> listByUser(Integer userId) {
        QueryWrapper<Feedback> queryWrapper = Wrappers.query(Feedback.class);
        queryWrapper.eq("user_id", userId);
        return list(queryWrapper);
    }

    @Override
    public List<Feedback> listByStatus(Integer status) {
        QueryWrapper<Feedback> queryWrapper = Wrappers.query(Feedback.class);
        queryWrapper.eq("status", status);
        return list(queryWrapper);
    }

    @Override
    public Result<Feedback> addFeedback(Feedback feedback) {
        feedback.setStatus(0);
        feedback.setCreatedAt(new java.util.Date());
        boolean saved = save(feedback);
        if (!saved) {
            return Result.error("提交反馈失败");
        }
        return Result.ok(feedback).setMessage("反馈提交成功");
    }

    @Override
    public Result<Feedback> handleFeedback(Feedback feedback) {
        if (feedback.getId() == null) {
            return Result.error("反馈ID不能为空");
        }
        Feedback existingFeedback = getById(feedback.getId());
        if (existingFeedback == null) {
            return Result.error("反馈不存在");
        }
        existingFeedback.setStatus(feedback.getStatus());
        existingFeedback.setReply(feedback.getReply());
        existingFeedback.setHandlerId(feedback.getHandlerId());
        existingFeedback.setHandledAt(new java.util.Date());
        boolean updated = updateById(existingFeedback);
        if (!updated) {
            return Result.error("处理反馈失败");
        }
        return Result.ok(existingFeedback).setMessage("处理成功");
    }
}




