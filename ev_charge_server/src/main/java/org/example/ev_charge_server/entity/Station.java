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
 * 充电站网点表
 * @TableName station
 */
@Schema(description = "充电站网点")
@TableName(value ="station")
@Data
public class Station {
    @Schema(description = "网点ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Integer id;

    @Schema(description = "网点名称", example = "北京朝阳充电站")
    private String name;

    @Schema(description = "详细地址", example = "北京市朝阳区某街道1号")
    private String address;

    @Schema(description = "经度", example = "116.4074")
    private BigDecimal longitude;

    @Schema(description = "纬度", example = "39.9042")
    private BigDecimal latitude;

    @Schema(description = "联系电话", example = "010-12345678")
    private String phone;

    @Schema(description = "营业开始时间")
    private Date openTime;

    @Schema(description = "营业结束时间")
    private Date closeTime;

    @Schema(description = "网点照片URL列表")
    private Object photos;

    @Schema(description = "状态: 0=维护中 1=正常运营 2=已停用", example = "1")
    private Integer status;

    @Schema(description = "创建时间")
    private Date createdAt;
}