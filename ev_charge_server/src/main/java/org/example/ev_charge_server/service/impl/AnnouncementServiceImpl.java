package org.example.ev_charge_server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.ev_charge_server.comm.Result;
import org.example.ev_charge_server.entity.Announcement;
import org.example.ev_charge_server.service.AnnouncementService;
import org.example.ev_charge_server.mapper.AnnouncementMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Given
* @description 针对表【announcement(公告表)】的数据库操作Service实现
* @createDate 2026-04-22 14:37:28
*/
@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement>
    implements AnnouncementService{

    @Override
    public List<Announcement> listByStatus(Integer status) {
        QueryWrapper<Announcement> queryWrapper = Wrappers.query(Announcement.class);
        queryWrapper.eq("status", status);
        return list(queryWrapper);
    }

    @Override
    public Result<String> incrementView(Integer id) {
        Announcement announcement = getById(id);
        if (announcement == null) {
            return Result.error("公告不存在");
        }
        announcement.setViewCount(announcement.getViewCount() + 1);
        boolean updated = updateById(announcement);
        if (!updated) {
            return Result.error("更新失败");
        }
        return Result.ok("阅读量+1");
    }
}




