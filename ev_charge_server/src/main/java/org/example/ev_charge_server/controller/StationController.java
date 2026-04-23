package org.example.ev_charge_server.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.ev_charge_server.comm.Result;
import org.example.ev_charge_server.entity.Station;
import org.example.ev_charge_server.service.StationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 充电站网点管理接口
 */
@Tag(name = "充电站网点管理", description = "充电站网点的新增、编辑、删除、查询等接口")
@RestController
@RequestMapping("/station")
public class StationController {

    @Autowired
    private StationService stationService;

    @Operation(summary = "查询所有网点", description = "分页获取所有充电站网点列表")
    @Parameter(name = "page", description = "页码，从1开始", required = true)
    @Parameter(name = "limit", description = "每页数量", required = true)
    @GetMapping("/list")
    public Result<List<Station>> list(Integer page, Integer limit) {
        Page<Station> pageObj = new Page<>(page, limit);
        return Result.ok(stationService.list(pageObj));
    }

    @Operation(summary = "根据ID查询网点", description = "根据网点ID获取网点详细信息")
    @Parameter(name = "id", description = "网点ID", required = true)
    @GetMapping("/{id}")
    public Result<Station> getById(@PathVariable Integer id) {
        Station station = stationService.getById(id);
        if (station == null) {
            return Result.error("网点不存在");
        }
        return Result.ok(station);
    }

    @Operation(summary = "新增网点", description = "新增充电站网点信息")
    @PostMapping("/add")
    public Result<Station> add(@RequestBody Station station) {
        boolean saved = stationService.save(station);
        if (!saved) {
            return Result.error("添加失败");
        }
        return Result.ok(station).setMessage("添加成功");
    }

    @Operation(summary = "修改网点", description = "修改充电站网点信息")
    @PostMapping("/update")
    public Result<Station> update(@RequestBody Station station) {
        if (station.getId() == null) {
            return Result.error("网点ID不能为空");
        }
        boolean updated = stationService.updateById(station);
        if (!updated) {
            return Result.error("修改失败");
        }
        return Result.ok(station).setMessage("修改成功");
    }

    @Operation(summary = "删除网点", description = "根据网点ID删除网点")
    @Parameter(name = "id", description = "网点ID", required = true)
    @GetMapping("/delete")
    public Result<String> delete(Integer id) {
        boolean removed = stationService.removeById(id);
        if (!removed) {
            return Result.error("删除失败");
        }
        return Result.ok("删除成功");
    }

    @Operation(summary = "按状态查询网点", description = "根据网点状态查询网点列表，status: 0=维护中 1=正常运营 2=已停用")
    @Parameter(name = "status", description = "状态", required = true)
    @GetMapping("/listByStatus")
    public Result<List<Station>> listByStatus(Integer status) {
        return Result.ok(stationService.listByStatus(status));
    }

    @Operation(summary = "搜索网点", description = "根据网点名称或地址模糊搜索")
    @Parameter(name = "keyword", description = "搜索关键词", required = true)
    @GetMapping("/search")
    public Result<List<Station>> search(String keyword) {
        return Result.ok(stationService.search(keyword));
    }
}
