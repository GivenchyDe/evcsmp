package org.example.ev_charge_server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 消息推送记录表
 * @TableName message_push
 */
@Data
@TableName("message_push")
public class MessagePush implements Serializable {
    /**
     * 推送记录ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 消息类型: 0=订单 1=充值 2=反馈 3=公告 4=故障
     */
    private Integer type;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 推送用户数
     */
    private Integer userCount;

    /**
     * 已读用户数
     */
    private Integer readCount;

    /**
     * 阅读率
     */
    private Double readRate;

    /**
     * 创建时间
     */
    private Date createdAt;

    private static final long serialVersionUID = 1L;
}