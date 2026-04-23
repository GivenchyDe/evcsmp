package org.example.ev_charge_server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.ev_charge_server.comm.Result;
import org.example.ev_charge_server.entity.Charger;
import org.example.ev_charge_server.service.ChargerService;
import org.example.ev_charge_server.mapper.ChargerMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Given
* @description 针对表【charger(充电桩表)】的数据库操作Service实现
* @createDate 2026-04-22 14:37:28
*/
@Service
public class ChargerServiceImpl extends ServiceImpl<ChargerMapper, Charger>
    implements ChargerService{

    @Override
    public List<Charger> listByStation(Integer stationId) {
        QueryWrapper<Charger> queryWrapper = Wrappers.query(Charger.class);
        queryWrapper.eq("station_id", stationId);
        return list(queryWrapper);
    }

    @Override
    public Result<Charger> addCharger(Charger charger) {
        QueryWrapper<Charger> queryWrapper = Wrappers.query(Charger.class);
        queryWrapper.eq("device_no", charger.getDeviceNo());
        Charger existingCharger = getOne(queryWrapper);
        if (existingCharger != null) {
            return Result.error("设备编号已存在");
        }
        boolean saved = save(charger);
        if (!saved) {
            return Result.error("添加失败");
        }
        return Result.ok(charger).setMessage("添加成功");
    }

    @Override
    public List<Charger> listByStatus(Integer status) {
        QueryWrapper<Charger> queryWrapper = Wrappers.query(Charger.class);
        queryWrapper.eq("status", status);
        return list(queryWrapper);
    }
}




