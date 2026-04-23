package org.example.ev_charge_server.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.ev_charge_server.comm.Result;
import org.example.ev_charge_server.entity.Message;
import org.example.ev_charge_server.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息通知管理接口
 */
@Tag(name = "消息通知管理", description = "用户消息通知的查询、标记已读、删除等接口")
@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Operation(summary = "查询所有消息", description = "分页获取所有消息通知列表")
    @Parameter(name = "page", description = "页码，从1开始", required = true)
    @Parameter(name = "limit", description = "每页数量", required = true)
    @GetMapping("/list")
    public Result<Page<Message>> list(Integer page, Integer limit, String keyword) {
        Page<Message> pageObj = new Page<>(page, limit);
        if (StringUtils.isNotBlank(keyword)) {
            QueryWrapper<Message> wrapper = new QueryWrapper<>();
            wrapper.like("title", keyword).or().like("content", keyword);
            return Result.ok(messageService.page(pageObj, wrapper));
        }
        return Result.ok(messageService.page(pageObj));
    }

    @Operation(summary = "根据用户查询消息", description = "根据用户ID查询该用户的所有消息")
    @Parameter(name = "userId", description = "用户ID", required = true)
    @GetMapping("/listByUser")
    public Result<List<Message>> listByUser(Integer userId) {
        return Result.ok(messageService.listByUser(userId));
    }

    @Operation(summary = "查询未读消息", description = "根据用户ID查询该用户的未读消息")
    @Parameter(name = "userId", description = "用户ID", required = true)
    @GetMapping("/listUnread")
    public Result<List<Message>> listUnread(Integer userId) {
        return Result.ok(messageService.listUnread(userId));
    }

    @Operation(summary = "发送消息", description = "系统向用户发送消息通知")
    @PostMapping("/add")
    public Result<Message> add(@RequestBody Message message) {
        boolean saved = messageService.save(message);
        if (!saved) {
            return Result.error("发送失败");
        }
        return Result.ok(message).setMessage("发送成功");
    }

    @Operation(summary = "标记已读", description = "将消息标记为已读状态")
    @Parameter(name = "id", description = "消息ID", required = true)
    @GetMapping("/markRead")
    public Result<Message> markRead(Integer id) {
        Message message = new Message();
        message.setId(id);
        message.setIsRead(1);
        boolean updated = messageService.updateById(message);
        if (!updated) {
            return Result.error("标记失败");
        }
        return Result.ok(message).setMessage("已标记为已读");
    }

    @Operation(summary = "批量标记已读", description = "将用户的所有未读消息标记为已读")
    @Parameter(name = "userId", description = "用户ID", required = true)
    @GetMapping("/markAllRead")
    public Result<String> markAllRead(Integer userId) {
        return messageService.markAllRead(userId);
    }

    @Operation(summary = "删除消息", description = "根据消息ID删除消息")
    @Parameter(name = "id", description = "消息ID", required = true)
    @GetMapping("/delete")
    public Result<String> delete(Integer id) {
        boolean removed = messageService.removeById(id);
        if (!removed) {
            return Result.error("删除失败");
        }
        return Result.ok("删除成功");
    }
}
