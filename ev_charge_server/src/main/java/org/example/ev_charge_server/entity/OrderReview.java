package org.example.ev_charge_server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Data;

/**
 * 订单评价表
 * @TableName order_review
 */
@Schema(description = "订单评价")
@TableName(value ="order_review")
@Data
public class OrderReview {
    @Schema(description = "评价ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Integer id;

    @Schema(description = "充电订单ID", example = "1")
    private Integer orderId;

    @Schema(description = "用户ID", example = "1")
    private Integer userId;

    @Schema(description = "网点ID", example = "1")
    private Integer stationId;

    @Schema(description = "评分 1-5星", example = "5")
    private Integer score;

    @Schema(description = "评价内容", example = "充电速度快，环境干净")
    private String content;

    @Schema(description = "评价图片URL列表")
    private Object images;

    @Schema(description = "是否删除: 0=正常 1=已删除", example = "0")
    private Integer isDeleted;

    @Schema(description = "创建时间")
    private Date createdAt;
}