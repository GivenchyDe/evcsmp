package org.example.ev_charge_server.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.ev_charge_server.comm.Result;
import org.example.ev_charge_server.entity.ChatMessage;
import org.example.ev_charge_server.service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 客服聊天管理接口
 */
@Tag(name = "客服聊天管理", description = "客服聊天记录的查询、发送等接口")
@RestController
@RequestMapping("/chatMessage")
public class ChatMessageController {

    @Autowired
    private ChatMessageService chatMessageService;

    @Operation(summary = "查询所有聊天记录", description = "分页获取所有客服聊天记录")
    @Parameter(name = "page", description = "页码，从1开始", required = true)
    @Parameter(name = "limit", description = "每页数量", required = true)
    @GetMapping("/list")
    public Result<List<ChatMessage>> list(Integer page, Integer limit) {
        Page<ChatMessage> pageObj = new Page<>(page, limit);
        return Result.ok(chatMessageService.list(pageObj));
    }

    @Operation(summary = "根据用户查询聊天记录", description = "根据用户ID查询该用户的所有聊天记录")
    @Parameter(name = "userId", description = "用户ID", required = true)
    @GetMapping("/listByUser")
    public Result<List<ChatMessage>> listByUser(Integer userId) {
        return Result.ok(chatMessageService.listByUser(userId));
    }

    @Operation(summary = "发送消息", description = "用户或客服发送聊天消息")
    @PostMapping("/send")
    public Result<ChatMessage> send(@RequestBody ChatMessage chatMessage) {
        boolean saved = chatMessageService.save(chatMessage);
        if (!saved) {
            return Result.error("发送失败");
        }
        return Result.ok(chatMessage).setMessage("发送成功");
    }

    @Operation(summary = "删除聊天记录", description = "根据记录ID删除聊天记录")
    @Parameter(name = "id", description = "记录ID", required = true)
    @GetMapping("/delete")
    public Result<String> delete(Integer id) {
        boolean removed = chatMessageService.removeById(id);
        if (!removed) {
            return Result.error("删除失败");
        }
        return Result.ok("删除成功");
    }
}
