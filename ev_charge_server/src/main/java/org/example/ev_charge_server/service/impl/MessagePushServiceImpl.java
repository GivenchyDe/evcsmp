package org.example.ev_charge_server.service.impl;

import org.example.ev_charge_server.entity.MessagePush;
import org.example.ev_charge_server.service.MessagePushService;
import org.example.ev_charge_server.mapper.MessagePushMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
* @author Given
* @description 针对表【message_push(消息推送记录表)】的数据库操作Service实现
* @createDate 2026-04-23 14:37:28
*/
@Service
public class MessagePushServiceImpl extends ServiceImpl<MessagePushMapper, MessagePush>
    implements MessagePushService {

}