package org.example.ev_charge_server.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.ev_charge_server.comm.Result;
import org.example.ev_charge_server.entity.Charger;
import org.example.ev_charge_server.service.ChargerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 充电桩管理接口
 */
@Tag(name = "充电桩管理", description = "充电桩的新增、编辑、删除、查询等接口")
@RestController
@RequestMapping("/charger")
public class ChargerController {

    @Autowired
    private ChargerService chargerService;

    @Operation(summary = "查询所有充电桩", description = "分页获取所有充电桩列表")
    @Parameter(name = "page", description = "页码，从1开始", required = true)
    @Parameter(name = "limit", description = "每页数量", required = true)
    @GetMapping("/list")
    public Result<List<Charger>> list(Integer page, Integer limit) {
        Page<Charger> pageObj = new Page<>(page, limit);
        return Result.ok(chargerService.list(pageObj));
    }

    @Operation(summary = "根据ID查询充电桩", description = "根据充电桩ID获取详细信息")
    @Parameter(name = "id", description = "充电桩ID", required = true)
    @GetMapping("/{id}")
    public Result<Charger> getById(@PathVariable Integer id) {
        Charger charger = chargerService.getById(id);
        if (charger == null) {
            return Result.error("充电桩不存在");
        }
        return Result.ok(charger);
    }

    @Operation(summary = "根据网点查询充电桩", description = "根据网点ID查询该网点下的所有充电桩")
    @Parameter(name = "stationId", description = "网点ID", required = true)
    @GetMapping("/listByStation")
    public Result<List<Charger>> listByStation(Integer stationId) {
        return Result.ok(chargerService.listByStation(stationId));
    }

    @Operation(summary = "新增充电桩", description = "新增充电桩设备")
    @PostMapping("/add")
    public Result<Charger> add(@RequestBody Charger charger) {
        return chargerService.addCharger(charger);
    }

    @Operation(summary = "修改充电桩", description = "修改充电桩信息")
    @PostMapping("/update")
    public Result<Charger> update(@RequestBody Charger charger) {
        if (charger.getId() == null) {
            return Result.error("充电桩ID不能为空");
        }
        boolean updated = chargerService.updateById(charger);
        if (!updated) {
            return Result.error("修改失败");
        }
        return Result.ok(charger).setMessage("修改成功");
    }

    @Operation(summary = "删除充电桩", description = "根据充电桩ID删除")
    @Parameter(name = "id", description = "充电桩ID", required = true)
    @GetMapping("/delete")
    public Result<String> delete(Integer id) {
        boolean removed = chargerService.removeById(id);
        if (!removed) {
            return Result.error("删除失败");
        }
        return Result.ok("删除成功");
    }

    @Operation(summary = "按状态查询充电桩", description = "根据状态查询充电桩，status: 0=故障 1=正常 2=维护中")
    @Parameter(name = "status", description = "状态", required = true)
    @GetMapping("/listByStatus")
    public Result<List<Charger>> listByStatus(Integer status) {
        return Result.ok(chargerService.listByStatus(status));
    }
}
