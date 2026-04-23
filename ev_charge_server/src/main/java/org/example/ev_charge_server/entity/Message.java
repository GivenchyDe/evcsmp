package org.example.ev_charge_server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Data;

/**
 * 消息通知表
 * @TableName message
 */
@Schema(description = "消息通知")
@TableName(value ="message")
@Data
public class Message {
    @Schema(description = "消息ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Integer id;

    @Schema(description = "接收用户ID", example = "1")
    private Integer userId;

    @Schema(description = "类型: 0=订单 1=充値 2=反馈 3=公告 4=故障", example = "0")
    private Integer type;

    @Schema(description = "消息标题", example = "您的订单已完成")
    private String title;

    @Schema(description = "消息内容", example = "充电订单CO20240101000001已完成，共充电了20.5度")
    private String content;

    @Schema(description = "是否已读: 0=未读 1=已读", example = "0")
    private Integer isRead;

    @Schema(description = "创建时间")
    private Date createdAt;

    @Schema(description = "过期时间(30天后)")
    private Date expireAt;
}