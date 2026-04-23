package org.example.ev_charge_server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.ev_charge_server.entity.ChatMessage;
import org.example.ev_charge_server.service.ChatMessageService;
import org.example.ev_charge_server.mapper.ChatMessageMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Given
* @description 针对表【chat_message(客服聊天记录表)】的数据库操作Service实现
* @createDate 2026-04-22 14:37:28
*/
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage>
    implements ChatMessageService{

    @Override
    public List<ChatMessage> listByUser(Integer userId) {
        QueryWrapper<ChatMessage> queryWrapper = Wrappers.query(ChatMessage.class);
        queryWrapper.eq("user_id", userId).orderByAsc("created_at");
        return list(queryWrapper);
    }
}




