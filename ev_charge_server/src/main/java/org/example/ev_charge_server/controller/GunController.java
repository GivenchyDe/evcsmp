package org.example.ev_charge_server.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.ev_charge_server.comm.Result;
import org.example.ev_charge_server.entity.Gun;
import org.example.ev_charge_server.service.GunService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 充电枪管理接口
 */
@Tag(name = "充电枪管理", description = "充电枪的新增、编辑、删除、状态修改等接口")
@RestController
@RequestMapping("/gun")
public class GunController {

    @Autowired
    private GunService gunService;

    @Operation(summary = "查询所有充电枪", description = "分页获取所有充电枪列表")
    @Parameter(name = "page", description = "页码，从1开始", required = true)
    @Parameter(name = "limit", description = "每页数量", required = true)
    @GetMapping("/list")
    public Result<Page<Gun>> list(Integer page, Integer limit, String keyword) {
        Page<Gun> pageObj = new Page<>(page, limit);
        if (StringUtils.isNotBlank(keyword)) {
            QueryWrapper<Gun> wrapper = new QueryWrapper<>();
            wrapper.like("gun_no", keyword);
            return Result.ok(gunService.page(pageObj, wrapper));
        }
        return Result.ok(gunService.page(pageObj));
    }

    @Operation(summary = "根据充电桩查询充电枪", description = "根据充电桩ID查询该设备下的所有充电枪")
    @Parameter(name = "chargerId", description = "充电桩ID", required = true)
    @GetMapping("/listByCharger")
    public Result<List<Gun>> listByCharger(Integer chargerId) {
        return Result.ok(gunService.listByCharger(chargerId));
    }

    @Operation(summary = "新增充电枪", description = "为指定充电桩新增充电枪")
    @PostMapping("/add")
    public Result<Gun> add(@RequestBody Gun gun) {
        boolean saved = gunService.save(gun);
        if (!saved) {
            return Result.error("添加失败");
        }
        return Result.ok(gun).setMessage("添加成功");
    }

    @Operation(summary = "修改充电枪", description = "修改充电枪信息")
    @PostMapping("/update")
    public Result<Gun> update(@RequestBody Gun gun) {
        if (gun.getId() == null) {
            return Result.error("充电枪ID不能为空");
        }
        boolean updated = gunService.updateById(gun);
        if (!updated) {
            return Result.error("修改失败");
        }
        return Result.ok(gun).setMessage("修改成功");
    }

    @Operation(summary = "修改充电枪状态", description = "修改充电枪状态，status: 0=空闲 1=繁忙 2=故障")
    @Parameter(name = "id", description = "充电枪ID", required = true)
    @Parameter(name = "status", description = "状态", required = true)
    @GetMapping("/updateStatus")
    public Result<Gun> updateStatus(Integer id, Integer status) {
        Gun gun = new Gun();
        gun.setId(id);
        gun.setStatus(status);
        boolean updated = gunService.updateById(gun);
        if (!updated) {
            return Result.error("修改状态失败");
        }
        return Result.ok(gun).setMessage("状态修改成功");
    }

    @Operation(summary = "删除充电枪", description = "根据充电枪ID删除")
    @Parameter(name = "id", description = "充电枪ID", required = true)
    @GetMapping("/delete")
    public Result<String> delete(Integer id) {
        boolean removed = gunService.removeById(id);
        if (!removed) {
            return Result.error("删除失败");
        }
        return Result.ok("删除成功");
    }
}
