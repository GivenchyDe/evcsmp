package org.example.ev_charge_server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Data;

/**
 * 充电桩表
 * @TableName charger
 */
@Schema(description = "充电桩")
@TableName(value ="charger")
@Data
public class Charger {
    @Schema(description = "充电桩ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Integer id;

    @Schema(description = "所属网点ID", example = "1")
    private Integer stationId;

    @Schema(description = "设备编号", example = "CZ-2024-001")
    private String deviceNo;

    @Schema(description = "类型: 0=慢充 1=快充", example = "1")
    private Integer type;

    @Schema(description = "生产厂家", example = "华为技术")
    private String manufacturer;

    @Schema(description = "安装时间")
    private Date installDate;

    @Schema(description = "状态: 0=故障 1=正常 2=维护中", example = "1")
    private Integer status;

    @Schema(description = "创建时间")
    private Date createdAt;
}