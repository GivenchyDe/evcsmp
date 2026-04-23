package org.example.ev_charge_server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Data;

/**
 * 客服聊天记录表
 * @TableName chat_message
 */
@Schema(description = "客服聊天记录")
@TableName(value ="chat_message")
@Data
public class ChatMessage {
    @Schema(description = "消息ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Integer id;

    @Schema(description = "用户ID", example = "1")
    private Integer userId;

    @Schema(description = "发送方: 0=用户 1=AI客服 2=人工客服", example = "0")
    private Integer sender;

    @Schema(description = "人工客服管理员ID", example = "1")
    private Integer adminId;

    @Schema(description = "消息内容", example = "您好，请问有什么可以帮助您？")
    private String content;

    @Schema(description = "消息类型: 0=文字 1=图片", example = "0")
    private Integer msgType;

    @Schema(description = "创建时间")
    private Date createdAt;

    @Schema(description = "过期时间(30天后)")
    private Date expireAt;
}