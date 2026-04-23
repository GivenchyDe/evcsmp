package org.example.ev_charge_server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.ev_charge_server.comm.Result;
import org.example.ev_charge_server.entity.Admin;

/**
 * 管理员模块的service逻辑层接口, 该接口会选择从IService<T>接口继承；
 * 继承IService<T>接口，会自动获得CRUD方法，并且会自动实现
 * CRUD方法: insert, update, delete, select
 */
public interface AdminService extends IService<Admin> {

    /**
     * 登录方法
     * @param username 用户名
     * @param password 密码
     * @return 登录成功返回true，否则返回false
     */

    Result<Admin> login(String username, String password);

    /**
     * 添加管理员
     * @param admin 管理员信息
     * @return 添加结果
     */
    Result<Admin> add(Admin admin);

    /**
     * 修改管理员信息
     * @param admin 管理员信息
     * @return 修改结果
     */
    Result<Admin> update(Admin admin);

}
