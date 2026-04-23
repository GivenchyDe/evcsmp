package org.example.ev_charge_server.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.ev_charge_server.comm.Result;
import org.example.ev_charge_server.entity.User;
import org.example.ev_charge_server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理接口
 */
@Tag(name = "用户管理", description = "用户注册、登录、信息管理等接口")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "查询所有用户", description = "分页获取系统中的所有用户列表")
    @Parameter(name = "page", description = "页码，从1开始", required = true)
    @Parameter(name = "limit", description = "每页数量", required = true)
    @GetMapping("/list")
    public Result<List<User>> list(Integer page, Integer limit) {
        Page<User> pageObj = new Page<>(page, limit);
        return Result.ok(userService.list(pageObj));
    }

    @Operation(summary = "根据ID查询用户", description = "根据用户ID获取用户详细信息")
    @Parameter(name = "id", description = "用户ID", required = true)
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Integer id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(null);
        return Result.ok(user);
    }

    @Operation(summary = "用户注册", description = "新用户注册，手机号不能重复")
    @PostMapping("/register")
    public Result<User> register(@RequestBody User user) {
        return userService.register(user);
    }

    @Operation(summary = "用户登录", description = "用户通过用户名和密码登录")
    @GetMapping("/login")
    public Result<User> login(String username, String password) {
        return userService.login(username, password);
    }

    @Operation(summary = "修改用户信息", description = "修改用户昵称、头像、手机号等信息")
    @PostMapping("/update")
    public Result<User> update(@RequestBody User user) {
        return userService.updateUser(user);
    }

    @Operation(summary = "修改用户状态", description = "禁用或启用用户账号，status: 0=禁用 1=正常")
    @Parameter(name = "id", description = "用户ID", required = true)
    @Parameter(name = "status", description = "状态: 0=禁用 1=正常", required = true)
    @GetMapping("/updateStatus")
    public Result<User> updateStatus(Integer id, Integer status) {
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        boolean updated = userService.updateById(user);
        if (!updated) {
            return Result.error("修改状态失败");
        }
        return Result.ok(user).setMessage("修改成功");
    }

    @Operation(summary = "用户充值", description = "为用户账户充值余额")
    @Parameter(name = "id", description = "用户ID", required = true)
    @Parameter(name = "amount", description = "充值金额", required = true)
    @GetMapping("/recharge")
    public Result<User> recharge(Integer id, java.math.BigDecimal amount) {
        return userService.recharge(id, amount);
    }

    @Operation(summary = "删除用户", description = "根据用户ID删除用户")
    @Parameter(name = "id", description = "用户ID", required = true)
    @GetMapping("/delete")
    public Result<String> delete(Integer id) {
        boolean removed = userService.removeById(id);
        if (!removed) {
            return Result.error("删除失败");
        }
        return Result.ok("删除成功");
    }
}
