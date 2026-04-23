package org.example.ev_charge_server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.ev_charge_server.entity.Station;
import org.example.ev_charge_server.service.StationService;
import org.example.ev_charge_server.mapper.StationMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Given
* @description 针对表【station(充电站网点表)】的数据库操作Service实现
* @createDate 2026-04-22 14:37:28
*/
@Service
public class StationServiceImpl extends ServiceImpl<StationMapper, Station>
    implements StationService{

    @Override
    public List<Station> listByStatus(Integer status) {
        QueryWrapper<Station> queryWrapper = Wrappers.query(Station.class);
        queryWrapper.eq("status", status);
        return list(queryWrapper);
    }

    @Override
    public List<Station> search(String keyword) {
        QueryWrapper<Station> queryWrapper = Wrappers.query(Station.class);
        queryWrapper.like("name", keyword).or().like("address", keyword);
        return list(queryWrapper);
    }
}




