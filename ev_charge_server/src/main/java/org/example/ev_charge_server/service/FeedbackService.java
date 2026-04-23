package org.example.ev_charge_server.service;

import org.example.ev_charge_server.comm.Result;
import org.example.ev_charge_server.entity.Feedback;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author Given
* @description 针对表【feedback(故障反馈表)】的数据库操作Service
* @createDate 2026-04-22 14:37:28
*/
public interface FeedbackService extends IService<Feedback> {

    /**
     * 根据用户ID查询反馈
     * @param userId 用户ID
     * @return 反馈列表
     */
    List<Feedback> listByUser(Integer userId);

    /**
     * 根据状态查询反馈
     * @param status 状态
     * @return 反馈列表
     */
    List<Feedback> listByStatus(Integer status);

    /**
     * 提交反馈
     * @param feedback 反馈信息
     * @return 提交结果
     */
    Result<Feedback> addFeedback(Feedback feedback);

    /**
     * 处理反馈
     * @param feedback 反馈信息
     * @return 处理结果
     */
    Result<Feedback> handleFeedback(Feedback feedback);

}
