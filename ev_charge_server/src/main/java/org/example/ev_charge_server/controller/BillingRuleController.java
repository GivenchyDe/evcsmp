package org.example.ev_charge_server.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.ev_charge_server.comm.Result;
import org.example.ev_charge_server.entity.BillingRule;
import org.example.ev_charge_server.service.BillingRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 计费规则管理接口
 */
@Tag(name = "计费规则管理", description = "充电桩计费规则的新增、编辑、删除、查询等接口")
@RestController
@RequestMapping("/billingRule")
public class BillingRuleController {

    @Autowired
    private BillingRuleService billingRuleService;

    @Operation(summary = "查询所有计费规则", description = "分页获取所有计费规则列表")
    @Parameter(name = "page", description = "页码，从1开始", required = true)
    @Parameter(name = "limit", description = "每页数量", required = true)
    @GetMapping("/list")
    public Result<List<BillingRule>> list(Integer page, Integer limit) {
        Page<BillingRule> pageObj = new Page<>(page, limit);
        return Result.ok(billingRuleService.list(pageObj));
    }

    @Operation(summary = "根据ID查询计费规则", description = "根据计费规则ID获取详细信息")
    @Parameter(name = "id", description = "计费规则ID", required = true)
    @GetMapping("/{id}")
    public Result<BillingRule> getById(@PathVariable Integer id) {
        BillingRule rule = billingRuleService.getById(id);
        if (rule == null) {
            return Result.error("计费规则不存在");
        }
        return Result.ok(rule);
    }

    @Operation(summary = "根据充电桩查询计费规则", description = "根据充电桩ID查询对应的计费规则")
    @Parameter(name = "chargerId", description = "充电桩ID", required = true)
    @GetMapping("/getByCharger")
    public Result<BillingRule> getByCharger(Integer chargerId) {
        return Result.ok(billingRuleService.getByCharger(chargerId));
    }

    @Operation(summary = "新增计费规则", description = "为充电桩新增计费规则")
    @PostMapping("/add")
    public Result<BillingRule> add(@RequestBody BillingRule billingRule) {
        boolean saved = billingRuleService.save(billingRule);
        if (!saved) {
            return Result.error("添加失败");
        }
        return Result.ok(billingRule).setMessage("添加成功");
    }

    @Operation(summary = "修改计费规则", description = "修改计费规则信息")
    @PostMapping("/update")
    public Result<BillingRule> update(@RequestBody BillingRule billingRule) {
        if (billingRule.getId() == null) {
            return Result.error("计费规则ID不能为空");
        }
        boolean updated = billingRuleService.updateById(billingRule);
        if (!updated) {
            return Result.error("修改失败");
        }
        return Result.ok(billingRule).setMessage("修改成功");
    }

    @Operation(summary = "删除计费规则", description = "根据计费规则ID删除")
    @Parameter(name = "id", description = "计费规则ID", required = true)
    @GetMapping("/delete")
    public Result<String> delete(Integer id) {
        boolean removed = billingRuleService.removeById(id);
        if (!removed) {
            return Result.error("删除失败");
        }
        return Result.ok("删除成功");
    }
}
