package org.example.ev_charge_server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Data;

/**
 * 故障反馈表
 * @TableName feedback
 */
@Schema(description = "故障反馈")
@TableName(value ="feedback")
@Data
public class Feedback {
    @Schema(description = "反馈ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Integer id;

    @Schema(description = "反馈编号", example = "FB20240101001")
    private String feedbackNo;

    @Schema(description = "用户ID", example = "1")
    private Integer userId;

    @Schema(description = "类型: 0=网点问题 1=设备故障 2=软件异常 3=使用体验 4=其他", example = "1")
    private Integer type;

    @Schema(description = "反馈内容", example = "充电桩无法识别车辆")
    private String content;

    @Schema(description = "图片URL列表")
    private Object images;

    @Schema(description = "关联网点ID", example = "1")
    private Integer stationId;

    @Schema(description = "关联充电桩ID", example = "1")
    private Integer chargerId;

    @Schema(description = "联系方式", example = "13800138000")
    private String contact;

    @Schema(description = "状态: 0=待处理 1=处理中 2=已处理 3=已驳回", example = "0")
    private Integer status;

    @Schema(description = "处理管理员ID", example = "1")
    private Integer handlerId;

    @Schema(description = "处理意见", example = "已派技术人员处理")
    private String reply;

    @Schema(description = "创建时间")
    private Date createdAt;

    @Schema(description = "处理时间")
    private Date handledAt;
}