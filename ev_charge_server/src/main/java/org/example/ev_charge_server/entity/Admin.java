package org.example.ev_charge_server.entity;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Admin {
    @Schema(description = "管理员ID", example = "1")
    private Integer id;

    @Schema(description = "账号", example = "admin")
    private String username;

    @Schema(description = "密码", example = "123456")
    private String password;

    @Schema(description = "姓名", example = "张三")
    private String name;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "状态：1-启用 0-禁用", example = "1")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime created_at;
}
