package org.example.ev_charge_server.service;

import org.example.ev_charge_server.comm.Result;
import org.example.ev_charge_server.entity.OrderReview;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author Given
* @description 针对表【order_review(订单评价表)】的数据库操作Service
* @createDate 2026-04-22 14:37:28
*/
public interface OrderReviewService extends IService<OrderReview> {

    /**
     * 根据网点ID查询评价
     * @param stationId 网点ID
     * @return 评价列表
     */
    List<OrderReview> listByStation(Integer stationId);

    /**
     * 根据用户ID查询评价
     * @param userId 用户ID
     * @return 评价列表
     */
    List<OrderReview> listByUser(Integer userId);

    /**
     * 提交评价
     * @param review 评价信息
     * @return 提交结果
     */
    Result<OrderReview> addReview(OrderReview review);

}
