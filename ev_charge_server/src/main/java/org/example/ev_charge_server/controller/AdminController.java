package org.example.ev_charge_server.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.example.ev_charge_server.comm.Result;
import org.example.ev_charge_server.entity.Admin;
import org.example.ev_charge_server.mapper.AdminMapper;
import org.example.ev_charge_server.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "管理员接口",description = "实现管理员相关的操作功能，登录，添加，信息修改，查询等")
@RestController
public class AdminController {
    @Autowired//依赖注入（DI/IOC）
    private AdminService adminService;

    //http://127.0.0.1:8080/admin/list
    @Operation(summary = "查询所有管理员",description = "查询所有管理员信息")
    @GetMapping("/admin/list")
    public Result<Page<Admin>> list(Integer page, Integer limit, String keyword) {
        Page<Admin> pageObj = new Page<>(page, limit);
        if (StringUtils.isNotBlank(keyword)) {
            QueryWrapper<Admin> wrapper = new QueryWrapper<>();
            wrapper.like("username", keyword).or().like("name", keyword).or().like("phone", keyword);
            return Result.ok(adminService.page(pageObj, wrapper));
        }
        return Result.ok(adminService.page(pageObj));
    }

    //http://127.0.0.1:8080/admin/login?username=admin&password=123456
    @Operation(summary = "管理员登录",description = "管理员登录功能")
    @GetMapping("/admin/login")
    public Result<Admin> login(String username, String password) {
        return adminService.login(username, password);
//        return adminService.login(username, password);
    }

    //http://127.0.0.1:8080/admin/add?username=benzhubenzhu&password=123456&name=笨猪本主&phone=12345678901&status=1
    @Operation(summary = "管理员添加",description = "管理员添加功能")
    @PostMapping("/admin/add")
    public Result<Admin> add(@RequestBody Admin admin) {
        //tobo 实现管理员添加功能
        //在AdminService接口中新增方法，在AdminServiceImpl类中实现该方法
        //在实现中需要判断管理员账号是否存在

        return adminService.add(admin);
    }

    //修改管理员信息
    @Operation(summary = "修改管理员信息",description = "修改管理员信息功能")
    @PostMapping("/admin/update")
    public Result<Admin> update(@RequestBody Admin admin) {
        return adminService.update(admin);
    }
}


//http://127.0.0.1:8080/doc.html