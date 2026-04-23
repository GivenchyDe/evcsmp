package org.example.ev_charge_server.service;

import org.example.ev_charge_server.entity.Station;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author Given
* @description 针对表【station(充电站网点表)】的数据库操作Service
* @createDate 2026-04-22 14:37:28
*/
public interface StationService extends IService<Station> {

    /**
     * 根据状态查询网点
     * @param status 状态
     * @return 网点列表
     */
    List<Station> listByStatus(Integer status);

    /**
     * 根据名称或地址搜索网点
     * @param keyword 关键词
     * @return 网点列表
     */
    List<Station> search(String keyword);

}
