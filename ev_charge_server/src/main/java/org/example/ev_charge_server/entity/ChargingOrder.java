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
 * 充电订单表
 * @TableName charging_order
 */
@Schema(description = "充电订单")
@TableName(value ="charging_order")
@Data
public class ChargingOrder {
    @Schema(description = "订单ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Integer id;

    @Schema(description = "订单号", example = "CO20240101000001")
    private String orderNo;

    @Schema(description = "用户ID", example = "1")
    private Integer userId;

    @Schema(description = "网点ID", example = "1")
    private Integer stationId;

    @Schema(description = "充电桩ID", example = "1")
    private Integer chargerId;

    @Schema(description = "充电枪ID", example = "1")
    private Integer gunId;

    @Schema(description = "下单时电费单价快照(元/度)", example = "0.85")
    private BigDecimal pricePerKwh;

    @Schema(description = "下单时服务费单价快照(元/度)", example = "0.30")
    private BigDecimal serviceFee;

    @Schema(description = "开始充电时间")
    private Date startTime;

    @Schema(description = "结束充电时间")
    private Date endTime;

    @Schema(description = "充电时长(秒)", example = "3600")
    private Integer duration;

    @Schema(description = "充电量(度)", example = "20.5")
    private BigDecimal electricity;

    @Schema(description = "总费用(元)", example = "23.75")
    private BigDecimal amount;

    @Schema(description = "支付方式: 0=余额 1=模拟微信 2=模拟支付宝", example = "0")
    private Integer payType;

    @Schema(description = "状态: 0=充电中 1=待结算 2=已完成 3=已取消 4=退款中 5=已退款", example = "2")
    private Integer status;

    @Schema(description = "退款原因", example = "充电异常，申请退款")
    private String refundReason;

    @Schema(description = "创建时间")
    private Date createdAt;

    @Schema(description = "结算时间")
    private Date paidAt;
}