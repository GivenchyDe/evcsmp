package org.example.ev_charge_server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.ev_charge_server.comm.Result;
import org.example.ev_charge_server.entity.OrderReview;
import org.example.ev_charge_server.service.OrderReviewService;
import org.example.ev_charge_server.mapper.OrderReviewMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Given
* @description 针对表【order_review(订单评价表)】的数据库操作Service实现
* @createDate 2026-04-22 14:37:28
*/
@Service
public class OrderReviewServiceImpl extends ServiceImpl<OrderReviewMapper, OrderReview>
    implements OrderReviewService{

    @Override
    public List<OrderReview> listByStation(Integer stationId) {
        QueryWrapper<OrderReview> queryWrapper = Wrappers.query(OrderReview.class);
        queryWrapper.eq("station_id", stationId).eq("is_deleted", 0);
        return list(queryWrapper);
    }

    @Override
    public List<OrderReview> listByUser(Integer userId) {
        QueryWrapper<OrderReview> queryWrapper = Wrappers.query(OrderReview.class);
        queryWrapper.eq("user_id", userId).eq("is_deleted", 0);
        return list(queryWrapper);
    }

    @Override
    public Result<OrderReview> addReview(OrderReview review) {
        review.setIsDeleted(0);
        review.setCreatedAt(new java.util.Date());
        boolean saved = save(review);
        if (!saved) {
            return Result.error("提交评价失败");
        }
        return Result.ok(review).setMessage("评价提交成功");
    }
}




