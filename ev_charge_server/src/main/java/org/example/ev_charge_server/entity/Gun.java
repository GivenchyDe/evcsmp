package org.example.ev_charge_server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 充电枪表
 * @TableName gun
 */
@Schema(description = "充电枪")
@TableName(value ="gun")
@Data
public class Gun {
    @Schema(description = "充电枪ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Integer id;

    @Schema(description = "所属充电桩ID", example = "1")
    private Integer chargerId;

    @Schema(description = "枪号", example = "A01")
    private String gunNo;

    @Schema(description = "状态: 0=空闲 1=繁忙 2=故障", example = "0")
    private Integer status;
}