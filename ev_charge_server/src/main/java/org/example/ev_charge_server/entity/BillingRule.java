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
 * 计费标准表
 * @TableName billing_rule
 */
@Schema(description = "计费规则")
@TableName(value ="billing_rule")
@Data
public class BillingRule {
    @Schema(description = "计费规则ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Integer id;

    @Schema(description = "充电桩ID（一桩一规则）", example = "1")
    private Integer chargerId;

    @Schema(description = "电费单价(元/度)", example = "0.85")
    private BigDecimal pricePerKwh;

    @Schema(description = "服务费单价(元/度)", example = "0.30")
    private BigDecimal serviceFee;

    @Schema(description = "免费充电时长(分钟)", example = "0")
    private Integer freeMinutes;

    @Schema(description = "备注说明", example = "工作日贵，周末优惠")
    private String remark;

    @Schema(description = "更新时间")
    private Date updatedAt;
}