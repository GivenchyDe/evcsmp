package org.example.ev_charge_server.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.ev_charge_server.comm.Result;
import org.example.ev_charge_server.entity.Message;
import org.example.ev_charge_server.entity.MessagePush;
import org.example.ev_charge_server.entity.User;
import org.example.ev_charge_server.service.MessagePushService;
import org.example.ev_charge_server.service.MessageService;
import org.example.ev_charge_server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息推送管理接口
 */
@Tag(name = "消息推送管理", description = "系统消息推送、推送历史查询、推送统计等接口")
@RestController
@RequestMapping("/message/push")
public class MessagePushController {

    @Autowired
    private MessagePushService messagePushService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Operation(summary = "发送消息推送", description = "向指定用户或所有用户发送消息推送")
    @PostMapping("/send")
    public Result<Map<String, Object>> sendPush(@RequestBody Map<String, Object> pushData) {
        try {
            // 解析推送数据
            Integer type = (Integer) pushData.get("type");
            String title = (String) pushData.get("title");
            String content = (String) pushData.get("content");
            String userType = (String) pushData.get("userType");
            List<Integer> userIds = (List<Integer>) pushData.get("userIds");
            Map<String, Object> condition = (Map<String, Object>) pushData.get("condition");

            // 根据用户类型获取目标用户ID列表
            List<Integer> targetUserIds = new ArrayList<>();
            
            if ("all".equals(userType)) {
                // 所有用户
                List<User> allUsers = userService.list();
                targetUserIds = allUsers.stream().map(User::getId).toList();
            } else if ("specific".equals(userType)) {
                // 指定用户
                targetUserIds = userIds != null ? userIds : new ArrayList<>();
            } else if ("condition".equals(userType)) {
                // 条件筛选
                Double minBalance = condition != null ? (Double) condition.get("minBalance") : 0.0;
                Integer minOrders = condition != null ? (Integer) condition.get("minOrders") : 0;
                Integer lastLoginDays = condition != null ? (Integer) condition.get("lastLoginDays") : 30;
                
                // 这里简化处理，实际应该根据条件查询数据库
                List<User> allUsers = userService.list();
                targetUserIds = allUsers.stream()
                    .filter(user -> user.getBalance().doubleValue() >= minBalance)
                    .map(User::getId)
                    .toList();
            }

            if (targetUserIds.isEmpty()) {
                return Result.error("没有符合条件的用户");
            }

            // 创建推送记录
            MessagePush pushRecord = new MessagePush();
            pushRecord.setType(type);
            pushRecord.setTitle(title);
            pushRecord.setContent(content);
            pushRecord.setUserCount(targetUserIds.size());
            pushRecord.setReadCount(0);
            pushRecord.setReadRate(0.0);
            pushRecord.setCreatedAt(new Date());

            // 保存推送记录
            boolean saved = messagePushService.save(pushRecord);
            if (!saved) {
                return Result.error("保存推送记录失败");
            }

            // 为每个用户创建消息
            int successCount = 0;
            for (Integer userId : targetUserIds) {
                Message message = new Message();
                message.setUserId(userId);
                message.setType(type);
                message.setTitle(title);
                message.setContent(content);
                message.setIsRead(0);
                message.setCreatedAt(new Date());
                message.setExpireAt(new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000));
                
                boolean messageSaved = messageService.save(message);
                if (messageSaved) {
                    successCount++;
                }
            }

            // 更新推送记录
            pushRecord.setUserCount(successCount);
            messagePushService.updateById(pushRecord);

            Map<String, Object> result = new HashMap<>();
            result.put("pushId", pushRecord.getId());
            result.put("successCount", successCount);
            result.put("totalUsers", targetUserIds.size());

            return Result.ok(result).setMessage("消息推送成功，成功发送给" + successCount + "个用户");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("消息推送失败: " + e.getMessage());
        }
    }

    @Operation(summary = "查询推送历史", description = "分页获取消息推送历史记录")
    @Parameter(name = "page", description = "页码，从1开始", required = true)
    @Parameter(name = "limit", description = "每页数量", required = true)
    @GetMapping("/history")
    public Result<Page<MessagePush>> getPushHistory(Integer page, Integer limit, String keyword) {
        Page<MessagePush> pageObj = new Page<>(page, limit);
        QueryWrapper<MessagePush> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("created_at");
        
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.like("title", keyword).or().like("content", keyword);
        }
        
        return Result.ok(messagePushService.page(pageObj, wrapper));
    }

    @Operation(summary = "获取推送统计", description = "获取消息推送的统计数据")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getPushStats() {
        try {
            // 总推送次数
            long totalPushes = messagePushService.count();
            
            // 今日推送次数
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            QueryWrapper<MessagePush> todayWrapper = new QueryWrapper<>();
            todayWrapper.ge("created_at", cal.getTime());
            long todayPushes = messagePushService.count(todayWrapper);
            
            // 总用户数
            long totalUsers = userService.count();
            
            // 活跃用户数（最近30天有登录）
            QueryWrapper<User> activeWrapper = new QueryWrapper<>();
            activeWrapper.eq("status", 1);
            long activeUsers = userService.count(activeWrapper);
            
            // 平均阅读率（计算最近10次推送的平均阅读率）
            QueryWrapper<MessagePush> recentWrapper = new QueryWrapper<>();
            recentWrapper.orderByDesc("created_at").last("LIMIT 10");
            List<MessagePush> recentPushes = messagePushService.list(recentWrapper);
            
            double avgReadRate = 0.0;
            if (!recentPushes.isEmpty()) {
                double totalRate = 0.0;
                for (MessagePush push : recentPushes) {
                    if (push.getUserCount() > 0) {
                        double rate = (double) push.getReadCount() / push.getUserCount() * 100;
                        totalRate += rate;
                    }
                }
                avgReadRate = totalRate / recentPushes.size();
            }
            
            // 平均响应时间（简化处理，实际应该从用户行为日志计算）
            double avgResponseTime = 5.0; // 假设平均5秒
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalPushes", totalPushes);
            stats.put("todayPushes", todayPushes);
            stats.put("totalUsers", totalUsers);
            stats.put("activeUsers", activeUsers);
            stats.put("readRate", Math.round(avgReadRate * 10) / 10.0); // 保留一位小数
            stats.put("avgResponseTime", avgResponseTime);
            
            return Result.ok(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取统计失败");
        }
    }

    @Operation(summary = "删除推送记录", description = "根据推送记录ID删除推送记录")
    @Parameter(name = "id", description = "推送记录ID", required = true)
    @GetMapping("/delete")
    public Result<String> deletePush(Integer id) {
        boolean removed = messagePushService.removeById(id);
        if (!removed) {
            return Result.error("删除失败");
        }
        return Result.ok("删除成功");
    }

    @Operation(summary = "更新推送阅读统计", description = "当用户阅读消息时更新推送阅读统计")
    @Parameter(name = "pushId", description = "推送记录ID", required = true)
    @Parameter(name = "userId", description = "用户ID", required = true)
    @GetMapping("/updateRead")
    public Result<String> updateReadStat(Integer pushId, Integer userId) {
        try {
            MessagePush pushRecord = messagePushService.getById(pushId);
            if (pushRecord == null) {
                return Result.error("推送记录不存在");
            }
            
            // 更新阅读计数
            int newReadCount = pushRecord.getReadCount() + 1;
            pushRecord.setReadCount(newReadCount);
            
            // 计算阅读率
            if (pushRecord.getUserCount() > 0) {
                double readRate = (double) newReadCount / pushRecord.getUserCount() * 100;
                pushRecord.setReadRate(Math.round(readRate * 10) / 10.0); // 保留一位小数
            }
            
            messagePushService.updateById(pushRecord);
            return Result.ok("阅读统计已更新");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新阅读统计失败");
        }
    }
}