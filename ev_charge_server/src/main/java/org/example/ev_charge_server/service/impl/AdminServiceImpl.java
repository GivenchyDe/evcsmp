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

    @Override
    public Result<Admin> add(Admin admin) {
        QueryWrapper<Admin> queryWrapper = Wrappers.query(Admin.class);
        queryWrapper.eq("username", admin.getUsername());
        Admin existingAdmin = getOne(queryWrapper);
        if (existingAdmin != null) {
            return Result.error("管理员账号已存在");
        }
        boolean saved = save(admin);
        if (!saved) {
            return Result.error("添加管理员失败");
        }
        return Result.ok(admin).setMessage("添加成功");
    }

    @Override
    public Result<Admin> update(Admin admin) {
        if (admin.getId() == null) {
            return Result.error("管理员ID不能为空");
        }
        Admin existingAdmin = getById(admin.getId());
        if (existingAdmin == null) {
            return Result.error("管理员不存在");
        }
        if (admin.getUsername() != null && !admin.getUsername().equals(existingAdmin.getUsername())) {
            QueryWrapper<Admin> queryWrapper = Wrappers.query(Admin.class);
            queryWrapper.eq("username", admin.getUsername());
            Admin otherAdmin = getOne(queryWrapper);
            if (otherAdmin != null) {
                return Result.error("管理员账号已存在");
            }
        }
        boolean updated = updateById(admin);
        if (!updated) {
            return Result.error("修改管理员失败");
        }
        return Result.ok(admin).setMessage("修改成功");
    }

}
