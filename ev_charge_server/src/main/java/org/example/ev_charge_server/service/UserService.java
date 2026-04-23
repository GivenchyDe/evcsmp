package org.example.ev_charge_server.service;

import org.example.ev_charge_server.comm.Result;
import org.example.ev_charge_server.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;

/**
* @author Given
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2026-04-22 14:37:28
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param user 用户信息
     * @return 注册结果
     */
    Result<User> register(User user);

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 登录结果
     */
    Result<User> login(String username, String password);

    /**
     * 修改用户信息
     * @param user 用户信息
     * @return 修改结果
     */
    Result<User> updateUser(User user);

    /**
     * 用户充值
     * @param id 用户ID
     * @param amount 充值金额
     * @return 充值结果
     */
    Result<User> recharge(Integer id, BigDecimal amount);

}
