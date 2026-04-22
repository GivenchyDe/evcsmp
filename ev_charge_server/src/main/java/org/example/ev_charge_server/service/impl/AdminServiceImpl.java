package org.example.ev_charge_server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.ev_charge_server.comm.Result;
import org.example.ev_charge_server.entity.Admin;
import org.example.ev_charge_server.mapper.AdminMapper;
import org.example.ev_charge_server.service.AdminService;
import org.springframework.stereotype.Service;

import java.sql.Wrapper;

@Service //标记当前类为javabean，并交给spring管理
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    @Override
    public Result<Admin> login(String username, String password) {
        //根据用户是否存在
        QueryWrapper<Admin> queryWrapper = Wrappers.query(Admin.class);
        queryWrapper.eq("username", username);
        Admin admin = getOne(queryWrapper);
        if (admin == null) {
            return Result.error("用户不存在");
        }
        //如果用户存在，则检查密码是否正确
        if (!admin.getPassword().equals(password)) {
            return Result.error("密码错误");
        }
        //检查账号是否可用（封禁）
        if (admin.getStatus() == 0) {
            return Result.error("账号已禁用");
        }
        //登录成功
        admin.setPassword(null);
        return Result.ok(admin).setMessage("登录成功");
    }
}
