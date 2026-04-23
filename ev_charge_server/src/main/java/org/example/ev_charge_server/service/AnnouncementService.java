package org.example.ev_charge_server.service;

import org.example.ev_charge_server.comm.Result;
import org.example.ev_charge_server.entity.Announcement;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author Given
* @description 针对表【announcement(公告表)】的数据库操作Service
* @createDate 2026-04-22 14:37:28
*/
public interface AnnouncementService extends IService<Announcement> {

    /**
     * 根据状态查询公告
     * @param status 状态
     * @return 公告列表
     */
    List<Announcement> listByStatus(Integer status);

    /**
     * 增加公告阅读量
     * @param id 公告ID
     * @return 结果
     */
    Result<String> incrementView(Integer id);

}
