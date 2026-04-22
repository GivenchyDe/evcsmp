package org.example.ev_charge_server.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.ev_charge_server.comm.Result;
import org.example.ev_charge_server.entity.Admin;
import org.example.ev_charge_server.mapper.AdminMapper;
import org.example.ev_charge_server.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AdminController {
    @Autowired//依赖注入（DI/IOC）
    private AdminService adminService;

    //http://127.0.0.1:8080/admin/list
    @GetMapping("/admin/list")
    public List<Admin> list() {
//        // 1. 创建一个空的 QueryWrapper（查询所有数据）
//        QueryWrapper<Admin> queryWrapper = new QueryWrapper<>();
//        // 2. 调用 selectList 方法
//        return adminMapper.selectList(queryWrapper);
        return adminService.list();
    }

    //http://127.0.0.1:8080/admin/login?username=admin&password=123456
    @GetMapping("/admin/login")
    public Result<Admin> login(String username, String password) {
        return adminService.login(username, password);
//        return adminService.login(username, password);
    }

    @GetMapping("/admin/add")
    public Result<Admin> add(Admin admin) {
        //tobo 实现管理员添加功能
        //在AdminService接口中新增方法，在AdminServiceImpl类中实现该方法
        //在实现中需要判断管理员账号是否存在
        return null;
    }
}
