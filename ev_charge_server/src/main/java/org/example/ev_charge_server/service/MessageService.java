package org.example.ev_charge_server.service;

import org.example.ev_charge_server.comm.Result;
import org.example.ev_charge_server.entity.Message;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author Given
* @description 针对表【message(消息通知表)】的数据库操作Service
* @createDate 2026-04-22 14:37:28
*/
public interface MessageService extends IService<Message> {

    /**
     * 根据用户ID查询消息
     * @param userId 用户ID
     * @return 消息列表
     */
    List<Message> listByUser(Integer userId);

    /**
     * 查询用户未读消息
     * @param userId 用户ID
     * @return 未读消息列表
     */
    List<Message> listUnread(Integer userId);

    /**
     * 批量标记已读
     * @param userId 用户ID
     * @return 结果
     */
    Result<String> markAllRead(Integer userId);

}
