package org.example.ev_charge_server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.ev_charge_server.comm.Result;
import org.example.ev_charge_server.entity.Message;
import org.example.ev_charge_server.service.MessageService;
import org.example.ev_charge_server.mapper.MessageMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Given
* @description 针对表【message(消息通知表)】的数据库操作Service实现
* @createDate 2026-04-22 14:37:28
*/
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message>
    implements MessageService{

    @Override
    public List<Message> listByUser(Integer userId) {
        QueryWrapper<Message> queryWrapper = Wrappers.query(Message.class);
        queryWrapper.eq("user_id", userId).orderByDesc("created_at");
        return list(queryWrapper);
    }

    @Override
    public List<Message> listUnread(Integer userId) {
        QueryWrapper<Message> queryWrapper = Wrappers.query(Message.class);
        queryWrapper.eq("user_id", userId).eq("is_read", 0);
        return list(queryWrapper);
    }

    @Override
    public Result<String> markAllRead(Integer userId) {
        Message message = new Message();
        message.setIsRead(1);
        QueryWrapper<Message> queryWrapper = Wrappers.query(Message.class);
        queryWrapper.eq("user_id", userId).eq("is_read", 0);
        update(message, queryWrapper);
        return Result.ok("批量标记已读成功");
    }
}




