package org.example.ev_charge_server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.ev_charge_server.comm.Result;
import org.example.ev_charge_server.entity.RechargeOrder;
import org.example.ev_charge_server.service.RechargeOrderService;
import org.example.ev_charge_server.mapper.RechargeOrderMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Given
* @description 针对表【recharge_order(充值订单表)】的数据库操作Service实现
* @createDate 2026-04-22 14:37:28
*/
@Service
public class RechargeOrderServiceImpl extends ServiceImpl<RechargeOrderMapper, RechargeOrder>
    implements RechargeOrderService{

    @Override
    public List<RechargeOrder> listByUser(Integer userId) {
        QueryWrapper<RechargeOrder> queryWrapper = Wrappers.query(RechargeOrder.class);
        queryWrapper.eq("user_id", userId);
        return list(queryWrapper);
    }

    @Override
    public List<RechargeOrder> listByStatus(Integer status) {
        QueryWrapper<RechargeOrder> queryWrapper = Wrappers.query(RechargeOrder.class);
        queryWrapper.eq("status", status);
        return list(queryWrapper);
    }

    @Override
    public Result<RechargeOrder> createOrder(RechargeOrder order) {
        order.setStatus(0);
        order.setCreatedAt(new java.util.Date());
        boolean saved = save(order);
        if (!saved) {
            return Result.error("创建订单失败");
        }
        return Result.ok(order).setMessage("充值订单创建成功");
    }

    @Override
    public Result<RechargeOrder> payOrder(Integer id, Integer payType) {
        RechargeOrder order = getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (order.getStatus() != 0) {
            return Result.error("订单状态不正确");
        }
        order.setStatus(1);
        order.setPayType(payType);
        order.setPaidAt(new java.util.Date());
        boolean updated = updateById(order);
        if (!updated) {
            return Result.error("支付失败");
        }
        return Result.ok(order).setMessage("支付成功");
    }

    @Override
    public Result<RechargeOrder> cancelOrder(Integer id) {
        RechargeOrder order = getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (order.getStatus() != 0) {
            return Result.error("只有待支付订单可取消");
        }
        order.setStatus(2);
        boolean updated = updateById(order);
        if (!updated) {
            return Result.error("取消失败");
        }
        return Result.ok(order).setMessage("取消成功");
    }
}




