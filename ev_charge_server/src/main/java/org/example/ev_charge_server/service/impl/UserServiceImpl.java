package org.example.ev_charge_server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.ev_charge_server.comm.Result;
import org.example.ev_charge_server.entity.User;
import org.example.ev_charge_server.service.UserService;
import org.example.ev_charge_server.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
* @author Given
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2026-04-22 14:37:28
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Override
    public Result<User> register(User user) {
        QueryWrapper<User> queryWrapper = Wrappers.query(User.class);
        queryWrapper.eq("phone", user.getPhone());
        User existingUser = getOne(queryWrapper);
        if (existingUser != null) {
            return Result.error("手机号已注册");
        }
        queryWrapper = Wrappers.query(User.class);
        queryWrapper.eq("username", user.getUsername());
        existingUser = getOne(queryWrapper);
        if (existingUser != null) {
            return Result.error("用户名已存在");
        }
        boolean saved = save(user);
        if (!saved) {
            return Result.error("注册失败");
        }
        return Result.ok(user).setMessage("注册成功");
    }

    @Override
    public Result<User> login(String username, String password) {
        QueryWrapper<User> queryWrapper = Wrappers.query(User.class);
        queryWrapper.eq("username", username);
        User user = getOne(queryWrapper);
        if (user == null) {
            return Result.error("用户不存在");
        }
        if (!user.getPassword().equals(password)) {
            return Result.error("密码错误");
        }
        if (user.getStatus() == 0) {
            return Result.error("账号已禁用");
        }
        user.setPassword(null);
        return Result.ok(user).setMessage("登录成功");
    }

    @Override
    public Result<User> updateUser(User user) {
        if (user.getId() == null) {
            return Result.error("用户ID不能为空");
        }
        User existingUser = getById(user.getId());
        if (existingUser == null) {
            return Result.error("用户不存在");
        }
        boolean updated = updateById(user);
        if (!updated) {
            return Result.error("修改失败");
        }
        return Result.ok(user).setMessage("修改成功");
    }

    @Override
    public Result<User> recharge(Integer id, BigDecimal amount) {
        User user = getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.error("充值金额必须大于0");
        }
        user.setBalance(user.getBalance().add(amount));
        boolean updated = updateById(user);
        if (!updated) {
            return Result.error("充值失败");
        }
        return Result.ok(user).setMessage("充值成功");
    }
}




