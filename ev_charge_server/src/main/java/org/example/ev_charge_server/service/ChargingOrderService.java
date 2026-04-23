package org.example.ev_charge_server.service;

import org.example.ev_charge_server.comm.Result;
import org.example.ev_charge_server.entity.ChargingOrder;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author Given
* @description 针对表【charging_order(充电订单表)】的数据库操作Service
* @createDate 2026-04-22 14:37:28
*/
public interface ChargingOrderService extends IService<ChargingOrder> {

    /**
     * 根据用户ID查询订单
     * @param userId 用户ID
     * @return 订单列表
     */
    List<ChargingOrder> listByUser(Integer userId);

    /**
     * 根据状态查询订单
     * @param status 状态
     * @return 订单列表
     */
    List<ChargingOrder> listByStatus(Integer status);

    /**
     * 创建充电订单
     * @param order 订单信息
     * @return 创建结果
     */
    Result<ChargingOrder> createOrder(ChargingOrder order);

    /**
     * 订单结算
     * @param id 订单ID
     * @param payType 支付方式
     * @return 结算结果
     */
    Result<ChargingOrder> payOrder(Integer id, Integer payType);

    /**
     * 取消订单
     * @param id 订单ID
     * @return 取消结果
     */
    Result<ChargingOrder> cancelOrder(Integer id);

    /**
     * 申请退款
     * @param order 订单信息
     * @return 退款结果
     */
    Result<ChargingOrder> refundOrder(ChargingOrder order);

}
