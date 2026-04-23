package org.example.ev_charge_server.service;

import org.example.ev_charge_server.comm.Result;
import org.example.ev_charge_server.entity.Charger;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author Given
* @description 针对表【charger(充电桩表)】的数据库操作Service
* @createDate 2026-04-22 14:37:28
*/
public interface ChargerService extends IService<Charger> {

    /**
     * 根据网点ID查询充电桩
     * @param stationId 网点ID
     * @return 充电桩列表
     */
    List<Charger> listByStation(Integer stationId);

    /**
     * 新增充电桩
     * @param charger 充电桩信息
     * @return 添加结果
     */
    Result<Charger> addCharger(Charger charger);

    /**
     * 根据状态查询充电桩
     * @param status 状态
     * @return 充电桩列表
     */
    List<Charger> listByStatus(Integer status);

}
