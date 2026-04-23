package org.example.ev_charge_server.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.ev_charge_server.comm.Result;
import org.example.ev_charge_server.entity.OrderReview;
import org.example.ev_charge_server.service.OrderReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单评价管理接口
 */
@Tag(name = "订单评价管理", description = "充电订单评价的查询、提交、删除等接口")
@RestController
@RequestMapping("/orderReview")
public class OrderReviewController {

    @Autowired
    private OrderReviewService orderReviewService;

    @Operation(summary = "查询所有评价", description = "分页获取所有订单评价列表")
    @Parameter(name = "page", description = "页码，从1开始", required = true)
    @Parameter(name = "limit", description = "每页数量", required = true)
    @GetMapping("/list")
    public Result<List<OrderReview>> list(Integer page, Integer limit) {
        Page<OrderReview> pageObj = new Page<>(page, limit);
        return Result.ok(orderReviewService.list(pageObj));
    }

    @Operation(summary = "根据ID查询评价", description = "根据评价ID获取详细信息")
    @Parameter(name = "id", description = "评价ID", required = true)
    @GetMapping("/{id}")
    public Result<OrderReview> getById(@PathVariable Integer id) {
        OrderReview review = orderReviewService.getById(id);
        if (review == null) {
            return Result.error("评价不存在");
        }
        return Result.ok(review);
    }

    @Operation(summary = "根据网点查询评价", description = "根据网点ID查询该网点的所有评价")
    @Parameter(name = "stationId", description = "网点ID", required = true)
    @GetMapping("/listByStation")
    public Result<List<OrderReview>> listByStation(Integer stationId) {
        return Result.ok(orderReviewService.listByStation(stationId));
    }

    @Operation(summary = "根据用户查询评价", description = "根据用户ID查询该用户的所有评价")
    @Parameter(name = "userId", description = "用户ID", required = true)
    @GetMapping("/listByUser")
    public Result<List<OrderReview>> listByUser(Integer userId) {
        return Result.ok(orderReviewService.listByUser(userId));
    }

    @Operation(summary = "提交评价", description = "用户对已完成订单提交评价")
    @PostMapping("/add")
    public Result<OrderReview> add(@RequestBody OrderReview review) {
        return orderReviewService.addReview(review);
    }

    @Operation(summary = "修改评价", description = "修改评价信息")
    @PostMapping("/update")
    public Result<OrderReview> update(@RequestBody OrderReview review) {
        if (review.getId() == null) {
            return Result.error("评价ID不能为空");
        }
        boolean updated = orderReviewService.updateById(review);
        if (!updated) {
            return Result.error("修改失败");
        }
        return Result.ok(review).setMessage("修改成功");
    }

    @Operation(summary = "删除评价", description = "根据评价ID删除评价（软删除）")
    @Parameter(name = "id", description = "评价ID", required = true)
    @GetMapping("/delete")
    public Result<String> delete(Integer id) {
        OrderReview review = new OrderReview();
        review.setId(id);
        review.setIsDeleted(1);
        boolean updated = orderReviewService.updateById(review);
        if (!updated) {
            return Result.error("删除失败");
        }
        return Result.ok("删除成功");
    }
}
