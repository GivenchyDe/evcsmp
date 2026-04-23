package org.example.ev_charge_server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 用户表
 * @TableName user
 */
@Schema(description = "用户")
@TableName(value ="user")
@Data
public class User {
    @Schema(description = "用户ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Integer id;

    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "昵称", example = "张三")
    private String nickname;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "账户余额(元)", example = "100.00")
    private BigDecimal balance;

    @Schema(description = "登录密码", example = "123456")
    private String password;

    @Schema(description = "状态: 0=禁用 1=正常", example = "1")
    private Integer status;

    @Schema(description = "注册时间")
    private Date createdAt;
}