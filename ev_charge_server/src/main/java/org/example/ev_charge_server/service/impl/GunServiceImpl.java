package org.example.ev_charge_server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.ev_charge_server.entity.Gun;
import org.example.ev_charge_server.service.GunService;
import org.example.ev_charge_server.mapper.GunMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Given
* @description 针对表【gun(充电枪表)】的数据库操作Service实现
* @createDate 2026-04-22 14:37:28
*/
@Service
public class GunServiceImpl extends ServiceImpl<GunMapper, Gun>
    implements GunService{

    @Override
    public List<Gun> listByCharger(Integer chargerId) {
        QueryWrapper<Gun> queryWrapper = Wrappers.query(Gun.class);
        queryWrapper.eq("charger_id", chargerId);
        return list(queryWrapper);
    }
}




