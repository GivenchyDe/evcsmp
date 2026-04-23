package org.example.ev_charge_server.service;

import org.example.ev_charge_server.comm.Result;
import org.example.ev_charge_server.entity.RechargeOrder;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author Given
* @description 针对表【recharge_order(充值订单表)】的数据库操作Service
* @createDate 2026-04-22 14:37:28
*/
public interface RechargeOrderService extends IService<RechargeOrder> {

    /**
     * 根据用户ID查询充值订单
     * @param userId 用户ID
     * @return 充值订单列表
     */
    List<RechargeOrder> listByUser(Integer userId);

    /**
     * 根据状态查询充值订单
     * @param status 状态
     * @return 充值订单列表
     */
    List<RechargeOrder> listByStatus(Integer status);

    /**
     * 创建充值订单
     * @param order 充值订单信息
     * @return 创建结果
     */
    Result<RechargeOrder> createOrder(RechargeOrder order);

    /**
     * 充值支付
     * @param id 充值订单ID
     * @param payType 支付方式
     * @return 支付结果
     */
    Result<RechargeOrder> payOrder(Integer id, Integer payType);

    /**
     * 取消充值订单
     * @param id 充值订单ID
     * @return 取消结果
     */
    Result<RechargeOrder> cancelOrder(Integer id);

}
