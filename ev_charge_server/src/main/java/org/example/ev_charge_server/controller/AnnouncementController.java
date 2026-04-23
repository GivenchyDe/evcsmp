package org.example.ev_charge_server.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.ev_charge_server.comm.Result;
import org.example.ev_charge_server.entity.Announcement;
import org.example.ev_charge_server.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告管理接口
 */
@Tag(name = "公告管理", description = "系统公告的发布、编辑、删除、查询等接口")
@RestController
@RequestMapping("/announcement")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @Operation(summary = "查询所有公告", description = "分页获取所有公告列表")
    @Parameter(name = "page", description = "页码，从1开始", required = true)
    @Parameter(name = "limit", description = "每页数量", required = true)
    @GetMapping("/list")
    public Result<Page<Announcement>> list(Integer page, Integer limit, String keyword) {
        Page<Announcement> pageObj = new Page<>(page, limit);
        if (StringUtils.isNotBlank(keyword)) {
            QueryWrapper<Announcement> wrapper = new QueryWrapper<>();
            wrapper.like("title", keyword);
            return Result.ok(announcementService.page(pageObj, wrapper));
        }
        return Result.ok(announcementService.page(pageObj));
    }

    @Operation(summary = "根据ID查询公告", description = "根据公告ID获取详细信息")
    @Parameter(name = "id", description = "公告ID", required = true)
    @GetMapping("/{id}")
    public Result<Announcement> getById(@PathVariable Integer id) {
        Announcement announcement = announcementService.getById(id);
        if (announcement == null) {
            return Result.error("公告不存在");
        }
        return Result.ok(announcement);
    }

    @Operation(summary = "根据状态查询公告", description = "根据状态查询公告，status: 0=未发布 1=已发布 2=已过期")
    @Parameter(name = "status", description = "状态", required = true)
    @GetMapping("/listByStatus")
    public Result<List<Announcement>> listByStatus(Integer status) {
        return Result.ok(announcementService.listByStatus(status));
    }

    @Operation(summary = "发布公告", description = "管理员发布新公告")
    @PostMapping("/add")
    public Result<Announcement> add(@RequestBody Announcement announcement) {
        boolean saved = announcementService.save(announcement);
        if (!saved) {
            return Result.error("发布失败");
        }
        return Result.ok(announcement).setMessage("发布成功");
    }

    @Operation(summary = "修改公告", description = "修改公告信息")
    @PostMapping("/update")
    public Result<Announcement> update(@RequestBody Announcement announcement) {
        if (announcement.getId() == null) {
            return Result.error("公告ID不能为空");
        }
        boolean updated = announcementService.updateById(announcement);
        if (!updated) {
            return Result.error("修改失败");
        }
        return Result.ok(announcement).setMessage("修改成功");
    }

    @Operation(summary = "删除公告", description = "根据公告ID删除公告")
    @Parameter(name = "id", description = "公告ID", required = true)
    @GetMapping("/delete")
    public Result<String> delete(Integer id) {
        boolean removed = announcementService.removeById(id);
        if (!removed) {
            return Result.error("删除失败");
        }
        return Result.ok("删除成功");
    }

    @Operation(summary = "增加阅读量", description = "用户查看公告时增加阅读量")
    @Parameter(name = "id", description = "公告ID", required = true)
    @GetMapping("/incrementView")
    public Result<String> incrementView(Integer id) {
        return announcementService.incrementView(id);
    }
}
