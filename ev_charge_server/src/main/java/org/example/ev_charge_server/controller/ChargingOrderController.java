package org.example.ev_charge_server.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.ev_charge_server.comm.Result;
import org.example.ev_charge_server.entity.ChargingOrder;
import org.example.ev_charge_server.service.ChargingOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 充电订单管理接口
 */
@Tag(name = "充电订单管理", description = "充电订单的查询、创建、结算、退款等接口")
@RestController
@RequestMapping("/chargingOrder")
public class ChargingOrderController {

    @Autowired
    private ChargingOrderService chargingOrderService;

    @Operation(summary = "查询所有充电订单", description = "分页获取所有充电订单列表")
    @Parameter(name = "page", description = "页码，从1开始", required = true)
    @Parameter(name = "limit", description = "每页数量", required = true)
    @GetMapping("/list")
    public Result<List<ChargingOrder>> list(Integer page, Integer limit) {
        Page<ChargingOrder> pageObj = new Page<>(page, limit);
        return Result.ok(chargingOrderService.list(pageObj));
    }

    @Operation(summary = "根据ID查询订单", description = "根据订单ID获取订单详细信息")
    @Parameter(name = "id", description = "订单ID", required = true)
    @GetMapping("/{id}")
    public Result<ChargingOrder> getById(@PathVariable Integer id) {
        ChargingOrder order = chargingOrderService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        return Result.ok(order);
    }

    @Operation(summary = "根据用户查询订单", description = "根据用户ID查询该用户的所有充电订单")
    @Parameter(name = "userId", description = "用户ID", required = true)
    @GetMapping("/listByUser")
    public Result<List<ChargingOrder>> listByUser(Integer userId) {
        return Result.ok(chargingOrderService.listByUser(userId));
    }

    @Operation(summary = "根据状态查询订单", description = "根据订单状态查询，status: 0=充电中 1=待结算 2=已完成 3=已取消 4=退款中 5=已退款")
    @Parameter(name = "status", description = "订单状态", required = true)
    @GetMapping("/listByStatus")
    public Result<List<ChargingOrder>> listByStatus(Integer status) {
        return Result.ok(chargingOrderService.listByStatus(status));
    }

    @Operation(summary = "创建充电订单", description = "用户开始充电时创建订单")
    @PostMapping("/create")
    public Result<ChargingOrder> create(@RequestBody ChargingOrder order) {
        return chargingOrderService.createOrder(order);
    }

    @Operation(summary = "订单结算", description = "充电完成后结算订单")
    @Parameter(name = "id", description = "订单ID", required = true)
    @Parameter(name = "payType", description = "支付方式: 0=余额 1=模拟微信 2=模拟支付宝", required = true)
    @GetMapping("/pay")
    public Result<ChargingOrder> pay(Integer id, Integer payType) {
        return chargingOrderService.payOrder(id, payType);
    }

    @Operation(summary = "取消订单", description = "取消待结算或充电中的订单")
    @Parameter(name = "id", description = "订单ID", required = true)
    @GetMapping("/cancel")
    public Result<ChargingOrder> cancel(Integer id) {
        return chargingOrderService.cancelOrder(id);
    }

    @Operation(summary = "申请退款", description = "用户申请订单退款")
    @PostMapping("/refund")
    public Result<ChargingOrder> refund(@RequestBody ChargingOrder order) {
        return chargingOrderService.refundOrder(order);
    }

    @Operation(summary = "修改订单", description = "修改订单信息")
    @PostMapping("/update")
    public Result<ChargingOrder> update(@RequestBody ChargingOrder order) {
        if (order.getId() == null) {
            return Result.error("订单ID不能为空");
        }
        boolean updated = chargingOrderService.updateById(order);
        if (!updated) {
            return Result.error("修改失败");
        }
        return Result.ok(order).setMessage("修改成功");
    }

    @Operation(summary = "删除订单", description = "根据订单ID删除订单")
    @Parameter(name = "id", description = "订单ID", required = true)
    @GetMapping("/delete")
    public Result<String> delete(Integer id) {
        boolean removed = chargingOrderService.removeById(id);
        if (!removed) {
            return Result.error("删除失败");
        }
        return Result.ok("删除成功");
    }
}
