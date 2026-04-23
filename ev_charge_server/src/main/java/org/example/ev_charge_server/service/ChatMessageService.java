package org.example.ev_charge_server.service;

import org.example.ev_charge_server.entity.ChatMessage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author Given
* @description 针对表【chat_message(客服聊天记录表)】的数据库操作Service
* @createDate 2026-04-22 14:37:28
*/
public interface ChatMessageService extends IService<ChatMessage> {

    /**
     * 根据用户ID查询聊天记录
     * @param userId 用户ID
     * @return 聊天记录列表
     */
    List<ChatMessage> listByUser(Integer userId);

}
