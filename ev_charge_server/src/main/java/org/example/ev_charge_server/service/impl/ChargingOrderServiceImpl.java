package org.example.ev_charge_server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.ev_charge_server.comm.Result;
import org.example.ev_charge_server.entity.ChargingOrder;
import org.example.ev_charge_server.service.ChargingOrderService;
import org.example.ev_charge_server.mapper.ChargingOrderMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Given
* @description 针对表【charging_order(充电订单表)】的数据库操作Service实现
* @createDate 2026-04-22 14:37:28
*/
@Service
public class ChargingOrderServiceImpl extends ServiceImpl<ChargingOrderMapper, ChargingOrder>
    implements ChargingOrderService{

    @Override
    public List<ChargingOrder> listByUser(Integer userId) {
        QueryWrapper<ChargingOrder> queryWrapper = Wrappers.query(ChargingOrder.class);
        queryWrapper.eq("user_id", userId);
        return list(queryWrapper);
    }

    @Override
    public List<ChargingOrder> listByStatus(Integer status) {
        QueryWrapper<ChargingOrder> queryWrapper = Wrappers.query(ChargingOrder.class);
        queryWrapper.eq("status", status);
        return list(queryWrapper);
    }

    @Override
    public Result<ChargingOrder> createOrder(ChargingOrder order) {
        order.setStatus(0);
        order.setCreatedAt(new java.util.Date());
        boolean saved = save(order);
        if (!saved) {
            return Result.error("创建订单失败");
        }
        return Result.ok(order).setMessage("订单创建成功");
    }

    @Override
    public Result<ChargingOrder> payOrder(Integer id, Integer payType) {
        ChargingOrder order = getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (order.getStatus() != 1) {
            return Result.error("订单状态不正确，无法结算");
        }
        order.setStatus(2);
        order.setPayType(payType);
        order.setPaidAt(new java.util.Date());
        boolean updated = updateById(order);
        if (!updated) {
            return Result.error("结算失败");
        }
        return Result.ok(order).setMessage("结算成功");
    }

    @Override
    public Result<ChargingOrder> cancelOrder(Integer id) {
        ChargingOrder order = getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (order.getStatus() != 0 && order.getStatus() != 1) {
            return Result.error("当前订单状态不可取消");
        }
        order.setStatus(3);
        boolean updated = updateById(order);
        if (!updated) {
            return Result.error("取消失败");
        }
        return Result.ok(order).setMessage("取消成功");
    }

    @Override
    public Result<ChargingOrder> refundOrder(ChargingOrder order) {
        if (order.getId() == null) {
            return Result.error("订单ID不能为空");
        }
        ChargingOrder existingOrder = getById(order.getId());
        if (existingOrder == null) {
            return Result.error("订单不存在");
        }
        if (existingOrder.getStatus() != 2) {
            return Result.error("只有已完成订单可申请退款");
        }
        existingOrder.setStatus(4);
        existingOrder.setRefundReason(order.getRefundReason());
        boolean updated = updateById(existingOrder);
        if (!updated) {
            return Result.error("申请退款失败");
        }
        return Result.ok(existingOrder).setMessage("退款申请已提交");
    }
}




