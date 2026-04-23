package org.example.ev_charge_server.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.ev_charge_server.comm.Result;
import org.example.ev_charge_server.entity.RechargeOrder;
import org.example.ev_charge_server.service.RechargeOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 充值订单管理接口
 */
@Tag(name = "充值订单管理", description = "充值订单的查询、创建、支付等接口")
@RestController
@RequestMapping("/rechargeOrder")
public class RechargeOrderController {

    @Autowired
    private RechargeOrderService rechargeOrderService;

    @Operation(summary = "查询所有充値订单", description = "分页获取所有充値订单列表")
    @Parameter(name = "page", description = "页码，从1开始", required = true)
    @Parameter(name = "limit", description = "每页数量", required = true)
    @GetMapping("/list")
    public Result<List<RechargeOrder>> list(Integer page, Integer limit) {
        Page<RechargeOrder> pageObj = new Page<>(page, limit);
        return Result.ok(rechargeOrderService.list(pageObj));
    }

    @Operation(summary = "根据ID查询充值订单", description = "根据充值订单ID获取详细信息")
    @Parameter(name = "id", description = "充值订单ID", required = true)
    @GetMapping("/{id}")
    public Result<RechargeOrder> getById(@PathVariable Integer id) {
        RechargeOrder order = rechargeOrderService.getById(id);
        if (order == null) {
            return Result.error("充值订单不存在");
        }
        return Result.ok(order);
    }

    @Operation(summary = "根据用户查询充值订单", description = "根据用户ID查询该用户的所有充值订单")
    @Parameter(name = "userId", description = "用户ID", required = true)
    @GetMapping("/listByUser")
    public Result<List<RechargeOrder>> listByUser(Integer userId) {
        return Result.ok(rechargeOrderService.listByUser(userId));
    }

    @Operation(summary = "根据状态查询充值订单", description = "根据状态查询，status: 0=待支付 1=已完成 2=已取消")
    @Parameter(name = "status", description = "订单状态", required = true)
    @GetMapping("/listByStatus")
    public Result<List<RechargeOrder>> listByStatus(Integer status) {
        return Result.ok(rechargeOrderService.listByStatus(status));
    }

    @Operation(summary = "创建充值订单", description = "用户发起充值时创建订单")
    @PostMapping("/create")
    public Result<RechargeOrder> create(@RequestBody RechargeOrder order) {
        return rechargeOrderService.createOrder(order);
    }

    @Operation(summary = "充值支付", description = "模拟支付充值订单")
    @Parameter(name = "id", description = "充值订单ID", required = true)
    @Parameter(name = "payType", description = "支付方式: 1=模拟微信 2=模拟支付宝", required = true)
    @GetMapping("/pay")
    public Result<RechargeOrder> pay(Integer id, Integer payType) {
        return rechargeOrderService.payOrder(id, payType);
    }

    @Operation(summary = "取消充值订单", description = "取消待支付的充值订单")
    @Parameter(name = "id", description = "充值订单ID", required = true)
    @GetMapping("/cancel")
    public Result<RechargeOrder> cancel(Integer id) {
        return rechargeOrderService.cancelOrder(id);
    }

    @Operation(summary = "删除充值订单", description = "根据充值订单ID删除")
    @Parameter(name = "id", description = "充值订单ID", required = true)
    @GetMapping("/delete")
    public Result<String> delete(Integer id) {
        boolean removed = rechargeOrderService.removeById(id);
        if (!removed) {
            return Result.error("删除失败");
        }
        return Result.ok("删除成功");
    }
}
