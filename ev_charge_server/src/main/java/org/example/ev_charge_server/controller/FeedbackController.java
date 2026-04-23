package org.example.ev_charge_server.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.ev_charge_server.comm.Result;
import org.example.ev_charge_server.entity.Feedback;
import org.example.ev_charge_server.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 故障反馈管理接口
 */
@Tag(name = "故障反馈管理", description = "用户故障反馈的提交、查询、处理等接口")
@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @Operation(summary = "查询所有反馈", description = "分页获取所有故障反馈列表")
    @Parameter(name = "page", description = "页码，从1开始", required = true)
    @Parameter(name = "limit", description = "每页数量", required = true)
    @GetMapping("/list")
    public Result<List<Feedback>> list(Integer page, Integer limit) {
        Page<Feedback> pageObj = new Page<>(page, limit);
        return Result.ok(feedbackService.list(pageObj));
    }

    @Operation(summary = "根据ID查询反馈", description = "根据反馈ID获取详细信息")
    @Parameter(name = "id", description = "反馈ID", required = true)
    @GetMapping("/{id}")
    public Result<Feedback> getById(@PathVariable Integer id) {
        Feedback feedback = feedbackService.getById(id);
        if (feedback == null) {
            return Result.error("反馈不存在");
        }
        return Result.ok(feedback);
    }

    @Operation(summary = "根据用户查询反馈", description = "根据用户ID查询该用户的所有反馈")
    @Parameter(name = "userId", description = "用户ID", required = true)
    @GetMapping("/listByUser")
    public Result<List<Feedback>> listByUser(Integer userId) {
        return Result.ok(feedbackService.listByUser(userId));
    }

    @Operation(summary = "根据状态查询反馈", description = "根据处理状态查询反馈，status: 0=待处理 1=处理中 2=已处理 3=已驳回")
    @Parameter(name = "status", description = "处理状态", required = true)
    @GetMapping("/listByStatus")
    public Result<List<Feedback>> listByStatus(Integer status) {
        return Result.ok(feedbackService.listByStatus(status));
    }

    @Operation(summary = "提交反馈", description = "用户提交故障反馈")
    @PostMapping("/add")
    public Result<Feedback> add(@RequestBody Feedback feedback) {
        return feedbackService.addFeedback(feedback);
    }

    @Operation(summary = "处理反馈", description = "管理员处理用户反馈，更新状态和处理意见")
    @PostMapping("/handle")
    public Result<Feedback> handle(@RequestBody Feedback feedback) {
        return feedbackService.handleFeedback(feedback);
    }

    @Operation(summary = "修改反馈", description = "修改反馈信息")
    @PostMapping("/update")
    public Result<Feedback> update(@RequestBody Feedback feedback) {
        if (feedback.getId() == null) {
            return Result.error("反馈ID不能为空");
        }
        boolean updated = feedbackService.updateById(feedback);
        if (!updated) {
            return Result.error("修改失败");
        }
        return Result.ok(feedback).setMessage("修改成功");
    }

    @Operation(summary = "删除反馈", description = "根据反馈ID删除反馈")
    @Parameter(name = "id", description = "反馈ID", required = true)
    @GetMapping("/delete")
    public Result<String> delete(Integer id) {
        boolean removed = feedbackService.removeById(id);
        if (!removed) {
            return Result.error("删除失败");
        }
        return Result.ok("删除成功");
    }
}
