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
 * 充值订单表
 * @TableName recharge_order
 */
@Schema(description = "充値订单")
@TableName(value ="recharge_order")
@Data
public class RechargeOrder {
    @Schema(description = "充値订单ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Integer id;

    @Schema(description = "订单号", example = "RC20240101000001")
    private String orderNo;

    @Schema(description = "用户ID", example = "1")
    private Integer userId;

    @Schema(description = "充値金额(元)", example = "100.00")
    private BigDecimal amount;

    @Schema(description = "赠送金额(元)", example = "10.00")
    private BigDecimal gift;

    @Schema(description = "支付方式: 1=模拟微信 2=模拟支付宝", example = "1")
    private Integer payType;

    @Schema(description = "状态: 0=待支付 1=已完成 2=已取消", example = "1")
    private Integer status;

    @Schema(description = "创建时间")
    private Date createdAt;

    @Schema(description = "支付时间")
    private Date paidAt;
}