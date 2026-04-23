package org.example.ev_charge_server.service;

import org.example.ev_charge_server.entity.Gun;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author Given
* @description 针对表【gun(充电枪表)】的数据库操作Service
* @createDate 2026-04-22 14:37:28
*/
public interface GunService extends IService<Gun> {

    /**
     * 根据充电桩ID查询充电枪
     * @param chargerId 充电桩ID
     * @return 充电枪列表
     */
    List<Gun> listByCharger(Integer chargerId);

}
