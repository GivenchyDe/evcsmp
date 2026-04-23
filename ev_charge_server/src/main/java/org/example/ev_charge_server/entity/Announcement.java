package org.example.ev_charge_server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Data;

/**
 * 公告表
 * @TableName announcement
 */
@Schema(description = "公告")
@TableName(value ="announcement")
@Data
public class Announcement {
    @Schema(description = "公告ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Integer id;

    @Schema(description = "标题", example = "元旦快乐充电优惠活动")
    private String title;

    @Schema(description = "内容(富文本)", example = "<p>节日快乐，充电优惠</p>")
    private String content;

    @Schema(description = "发布管理员ID", example = "1")
    private Integer adminId;

    @Schema(description = "状态: 0=未发布 1=已发布 2=已过期", example = "1")
    private Integer status;

    @Schema(description = "阅读量", example = "100")
    private Integer viewCount;

    @Schema(description = "发布时间")
    private Date publishAt;

    @Schema(description = "过期时间")
    private Date expireAt;

    @Schema(description = "创建时间")
    private Date createdAt;
}